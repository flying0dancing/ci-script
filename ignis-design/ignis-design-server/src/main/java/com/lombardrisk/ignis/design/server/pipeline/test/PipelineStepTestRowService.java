package com.lombardrisk.ignis.design.server.pipeline.test;

import com.lombardrisk.ignis.client.design.pipeline.test.view.StepTestRowView;
import com.lombardrisk.ignis.data.common.Identifiable;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.data.common.utils.CsvUtils;
import com.lombardrisk.ignis.data.common.utils.CsvUtils.CsvOutputStream;
import com.lombardrisk.ignis.design.field.FieldService;
import com.lombardrisk.ignis.design.field.model.Field;
import com.lombardrisk.ignis.design.server.pipeline.api.PipelineStepRepository;
import com.lombardrisk.ignis.design.server.pipeline.api.PipelineStepTestRepository;
import com.lombardrisk.ignis.design.server.pipeline.api.PipelineStepTestRowRepository;
import com.lombardrisk.ignis.design.server.pipeline.model.PipelineStep;
import com.lombardrisk.ignis.design.server.pipeline.test.model.ActualDataRow;
import com.lombardrisk.ignis.design.server.pipeline.test.model.ExpectedDataRow;
import com.lombardrisk.ignis.design.server.pipeline.test.model.InputDataRow;
import com.lombardrisk.ignis.design.server.pipeline.test.model.PipelineStepTest;
import com.lombardrisk.ignis.design.server.pipeline.test.model.PipelineStepTestCell;
import com.lombardrisk.ignis.design.server.pipeline.test.model.PipelineStepTestRow;
import com.lombardrisk.ignis.design.server.productconfig.schema.Schema;
import com.lombardrisk.ignis.design.server.productconfig.schema.SchemaService;
import io.vavr.control.Option;
import io.vavr.control.Validation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@AllArgsConstructor
@Slf4j
public class PipelineStepTestRowService {

    private final PipelineStepRepository pipelineStepRepository;
    private final PipelineStepTestRepository pipelineStepTestRepository;
    private final PipelineStepTestRowRepository pipelineStepTestRowRepository;
    private final SchemaService schemaService;
    private final FieldService fieldService;
    private final Integer csvImportMaxLines;

    @Transactional
    public Validation<CRUDFailure, StepTestRowView> createInputDataRow(final Long stepTestId, final Long schemaId) {
        Option<PipelineStepTest> pipelineStepTestOption = pipelineStepTestRepository.findById(stepTestId);

        if (!pipelineStepTestOption.isDefined()) {
            return Validation.invalid(CRUDFailure.notFoundIds("PipelineStepTest", stepTestId));
        }

        Validation<CRUDFailure, Schema> schemaResult =
                findSchemaForInputRowData(pipelineStepTestOption.get(), schemaId);
        if (schemaResult.isInvalid()) {
            return Validation.invalid(schemaResult.getError());
        }

        InputDataRow inputDataRow = pipelineStepTestRowRepository.create(InputDataRow
                .builder()
                .schemaId(schemaId)
                .isRun(false)
                .pipelineStepTestId(stepTestId)
                .cells(schemaResult.get().getFields().stream()
                        .map(field -> PipelineStepTestCell.builder()
                                .fieldId(field.getId())
                                .data("")
                                .build())
                        .collect(Collectors.toSet()))
                .build());

        return Validation.valid(
                toStepTestRowView(inputDataRow));
    }

    @Transactional
    public Validation<CRUDFailure, List<StepTestRowView>> importCsvInputDataRow(
            final Long stepTestId,
            final Long schemaId,
            final InputStream inputStream) {
        Option<PipelineStepTest> pipelineStepTestOption = pipelineStepTestRepository.findById(stepTestId);

        if (!pipelineStepTestOption.isDefined()) {
            return Validation.invalid(CRUDFailure.notFoundIds("PipelineStepTest", stepTestId));
        }

        Validation<CRUDFailure, Schema> schemaResult =
                findSchemaForInputRowData(pipelineStepTestOption.get(), schemaId);

        if (schemaResult.isInvalid()) {
            return Validation.invalid(schemaResult.getError());
        }

        Validation<CRUDFailure, List<Map<String, String>>> csvContent = CsvUtils.readCsv(
                schemaResult.get().getFields().stream()
                        .map(Field::getName)
                        .collect(Collectors.toCollection(LinkedHashSet::new)),
                inputStream);

        if (csvContent.isInvalid()) {
            return Validation.invalid(csvContent.getError());
        }

        if (csvContent.get().size() > csvImportMaxLines) {
            return Validation.invalid(CRUDFailure.constraintFailure("Csv file would contain more lines ["
                    + csvContent.get().size()
                    + "] than max import lines allows [" + csvImportMaxLines + "]"));
        }

        List<InputDataRow> inputDataRows = csvContent.get().stream()
                .map(s -> createDataRow(stepTestId, schemaResult.get(), s))
                .collect(toList());

        return Validation.valid(
                inputDataRows.stream()
                        .map(this::toStepTestRowView)
                        .collect(toList()));
    }

    public Validation<CRUDFailure, StepTestRowView> createExpectedDataRow(final Long stepTestId, final Long schemaId) {
        Option<PipelineStepTest> pipelineStepTestOption = pipelineStepTestRepository.findById(stepTestId);

        if (!pipelineStepTestOption.isDefined()) {
            return Validation.invalid(CRUDFailure.notFoundIds("PipelineStepTest", stepTestId));
        }

        Validation<CRUDFailure, Schema> schemaResult =
                findSchemaForOutputRowData(pipelineStepTestOption.get(), schemaId);
        if (schemaResult.isInvalid()) {
            return Validation.invalid(schemaResult.getError());
        }

        ExpectedDataRow expectedDataRow = pipelineStepTestRowRepository.createExpected(ExpectedDataRow
                .builder()
                .schemaId(schemaId)
                .isRun(false)
                .pipelineStepTestId(stepTestId)
                .cells(schemaResult.get().getFields().stream()
                        .map(field -> PipelineStepTestCell.builder()
                                .fieldId(field.getId())
                                .data("")
                                .build())
                        .collect(Collectors.toSet()))
                .build());

        return Validation.valid(
                toStepTestRowView(expectedDataRow));
    }

    public Validation<CRUDFailure, List<StepTestRowView>> importCsvExpectedDataRow(
            final Long stepTestId,
            final Long schemaId,
            final InputStream inputStream) {
        Option<PipelineStepTest> pipelineStepTestOption = pipelineStepTestRepository.findById(stepTestId);

        if (!pipelineStepTestOption.isDefined()) {
            return Validation.invalid(CRUDFailure.notFoundIds("PipelineStepTest", stepTestId));
        }

        Validation<CRUDFailure, Schema> schemaResult =
                findSchemaForOutputRowData(pipelineStepTestOption.get(), schemaId);

        if (schemaResult.isInvalid()) {
            return Validation.invalid(schemaResult.getError());
        }

        Validation<CRUDFailure, List<Map<String, String>>> csvContent = CsvUtils.readCsv(
                schemaResult.get().getFields().stream()
                        .map(Field::getName)
                        .collect(Collectors.toCollection(LinkedHashSet::new)),
                inputStream);

        if (csvContent.isInvalid()) {
            return Validation.invalid(csvContent.getError());
        }

        if (csvContent.get().size() > csvImportMaxLines) {
            return Validation.invalid(CRUDFailure.constraintFailure("Csv file would contain more lines ["
                    + csvContent.get().size()
                    + "] than max import lines allows [" + csvImportMaxLines + "]"));
        }

        List<ExpectedDataRow> inputDataRows = csvContent.get().stream()
                .map(s -> createExpectedDataRow(stepTestId, schemaResult.get(), s))
                .collect(toList());

        return Validation.valid(
                inputDataRows.stream()
                        .map(this::toStepTestRowView)
                        .collect(toList()));
    }

    @Transactional
    public Validation<CRUDFailure, StepTestRowView> updateCellValue(
            final Long testId, final Long rowId,
            final Long rowCellId,
            final String cellData) {

        Optional<PipelineStepTestRow> optionalRow = pipelineStepTestRowRepository.findById(rowId);
        if (!optionalRow.isPresent()) {
            return Validation.invalid(CRUDFailure.notFoundIds(PipelineStepTestRow.class.getSimpleName(), rowId));
        }

        PipelineStepTestRow testRow = optionalRow.get();

        Optional<PipelineStepTestCell> optionalCell = testRow.getCells().stream()
                .filter(cell -> rowCellId.equals(cell.getId()))
                .findAny();

        if (!optionalCell.isPresent()) {
            return Validation.invalid(CRUDFailure.notFoundIds(PipelineStepTestCell.class.getSimpleName(), rowCellId));
        }

        PipelineStepTestCell cell = optionalCell.get();

        Validation<CRUDFailure, Field> optionalField = fieldService.findWithValidation(cell.getFieldId());

        if (optionalField.isInvalid()) {
            return Validation.invalid(optionalField.getError());
        }

        Field field = optionalField.get();
        try {
            field.parse(cellData);
        } catch (Exception e) {
            log.error("Could not parse cell data [{}]", cellData, e);
            return Validation.invalid(CRUDFailure.invalidParameters()
                    .paramError(field.getName() + " : " + cellData, e.getMessage())
                    .asFailure());
        }

        testRow.setRun(false);
        cell.setData(cellData);

        PipelineStepTestRow updatedRow = pipelineStepTestRowRepository.saveRow(testRow);

        return Validation.valid(toStepTestRowView(updatedRow));
    }

    public Validation<CRUDFailure, Identifiable> deleteInput(final long id) {
        Option<PipelineStepTestRow> pipelineStepTestRows =
                Option.ofOptional(pipelineStepTestRowRepository.findById(id));

        if (pipelineStepTestRows.isEmpty()) {
            return Validation.invalid(CRUDFailure.notFoundIds("PipelineStepTestRow", id));
        }
        pipelineStepTestRowRepository.deleteInput(pipelineStepTestRows.get());
        return Validation.valid(() -> id);
    }

    public Validation<CRUDFailure, Identifiable> deleteExpected(final long id) {
        Option<PipelineStepTestRow> pipelineStepTestRows =
                Option.ofOptional(pipelineStepTestRowRepository.findById(id));

        if (pipelineStepTestRows.isEmpty()) {
            return Validation.invalid(CRUDFailure.notFoundIds("PipelineStepTestRow", id));
        }
        pipelineStepTestRowRepository.deleteExpected(pipelineStepTestRows.get());
        return Validation.valid(() -> id);
    }

    @Transactional
    public <T extends OutputStream> Validation<CRUDFailure, CsvOutputStream<T>> exportCsvInputDataRow(
            final Long stepTestId,
            final Long schemaId, final T outputStream) {
        Option<PipelineStepTest> pipelineStepOption = pipelineStepTestRepository.findById(stepTestId);

        if (!pipelineStepOption.isDefined()) {
            return Validation.invalid(CRUDFailure.notFoundIds("PipelineStepTest", stepTestId));
        }

        Validation<CRUDFailure, Schema> schemaResult =
                findSchemaForInputRowData(pipelineStepOption.get(), schemaId);

        if (schemaResult.isInvalid()) {
            return Validation.invalid(schemaResult.getError());
        }

        List<InputDataRow> inputRows = pipelineStepTestRowRepository.getTestInputRows(stepTestId)
                .stream()
                .filter(item -> Long.compare(item.getSchemaId(), schemaId) == 0)
                .collect(Collectors.toList());

        List<List<String>> csvContent = buildCsvContent(
                schemaResult.get().getFields(),
                inputRows.stream()
                        .map(p -> buildCsvLine(p.getCells()))
                        .collect(toList()));

        return CsvUtils.writeCsv(
                outputStream,
                String.format("input_%s", schemaResult.get().getDisplayName()),
                csvContent);
    }

    public <T extends OutputStream> Validation<CRUDFailure, CsvOutputStream<T>> exportCsvExpectedDataRow(
            final Long stepTestId,
            final Long schemaId, final T outputStream) {
        Option<PipelineStepTest> pipelineStepOption = pipelineStepTestRepository.findById(stepTestId);

        if (!pipelineStepOption.isDefined()) {
            return Validation.invalid(CRUDFailure.notFoundIds("PipelineStepTest", stepTestId));
        }

        Validation<CRUDFailure, Schema> schemaResult =
                findSchemaForOutputRowData(pipelineStepOption.get(), schemaId);

        if (schemaResult.isInvalid()) {
            return Validation.invalid(schemaResult.getError());
        }

        List<ExpectedDataRow> expectedRows = pipelineStepTestRowRepository.getTestExpectedRows(stepTestId);

        List<List<String>> csvContent = buildCsvContent(
                schemaResult.get().getFields(),
                expectedRows.stream()
                        .map(p -> buildCsvLine(p.getCells()))
                        .collect(toList()));

        return CsvUtils.writeCsv(
                outputStream,
                String.format("expected_%s", schemaResult.get().getDisplayName()),
                csvContent);
    }

    public <T extends OutputStream> Validation<CRUDFailure, CsvOutputStream<T>> exportCsvActualDataRow(
            final Long stepTestId,
            final Long schemaId, final T outputStream) {
        Option<PipelineStepTest> pipelineStepOption = pipelineStepTestRepository.findById(stepTestId);

        if (!pipelineStepOption.isDefined()) {
            return Validation.invalid(CRUDFailure.notFoundIds("PipelineStepTest", stepTestId));
        }

        Validation<CRUDFailure, Schema> schemaResult =
                findSchemaForOutputRowData(pipelineStepOption.get(), schemaId);

        if (schemaResult.isInvalid()) {
            return Validation.invalid(schemaResult.getError());
        }

        List<ActualDataRow> actualRows = pipelineStepTestRowRepository.getTestActualRows(stepTestId);

        List<List<String>> csvContent = buildCsvContent(
                schemaResult.get().getFields(),
                actualRows.stream()
                        .map(p -> buildCsvLine(p.getCells()))
                        .collect(toList()));

        return CsvUtils.writeCsv(
                outputStream,
                String.format("actual_%s", schemaResult.get().getDisplayName()),
                csvContent);
    }

    private Validation<CRUDFailure, Schema> findSchemaForInputRowData(
            final PipelineStepTest pipelineStepTest,
            final Long schemaId) {

        Option<PipelineStep> pipelineStepOption = pipelineStepRepository
                .findById(pipelineStepTest.getPipelineStepId());
        if (!pipelineStepOption.isDefined()) {
            return Validation.invalid(CRUDFailure.notFoundIds(
                    "PipelineStep",
                    pipelineStepTest.getPipelineStepId()));
        }

        if (!pipelineStepOption.get().getInputs().contains(schemaId)) {
            return Validation.invalid(schemaService.notFound(schemaId));
        }

        Option<Schema> schemaOption = schemaService.findById(schemaId);
        if (!schemaOption.isDefined()) {

            return Validation.invalid(schemaService.notFound(schemaId));
        }
        return Validation.valid(schemaOption.get());
    }

    private Validation<CRUDFailure, Schema> findSchemaForOutputRowData(
            final PipelineStepTest pipelineStepTest,
            final Long schemaId) {

        Option<PipelineStep> pipelineStepOption = pipelineStepRepository
                .findById(pipelineStepTest.getPipelineStepId());
        if (!pipelineStepOption.isDefined()) {
            return Validation.invalid(CRUDFailure.notFoundIds(
                    "PipelineStep",
                    pipelineStepTest.getPipelineStepId()));
        }

        if (!pipelineStepOption.get().getOutput().equals(schemaId)) {
            return Validation.invalid(schemaService.notFound(schemaId));
        }

        Option<Schema> schemaOption = schemaService.findById(schemaId);
        if (!schemaOption.isDefined()) {
            return Validation.invalid(schemaService.notFound(schemaId));
        }
        return Validation.valid(schemaOption.get());
    }

    private StepTestRowView toStepTestRowView(final PipelineStepTestRow inputDataRow) {
        return StepTestRowView.builder()
                .id(inputDataRow.getId())
                .cells(inputDataRow.getCells().stream()
                        .map(cell -> StepTestRowView.CellIdAndField.builder()
                                .id(cell.getId())
                                .cellData(cell.getData())
                                .fieldId(cell.getFieldId())
                                .build())
                        .collect(toList()))
                .build();
    }

    private InputDataRow createDataRow(
            final Long stepTestId,
            final Schema schema,
            final Map<String, String> csvRow) {

        return pipelineStepTestRowRepository.create(InputDataRow
                .builder()
                .schemaId(schema.getId())
                .isRun(false)
                .pipelineStepTestId(stepTestId)
                .cells(schema.getFields().stream()
                        .map(field -> PipelineStepTestCell.builder()
                                .fieldId(field.getId())
                                .data(csvRow.get(field.getName()))
                                .build())
                        .filter(pipelineStepTestCell -> pipelineStepTestCell.getData() != null)
                        .collect(Collectors.toSet()))
                .build());
    }

    private ExpectedDataRow createExpectedDataRow(
            final Long stepTestId,
            final Schema schema,
            final Map<String, String> csvRow) {

        return pipelineStepTestRowRepository.createExpected(ExpectedDataRow
                .builder()
                .schemaId(schema.getId())
                .isRun(false)
                .pipelineStepTestId(stepTestId)
                .cells(schema.getFields().stream()
                        .map(field -> PipelineStepTestCell.builder()
                                .fieldId(field.getId())
                                .data(csvRow.get(field.getName()))
                                .build())
                        .filter(pipelineStepTestCell -> pipelineStepTestCell.getData() != null)
                        .collect(Collectors.toSet()))
                .build());
    }

    private List<String> buildCsvLine(Set<PipelineStepTestCell> cells) {
        return cells.stream()
                .sorted(Comparator.comparing(PipelineStepTestCell::getFieldId))
                .map(cell -> cell.getData() != null ? cell.getData() : "")
                .collect(toList());
    }

    private List<List<String>> buildCsvContent(final Set<Field> fields, List<List<String>> csvLines) {
        List<List<String>> csvContent = new ArrayList<>();

        csvContent.add(fields.stream()
                .sorted(Comparator.comparing(Field::getId))
                .map(Field::getName)
                .collect(Collectors.toList()));

        csvContent.addAll(csvLines);

        return csvContent;
    }
}
