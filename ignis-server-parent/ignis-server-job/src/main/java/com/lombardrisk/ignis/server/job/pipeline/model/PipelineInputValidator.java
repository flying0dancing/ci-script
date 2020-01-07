package com.lombardrisk.ignis.server.job.pipeline.model;

import com.google.common.collect.ImmutableSet;
import com.lombardrisk.ignis.client.external.job.pipeline.PipelineRequest;
import com.lombardrisk.ignis.common.MapperUtils;
import com.lombardrisk.ignis.common.stream.CollectorUtils;
import com.lombardrisk.ignis.common.stream.SeqUtils;
import com.lombardrisk.ignis.data.common.error.ErrorResponse;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.feature.FeatureNotActiveException;
import com.lombardrisk.ignis.feature.IgnisFeature;
import com.lombardrisk.ignis.pipeline.PipelinePlan;
import com.lombardrisk.ignis.pipeline.PipelinePlanError;
import com.lombardrisk.ignis.pipeline.PipelinePlanGenerator;
import com.lombardrisk.ignis.pipeline.step.api.OrderSpec;
import com.lombardrisk.ignis.pipeline.step.api.SelectColumn;
import com.lombardrisk.ignis.pipeline.step.api.UnionSpec;
import com.lombardrisk.ignis.pipeline.step.api.WindowSpec;
import com.lombardrisk.ignis.server.dataset.DatasetService;
import com.lombardrisk.ignis.server.product.pipeline.PipelineService;
import com.lombardrisk.ignis.server.product.pipeline.PipelineStepService;
import com.lombardrisk.ignis.server.product.pipeline.details.SchemaDetails;
import com.lombardrisk.ignis.server.product.pipeline.model.Pipeline;
import com.lombardrisk.ignis.server.product.pipeline.model.PipelineAggregationStep;
import com.lombardrisk.ignis.server.product.pipeline.model.PipelineJoinStep;
import com.lombardrisk.ignis.server.product.pipeline.model.PipelineMapStep;
import com.lombardrisk.ignis.server.product.pipeline.model.PipelineScriptletStep;
import com.lombardrisk.ignis.server.product.pipeline.model.PipelineStep;
import com.lombardrisk.ignis.server.product.pipeline.model.PipelineUnionStep;
import com.lombardrisk.ignis.server.product.pipeline.model.PipelineWindowStep;
import com.lombardrisk.ignis.server.product.pipeline.model.TransformationType;
import com.lombardrisk.ignis.server.product.pipeline.select.Order;
import com.lombardrisk.ignis.server.product.pipeline.select.Select;
import com.lombardrisk.ignis.server.product.pipeline.select.Window;
import com.lombardrisk.ignis.server.product.table.TableService;
import com.lombardrisk.ignis.server.product.table.model.Field;
import com.lombardrisk.ignis.server.product.table.model.Table;
import io.vavr.control.Either;
import io.vavr.control.Option;
import io.vavr.control.Validation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.togglz.core.manager.FeatureManager;

import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.Collections.singletonList;

@AllArgsConstructor
@Slf4j
public class PipelineInputValidator {

    private final PipelineService pipelineService;
    private final PipelineStepService pipelineStepService;
    private final DatasetService datasetService;
    private final TableService tableService;
    private final PipelinePlanGenerator<SchemaDetails, PipelineStep> pipelinePlanGenerator =
            new PipelinePlanGenerator<>();
    private final FeatureManager featureManager;

    @Transactional
    public Validation<List<ErrorResponse>, PipelineInput> validatePipelineRequest(
            final PipelineRequest pipelineRequest) {

        Long pipelineId = pipelineRequest.getPipelineId();

        Validation<CRUDFailure, Pipeline> pipelineResult = pipelineService.findByIdWithFilters(pipelineId);
        if (pipelineResult.isInvalid()) {
            return Validation.invalid(singletonList(
                    pipelineResult.getError().toErrorResponse()));
        }
        Pipeline pipeline = pipelineResult.get();

        Long pipelineStepId = null;
        if (pipelineRequest.getStepId() != null) {
            if (!featureManager.isActive(IgnisFeature.RUN_PIPLINE_STEP)) {
                log.warn(
                        "Attempted to run  pipeline for a single step but {} feature not active",
                        IgnisFeature.RUN_PIPLINE_STEP.name());

                throw new FeatureNotActiveException(IgnisFeature.RUN_PIPLINE_STEP);
            }

            Validation<CRUDFailure, PipelineStep> pipelineStepResult = findPipelineStepById(
                    pipeline,
                    pipelineRequest.getStepId());

            if (pipelineStepResult.isInvalid()) {
                return Validation.invalid(singletonList(
                        pipelineStepResult.getError().toErrorResponse()));
            }
            pipelineStepId = pipelineRequest.getStepId();
        }

        String entityCode = pipelineRequest.getEntityCode();
        LocalDate referenceDate = pipelineRequest.getReferenceDate();

        return validatePipelineInput(pipelineRequest.getName(), pipeline, entityCode, referenceDate, pipelineStepId);
    }

    private Validation<CRUDFailure, PipelineStep> findPipelineStepById(
            final Pipeline pipeline,
            final Long pipelineStepId) {
        return Option.ofOptional(pipeline.getSteps().stream()
                .filter(s -> pipelineStepId.equals(s.getId()))
                .findFirst())
                .toValid(CRUDFailure.notFoundIds("Pipeline Step", pipelineStepId));
    }

    private Validation<List<ErrorResponse>, PipelineInput> validatePipelineInput(
            final String jobName,
            final Pipeline pipeline,
            final String entityCode,
            final LocalDate referenceDate,
            final Long pipelineStepId) {

        Validation<List<ErrorResponse>, PipelinePlan<SchemaDetails, PipelineStep>> pipelinePlan =
                pipelinePlanGenerator.generateExecutionPlan(pipeline)
                        .mapError(PipelinePlanError::getErrors);

        if (pipelinePlan.isInvalid()) {
            return Validation.invalid(pipelinePlan.getError());
        }

        return calculatePipelineStepExecutionOrder(pipelinePlan.get(), entityCode, referenceDate, pipelineStepId)
                .map(pipelineStepInputs -> PipelineInput.builder()
                        .jobName(jobName)
                        .pipeline(pipeline)
                        .entityCode(entityCode)
                        .referenceDate(referenceDate)
                        .pipelineSteps(pipelineStepInputs)
                        .build());
    }

    private Validation<List<ErrorResponse>, List<PipelineStepInput>> calculatePipelineStepExecutionOrder(
            final PipelinePlan<SchemaDetails, PipelineStep> plan,
            final String entityCode,
            final LocalDate referenceDate,
            final Long pipelineStepId) {

        return plan.getPipelineStepsInOrderOfExecution().stream()
                .map(step -> validatePipelineStepInput(step, plan, entityCode, referenceDate, pipelineStepId))
                .collect(CollectorUtils.groupCollectionValidations());
    }

    private Validation<List<ErrorResponse>, PipelineStepInput> validatePipelineStepInput(
            final PipelineStep step,
            final PipelinePlan<SchemaDetails, PipelineStep> plan,
            final String entityCode,
            final LocalDate referenceDate,
            final Long requestStepId) {

        Validation<List<ErrorResponse>, Set<PipelineStepDatasetInput>> datasetInputs =
                findDatasetInputsForPipelineStep(plan, step, entityCode, referenceDate, requestStepId);

        Validation<List<ErrorResponse>, Table> datasetOutput = validatePipelineStepDatasetOutput(step);

        Validation<List<ErrorResponse>, Set<SelectColumn>> stepSelectColumns =
                datasetOutput.flatMap(output -> validatePipelineStepSelects(step, output));

        boolean skippedStep = requestStepId != null && !step.getId().equals(requestStepId);

        return Validation.combine(datasetInputs, datasetOutput, stepSelectColumns)
                .ap((inputs, output, selects) -> convertToPipelineStepInput(step, inputs, output, selects, skippedStep))
                .mapError(SeqUtils::flatMapToList);
    }

    private Validation<List<ErrorResponse>, Set<PipelineStepDatasetInput>> findDatasetInputsForPipelineStep(
            final PipelinePlan<SchemaDetails, PipelineStep> pipelinePlan,
            final PipelineStep step,
            final String entityCode,
            final LocalDate referenceDate,
            final Long requestStepId) {

        return pipelinePlan.getStepDependencies(step).stream()
                .map(stepDependency -> validateStepDependency(
                        stepDependency, entityCode, referenceDate, step.getId().equals(requestStepId)))
                .collect(CollectorUtils.groupValidations())
                .map(ImmutableSet::copyOf);
    }

    private Validation<ErrorResponse, PipelineStepDatasetInput> validateStepDependency(
            final Either<SchemaDetails, PipelineStep> stepDependency,
            final String entityCode,
            final LocalDate referenceDate,
            final boolean forceSchemaDatasetRequest) {

        if (stepDependency.isRight()) {
            if (forceSchemaDatasetRequest) {
                return validateLatestDatasetForSchema(
                        stepDependency.get().getOutput().getId(),
                        entityCode,
                        referenceDate);
            }

            return Validation.valid(DependentPipelineStep.builder()
                    .dependentStep(stepDependency.get())
                    .build());
        }

        return validateLatestDatasetForSchema(stepDependency.getLeft().getId(), entityCode, referenceDate);
    }

    private Validation<ErrorResponse, PipelineStepDatasetInput> validateLatestDatasetForSchema(
            final Long schemaId, final String entityCode, final LocalDate referenceDate) {

        Validation<CRUDFailure, PipelineStepDatasetInput> stagedDatasetInputs =
                datasetService.findLatestDatasetForSchemaId(schemaId, entityCode, referenceDate)
                        .map(dataset -> StagedDatasetInput.builder().dataset(dataset).build());

        return stagedDatasetInputs.mapError(CRUDFailure::toErrorResponse);
    }

    private Validation<List<ErrorResponse>, Table> validatePipelineStepDatasetOutput(final PipelineStep step) {
        Validation<CRUDFailure, Table> stepOutputSchema =
                tableService.findWithValidation(schemaOutputIdForStep(step));

        return stepOutputSchema.mapError(CRUDFailure::toErrorResponse).mapError(Collections::singletonList);
    }

    private Validation<List<ErrorResponse>, Set<SelectColumn>> validatePipelineStepSelects(
            final PipelineStep step, final Table outputSchema) {

        return pipelineStepService.getSelects(step).stream()
                .sorted(Select.sortOrder())
                .map(select -> validatePipelineStepSelect(select, outputSchema))
                .collect(CollectorUtils.groupValidations())
                .map(LinkedHashSet::new);
    }

    private Validation<ErrorResponse, SelectColumn> validatePipelineStepSelect(
            final Select select, final Table outputSchema) {

        Optional<Field> optionalField = outputSchema.getFields().stream()
                .filter(field -> field.getId().equals(select.getOutputFieldId()))
                .findFirst();

        CRUDFailure fieldIdNotFoundError = CRUDFailure.cannotFind(Field.class.getSimpleName())
                .with("schema", outputSchema.getDisplayName())
                .with("fieldId", select.getOutputFieldId())
                .asFailure();

        return Option.ofOptional(optionalField)
                .map(field -> toSelectColumn(select, field))
                .toValid(fieldIdNotFoundError.toErrorResponse());
    }

    private SelectColumn toSelectColumn(final Select select, final Field field) {
        if (select.isWindow()) {
            return SelectColumn.builder()
                    .select(select.getSelect())
                    .as(field.getName())
                    .intermediateResult(select.isIntermediateResult())
                    .over(toWindowSpec(select.getWindow()))
                    .build();
        }

        if (select.isUnion()) {
            SchemaDetails unionSchema = select.getSelectUnion().getUnionSchema();

            return SelectColumn.builder()
                    .select(select.getSelect())
                    .as(field.getName())
                    .intermediateResult(select.isIntermediateResult())
                    .union(UnionSpec.builder()
                            .schemaInPhysicalName(unionSchema.getPhysicalTableName())
                            .schemaId(unionSchema.getId())
                            .build())
                    .build();
        }

        return SelectColumn.builder()
                .select(select.getSelect())
                .as(field.getName())
                .intermediateResult(select.isIntermediateResult())
                .build();
    }

    private WindowSpec toWindowSpec(final Window window) {
        return WindowSpec.builder()
                .partitionBy(window.getPartitions())
                .orderBy(MapperUtils.map(window.getOrders(), this::toOrderSpec))
                .build();
    }

    private OrderSpec toOrderSpec(final Order order) {
        return OrderSpec.column(order.getFieldName(), OrderSpec.Direction.valueOf(order.getDirection().toString()));
    }

    private PipelineStepInput convertToPipelineStepInput(
            final PipelineStep step,
            final Set<PipelineStepDatasetInput> datasetInputs,
            final Table schemaOut,
            final Set<SelectColumn> selectColumns,
            final boolean skipped) {

        switch (step.getType()) {
            case MAP:
                return PipelineMapInput.builder()
                        .pipelineStep((PipelineMapStep) step)
                        .datasetInput(datasetInputs.iterator().next())
                        .schemaOut(schemaOut)
                        .selectColumns(selectColumns)
                        .skipped(skipped)
                        .build();

            case AGGREGATION:
                return PipelineAggregationInput.builder()
                        .pipelineStep((PipelineAggregationStep) step)
                        .datasetInput(datasetInputs.iterator().next())
                        .schemaOut(schemaOut)
                        .selectColumns(selectColumns)
                        .skipped(skipped)
                        .build();

            case JOIN:
                return PipelineJoinInput.builder()
                        .pipelineStep((PipelineJoinStep) step)
                        .datasetInputs(datasetInputs)
                        .schemaOut(schemaOut)
                        .selectColumns(selectColumns)
                        .skipped(skipped)
                        .build();

            case WINDOW:
                return PipelineWindowInput.builder()
                        .pipelineStep((PipelineWindowStep) step)
                        .datasetInput(datasetInputs.iterator().next())
                        .schemaOut(schemaOut)
                        .selectColumns(selectColumns)
                        .skipped(skipped)
                        .build();

            case UNION:
                return PipelineUnionInput.builder()
                        .pipelineStep((PipelineUnionStep) step)
                        .datasetInputs(datasetInputs)
                        .schemaOut(schemaOut)
                        .selectColumns(selectColumns)
                        .skipped(skipped)
                        .build();

            case SCRIPTLET:
                return PipelineScriptletInput.builder()
                        .pipelineStep((PipelineScriptletStep) step)
                        .datasetInputs(datasetInputs)
                        .schemaOut(schemaOut)
                        .skipped(skipped)
                        .build();

            default:
                throw transformationTypeNotSupported(step.getType());
        }
    }

    private Long schemaOutputIdForStep(final PipelineStep step) {
        switch (step.getType()) {
            case MAP:
                PipelineMapStep mapStep = (PipelineMapStep) step;
                return mapStep.getSchemaOutId();

            case AGGREGATION:
                PipelineAggregationStep aggregationStep = (PipelineAggregationStep) step;
                return aggregationStep.getSchemaOutId();

            case JOIN:
                PipelineJoinStep joinStep = (PipelineJoinStep) step;
                return joinStep.getSchemaOutId();

            case WINDOW:
                PipelineWindowStep windowStep = (PipelineWindowStep) step;
                return windowStep.getSchemaOutId();

            case UNION:
                PipelineUnionStep unionStep = (PipelineUnionStep) step;
                return unionStep.getSchemaOutId();

            case SCRIPTLET:
                PipelineScriptletStep scriptletStep = (PipelineScriptletStep) step;
                return scriptletStep.getSchemaOutId();

            default:
                throw transformationTypeNotSupported(step.getType());
        }
    }

    private static RuntimeException transformationTypeNotSupported(final TransformationType transformationType) {
        return new IllegalArgumentException(transformationType + " not supported");
    }
}
