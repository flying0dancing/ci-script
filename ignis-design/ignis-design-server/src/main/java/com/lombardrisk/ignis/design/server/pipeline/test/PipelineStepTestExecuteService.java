package com.lombardrisk.ignis.design.server.pipeline.test;

import com.lombardrisk.ignis.client.design.pipeline.test.StepTestStatus;
import com.lombardrisk.ignis.common.MapperUtils;
import com.lombardrisk.ignis.common.stream.CollectorUtils;
import com.lombardrisk.ignis.data.common.error.ErrorResponse;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.design.field.model.Field;
import com.lombardrisk.ignis.design.server.pipeline.PipelineStepExecutor;
import com.lombardrisk.ignis.design.server.pipeline.SparkUDFService;
import com.lombardrisk.ignis.design.server.pipeline.model.ExecuteStepContext;
import com.lombardrisk.ignis.design.server.pipeline.model.PipelineStep;
import com.lombardrisk.ignis.design.server.pipeline.test.model.ExpectedDataRow;
import com.lombardrisk.ignis.design.server.pipeline.test.model.InputDataRow;
import com.lombardrisk.ignis.design.server.pipeline.test.model.PipelineStepTest;
import com.lombardrisk.ignis.design.server.pipeline.test.model.PipelineStepTestCell;
import com.lombardrisk.ignis.design.server.pipeline.test.model.PipelineStepTestResult;
import com.lombardrisk.ignis.design.server.pipeline.test.model.PipelineStepTestRow;
import com.lombardrisk.ignis.design.server.productconfig.schema.Schema;
import com.lombardrisk.ignis.design.server.productconfig.schema.SchemaService;
import io.vavr.Tuple;
import io.vavr.Tuple0;
import io.vavr.Tuple2;
import io.vavr.control.Validation;
import lombok.AllArgsConstructor;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@AllArgsConstructor
public class PipelineStepTestExecuteService {

    private final PipelineStepExecutor pipelineStepExecutor;
    private final SparkUDFService sparkUDFService;
    private final SchemaService schemaService;

    public Validation<List<ErrorResponse>, PipelineStepTestResult> runTest(
            final PipelineStep pipelineStep,
            final PipelineStepTest pipelineStepTest) {

        Validation<CRUDFailure, Schema> outputSchema = schemaService.findWithValidation(pipelineStep.getOutput());
        Validation<CRUDFailure, List<Schema>> inputSchemas = schemaService.findOrIdsNotFound(pipelineStep.getInputs());
        if (inputSchemas.isInvalid()) {
            return Validation.invalid(singletonList(inputSchemas.getError().toErrorResponse()));
        }
        if (outputSchema.isInvalid()) {
            return Validation.invalid(singletonList(outputSchema.getError().toErrorResponse()));
        }

        if (pipelineStepTest.getInputData().isEmpty() && pipelineStepTest.getExpectedData().isEmpty()) {
            return Validation.valid(PipelineStepTestResult.getPassEmptyPipelineStepTestResult());
        }

        List<Long> inputRowIds = MapperUtils.map(pipelineStepTest.getInputData(), InputDataRow::getId);

        Validation<List<ErrorResponse>, Map<Schema, List<Row>>> inputData =
                validateRowSchemas(pipelineStepTest.getInputData())
                        .map(schemaMap -> convertInputDataRows(pipelineStepTest.getInputData(), schemaMap))
                .map(schemaData -> {
                    inputSchemas.get()
                            .forEach(schema -> schemaData.putIfAbsent(schema, emptyList()));
                    return schemaData;
                });

        Validation<List<ErrorResponse>, Map<Schema, List<Tuple2<Long, Row>>>> expectedData =
                validateRowSchemas(pipelineStepTest.getExpectedData())
                        .map(schemaMap -> convertExpectedDataRows(pipelineStepTest.getExpectedData(), schemaMap));

        List<ErrorResponse> combinedErrors = new ArrayList<>();

        if (inputData.isInvalid()) {
            combinedErrors.addAll(inputData.getError());
        }

        if (expectedData.isInvalid()) {
            combinedErrors.addAll(expectedData.getError());
        }

        if (combinedErrors.size() > 0) {
            return Validation.invalid(combinedErrors);
        }

        if (expectedData.get().size() > 1) {
            return Validation.valid(PipelineStepTestResult.builder()
                    .status(StepTestStatus.FAIL)
                    .build());
        }

        List<Tuple2<Long, Row>> expected = expectedData.get().values()
                .stream()
                .findFirst()
                .orElse(Collections.emptyList());

        ExecuteStepContext executeStepContext =
                ExecuteStepContext.realData(inputSchemas.get(), outputSchema.get(), inputData.get());

        sparkUDFService.registerUdfs(pipelineStepTest.getTestReferenceDate());

        Validation<List<ErrorResponse>, Dataset<Row>>
                rowDataset = pipelineStepExecutor.executeFullTransformation(pipelineStep, executeStepContext);

        return verifyResult(expected, rowDataset.map(Dataset::collectAsList), inputRowIds);
    }

    private <T extends PipelineStepTestRow> Validation<List<ErrorResponse>, Map<Long, Schema>> validateRowSchemas(
            final Set<T> rows) {

        Set<Long> schemaIds = rows.stream().map(PipelineStepTestRow::getSchemaId).collect(toSet());

        Validation<List<CRUDFailure>, List<Schema>> schemaValidation = schemaIds.stream()
                .map(schemaService::findWithValidation)
                .collect(CollectorUtils.groupValidations());

        if (schemaValidation.isInvalid()) {
            return Validation.invalid(schemaValidation.getError().stream()
                    .map(CRUDFailure::toErrorResponse)
                    .collect(toList()));
        }

        Map<Long, Schema> schemaMap = schemaValidation.get().stream()
                .collect(Collectors.toMap(Schema::getId, Function.identity()));

        Validation<List<ErrorResponse>, List<Tuple0>> rowValidation = rows.stream()
                .map(row -> validateRowFields(row, schemaMap.get(row.getSchemaId())))
                .collect(CollectorUtils.groupCollectionValidations());

        if (rowValidation.isInvalid()) {
            return Validation.invalid(rowValidation.getError());
        }

        return Validation.valid(schemaMap);
    }

    private Validation<List<ErrorResponse>, Tuple0> validateRowFields(
            final PipelineStepTestRow row, final Schema schema) {

        Map<Long, Field> fieldIdsToFields = schema.getFields().stream()
                .collect(Collectors.toMap(Field::getId, Function.identity()));

        List<ErrorResponse> invalidFields = row.getCells().stream()
                .map(PipelineStepTestCell::getFieldId)
                .filter(fieldId -> fieldIdsToFields.get(fieldId) == null)
                .map(fieldId -> CRUDFailure.notFoundIds(Field.class.getSimpleName(), fieldId).toErrorResponse())
                .collect(toList());

        if (!invalidFields.isEmpty()) {
            return Validation.invalid(invalidFields);
        }

        return Validation.valid(Tuple.empty());
    }

    private Map<Schema, List<Row>> convertInputDataRows(
            final Set<InputDataRow> rows, final Map<Long, Schema> schemaMap) {

        return rows.stream()
                .collect(CollectorUtils.toMultimapList(
                        row -> schemaMap.get(row.getSchemaId()),
                        row -> PipelineStepTestExecuteService.convertToSparkDataSet(
                                row, schemaMap.get(row.getSchemaId()))));
    }

    private Map<Schema, List<Tuple2<Long, Row>>> convertExpectedDataRows(
            final Set<ExpectedDataRow> rows, final Map<Long, Schema> schemaMap) {

        return rows.stream()
                .collect(CollectorUtils.toMultimapList(
                        row -> schemaMap.get(row.getSchemaId()),
                        row -> PipelineStepTestExecuteService.convertToRowAndId(
                                row, schemaMap.get(row.getSchemaId()))));
    }

    private Validation<List<ErrorResponse>, PipelineStepTestResult> verifyResult(
            final List<Tuple2<Long, Row>> expected,
            final Validation<List<ErrorResponse>, List<Row>> actualRowsValidation,
            final List<Long> inputRowIds) {
        if (actualRowsValidation.isInvalid()) {
            return Validation.invalid(actualRowsValidation.getError());
        }

        List<Row> actualRows = actualRowsValidation.get();
        List<Long> matching = new ArrayList<>();
        List<Long> notFoundIds = new ArrayList<>();

        for (Tuple2<Long, Row> expectedRow : expected) {
            if (actualRows.contains(expectedRow._2) &&
                    matchingDoesNotContainExpectedRow(expected, matching, expectedRow)) {
                matching.add(expectedRow._1);
            } else {
                notFoundIds.add(expectedRow._1);
            }
        }

        List<Map<String, Object>> unexpectedRows = findUnexpectedRows(expected, actualRows);

        if (expected.size() == matching.size() && unexpectedRows.isEmpty()) {
            return Validation.valid(PipelineStepTestResult.builder()
                    .status(StepTestStatus.PASS)
                    .inputRows(inputRowIds)
                    .matching(matching)
                    .notFound(Collections.emptyList())
                    .unexpected(Collections.emptyList())
                    .actualResults(MapperUtils.map(actualRows, this::convertRowToFieldNameAndResult))
                    .build());
        }

        return Validation.valid(PipelineStepTestResult.builder()
                .status(StepTestStatus.FAIL)
                .inputRows(inputRowIds)
                .notFound(notFoundIds)
                .matching(matching)
                .unexpected(unexpectedRows)
                .actualResults(MapperUtils.map(actualRows, this::convertRowToFieldNameAndResult))
                .build());
    }

    private boolean matchingDoesNotContainExpectedRow(
            final List<Tuple2<Long, Row>> expected,
            final List<Long> matching,
            final Tuple2<Long, Row> expectedRow) {
        return matching.stream()
                .noneMatch(match -> expected.stream()
                        .anyMatch(exp -> exp._1.equals(match) && exp._2.equals(expectedRow._2)));
    }

    private List<Map<String, Object>> findUnexpectedRows(
            final List<Tuple2<Long, Row>> expected,
            final List<Row> actualRows) {

        List<Map<String, Object>> unexpectedRows = new ArrayList<>();

        List<Row> alreadyMatched = new ArrayList<>();
        for (Row actualRow : actualRows) {
            if (expected.stream()
                    .map(Tuple2::_2)
                    .noneMatch(row -> row.equals(actualRow))) {

                Map<String, Object> unexpectedRow = convertRowToFieldNameAndResult(actualRow);
                unexpectedRows.add(unexpectedRow);
            }
            if (expected.stream()
                    .map(Tuple2::_2)
                    .anyMatch(row -> row.equals(actualRow))) {
                if (!alreadyMatched.contains(actualRow)) {
                    alreadyMatched.add(actualRow);
                } else {
                    Map<String, Object> unexpectedRow = convertRowToFieldNameAndResult(actualRow);
                    unexpectedRows.add(unexpectedRow);
                }
            }
        }
        return unexpectedRows;
    }

    private Map<String, Object> convertRowToFieldNameAndResult(final Row actualRow) {
        Map<String, Object> unexpectedRow = new HashMap<>();
        String[] fieldNames = actualRow.schema().fieldNames();

        for (String fieldName : fieldNames) {
            Object value = actualRow.getAs(fieldName);
            unexpectedRow.put(fieldName, value);
        }
        return unexpectedRow;
    }

    private static Row convertToSparkDataSet(final PipelineStepTestRow row, final Schema schema) {
        Map<Long, Field> fieldIdsToFields = schema.getFields().stream()
                .collect(Collectors.toMap(Field::getId, Function.identity()));

        Object[] cellValues = row.getCells().stream()
                //make sure row is in the same order as schema
                .sorted(Comparator.comparing(PipelineStepTestCell::getFieldId))
                .map(cell -> fieldIdsToFields.get(cell.getFieldId()).parse(cell.getData()))
                .toArray();

        return RowFactory.create(cellValues);
    }

    private static Tuple2<Long, Row> convertToRowAndId(final PipelineStepTestRow row, final Schema schema) {
        Map<Long, Field> fieldIdsToFields = schema.getFields().stream()
                .collect(Collectors.toMap(Field::getId, Function.identity()));

        Row sparkRow = RowFactory.create(
                row.getCells().stream()
                        //make sure row is in the same order as schema
                        .sorted(Comparator.comparing(PipelineStepTestCell::getFieldId))
                        .map(cell -> fieldIdsToFields.get(cell.getFieldId()).parse(cell.getData()))
                        .toArray());

        return Tuple.of(row.getId(), sparkRow);
    }
}
