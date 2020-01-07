package com.lombardrisk.ignis.design.server.pipeline.test;

import com.lombardrisk.ignis.client.design.pipeline.test.StepTestStatus;
import com.lombardrisk.ignis.client.design.pipeline.test.request.CreateStepTestRequest;
import com.lombardrisk.ignis.client.design.pipeline.test.request.UpdateStepTestRequest;
import com.lombardrisk.ignis.client.design.pipeline.test.view.StepRowInputDataView;
import com.lombardrisk.ignis.client.design.pipeline.test.view.StepRowOutputDataView;
import com.lombardrisk.ignis.client.design.pipeline.test.view.StepTestCellView;
import com.lombardrisk.ignis.client.design.pipeline.test.view.StepTestView;
import com.lombardrisk.ignis.client.design.pipeline.test.view.TestRunResultView;
import com.lombardrisk.ignis.data.common.Identifiable;
import com.lombardrisk.ignis.data.common.error.ErrorResponse;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.data.common.failure.NotFoundFailure;
import com.lombardrisk.ignis.design.field.model.DateField;
import com.lombardrisk.ignis.design.field.model.Field;
import com.lombardrisk.ignis.design.server.pipeline.PipelineStepSelectsValidator;
import com.lombardrisk.ignis.design.server.pipeline.api.PipelineStepRepository;
import com.lombardrisk.ignis.design.server.pipeline.api.PipelineStepTestRepository;
import com.lombardrisk.ignis.design.server.pipeline.api.PipelineStepTestRowRepository;
import com.lombardrisk.ignis.design.server.pipeline.model.PipelineStep;
import com.lombardrisk.ignis.design.server.pipeline.test.model.ActualDataRow;
import com.lombardrisk.ignis.design.server.pipeline.test.model.ExpectedDataRow;
import com.lombardrisk.ignis.design.server.pipeline.test.model.InputDataRow;
import com.lombardrisk.ignis.design.server.pipeline.test.model.PipelineStepTest;
import com.lombardrisk.ignis.design.server.pipeline.test.model.PipelineStepTestCell;
import com.lombardrisk.ignis.design.server.pipeline.test.model.PipelineStepTestResult;
import com.lombardrisk.ignis.design.server.pipeline.test.model.PipelineStepTestRow;
import com.lombardrisk.ignis.design.server.productconfig.schema.SchemaService;
import io.vavr.control.Option;
import io.vavr.control.Validation;
import lombok.AllArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Collections.singleton;

@AllArgsConstructor
public class PipelineStepTestService {

    private final PipelineStepRepository pipelineStepRepository;
    private final PipelineStepTestRepository pipelineStepTestRepository;
    private final PipelineStepTestRowRepository pipelineStepTestRowRepository;
    private final PipelineStepTestExecuteService pipelineStepTestExecuteService;
    private final PipelineStepSelectsValidator pipelineStepSelectsValidator;
    private final SchemaService schemaService;

    public Validation<CRUDFailure, Identifiable> create(final CreateStepTestRequest request) {
        Option<PipelineStep> pipelineStepOptional = pipelineStepRepository.findById(request.getPipelineStepId());

        if (!pipelineStepOptional.isDefined()) {
            return Validation.invalid(CRUDFailure.notFoundIds("PipelineStep", request.getPipelineStepId()));
        }

        PipelineStepTest stepTest = pipelineStepTestRepository.save(PipelineStepTest
                .builder()
                .pipelineStepId(request.getPipelineStepId())
                .name(request.getName())
                .pipelineStepStatus(StepTestStatus.PENDING)
                .description(request.getDescription())
                .testReferenceDate(request.getTestReferenceDate())
                .build());

        return Validation.valid(stepTest);
    }

    public List<StepTestView> findAllTestsByStepId(final long pipelineStepId) {
        return pipelineStepTestRepository.findViewsByPipelineStepId(pipelineStepId);
    }

    public List<StepTestView> findAllTestsByPipelineId(final long pipelineId) {
        return pipelineStepTestRepository.findViewsByPipelineId(pipelineId);
    }

    public Validation<CRUDFailure, StepTestView> findById(final Long id) {
        return pipelineStepTestRepository.findViewById(id)
                .toValid(notFound(id));
    }

    @Transactional
    public Validation<CRUDFailure, Map<Long, List<StepRowInputDataView>>> getTestInputRows(final Long testId) {
        Validation<CRUDFailure, PipelineStep> pipelineStep = pipelineStepTestRepository.findById(testId)
                .map(PipelineStepTest::getPipelineStep)
                .toValid(notFound(testId));

        if (pipelineStep.isInvalid()) {
            return Validation.invalid(pipelineStep.getError());
        }

        List<InputDataRow> inputRows = pipelineStepTestRowRepository.getTestInputRows(testId);
        return Validation.valid(toInputData(pipelineStep.get(), inputRows));
    }

    public List<StepRowOutputDataView> getTestOutputRows(final Long testId) {
        List<ExpectedDataRow> expectedRows = pipelineStepTestRowRepository.getTestExpectedRows(testId);
        List<ActualDataRow> actualRows = pipelineStepTestRowRepository.getTestActualRows(testId);
        return toOutputData(expectedRows, actualRows);
    }

    public NotFoundFailure<Long> notFound(final Long id) {
        return CRUDFailure.notFoundIds("StepTest", id);
    }

    @Transactional
    public Validation<CRUDFailure, Identifiable> deleteById(final Long id) {
        Option<PipelineStepTest> testResult = pipelineStepTestRepository.findById(id);
        if (!testResult.isDefined()) {
            return Validation.invalid(notFound(id));
        }

        PipelineStepTest pipelineStepTest = testResult.get();

        Set<Long> rowIds = pipelineStepTest.dependantRows()
                .map(PipelineStepTestRow::getId)
                .collect(Collectors.toSet());

        pipelineStepTestRowRepository.deleteRowsById(rowIds);

        pipelineStepTestRepository.delete(pipelineStepTest);
        return Validation.valid(() -> id);
    }

    @Transactional
    public Validation<List<ErrorResponse>, TestRunResultView> runTest(final Long testId) {
        Option<PipelineStepTest> testOptional = pipelineStepTestRepository.findById(testId);
        if (!testOptional.isDefined()) {
            return Validation.invalid(Collections.singletonList(
                    notFound(testId)
                            .toErrorResponse()));
        }

        PipelineStepTest pipelineStepTest = testOptional.get();
        PipelineStep pipelineStep = pipelineStepTest.getPipelineStep();

        Validation<List<ErrorResponse>, PipelineStepTestResult> stepTestExecutionResultValidation =
                pipelineStepTestExecuteService.runTest(pipelineStep, pipelineStepTest);

        if (stepTestExecutionResultValidation.isValid()) {
            PipelineStepTestResult stepTestExecutionResult = stepTestExecutionResultValidation.get();
            updateInputAndExpectedRows(testId, stepTestExecutionResult);
            saveActualRows(testId, pipelineStep, stepTestExecutionResult);

            pipelineStepTest.setPipelineStepStatus(stepTestExecutionResult.getStatus());
            pipelineStepTestRepository.save(pipelineStepTest);

            return Validation.valid(TestRunResultView.builder()
                    .id(testId)
                    .status(StepTestStatus.valueOf(stepTestExecutionResult.getStatus().name()))
                    .build());
        } else {
            return Validation.invalid(stepTestExecutionResultValidation.getError());
        }
    }

    private void saveActualRows(
            final Long testId,
            final PipelineStep pipelineStep,
            final PipelineStepTestResult stepTestExecutionResult) {
        List<Map<String, Object>> unexpectedRows = stepTestExecutionResult.getUnexpected();
        List<ActualDataRow> actualDataRows =
                buildActualDataRows(testId, pipelineStep, stepTestExecutionResult, unexpectedRows);

        pipelineStepTestRowRepository.saveActualRows(actualDataRows);
    }

    private void updateInputAndExpectedRows(final Long testId, final PipelineStepTestResult stepTestExecutionResult) {
        if (stepTestExecutionResult.getInputRows().size() > 0) {
            pipelineStepTestRowRepository.updateInputRowsRunStatus(stepTestExecutionResult.getInputRows(), true);
        }
        if (stepTestExecutionResult.getMatching().size() > 0) {
            pipelineStepTestRowRepository.updateMatchingExpectedRows(stepTestExecutionResult.getMatching());
        }
        if (stepTestExecutionResult.getNotFound().size() > 0) {
            pipelineStepTestRowRepository.updateNotFoundExpectedRows(stepTestExecutionResult.getNotFound());
        }
        pipelineStepTestRowRepository.clearPreviousActualRows(testId);
    }

    public Validation<CRUDFailure, StepTestView> update(final Long stepTestId, final UpdateStepTestRequest request) {
        Option<PipelineStepTest> pipelineStepTestOptional = pipelineStepTestRepository.findById(stepTestId);

        if (!pipelineStepTestOptional.isDefined()) {
            return Validation.invalid(CRUDFailure.notFoundIds("PipelineStepTest", stepTestId));
        }

        PipelineStepTest pipelineStepTest = pipelineStepTestOptional.get();
        pipelineStepTest.setName(request.getName());
        pipelineStepTest.setDescription(request.getDescription());
        pipelineStepTest.setTestReferenceDate(request.getTestReferenceDate());

        PipelineStepTest stepTest = pipelineStepTestRepository.save(pipelineStepTest);

        return Validation.valid(pipelineStepTestRepository.findViewById(stepTest.getId()).get());
    }

    private List<ActualDataRow> buildActualDataRows(
            final Long testId,
            final PipelineStep pipelineStep,
            final PipelineStepTestResult stepTestExecutionResult,
            final List<Map<String, Object>> unexpectedRows) {
        Map<String, Field> fieldLookup = schemaService.findAllByIds(singleton(pipelineStep.getOutput())).stream()
                .flatMap(schema -> schema.getFields().stream())
                .collect(Collectors.toMap(Field::getName, field -> field));

        int unexpectedCheckedCount = unexpectedRows.size();
        List<ActualDataRow> actualDataRows = new ArrayList<>();
        for (Map<String, Object> rowMap : stepTestExecutionResult.getActualResults()) {

            Set<PipelineStepTestCell> cells = new LinkedHashSet<>();

            for (Map.Entry<String, Object> columnToValue : rowMap.entrySet()) {
                Field field = fieldLookup.get(columnToValue.getKey());
                cells.add(PipelineStepTestCell.builder()
                        .fieldId(field.getId())
                        .data(columnToValue.getValue() == null ? "" :
                                field instanceof DateField ?
                                        new SimpleDateFormat(((DateField) field).getFormat())
                                                .format(columnToValue.getValue()) :
                                        field.parse(columnToValue.getValue().toString()).toString())
                        .build());
            }

            ActualDataRow.Status status;
            if (unexpectedRows.contains(rowMap) && unexpectedCheckedCount > 0) {
                status = ActualDataRow.Status.UNEXPECTED;
                unexpectedCheckedCount--;
            } else {
                status = ActualDataRow.Status.MATCHED;
            }

            ActualDataRow actualDataRow = ActualDataRow.builder()
                    .schemaId(pipelineStep.getOutput())
                    .isRun(true)
                    .pipelineStepTestId(testId)
                    .cells(cells)
                    .status(status)
                    .build();
            actualDataRows.add(actualDataRow);
        }
        return actualDataRows;
    }

    private Map<Long, List<StepRowInputDataView>> toInputData(
            final PipelineStep pipelineStep,
            final List<InputDataRow> inputDataRows) {

        Map<Long, List<StepRowInputDataView>> schemaToInputRows = pipelineStep.getInputs().stream()
                .collect(Collectors.toMap(Function.identity(), value -> new ArrayList<>()));

        inputDataRows.stream().map(
                inputDataRow -> StepRowInputDataView.builder()
                        .id(inputDataRow.getId())
                        .schemaId(inputDataRow.getSchemaId())
                        .run(inputDataRow.isRun())
                        .cells(inputDataRow.getCells().stream()
                                .map(cell -> StepTestCellView.builder()
                                        .id(cell.getId())
                                        .fieldId(cell.getFieldId())
                                        .data(cell.getData())
                                        .build())
                                .collect(Collectors.toMap(StepTestCellView::getFieldId, Function.identity())))
                        .build())
                .forEach(stepRowInputDataView ->
                        schemaToInputRows.get(stepRowInputDataView.getSchemaId())
                                .add(stepRowInputDataView));

        return schemaToInputRows;
    }

    private List<StepRowOutputDataView> toOutputData(
            final List<ExpectedDataRow> expectedData,
            final List<ActualDataRow> actualData) {
        List<StepRowOutputDataView> expectedDataViews = expectedData.stream()
                .map(PipelineStepTestService::toOutputDataFromExpectedData)
                .collect(Collectors.toList());

        List<StepRowOutputDataView> actualDataViews = actualData.stream()
                .map(PipelineStepTestService::toOutputDataFromActualData)
                .collect(Collectors.toList());

        expectedDataViews.addAll(actualDataViews);

        return expectedDataViews;
    }

    private static StepRowOutputDataView toOutputDataFromExpectedData(final ExpectedDataRow expectedDataRow) {
        return StepRowOutputDataView.builder()
                .id(expectedDataRow.getId())
                .run(expectedDataRow.isRun())
                .type(expectedDataRow.getType().name())
                .status(expectedDataRow.getStatus() != null ? expectedDataRow.getStatus().name() : "")
                .schemaId(expectedDataRow.getSchemaId())
                .cells(expectedDataRow.getCells().stream()
                        .map(cell -> StepTestCellView.builder()
                                .id(cell.getId())
                                .fieldId(cell.getFieldId())
                                .data(cell.getData())
                                .build())
                        .collect(Collectors.toMap(StepTestCellView::getFieldId, Function.identity())))
                .build();
    }

    private static StepRowOutputDataView toOutputDataFromActualData(final ActualDataRow actualDataRow) {
        return StepRowOutputDataView.builder()
                .id(actualDataRow.getId())
                .run(actualDataRow.isRun())
                .type(actualDataRow.getType().name())
                .status(actualDataRow.getStatus() != null ? actualDataRow.getStatus().name() : "")
                .cells(actualDataRow.getCells().stream()
                        .map(cell -> StepTestCellView.builder()
                                .id(cell.getId())
                                .fieldId(cell.getFieldId())
                                .data(cell.getData())
                                .build())
                        .collect(Collectors.toMap(StepTestCellView::getFieldId, Function.identity())))
                .build();
    }
}
