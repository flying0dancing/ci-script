package com.lombardrisk.ignis.design.server.pipeline;

import com.lombardrisk.ignis.client.design.pipeline.error.SelectResult;
import com.lombardrisk.ignis.common.stream.CollectorUtils;
import com.lombardrisk.ignis.data.common.error.ErrorResponse;
import com.lombardrisk.ignis.design.field.model.Field;
import com.lombardrisk.ignis.design.server.pipeline.converter.SchemaStructTypeConverter;
import com.lombardrisk.ignis.design.server.pipeline.converter.SelectColumnConverter;
import com.lombardrisk.ignis.design.server.pipeline.converter.TransformationConverter;
import com.lombardrisk.ignis.design.server.pipeline.model.ExecuteStepContext;
import com.lombardrisk.ignis.design.server.pipeline.model.PipelineAggregationStep;
import com.lombardrisk.ignis.design.server.pipeline.model.PipelineJoinStep;
import com.lombardrisk.ignis.design.server.pipeline.model.PipelineMapStep;
import com.lombardrisk.ignis.design.server.pipeline.model.PipelineStep;
import com.lombardrisk.ignis.design.server.pipeline.model.PipelineUnionStep;
import com.lombardrisk.ignis.design.server.pipeline.model.PipelineWindowStep;
import com.lombardrisk.ignis.design.server.pipeline.model.select.PipelineFilter;
import com.lombardrisk.ignis.design.server.pipeline.model.select.Select;
import com.lombardrisk.ignis.design.server.pipeline.validator.transformation.dataframe.SparkTransformUtils;
import com.lombardrisk.ignis.design.server.productconfig.schema.Schema;
import com.lombardrisk.ignis.pipeline.step.api.SelectColumn;
import com.lombardrisk.ignis.pipeline.step.api.UnionSpec;
import com.lombardrisk.ignis.pipeline.step.common.MapStepExecutor;
import com.lombardrisk.ignis.pipeline.step.common.Transformation;
import com.lombardrisk.ignis.pipeline.step.common.UnionStepExecutor;
import com.lombardrisk.ignis.pipeline.step.common.WindowStepExecutor;
import io.vavr.Function3;
import io.vavr.control.Validation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.StructType;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.lombardrisk.ignis.common.stream.CollectionUtils.concat;
import static com.lombardrisk.ignis.common.stream.CollectionUtils.orEmpty;
import static com.lombardrisk.ignis.design.server.pipeline.validator.transformation.dataframe.SparkTransformUtils.handleException;
import static com.lombardrisk.ignis.design.server.pipeline.validator.transformation.dataframe.SparkTransformUtils.outputContainsExpectedField;
import static com.lombardrisk.ignis.design.server.pipeline.validator.transformation.dataframe.SparkTransformUtils.toDisplayName;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.toMap;

@AllArgsConstructor
@Slf4j
public class PipelineStepExecutor {

    private final SparkSession sparkSession;
    private final MapStepExecutor mapStepExecutor;
    private final WindowStepExecutor windowStepExecutor;
    private final UnionStepExecutor unionStepExecutor;
    private final PipelineStepSparkSqlExecutor sparkSqlExecutor;
    private final SchemaStructTypeConverter schemaStructTypeConverter = new SchemaStructTypeConverter();

    public Validation<List<ErrorResponse>, Dataset<Row>> executeFullTransformation(
            final PipelineStep pipelineStep,
            final ExecuteStepContext executeStepContext) {

        List<Schema> inputSchemas = executeStepContext.getInputSchemas();
        Schema outputSchema = executeStepContext.getOutputSchema();
        createDatasetInputData(executeStepContext);
        StructType schemaOutStructType = schemaStructTypeConverter.apply(outputSchema.getFields());

        switch (pipelineStep.getType()) {
            case MAP:
                PipelineMapStep mapStep = (PipelineMapStep) pipelineStep;
                return executeSingleInput(mapStep.getName(),
                        mapStep.getSelects(), mapStep.getFilters(), executeStepContext,
                        (selects, filters, inputDataset) ->
                                runMap(outputSchema, selects, filters, inputDataset))
                        .flatMap(dataset -> SparkTransformUtils.validateFields(dataset, schemaOutStructType));
            case WINDOW:
                PipelineWindowStep windowStep = (PipelineWindowStep) pipelineStep;

                Validation<List<ErrorResponse>, Dataset<Row>> result = executeSingleInput(windowStep.getName(),
                        windowStep.getSelects(), windowStep.getFilters(),
                        executeStepContext,
                        (selects, datasetName, inputDataset) -> runWindow(
                                outputSchema,
                                selects,
                                windowStep.getFilters(),
                                inputDataset));

                return result.flatMap(dataset -> SparkTransformUtils.validateFields(dataset, schemaOutStructType));
            case UNION:
                PipelineUnionStep pipelineUnionStep = (PipelineUnionStep) pipelineStep;

                return executeFullUnion(pipelineUnionStep, executeStepContext)
                        .flatMap(dataset -> SparkTransformUtils.validateFields(dataset, schemaOutStructType));

            case AGGREGATION:
                return executeAggregation((PipelineAggregationStep) pipelineStep, executeStepContext)
                        .flatMap(dataset -> SparkTransformUtils.validateFields(dataset, schemaOutStructType));
            case JOIN:
                return executeJoin((PipelineJoinStep) pipelineStep, executeStepContext)
                        .flatMap(dataset -> SparkTransformUtils.validateFields(dataset, schemaOutStructType));
            default:
                throw new IllegalArgumentException("Transformation type not supported " + pipelineStep.getType());
        }
    }

    public List<SelectResult> transformOutputFieldsIndividually(
            final PipelineStep pipelineStep,
            final ExecuteStepContext executeStepContext) {
        List<Schema> inputSchemas = executeStepContext.getInputSchemas();
        Schema outputSchema = executeStepContext.getOutputSchema();

        switch (pipelineStep.getType()) {
            case AGGREGATION:
            case JOIN:
                return transformSqlTransformationsIndividually(pipelineStep, executeStepContext);
            case UNION:
                return transformUnionSelectsIndividually((PipelineUnionStep) pipelineStep, executeStepContext);
            case WINDOW:
                return transformDependantSelectsIndividually(
                        pipelineStep.getName(),
                        pipelineStep.getSelects(),
                        executeStepContext,
                        (selects, filters, inputDataset) -> runWindow(outputSchema, selects,
                                pipelineStep.getFilters(),
                                inputDataset));
            default:
                return transformDependantSelectsIndividually(
                        pipelineStep.getName(),
                        pipelineStep.getSelects(),
                        executeStepContext,
                        (selects, filters, inputDataset) -> runMap(outputSchema, selects, filters, inputDataset));
        }
    }

    private Dataset<Row> runMap(
            final Schema outputSchema,
            final Collection<SelectColumn> selects,
            final Set<String> filters,
            final Dataset<Row> inputDataset) {

        return mapStepExecutor.runMap(
                selects,
                filters,
                outputSchema.getPhysicalTableName(),
                inputDataset);
    }

    private Dataset<Row> runWindow(
            final Schema outputSchema,
            final Collection<SelectColumn> selects,
            final Set<String> filters, final Dataset<Row> inputDataset) {
        return windowStepExecutor.runWindow(
                selects,
                filters,
                outputSchema.getPhysicalTableName(),
                inputDataset);
    }

    private List<SelectResult> transformSqlTransformationsIndividually(
            final PipelineStep pipelineStep,
            final ExecuteStepContext executeStepContext) {

        Schema outputSchema = executeStepContext.getOutputSchema();
        List<Schema> inputSchemas = executeStepContext.getInputSchemas();

        Map<Schema, Dataset<Row>> inputs = createDatasetInputData(executeStepContext);
        Set<SelectColumn> selectColumns = createSelectColumns(pipelineStep.getSelects(), outputSchema)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Map<String, Field> fieldByName = outputSchema.getFields().stream()
                .collect(toMap(Field::getName, Function.identity()));

        List<SelectResult> selectResults = new ArrayList<>();
        Set<Select> dependentSelects = new LinkedHashSet<>();

        for (SelectColumn select : selectColumns) {

            Field outputField = fieldByName.get(select.getAs());

            SelectResult selectResult = runIndividualSqlSelect(
                    select, pipelineStep, inputSchemas, inputs, outputField);
            selectResults.add(selectResult);
        }

        return selectResults;
    }

    private SelectResult runIndividualSqlSelect(
            final SelectColumn select,
            final PipelineStep pipelineStep,
            final List<Schema> inputSchemas,
            final Map<Schema, Dataset<Row>> inputs,
            final Field outputField) {
        Validation<List<ErrorResponse>, Transformation> transformationValidation = createTransformation(
                pipelineStep, inputSchemas, select);

        if (transformationValidation.isInvalid()) {
            return SelectResult.error(
                    outputField.getName(),
                    outputField.getId(),
                    null,
                    transformationValidation.getError());
        }

        Transformation transformation = transformationValidation.get();
        String sql = transformation.toSparkSql();

        Dataset<Row> outputDataset;
        try {
            outputDataset = sparkSqlExecutor.executeSqlNew(transformation.toSparkSql(), inputs);
        } catch (Exception e) {

            Validation<List<ErrorResponse>, Object> result = handleException(pipelineStep.getName(), e);
            return SelectResult.error(
                    outputField.getName(),
                    outputField.getId(),
                    select.unionSchemaId(),
                    result.getError());
        }

        if (!outputContainsExpectedField(outputDataset, outputField)) {

            ErrorResponse errorResponse = ErrorResponse.valueOf(
                    "Output fields required the following fields that were not found " + toDisplayName(
                            SchemaStructTypeConverter.fieldToStructField(outputField)),
                    "UNMAPPED_OUTPUT_FIELDS");

            SelectResult.error(
                    outputField.getName(),
                    outputField.getId(),
                    select.unionSchemaId(),
                    singletonList(errorResponse));
        }

        return SelectResult.success(
                outputField.getName(),
                outputField.getId(),
                select.unionSchemaId());
    }

    private Validation<List<ErrorResponse>, Transformation> createTransformation(
            final PipelineStep pipelineStep,
            final List<Schema> inputSchemas, final SelectColumn select) {
        switch (pipelineStep.getType()) {
            case AGGREGATION:
                Transformation transformation = TransformationConverter.convertAggregationStep(
                        (PipelineAggregationStep) pipelineStep,
                        singleton(select),
                        inputSchemas.get(0));
                return Validation.valid(transformation);
            case JOIN:
                return TransformationConverter.convertJoinStep(
                        (PipelineJoinStep) pipelineStep,
                        singleton(select),
                        inputSchemas);
            default:
                throw new IllegalArgumentException(
                        "Sql transformation not supported for type " +
                                pipelineStep.getType());
        }
    }

    private List<SelectResult> transformDependantSelectsIndividually(
            final String pipelineStepName,
            final Set<Select> selects,
            final ExecuteStepContext executeStepContext,
            final Function3<Collection<SelectColumn>, Set<String>, Dataset<Row>, Dataset<Row>> executor) {

        List<Schema> inputSchemas = executeStepContext.getInputSchemas();
        Schema outputSchema = executeStepContext.getOutputSchema();

        Map<Long, Field> fieldById = outputSchema.getFields().stream()
                .collect(toMap(Field::getId, Function.identity()));

        List<SelectResult> selectResults = new ArrayList<>();
        Set<Select> dependentSelects = new LinkedHashSet<>();

        List<Select> intermediateSelects = selects.stream()
                .filter(Select::isIntermediate)
                .sorted(Select.selectOrder())
                .collect(Collectors.toList());

        List<Select> remainingSelects = selects.stream()
                .filter(select1 -> !select1.isIntermediate())
                .collect(Collectors.toList());

        for (Select select : intermediateSelects) {
            SelectResult selectResult =
                    runSelect(select, dependentSelects, pipelineStepName, executeStepContext, fieldById, executor);
            selectResults.add(selectResult);
        }

        for (Select select : remainingSelects) {
            SelectResult selectResult =
                    runSelect(select, dependentSelects, pipelineStepName, executeStepContext, fieldById, executor);
            selectResults.add(selectResult);
        }

        return selectResults;
    }

    private SelectResult runSelect(
            final Select select,
            final Set<Select> dependentSelects,
            final String pipelineStepName,
            final ExecuteStepContext executeStepContext,
            final Map<Long, Field> fieldById,
            final Function3<Collection<SelectColumn>, Set<String>, Dataset<Row>, Dataset<Row>> executor) {

        Field outputField = fieldById.get(select.getOutputFieldId());

        Set<Select> selectsToExecute = new LinkedHashSet<>(dependentSelects);
        selectsToExecute.add(select);

        Validation<List<ErrorResponse>, Dataset<Row>> result = executeSingleInput(
                pipelineStepName, selectsToExecute, emptySet(), executeStepContext, executor);

        if (select.isIntermediate()) {
            dependentSelects.add(setMockDataForSubsequentSelects(select, outputField));
        }

        if (result.isInvalid()) {
            return SelectResult.error(
                    outputField.getName(), outputField.getId(), select.unionSchemaId(), result.getError());
        }

        if (!outputContainsExpectedField(result.get(), outputField)) {

            ErrorResponse errorResponse = ErrorResponse.valueOf(
                    "Output fields required the following fields that were not found " + toDisplayName(
                            SchemaStructTypeConverter.fieldToStructField(outputField)),
                    "UNMAPPED_OUTPUT_FIELDS");

            return SelectResult.error(
                    outputField.getName(),
                    outputField.getId(),
                    null,
                    singletonList(errorResponse));
        }

        return SelectResult.success(outputField.getName(), outputField.getId(), select.unionSchemaId());
    }

    private List<SelectResult> transformUnionSelectsIndividually(
            final PipelineUnionStep pipelineUnionStep,
            final ExecuteStepContext executeStepContext) {

        List<Schema> inputSchemas = executeStepContext.getInputSchemas();
        Schema outputSchema = executeStepContext.getOutputSchema();
        Map<Schema, Dataset<Row>> datasetInputData = createDatasetInputData(executeStepContext);

        Map<Long, Schema> schemaLookup = new HashMap<>();
        Map<Long, Map<Long, Field>> fieldLookup = new HashMap<>();

        for (Schema inputSchema : inputSchemas) {
            schemaLookup.put(inputSchema.getId(), inputSchema);

            Map<Long, Field> fieldMap = inputSchema.getFields().stream()
                    .collect(toMap(Field::getId, Function.identity()));
            fieldLookup.put(inputSchema.getId(), fieldMap);
        }

        Map<Long, Field> outputFieldMap = outputSchema.getFields().stream()
                .collect(toMap(Field::getId, Function.identity()));
        fieldLookup.put(outputSchema.getId(), outputFieldMap);

        Map<UnionSpec, Set<Select>> unionSpecSetMap = pipelineUnionStep.getSelects().stream()
                .sorted(Select.selectOrder())
                .collect(CollectorUtils.toMultimap(
                        select ->
                                TransformationConverter.convertUnionSpec(
                                        schemaLookup.get(select.getUnion()
                                                .getUnionSchemaId())
                                                .getPhysicalTableName(),
                                        select.getUnion().getUnionSchemaId()),
                        select -> select));

        Map<UnionSpec, Set<String>> filterMap = createUnionFilterMap(pipelineUnionStep, schemaLookup);

        List<SelectResult> selectResults = new ArrayList<>();

        for (Map.Entry<UnionSpec, Set<Select>> unionSpecSetEntry : unionSpecSetMap.entrySet()) {
            UnionSpec unionSpec = unionSpecSetEntry.getKey();
            Schema inputSchema = schemaLookup.get(unionSpec.getSchemaId());
            Dataset<Row> inputData = datasetInputData.get(inputSchema);

            Set<String> filters = filterMap.getOrDefault(unionSpec, emptySet());
            List<SelectColumn> previousSelects = new ArrayList<>();

            for (Select select : unionSpecSetEntry.getValue()) {
                Field outputField = findUnionField(fieldLookup, outputSchema.getId(), select);
                SelectColumn selectColumn = SelectColumnConverter.toUnionSelectColumn(
                        select, outputField, schemaLookup.get(select.unionSchemaId()));

                SelectResult selectResult = runSingleDependendantUnionSelect(
                        select,
                        previousSelects,
                        filters,
                        unionSpec,
                        schemaLookup,
                        pipelineUnionStep,
                        executeStepContext,
                        inputData,
                        outputField);

                selectResults.add(selectResult);
            }
        }

        return selectResults;
    }

    private SelectResult runSingleDependendantUnionSelect(
            final Select select,
            final List<SelectColumn> previousSelects,
            final Set<String> filters,
            final UnionSpec unionSpec,
            final Map<Long, Schema> schemaLookup,
            final PipelineUnionStep pipelineUnionStep,
            final ExecuteStepContext executeStepContext,
            final Dataset<Row> inputData,
            final Field outputField) {

        SelectColumn selectColumn = SelectColumnConverter.toUnionSelectColumn(
                select, outputField, schemaLookup.get(select.unionSchemaId()));

        Collection<SelectColumn> selectToRun = concat(previousSelects, selectColumn, LinkedHashSet::new);

        Dataset<Row> outputDataset;
        try {
            outputDataset = runMap(schemaLookup.get(select.unionSchemaId()), selectToRun, filters, inputData);

            if (select.isIntermediate()) {
                Select mockedSelect = setMockDataForSubsequentSelects(select, outputField);
                previousSelects.add(SelectColumnConverter.toUnionSelectColumn(
                        mockedSelect, outputField, schemaLookup.get(select.unionSchemaId())));
            }
        } catch (Exception e) {
            List<ErrorResponse> errors = handleException(pipelineUnionStep.getName(), e)
                    .getError();

            return SelectResult.error(
                    selectColumn.getAs(),
                    select.getOutputFieldId(),
                    unionSpec.getSchemaId(),
                    errors);
        }

        if (!outputContainsExpectedField(outputDataset, outputField)) {
            ErrorResponse errorResponse = ErrorResponse.valueOf(
                    "Output fields required the following fields that were not found " + toDisplayName(
                            SchemaStructTypeConverter.fieldToStructField(outputField)),
                    "UNMAPPED_OUTPUT_FIELDS");

            return SelectResult.error(
                    outputField.getName(),
                    outputField.getId(),
                    unionSpec.getSchemaId(),
                    singletonList(errorResponse));
        }

        return SelectResult.success(
                outputField.getName(),
                outputField.getId(),
                unionSpec.getSchemaId());
    }

    private Validation<List<ErrorResponse>, Dataset<Row>> executeSingleInput(
            final String stepName,
            final Set<Select> selects,
            final Set<String> filters,
            final ExecuteStepContext executeStepContext,
            final Function3<Collection<SelectColumn>, Set<String>, Dataset<Row>, Dataset<Row>> executor) {
        try {

            Schema inputSchema = executeStepContext.getInputSchemas().get(0);
            Schema outputSchema = executeStepContext.getOutputSchema();

            Dataset<Row> dataset = createDatasetInputData(executeStepContext).get(inputSchema);
            List<SelectColumn> selectColumns = createSelectColumns(selects, outputSchema)
                    .collect(Collectors.toList());

            dataset = executor.apply(selectColumns, filters, dataset);
            dataset.collectAsList();

            return Validation.valid(dataset);
        } catch (Exception e) {
            return handleException(stepName, e);
        }
    }

    private Validation<List<ErrorResponse>, Dataset<Row>> executeAggregation(
            final PipelineAggregationStep pipelineAggregationStep,
            final ExecuteStepContext executeStepContext) {

        Schema inputSchema = executeStepContext.getInputSchemas().get(0);
        Schema outputSchema = executeStepContext.getOutputSchema();
        Dataset<Row> dataset = createDatasetInputData(executeStepContext).get(inputSchema);

        Set<SelectColumn> selectColumns = createSelectColumns(pipelineAggregationStep.getSelects(), outputSchema)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Transformation transformation = TransformationConverter.convertAggregationStep(
                pipelineAggregationStep,
                selectColumns,
                inputSchema);

        try {
            Dataset<Row> outputDataset = sparkSqlExecutor.executeSqlNew(
                    transformation.toSparkSql(), singletonMap(inputSchema, dataset));

            return Validation.valid(outputDataset);
        } catch (Exception e) {
            return handleException(pipelineAggregationStep.getName(), e);
        }
    }

    private Validation<List<ErrorResponse>, Dataset<Row>> executeFullUnion(
            final PipelineUnionStep pipelineUnionStep,
            final ExecuteStepContext executeStepContext) {

        List<Schema> inputSchemas = executeStepContext.getInputSchemas();
        Schema outputSchema = executeStepContext.getOutputSchema();

        Map<String, Dataset<Row>> datasetInputs = createDatasetInputData(executeStepContext).entrySet()
                .stream()
                .collect(Collectors.toMap(entry -> entry.getKey().getPhysicalTableName(), Map.Entry::getValue));

        Map<Long, Schema> schemaLookup = new HashMap<>();
        Map<Long, Map<Long, Field>> fieldLookup = new HashMap<>();

        for (Schema inputSchema : inputSchemas) {
            schemaLookup.put(inputSchema.getId(), inputSchema);

            Map<Long, Field> fieldMap = inputSchema.getFields().stream()
                    .collect(toMap(Field::getId, Function.identity()));
            fieldLookup.put(inputSchema.getId(), fieldMap);
        }

        Map<Long, Field> outputFieldMap = outputSchema.getFields().stream()
                .collect(toMap(Field::getId, Function.identity()));
        fieldLookup.put(outputSchema.getId(), outputFieldMap);

        Map<UnionSpec, Set<SelectColumn>> unionSpecSetMap =
                pipelineUnionStep.getSelects().stream()
                        .sorted(Select.selectOrder())
                        .collect(CollectorUtils.toMultimap(
                                select ->
                                        TransformationConverter.convertUnionSpec(
                                                schemaLookup.get(select.getUnion()
                                                        .getUnionSchemaId())
                                                        .getPhysicalTableName(),
                                                select.getUnion().getUnionSchemaId()),
                                select -> SelectColumnConverter.toUnionSelectColumn(
                                        select,
                                        findUnionField(fieldLookup, outputSchema.getId(), select),
                                        schemaLookup.get(select.unionSchemaId()))));

        Map<UnionSpec, Set<String>> filterMap = createUnionFilterMap(pipelineUnionStep, schemaLookup);

        try {

            Dataset<Row> output = unionStepExecutor.runUnion(unionSpecSetMap, filterMap, datasetInputs);

            return Validation.valid(output);
        } catch (Exception e) {
            return handleException(pipelineUnionStep.getName(), e);
        }
    }

    private Field findUnionField(
            final Map<Long, Map<Long, Field>> fieldLookup,
            final Long schemaId,
            final Select select) {
        Map<Long, Field> fieldsForUnionSchema = fieldLookup.get(schemaId);

        if (fieldsForUnionSchema == null) {
            throw new IllegalStateException("No schema found for schema [" + schemaId + "]");
        }

        Field field = fieldsForUnionSchema.get(select.getOutputFieldId());
        if (field == null) {
            throw new IllegalStateException("No field found for field id ["
                    + select.getOutputFieldId()
                    + "] in schema ["
                    + schemaId
                    + "]");
        }
        return field;
    }

    private Validation<List<ErrorResponse>, Dataset<Row>> executeJoin(
            final PipelineJoinStep pipelineJoinStep,
            final ExecuteStepContext executeStepContext) {

        List<Schema> inputSchemas = executeStepContext.getInputSchemas();
        Schema outputSchema = executeStepContext.getOutputSchema();

        Map<Schema, Dataset<Row>> inputs = createDatasetInputData(executeStepContext);
        Set<SelectColumn> selectColumns = createSelectColumns(pipelineJoinStep.getSelects(), outputSchema)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Validation<List<ErrorResponse>, Transformation> transformation = TransformationConverter.convertJoinStep(
                pipelineJoinStep,
                selectColumns,
                inputSchemas);
        if (transformation.isInvalid()) {
            return Validation.invalid(transformation.getError());
        }

        try {

            Dataset<Row> outputDataset = sparkSqlExecutor.executeSqlNew(transformation.get().toSparkSql(), inputs);

            return Validation.valid(outputDataset);
        } catch (Exception e) {
            return handleException(pipelineJoinStep.getName(), e);
        }
    }

    private static Stream<SelectColumn> createSelectColumns(final Set<Select> selects, final Schema schema) {
        Map<Long, Field> fieldById = schema.getFields().stream()
                .collect(toMap(Field::getId, Function.identity()));

        return selects.stream()
                .sorted(Comparator.comparing(PipelineStepExecutor::orderOrOutputFieldId))
                .map(select -> SelectColumnConverter.toSelectColumn(select, fieldById.get(select.getOutputFieldId())));
    }

    private Select setMockDataForSubsequentSelects(final Select select, final Field outputField) {
        Comparable mockData = outputField.generateData();
        Select copiedSelect = select.copy();
        if (mockData instanceof Double) {
            copiedSelect.setSelect(mockData.toString() + "D");
        } else if (mockData instanceof Long) {
            copiedSelect.setSelect(mockData.toString() + "L");
        } else if (mockData instanceof String) {
            copiedSelect.setSelect("'" + mockData.toString() + "'");
        } else if (mockData instanceof Date) {
            copiedSelect.setSelect("date('" + mockData.toString() + "')");
        } else if (mockData instanceof Timestamp) {
            copiedSelect.setSelect("timestamp('" + mockData.toString() + "')");
        } else {
            copiedSelect.setSelect(mockData.toString());
        }

        log.debug("Mocking data for intermediate result {}", copiedSelect.getSelect());
        return copiedSelect;
    }

    private static long orderOrOutputFieldId(final Select select) {
        return select.getOrder() == null ? select.getOutputFieldId() : select.getOrder();
    }

    private Map<UnionSpec, Set<String>> createUnionFilterMap(
            final PipelineUnionStep pipelineUnionStep,
            final Map<Long, Schema> schemaLookup) {

        return orEmpty(pipelineUnionStep.getPipelineFilters()).stream()
                .collect(CollectorUtils.toMultimap(
                        select -> TransformationConverter.convertUnionSpec(
                                schemaLookup.get(select.getUnionSchemaId()).getPhysicalTableName(),
                                select.getUnionSchemaId()),
                        PipelineFilter::getFilter));
    }

    private Map<Schema, Dataset<Row>> createDatasetInputData(final ExecuteStepContext executeStepContext) {
        Map<Schema, Dataset<Row>> inputDatasets = new HashMap<>();
        for (Map.Entry<Schema, List<Row>> schemaAndData : executeStepContext.getExecutionInputData().entrySet()) {
            Schema schema = schemaAndData.getKey();
            List<Row> data = schemaAndData.getValue();
            Dataset<Row> dataset =
                    sparkSession.createDataFrame(data, schemaStructTypeConverter.apply(schema.getFields()));

            inputDatasets.put(schema, dataset);
        }
        return inputDatasets;
    }
}
