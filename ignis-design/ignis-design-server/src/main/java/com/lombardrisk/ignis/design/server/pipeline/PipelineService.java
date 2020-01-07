package com.lombardrisk.ignis.design.server.pipeline;

import com.lombardrisk.ignis.client.design.pipeline.CreatePipelineRequest;
import com.lombardrisk.ignis.client.design.pipeline.PipelineConnectedGraphs;
import com.lombardrisk.ignis.client.design.pipeline.PipelineDisplayErrorView;
import com.lombardrisk.ignis.client.design.pipeline.PipelineEdgeView;
import com.lombardrisk.ignis.client.design.pipeline.PipelineView;
import com.lombardrisk.ignis.client.design.pipeline.UpdatePipelineRequest;
import com.lombardrisk.ignis.common.MapperUtils;
import com.lombardrisk.ignis.data.common.Identifiable;
import com.lombardrisk.ignis.data.common.error.ErrorResponse;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.data.common.failure.NotFoundFailure;
import com.lombardrisk.ignis.data.common.service.CRUDService;
import com.lombardrisk.ignis.design.server.pipeline.api.PipelineRepository;
import com.lombardrisk.ignis.design.server.pipeline.converter.PipelineConverter;
import com.lombardrisk.ignis.design.server.pipeline.converter.PipelineStepConverter;
import com.lombardrisk.ignis.design.server.pipeline.model.Pipeline;
import com.lombardrisk.ignis.design.server.pipeline.model.PipelineStep;
import com.lombardrisk.ignis.pipeline.display.PipelineCycleError;
import com.lombardrisk.ignis.pipeline.display.PipelineDisplayGraph;
import com.lombardrisk.ignis.pipeline.display.PipelineDisplayGraphGenerator;
import com.lombardrisk.ignis.pipeline.display.PipelineEdge;
import io.vavr.control.Option;
import io.vavr.control.Validation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toList;

@Service
@Transactional
@Slf4j
@AllArgsConstructor
public class PipelineService implements CRUDService<Pipeline> {

    private final PipelineRepository pipelineRepository;
    private final PipelineConverter pipelineConverter;
    private final PipelineStepConverter pipelineStepConverter = new PipelineStepConverter();
    private final PipelineDisplayGraphGenerator<Long, PipelineStep> pipelineDisplayGraphGenerator =
            new PipelineDisplayGraphGenerator<>();

    @Override
    public String entityName() {
        return Pipeline.class.getSimpleName();
    }

    @Override
    public Option<Pipeline> findById(final long id) {
        return Option.ofOptional(pipelineRepository.findById(id));
    }

    @Override
    public List<Pipeline> findAllByIds(final Iterable<Long> ids) {
        return pipelineRepository.findAllById(ids);
    }

    @Override
    public List<Pipeline> findAll() {
        return pipelineRepository.findAll();
    }

    @Override
    public Pipeline delete(final Pipeline pipeline) {
        pipelineRepository.delete(pipeline);
        return pipeline;
    }

    public Validation<CRUDFailure, Identifiable> deleteById(final long id) {
        return findWithValidation(id)
                .map(this::delete)
                .map(Identifiable::toIdentifiable);
    }

    public List<PipelineView> findAllPipelines() {
        return findAll().stream()
                .map(pipelineConverter)
                .collect(toList());
    }

    public List<Pipeline> findByProductId(final Long productId) {
        return pipelineRepository.findAllByProductId(productId);
    }

    public Validation<CRUDFailure, PipelineView> findOne(final long id) {
        return findWithValidation(id)
                .map(pipelineConverter);
    }

    @Transactional
    public Validation<ErrorResponse, PipelineView> saveNewPipeline(final CreatePipelineRequest createPipelineRequest) {

        Optional<Pipeline> existingPipeline = pipelineRepository.findByName(createPipelineRequest.getName());
        if (existingPipeline.isPresent()) {
            return Validation.invalid(ErrorResponse.valueOf(
                    "Pipeline '" + createPipelineRequest.getName() + "' already exists", "name"));
        }

        Pipeline savedPipeline = pipelineRepository.save(
                Pipeline.builder()
                        .name(createPipelineRequest.getName())
                        .productId(createPipelineRequest.getProductId())
                        .build());

        return Validation.valid(pipelineConverter.apply(savedPipeline));
    }

    public Validation<CRUDFailure, PipelineView> updatePipeline(
            final Long pipelineId,
            final UpdatePipelineRequest updatePipelineRequest) {

        Optional<Pipeline> existingPipelineOptional = pipelineRepository.findById(pipelineId);
        if (!existingPipelineOptional.isPresent()) {
            return Validation.invalid(CRUDFailure.notFoundIds(Pipeline.class.getSimpleName(), pipelineId));
        }

        Pipeline existingPipeline = existingPipelineOptional.get();

        existingPipeline.setName(updatePipelineRequest.getName());

        Pipeline updatedPipeline = pipelineRepository.save(existingPipeline);

        return Validation.valid(pipelineConverter.apply(updatedPipeline));
    }

    public void deleteAllByProductId(final Long productId) {
        pipelineRepository.deleteAllByProductId(productId);
    }

    public Validation<PipelineDisplayErrorView, PipelineConnectedGraphs> calculatePipelineEdgeData(final Long pipelineId) {
        Optional<Pipeline> existingPipelineOptional = pipelineRepository.findById(pipelineId);
        if (!existingPipelineOptional.isPresent()) {

            NotFoundFailure notFoundFailure = notFound(pipelineId);
            return Validation.invalid(PipelineDisplayErrorView.notFound(
                    notFoundFailure.getErrorCode(), notFoundFailure.getErrorMessage()));
        }

        Pipeline pipeline = existingPipelineOptional.get();

        Validation<PipelineCycleError<Long, PipelineStep>, PipelineDisplayGraph<Long, PipelineStep>>
                pipelineDisplayGraph = pipelineDisplayGraphGenerator.generateDisplayGraph(pipeline.getSteps());

        if (pipelineDisplayGraph.isInvalid()) {
            PipelineCycleError<Long, PipelineStep> error = pipelineDisplayGraph.getError();

            PipelineDisplayErrorView hasCyclesError = PipelineDisplayErrorView.hasCyclesError(
                    error.getParentChain(), error.getCyclicalStep().getId());

            return Validation.invalid(hasCyclesError);
        }

        List<Set<PipelineEdgeView>> connectedSets = MapperUtils.mapCollectionOrEmpty(
                pipelineDisplayGraph.get().connectedSets(),
                this::extractEdges,
                ArrayList::new);

        return Validation.valid(new PipelineConnectedGraphs(connectedSets));
    }

    private Set<PipelineEdgeView> extractEdges(final Set<PipelineEdge<Long, PipelineStep>> edges) {
        return MapperUtils.mapSet(
                edges,
                edge -> new PipelineEdgeView(
                        edge.getSource(),
                        edge.getTarget(),
                        pipelineStepConverter.apply(edge.getPipelineStep())));
    }
}
