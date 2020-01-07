package com.lombardrisk.ignis.server.dataset.drillback;

import com.lombardrisk.ignis.client.external.drillback.DatasetRowDataView;
import com.lombardrisk.ignis.client.external.drillback.DrillBackStepDetails;
import com.lombardrisk.ignis.client.external.productconfig.view.FieldView;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.data.common.search.FilterExpression;
import com.lombardrisk.ignis.pipeline.step.api.DrillbackColumnLink;
import com.lombardrisk.ignis.server.dataset.DatasetService;
import com.lombardrisk.ignis.server.dataset.model.Dataset;
import com.lombardrisk.ignis.server.dataset.pipeline.invocation.PipelineInvocationService;
import com.lombardrisk.ignis.server.dataset.pipeline.invocation.model.PipelineStepInvocation;
import com.lombardrisk.ignis.server.dataset.result.DatasetResultRepository;
import com.lombardrisk.ignis.server.dataset.result.DatasetRowData;
import com.lombardrisk.ignis.server.product.pipeline.PipelineService;
import com.lombardrisk.ignis.server.product.pipeline.PipelineStepService;
import com.lombardrisk.ignis.server.product.pipeline.model.Pipeline;
import com.lombardrisk.ignis.server.product.pipeline.model.PipelineAggregationStep;
import com.lombardrisk.ignis.server.product.pipeline.model.PipelineJoinStep;
import com.lombardrisk.ignis.server.product.pipeline.model.PipelineMapStep;
import com.lombardrisk.ignis.server.product.pipeline.model.PipelineScriptletStep;
import com.lombardrisk.ignis.server.product.pipeline.model.PipelineStep;
import com.lombardrisk.ignis.server.product.pipeline.model.PipelineUnionStep;
import com.lombardrisk.ignis.server.product.pipeline.model.PipelineWindowStep;
import com.lombardrisk.ignis.server.product.pipeline.transformation.ScriptletInput;
import com.lombardrisk.ignis.server.product.table.TableService;
import com.lombardrisk.ignis.server.product.table.model.Table;
import com.lombardrisk.ignis.server.product.table.view.FieldToFieldView;
import com.lombardrisk.ignis.server.product.util.PageConverter;
import io.vavr.control.Validation;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.lombardrisk.ignis.common.stream.CollectorUtils.toNestedMap;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.toList;

@AllArgsConstructor
public class DrillBackService {

    private final DatasetResultRepository datasetResultRepository;
    private final DatasetService datasetService;
    private final PipelineService pipelineService;
    private final PipelineStepService pipelineStepService;
    private final PipelineInvocationService pipelineInvocationService;
    private final TableService tableService;
    private final FieldToFieldView fieldToFieldView = new FieldToFieldView();
    private final PageConverter pageConverter = new PageConverter();

    @Transactional
    public Validation<CRUDFailure, DrillBackStepDetails> findDrillBackStepDetails(
            final long pipelineId, final long pipelineStepId) {

        Validation<CRUDFailure, Pipeline> findPipelineResult = pipelineService.findById(pipelineId);
        if (findPipelineResult.isInvalid()) {
            return Validation.invalid(findPipelineResult.getError());
        }

        Optional<PipelineStep> stepResult = findPipelineResult.get().getSteps().stream()
                .filter(step -> step.getId().equals(pipelineStepId))
                .findFirst();

        if (!stepResult.isPresent()) {
            return Validation.invalid(CRUDFailure.notFoundIds("PipelineStep", pipelineStepId));
        }

        PipelineStep pipelineStep = stepResult.get();
        switch (pipelineStep.getType()) {
            case MAP:
                PipelineMapStep pipelineMapStep = (PipelineMapStep) pipelineStep;
                return drillBackMapStep(pipelineMapStep);
            case AGGREGATION:
                PipelineAggregationStep pipelineAggregationStep = (PipelineAggregationStep) pipelineStep;
                return drillBackAggregationStep(pipelineAggregationStep);
            case JOIN:
                return drillBackJoinStep((PipelineJoinStep) pipelineStep);
            case WINDOW:
                return drillBackWindowStep((PipelineWindowStep) pipelineStep);
            case UNION:
                return drillBackUnionStep((PipelineUnionStep) pipelineStep);
            case SCRIPTLET:
                return drillBackScriptletStep((PipelineScriptletStep) pipelineStep);
            default:
                throw new NotImplementedException("Drillback not supported for " + pipelineStep.getType());
        }
    }

    private Validation<CRUDFailure, DrillBackStepDetails> drillBackMapStep(final PipelineMapStep pipelineStep) {
        Long schemaInId = pipelineStep.getSchemaInId();
        Long schemaOutId = pipelineStep.getSchemaOutId();

        Validation<CRUDFailure, Table> schemaInResult = tableService.findWithValidation(schemaInId);
        Validation<CRUDFailure, Table> schemaOutResult = tableService.findWithValidation(schemaOutId);

        if (schemaInResult.isInvalid() || schemaOutResult.isInvalid()) {
            throw cannotFindSchemasForStepIllegalState(pipelineStep.getId());
        }

        List<FieldView> schemaIn = convertToFieldViews(schemaInResult.get());
        List<FieldView> schemaOut = convertToFieldViews(schemaOutResult.get());

        Map<String, Map<String, String>> drillbackColumnMap = pipelineStepService
                .getDrillbackColumnLinks(pipelineStep).stream()
                .collect(toNestedMap(
                        DrillbackColumnLink::getInputSchema,
                        Function.identity(),
                        DrillbackColumnLink::getInputColumn,
                        DrillbackColumnLink::toDrillbackColumn));

        return Validation.valid(DrillBackStepDetails.builder()
                .schemasIn(singletonMap(schemaInId, schemaIn))
                .schemasOut(singletonMap(schemaOutId, schemaOut))
                .schemaToInputFieldToOutputFieldMapping(drillbackColumnMap)
                .build());
    }

    private Validation<CRUDFailure, DrillBackStepDetails> drillBackAggregationStep(
            final PipelineAggregationStep pipelineStep) {

        Long schemaInId = pipelineStep.getSchemaInId();
        Long schemaOutId = pipelineStep.getSchemaOutId();

        Validation<CRUDFailure, Table> schemaInResult = tableService.findWithValidation(schemaInId);
        Validation<CRUDFailure, Table> schemaOutResult = tableService.findWithValidation(schemaOutId);

        if (schemaInResult.isInvalid() || schemaOutResult.isInvalid()) {
            throw cannotFindSchemasForStepIllegalState(pipelineStep.getId());
        }

        List<FieldView> schemaIn = convertToFieldViews(schemaInResult.get());
        List<FieldView> schemaOut = convertToFieldViews(schemaOutResult.get());

        Map<String, Map<String, String>> drillbackColumnMap = pipelineStepService
                .getDrillbackColumnLinks(pipelineStep).stream()
                .collect(toNestedMap(
                        DrillbackColumnLink::getInputSchema,
                        Function.identity(),
                        DrillbackColumnLink::getInputColumn,
                        DrillbackColumnLink::toDrillbackColumn));

        return Validation.valid(DrillBackStepDetails.builder()
                .schemasIn(singletonMap(schemaInId, schemaIn))
                .schemasOut(singletonMap(schemaOutId, schemaOut))
                .schemaToInputFieldToOutputFieldMapping(drillbackColumnMap)
                .build());
    }

    private Validation<CRUDFailure, DrillBackStepDetails> drillBackJoinStep(final PipelineJoinStep pipelineStep) {
        Long schemaOutId = pipelineStep.getSchemaOutId();

        Validation<CRUDFailure, List<Table>> tablesOrNotFound =
                tableService.findOrIdsNotFound(pipelineStep.findInputSchemas());

        Validation<CRUDFailure, Table> schemaOutResult = tableService.findWithValidation(
                schemaOutId);

        if (tablesOrNotFound.isInvalid() || schemaOutResult.isInvalid()) {
            throw cannotFindSchemasForStepIllegalState(pipelineStep.getId());
        }

        List<Table> inputSchemas = tablesOrNotFound.get();
        Map<Long, List<FieldView>> schemasIn = inputSchemas.stream()
                .collect(Collectors.toMap(Table::getId, this::convertToFieldViews));

        Table schemaOut = schemaOutResult.get();
        List<FieldView> schemaOutFields = convertToFieldViews(schemaOut);

        Map<String, Map<String, String>> drillbackColumnMap = pipelineStepService
                .getDrillbackColumnLinks(pipelineStep).stream()
                .collect(toNestedMap(
                        DrillbackColumnLink::getInputSchema,
                        Function.identity(),
                        DrillbackColumnLink::getInputColumn,
                        DrillbackColumnLink::toDrillbackColumn));

        return Validation.valid(DrillBackStepDetails.builder()
                .schemasIn(schemasIn)
                .schemasOut(singletonMap(schemaOutId, schemaOutFields))
                .schemaToInputFieldToOutputFieldMapping(drillbackColumnMap)
                .build());
    }

    private Validation<CRUDFailure, DrillBackStepDetails> drillBackWindowStep(final PipelineWindowStep pipelineStep) {
        Long schemaInId = pipelineStep.getSchemaInId();
        Long schemaOutId = pipelineStep.getSchemaOutId();

        Validation<CRUDFailure, Table> schemaInResult = tableService.findWithValidation(schemaInId);
        Validation<CRUDFailure, Table> schemaOutResult = tableService.findWithValidation(schemaOutId);

        if (schemaInResult.isInvalid() || schemaOutResult.isInvalid()) {
            throw cannotFindSchemasForStepIllegalState(pipelineStep.getId());
        }

        List<FieldView> schemaIn = convertToFieldViews(schemaInResult.get());
        List<FieldView> schemaOut = convertToFieldViews(schemaOutResult.get());

        Map<String, Map<String, String>> drillbackColumnMap = pipelineStepService
                .getDrillbackColumnLinks(pipelineStep).stream()
                .collect(toNestedMap(
                        DrillbackColumnLink::getInputSchema,
                        Function.identity(),
                        DrillbackColumnLink::getInputColumn,
                        DrillbackColumnLink::toDrillbackColumn));

        return Validation.valid(DrillBackStepDetails.builder()
                .schemasIn(singletonMap(schemaInId, schemaIn))
                .schemasOut(singletonMap(schemaOutId, schemaOut))
                .schemaToInputFieldToOutputFieldMapping(drillbackColumnMap)
                .build());
    }

    private Validation<CRUDFailure, DrillBackStepDetails> drillBackUnionStep(final PipelineUnionStep pipelineStep) {
        Long schemaOutId = pipelineStep.getSchemaOutId();

        Validation<CRUDFailure, List<Table>> tablesOrNotFound =
                tableService.findOrIdsNotFound(pipelineStep.getSchemaInIds());

        Validation<CRUDFailure, Table> schemaOutResult = tableService.findWithValidation(
                schemaOutId);

        if (tablesOrNotFound.isInvalid() || schemaOutResult.isInvalid()) {
            throw cannotFindSchemasForStepIllegalState(pipelineStep.getId());
        }

        List<Table> inputSchemas = tablesOrNotFound.get();
        Map<Long, List<FieldView>> schemasIn = inputSchemas.stream()
                .collect(Collectors.toMap(Table::getId, this::convertToFieldViews));

        Table schemaOut = schemaOutResult.get();
        List<FieldView> schemaOutFields = convertToFieldViews(schemaOut);

        Map<String, Map<String, String>> drillbackColumnMap = pipelineStepService
                .getDrillbackColumnLinks(pipelineStep).stream()
                .collect(toNestedMap(
                        DrillbackColumnLink::getInputSchema,
                        Function.identity(),
                        DrillbackColumnLink::getInputColumn,
                        DrillbackColumnLink::toDrillbackColumn));

        return Validation.valid(DrillBackStepDetails.builder()
                .schemasIn(schemasIn)
                .schemasOut(singletonMap(schemaOutId, schemaOutFields))
                .schemaToInputFieldToOutputFieldMapping(drillbackColumnMap)
                .build());
    }

    private Validation<CRUDFailure, DrillBackStepDetails> drillBackScriptletStep(
            final PipelineScriptletStep pipelineStep) {

        Set<Long> schemaInIds = pipelineStep.getSchemaIns().stream()
                .map(ScriptletInput::getSchemaInId)
                .collect(Collectors.toSet());

        Long schemaOutId = pipelineStep.getSchemaOutId();

        Validation<CRUDFailure, List<Table>> tablesOrNotFound = tableService.findOrIdsNotFound(schemaInIds);
        Validation<CRUDFailure, Table> schemaOutResult = tableService.findWithValidation(schemaOutId);

        if (tablesOrNotFound.isInvalid() || schemaOutResult.isInvalid()) {
            throw cannotFindSchemasForStepIllegalState(pipelineStep.getId());
        }

        List<Table> inputSchemas = tablesOrNotFound.get();
        Map<Long, List<FieldView>> schemasIn = inputSchemas.stream()
                .collect(Collectors.toMap(Table::getId, this::convertToFieldViews));

        Table schemaOut = schemaOutResult.get();
        List<FieldView> schemaOutFields = convertToFieldViews(schemaOut);

        return Validation.valid(DrillBackStepDetails.builder()
                .schemasIn(schemasIn)
                .schemasOut(singletonMap(schemaOutId, schemaOutFields))
                .schemaToInputFieldToOutputFieldMapping(emptyMap())
                .build());
    }

    private IllegalStateException cannotFindSchemasForStepIllegalState(final Long stepId) {
        return new IllegalStateException("Cannot find schemas for pipeline step " + stepId);
    }

    @Transactional
    public Validation<CRUDFailure, DatasetRowDataView> findDrillBackOutputDatasetRows(
            final Long datasetId, final Pageable pageRequest, final FilterExpression filter) {

        Validation<CRUDFailure, Dataset> datasetValidation = datasetService.findWithValidation(datasetId);
        if (datasetValidation.isInvalid()) {
            return Validation.invalid(datasetValidation.getError());
        }

        Dataset dataset = datasetValidation.get();

        if (dataset.getPipelineInvocationId() == null) {
            return Validation.invalid(CRUDFailure.invalidParameters()
                    .paramError("dataset", "Dataset is not a pipeline dataset")
                    .asFailure());
        }

        Validation<CRUDFailure, PipelineStepInvocation> pipelineStepInvocationResult =
                pipelineInvocationService.findByInvocationIdAndStepInvocationId(
                        dataset.getPipelineInvocationId(), dataset.getPipelineStepInvocationId());

        if (pipelineStepInvocationResult.isInvalid()) {
            return Validation.invalid(pipelineStepInvocationResult.getError());
        }

        DatasetRowData datasetResults = datasetResultRepository.findDrillbackOutputDatasetRowData(dataset,
                pipelineStepInvocationResult.get().getPipelineStep(),
                pageRequest, filter);
        DatasetRowDataView datasetResultsView = toDatasetRowDataView(dataset, datasetResults);

        return Validation.valid(datasetResultsView);
    }

    @Transactional
    public Validation<CRUDFailure, DatasetRowDataView> findDrillBackInputDatasetRows(
            final Long datasetId, final Long pipelineId, final Long pipelineStepId, final Pageable pageRequest,
            final Boolean showOnlyDrillbackRows, final Long outputTableRowKey, final FilterExpression filter) {

        Validation<CRUDFailure, Dataset> datasetValidation = datasetService.findWithValidation(datasetId);
        if (datasetValidation.isInvalid()) {
            return Validation.invalid(datasetValidation.getError());
        }

        Dataset dataset = datasetValidation.get();

        Validation<CRUDFailure, PipelineStep> pipelineStep = pipelineService.findStepById(
                pipelineId, pipelineStepId);

        if (pipelineStep.isInvalid()) {
            return Validation.invalid(pipelineStep.getError());
        }

        DatasetRowData datasetResults = datasetResultRepository.findDrillbackInputDatasetRowData(
                dataset, pipelineStep.get(), pageRequest, showOnlyDrillbackRows, outputTableRowKey, filter);

        DatasetRowDataView datasetResultsView = toDatasetRowDataView(dataset, datasetResults);

        return Validation.valid(datasetResultsView);
    }

    private DatasetRowDataView toDatasetRowDataView(final Dataset dataset, final DatasetRowData datasetResults) {
        return DatasetRowDataView.builder()
                .datasetId(dataset.getId())
                .data(datasetResults.getResultData().getContent())
                .page(pageConverter.apply(datasetResults.getResultData()))
                .build();
    }

    private List<FieldView> convertToFieldViews(final Table table) {
        return table.getFields().stream()
                .map(fieldToFieldView)
                .collect(toList());
    }
}
