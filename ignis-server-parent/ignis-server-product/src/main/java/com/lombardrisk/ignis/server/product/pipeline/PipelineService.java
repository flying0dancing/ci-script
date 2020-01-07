
package com.lombardrisk.ignis.server.product.pipeline;

import com.lombardrisk.ignis.client.external.pipeline.view.DownstreamPipelineView;
import com.lombardrisk.ignis.client.external.pipeline.view.PipelineEdgeView;
import com.lombardrisk.ignis.client.external.pipeline.view.PipelineView;
import com.lombardrisk.ignis.client.external.pipeline.view.SchemaDetailsView;
import com.lombardrisk.ignis.common.MapperUtils;
import com.lombardrisk.ignis.data.common.error.ErrorResponse;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.pipeline.PipelinePlan;
import com.lombardrisk.ignis.pipeline.PipelinePlanError;
import com.lombardrisk.ignis.pipeline.PipelinePlanGenerator;
import com.lombardrisk.ignis.pipeline.display.PipelineDisplayGraph;
import com.lombardrisk.ignis.pipeline.display.PipelineDisplayGraphGenerator;
import com.lombardrisk.ignis.server.product.pipeline.details.SchemaDetails;
import com.lombardrisk.ignis.server.product.pipeline.model.Pipeline;
import com.lombardrisk.ignis.server.product.pipeline.model.PipelineStep;
import com.lombardrisk.ignis.server.product.pipeline.select.PipelineFilter;
import com.lombardrisk.ignis.server.product.pipeline.select.Select;
import com.lombardrisk.ignis.server.product.pipeline.view.PipelineStepToPipelineStepView;
import com.lombardrisk.ignis.server.product.pipeline.view.PipelineToPipelineView;
import com.lombardrisk.ignis.server.product.pipeline.view.SchemaDetailsToSchemaDetailsView;
import io.vavr.control.Option;
import io.vavr.control.Validation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.lombardrisk.ignis.common.stream.CollectionUtils.orEmpty;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toSet;

@Slf4j
@RequiredArgsConstructor
public class PipelineService {

    private final PipelineJpaRepository pipelineRepository;

    private final PipelineToPipelineView pipelineDetailsToPipelineView =
            new PipelineToPipelineView();
    private final SchemaDetailsToSchemaDetailsView toSchemaView =
            new SchemaDetailsToSchemaDetailsView();
    private final PipelinePlanGenerator<SchemaDetails, PipelineStep> pipelinePlanGenerator =
            new PipelinePlanGenerator<>();
    private final PipelineDisplayGraphGenerator<SchemaDetails, PipelineStep> pipelineDisplayGraphGenerator =
            new PipelineDisplayGraphGenerator<>();

    @Transactional
    public List<PipelineView> findAll() {
        return MapperUtils.map(pipelineRepository.findAll(), pipelineDetailsToPipelineView::apply);
    }

    public Validation<CRUDFailure, Pipeline> findById(final long pipelineId) {
        return Option.ofOptional(pipelineRepository.findById(pipelineId))
                .toValid(CRUDFailure.notFoundIds("Pipeline", pipelineId));
    }

    public Validation<CRUDFailure, Pipeline> findByIdWithFilters(final long pipelineId) {
        return Option.ofOptional(pipelineRepository.findByIdFetchingFilters(pipelineId))
                .toValid(CRUDFailure.notFoundIds("Pipeline", pipelineId));
    }

    public Validation<CRUDFailure, Pipeline> findByName(final String pipelineName) {
        return Option.ofOptional(pipelineRepository.findByName(pipelineName))
                .toValid(CRUDFailure.cannotFind("Pipeline").with("name", pipelineName).asFailure());
    }

    boolean existsByName(final String pipelineName) {
        return pipelineRepository.existsByName(pipelineName);
    }

    public Validation<CRUDFailure, PipelineStep> findStepById(final Long pipelineId, final Long pipelineStepId) {
        Validation<CRUDFailure, Pipeline> pipelineResult = findById(pipelineId);
        if (pipelineResult.isInvalid()) {
            return Validation.invalid(pipelineResult.getError());
        }

        Optional<PipelineStep> pipelineStepOptional = pipelineResult.get().getSteps().stream()
                .filter(step -> step.getId().equals(pipelineStepId))
                .findFirst();

        return Option.ofOptional(pipelineStepOptional)
                .toValid(CRUDFailure.notFoundIds("PipelineStep", pipelineStepId));
    }

    public List<Pipeline> findByProductId(final long productId) {
        return pipelineRepository.findByProductId(productId);
    }

    @Transactional
    public List<Pipeline> savePipelines(final List<Pipeline> pipelines) {
        List<Pipeline> savedPipelines = new ArrayList<>();
        for (Pipeline pipeline : pipelines) {
            Pipeline savedPipeline = savePipeline(pipeline);
            savedPipelines.add(savedPipeline);
        }
        return savedPipelines;
    }

    @Transactional
    public Pipeline savePipeline(final Pipeline pipeline) {
        for (PipelineStep step : pipeline.getSteps()) {
            step.setPipeline(pipeline);
            for (Select select : orEmpty(step.getSelects())) {
                select.setPipelineStep(step);
            }
            for (PipelineFilter filter : orEmpty(step.getPipelineFilters())) {
                filter.setPipelineStep(step);
            }
        }
        return pipelineRepository.save(pipeline);
    }

    @Transactional
    public void deletePipelinesForProducts(final List<Long> productIds) {
        pipelineRepository.deleteAllByProductIdIn(productIds);
    }

    @Transactional
    public Validation<List<ErrorResponse>, Set<SchemaDetailsView>> getRequiredSchemas(final Long pipelineId) {
        Validation<List<ErrorResponse>, PipelinePlan<SchemaDetails, PipelineStep>> pipelinePlan = findById(pipelineId)
                .mapError(crudFailure -> singletonList(crudFailure.toErrorResponse()))
                .flatMap((Pipeline pipeline) -> pipelinePlanGenerator.generateExecutionPlan(pipeline)
                        .mapError(PipelinePlanError::getErrors));

        if (pipelinePlan.isValid()) {
            return Validation.valid(pipelinePlan.get().getRequiredPipelineInputs().stream()
                    .map(toSchemaView)
                    .collect(toSet()));
        }

        log.error("Could not find required schemas for pipeline {}", pipelinePlan.getError());
        return Validation.invalid(pipelinePlan.getError());
    }

    @Transactional
    public Validation<CRUDFailure, List<PipelineEdgeView>> findPipelineEdgeData(final Long pipelineId) {
        Optional<Pipeline> existingPipelineOptional = pipelineRepository.findById(pipelineId);
        if (!existingPipelineOptional.isPresent()) {
            return Validation.invalid(CRUDFailure.notFoundIds(Pipeline.class.getSimpleName(), pipelineId));
        }

        Pipeline pipeline = existingPipelineOptional.get();

        PipelineDisplayGraph<SchemaDetails, PipelineStep> pipelineDisplayGraph =
                pipelineDisplayGraphGenerator.generateDisplayGraph(pipeline.getSteps())
                        .get();

        List<PipelineEdgeView> edges = MapperUtils.mapCollectionOrEmpty(
                pipelineDisplayGraph.edges(),
                edge -> new PipelineEdgeView(
                        edge.getSource().getId(),
                        edge.getTarget().getId(),
                        new PipelineStepToPipelineStepView().apply(edge.getPipelineStep())),
                ArrayList::new);

        return Validation.valid(edges);
    }

    @Transactional
    public Validation<CRUDFailure, Set<DownstreamPipelineView>> findDownstreamPipelines() {

        Set<DownstreamPipelineView> downstreamPipelineViews = new HashSet<>();
        List<Pipeline> pipelines = pipelineRepository.findAll();

        for (Pipeline pipeline : pipelines) {
            Validation<PipelinePlanError<SchemaDetails, PipelineStep>, PipelinePlan<SchemaDetails, PipelineStep>>
                    pipelinePlan = pipelinePlanGenerator.generateExecutionPlan(pipeline);

            if (pipelinePlan.isInvalid()) {
                log.warn("Skipping invalid pipeline [{}]", pipeline.getId());
            } else {
                Set<SchemaDetailsView> requiredSchemas = pipelinePlan.get().getRequiredPipelineInputs().stream()
                        .map(toSchemaView)
                        .collect(toSet());

                downstreamPipelineViews.add(DownstreamPipelineView.builder()
                        .pipelineId(pipeline.getId())
                        .pipelineName(pipeline.getName())
                        .requiredSchemas(requiredSchemas)
                        .build());
            }
        }

        return Validation.valid(downstreamPipelineViews);
    }
}
