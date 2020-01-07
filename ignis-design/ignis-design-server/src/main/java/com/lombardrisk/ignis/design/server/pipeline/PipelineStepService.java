package com.lombardrisk.ignis.design.server.pipeline;

import com.lombardrisk.ignis.client.design.pipeline.PipelineStepRequest;
import com.lombardrisk.ignis.client.design.pipeline.PipelineStepView;
import com.lombardrisk.ignis.client.design.pipeline.SyntaxCheckRequest;
import com.lombardrisk.ignis.client.design.pipeline.aggregation.PipelineAggregationStepRequest;
import com.lombardrisk.ignis.client.design.pipeline.error.SelectResult;
import com.lombardrisk.ignis.client.design.pipeline.error.StepExecutionResult;
import com.lombardrisk.ignis.client.design.pipeline.error.UpdatePipelineError;
import com.lombardrisk.ignis.client.design.pipeline.join.PipelineJoinStepRequest;
import com.lombardrisk.ignis.client.design.pipeline.map.PipelineMapStepRequest;
import com.lombardrisk.ignis.client.design.pipeline.scriptlet.PipelineScriptletStepRequest;
import com.lombardrisk.ignis.client.design.pipeline.select.OrderRequest;
import com.lombardrisk.ignis.client.design.pipeline.select.SelectRequest;
import com.lombardrisk.ignis.client.design.pipeline.select.UnionRequest;
import com.lombardrisk.ignis.client.design.pipeline.union.PipelineUnionStepRequest;
import com.lombardrisk.ignis.client.design.pipeline.window.PipelineWindowStepRequest;
import com.lombardrisk.ignis.client.external.pipeline.export.TransformationType;
import com.lombardrisk.ignis.common.MapperUtils;
import com.lombardrisk.ignis.data.common.Identifiable;
import com.lombardrisk.ignis.data.common.error.ErrorResponse;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.data.common.service.RetrieveService;
import com.lombardrisk.ignis.design.server.pipeline.api.PipelineRepository;
import com.lombardrisk.ignis.design.server.pipeline.api.PipelineStepRepository;
import com.lombardrisk.ignis.design.server.pipeline.api.PipelineStepTestRepository;
import com.lombardrisk.ignis.design.server.pipeline.converter.PipelineStepConverter;
import com.lombardrisk.ignis.design.server.pipeline.model.Pipeline;
import com.lombardrisk.ignis.design.server.pipeline.model.PipelineAggregationStep;
import com.lombardrisk.ignis.design.server.pipeline.model.PipelineJoinStep;
import com.lombardrisk.ignis.design.server.pipeline.model.PipelineMapStep;
import com.lombardrisk.ignis.design.server.pipeline.model.PipelineScriptletStep;
import com.lombardrisk.ignis.design.server.pipeline.model.PipelineStep;
import com.lombardrisk.ignis.design.server.pipeline.model.PipelineUnionStep;
import com.lombardrisk.ignis.design.server.pipeline.model.PipelineWindowStep;
import com.lombardrisk.ignis.design.server.pipeline.model.ScriptletInput;
import com.lombardrisk.ignis.design.server.pipeline.model.join.Join;
import com.lombardrisk.ignis.design.server.pipeline.model.join.JoinField;
import com.lombardrisk.ignis.design.server.pipeline.model.select.Order;
import com.lombardrisk.ignis.design.server.pipeline.model.select.PipelineFilter;
import com.lombardrisk.ignis.design.server.pipeline.model.select.Select;
import com.lombardrisk.ignis.design.server.pipeline.model.select.Union;
import com.lombardrisk.ignis.design.server.pipeline.model.select.Window;
import com.lombardrisk.ignis.design.server.pipeline.test.model.PipelineStepTest;
import io.vavr.control.Option;
import io.vavr.control.Validation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static com.google.common.collect.Sets.newLinkedHashSet;
import static java.util.stream.Collectors.toSet;

@Service
@Transactional
@Slf4j
@AllArgsConstructor
public class PipelineStepService implements RetrieveService<PipelineStep> {

    private final PipelineStepRepository stepRepository;
    private final PipelineStepConverter stepConverter;
    private final PipelineRepository pipelineRepository;
    private final PipelineStepTestRepository pipelineStepTestRepository;
    private final PipelineStepSelectsValidator stepSelectsValidator;

    @Override
    public String entityName() {
        return PipelineStep.class.getSimpleName();
    }

    @Override
    public Option<PipelineStep> findById(final long id) {
        return stepRepository.findById(id);
    }

    @Override
    public List<PipelineStep> findAllByIds(final Iterable<Long> ids) {
        return stepRepository.findAllById(ids);
    }

    @Override
    public List<PipelineStep> findAll() {
        throw new UnsupportedOperationException();
    }

    public PipelineStep delete(final PipelineStep step) {
        stepRepository.delete(step);
        return step;
    }

    public Validation<CRUDFailure, Identifiable> deletePipelineStep(final long id) {
        return findWithValidation(id)
                .map(this::delete)
                .map(Identifiable::toIdentifiable);
    }

    @Transactional
    public Validation<UpdatePipelineError, PipelineStepView> savePipelineStep(
            final Long pipelineId,
            final PipelineStepRequest stepRequest) {

        Optional<Pipeline> optionalExistingPipeline = pipelineRepository.findById(pipelineId);
        if (!optionalExistingPipeline.isPresent()) {
            return Validation.invalid(
                    UpdatePipelineError.builder()
                            .pipelineNotFoundError(CRUDFailure.notFoundIds(Pipeline.class.getSimpleName()))
                            .build());
        }

        PipelineStep newPipelineStep = buildPipelineStepFromRequest(stepRequest);
        newPipelineStep.setPipelineId(pipelineId);

        if (!newPipelineStep.getType().equals(TransformationType.SCRIPTLET)) {
            StepExecutionResult selectsValidationFailure =
                    stepSelectsValidator.validateWithIndividualErrors(newPipelineStep);

            if (!selectsValidationFailure.isSuccessful()) {
                return Validation.invalid(UpdatePipelineError.builder()
                        .stepExecutionResult(selectsValidationFailure)
                        .build());
            }
        }

        PipelineStep savedPipelineStep = stepRepository.savePipelineStep(newPipelineStep);
        return Validation.valid(stepConverter.apply(savedPipelineStep));
    }

    @Transactional
    public Validation<UpdatePipelineError, PipelineStepView> updatePipelineStep(
            final Long pipelineId,
            final Long pipelineStepId,
            final PipelineStepRequest stepRequest) {

        Optional<Pipeline> optionalExistingPipeline = pipelineRepository.findById(pipelineId);
        if (!optionalExistingPipeline.isPresent()) {
            return Validation.invalid(
                    UpdatePipelineError.builder()
                            .pipelineNotFoundError(CRUDFailure.notFoundIds(Pipeline.class.getSimpleName(), pipelineId))
                            .build());
        }

        Validation<CRUDFailure, PipelineStep> findStep = findWithValidation(pipelineStepId);
        if (findStep.isInvalid()) {
            return Validation.invalid(
                    UpdatePipelineError.builder()
                            .pipelineNotFoundError(findStep.getError())
                            .build());
        }

        PipelineStep existingStep = findStep.get();
        PipelineStep newPipelineStep = buildPipelineStepFromRequest(stepRequest);

        if (!newPipelineStep.getType().equals(TransformationType.SCRIPTLET)) {
            StepExecutionResult selectsValidationFailure =
                    stepSelectsValidator.validateWithIndividualErrors(newPipelineStep);

            if (!selectsValidationFailure.isSuccessful()) {
                return Validation.invalid(UpdatePipelineError.builder().stepExecutionResult(selectsValidationFailure)
                        .build());
            }
        }

        PipelineStepView updatedPipelineStep = saveUpdatedStep(existingStep, newPipelineStep, pipelineId);
        return Validation.valid(updatedPipelineStep);
    }

    @Transactional
    public void importPipelineSteps(final Long pipelineId, final Set<PipelineStepRequest> pipelineStepRequests) {
        for (PipelineStepRequest pipelineStepRequest : pipelineStepRequests) {
            PipelineStep pipelineStep = buildPipelineStepFromRequest(pipelineStepRequest);
            pipelineStep.setPipelineId(pipelineId);

            log.debug("Importing pipeline step [{}] for pipeline [{}]", pipelineStep.getName(), pipelineId);
            stepRepository.savePipelineStep(pipelineStep);
        }
    }

    public Validation<List<ErrorResponse>, SelectResult> checkSyntax(final SyntaxCheckRequest syntaxCheckRequest) {
        PipelineStep pipelineStep = buildPipelineStepFromRequest(syntaxCheckRequest.getPipelineStep());
        return stepSelectsValidator.checkSyntax(pipelineStep, syntaxCheckRequest);
    }

    private PipelineStep buildPipelineStepFromRequest(final PipelineStepRequest stepRequest) {
        switch (stepRequest.getType()) {
            case MAP:
                return buildMapStep((PipelineMapStepRequest) stepRequest);
            case AGGREGATION:
                return buildAggregationStep((PipelineAggregationStepRequest) stepRequest);
            case JOIN:
                return buildJoinStep((PipelineJoinStepRequest) stepRequest);
            case WINDOW:
                return buildWindowStep((PipelineWindowStepRequest) stepRequest);
            case UNION:
                return buildUnionStep((PipelineUnionStepRequest) stepRequest);
            case SCRIPTLET:
                return buildScriptletStep((PipelineScriptletStepRequest) stepRequest);
            default:
                throw new NotImplementedException(stepRequest.getType() + " not supported yet");
        }
    }

    private PipelineStep buildScriptletStep(
            final PipelineScriptletStepRequest stepRequest) {
        Set<ScriptletInput> scriptletInput = stepRequest.getScriptletInputs().stream()
                .map(input -> ScriptletInput.builder()
                        .inputName(input.getMetadataInput())
                        .schemaInId(input.getSchemaInId())
                        .build())
                .collect(toSet());

        return PipelineScriptletStep.builder()
                .name(stepRequest.getName())
                .description(stepRequest.getDescription())
                .jarFile(stepRequest.getJarFile())
                .className(stepRequest.getJavaClass())
                .schemaIns(scriptletInput)
                .schemaOutId(stepRequest.getSchemaOutId())
                .build();
    }

    private PipelineStep buildMapStep(final PipelineMapStepRequest stepRequest) {
        return PipelineMapStep.builder()
                .name(stepRequest.getName())
                .description(stepRequest.getDescription())
                .schemaInId(stepRequest.getSchemaInId())
                .schemaOutId(stepRequest.getSchemaOutId())
                .selects(toSelects(stepRequest.getSelects()))
                .filters(newLinkedHashSet(stepRequest.getFilters()))
                .build();
    }

    private PipelineStep buildUnionStep(final PipelineUnionStepRequest stepRequest) {
        return PipelineUnionStep.builder()
                .name(stepRequest.getName())
                .description(stepRequest.getDescription())
                .schemaInIds(stepRequest.getUnionSchemas().keySet())
                .selects(
                        stepRequest.getUnionSchemas().entrySet()
                                .stream()
                                .flatMap(this::unionToSelect)
                                .collect(toSet()))
                .filters(
                        stepRequest.getUnionSchemas().entrySet()
                                .stream()
                                .flatMap(this::unionToFilter)
                                .collect(toSet()))
                .schemaOutId(stepRequest.getSchemaOutId())
                .build();
    }

    private Stream<Select> unionToSelect(final Map.Entry<Long, UnionRequest> schemaIdToUnion) {
        return schemaIdToUnion.getValue().getSelects().stream()
                .map(selectRequest -> Select.builder()
                        .select(selectRequest.getSelect())
                        .outputFieldId(selectRequest.getOutputFieldId())
                        .isUnion(true)
                        .isIntermediate(selectRequest.isIntermediateResult())
                        .order(selectRequest.getOrder())
                        .union(Union.forSchema(schemaIdToUnion.getKey()))
                        .build());
    }

    private Stream<PipelineFilter> unionToFilter(final Map.Entry<Long, UnionRequest> schemaIdToUnion) {
        List<String> filters = schemaIdToUnion.getValue().getFilters();
        if (filters == null) {
            return Stream.empty();
        }

        return filters.stream()
                .map(filter -> PipelineFilter.builder()
                        .unionSchemaId(schemaIdToUnion.getKey())
                        .filter(filter)
                        .build());
    }

    private PipelineStep buildAggregationStep(final PipelineAggregationStepRequest stepRequest) {
        return PipelineAggregationStep.builder()
                .name(stepRequest.getName())
                .description(stepRequest.getDescription())
                .schemaInId(stepRequest.getSchemaInId())
                .schemaOutId(stepRequest.getSchemaOutId())
                .selects(toSelects(stepRequest.getSelects()))
                .filters(newLinkedHashSet(stepRequest.getFilters()))
                .groupings(newLinkedHashSet(stepRequest.getGroupings()))
                .build();
    }

    private PipelineStep buildJoinStep(final PipelineJoinStepRequest stepRequest) {
        return PipelineJoinStep.builder()
                .name(stepRequest.getName())
                .description(stepRequest.getDescription())
                .schemaOutId(stepRequest.getSchemaOutId())
                .selects(toSelects(stepRequest.getSelects()))
                .joins(MapperUtils.mapCollection(
                        stepRequest.getJoins(),
                        joinRequest -> Join.builder()
                                .joinType(joinRequest.getJoinType())
                                .leftSchemaId(joinRequest.getLeftSchemaId())
                                .rightSchemaId(joinRequest.getRightSchemaId())
                                .joinFields(joinRequest.getJoinFields().stream()
                                        .map(joinFieldRequest -> JoinField.builder()
                                                .leftJoinFieldId(joinFieldRequest.getLeftFieldId())
                                                .rightJoinFieldId(joinFieldRequest.getRightFieldId())
                                                .build())
                                        .collect(toSet()))
                                .build(),
                        LinkedHashSet::new))
                .build();
    }

    private PipelineStep buildWindowStep(final PipelineWindowStepRequest stepRequest) {
        return PipelineWindowStep.builder()
                .name(stepRequest.getName())
                .description(stepRequest.getDescription())
                .schemaInId(stepRequest.getSchemaInId())
                .schemaOutId(stepRequest.getSchemaOutId())
                .selects(toSelects(stepRequest.getSelects()))
                .filters(stepRequest.getFilters())
                .build();
    }

    private Set<Select> toSelects(final Set<SelectRequest> requests) {
        return MapperUtils.mapCollection(requests, this::toSelect, LinkedHashSet::new);
    }

    private Select toSelect(final SelectRequest request) {
        return Select.builder()
                .outputFieldId(request.getOutputFieldId())
                .select(request.getSelect())
                .isIntermediate(request.isIntermediateResult())
                .isWindow(request.isHasWindow())
                .window(toWindow(request))
                .order(request.isIntermediateResult() ? request.getOrder() : null)
                .build();
    }

    private Window toWindow(final SelectRequest request) {
        return Option.of(request.getWindow())
                .map(window -> Window.builder()
                        .partitions(window.getPartitionBy())
                        .orders(MapperUtils.mapSet(window.getOrderBy(), this::toOrder))
                        .build())
                .getOrElse(Window.none());
    }

    private Order toOrder(final OrderRequest request) {
        return Order.builder()
                .fieldName(request.getFieldName())
                .direction(Order.Direction.valueOf(request.getDirection().toString()))
                .priority(request.getPriority())
                .build();
    }

    private PipelineStepView saveUpdatedStep(
            final PipelineStep existingPipelineStep,
            final PipelineStep newPipelineStep,
            final Long pipelineId) {

        newPipelineStep.setId(null);
        newPipelineStep.setPipelineId(pipelineId);
        PipelineStep updatedStep = stepRepository.savePipelineStep(newPipelineStep);

        List<PipelineStepTest> stepTests =
                pipelineStepTestRepository.findAllByPipelineStepId(existingPipelineStep.getId());
        stepTests.forEach(test -> test.setPipelineStepId(updatedStep.getId()));
        pipelineStepTestRepository.saveAll(stepTests);

        stepRepository.delete(existingPipelineStep);

        log.debug("Edited pipeline step with id [{}] for pipeline with id [{}]", updatedStep.getId(), pipelineId);

        return stepConverter.apply(updatedStep);
    }
}
