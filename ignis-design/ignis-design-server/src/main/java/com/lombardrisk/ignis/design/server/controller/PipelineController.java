package com.lombardrisk.ignis.design.server.controller;

import com.lombardrisk.ignis.client.design.path.design;
import com.lombardrisk.ignis.client.design.pipeline.CreatePipelineRequest;
import com.lombardrisk.ignis.client.design.pipeline.PipelineConnectedGraphs;
import com.lombardrisk.ignis.client.design.pipeline.PipelineDisplayErrorView;
import com.lombardrisk.ignis.client.design.pipeline.PipelineView;
import com.lombardrisk.ignis.client.design.pipeline.UpdatePipelineRequest;
import com.lombardrisk.ignis.data.common.Identifiable;
import com.lombardrisk.ignis.design.server.pipeline.PipelineService;
import com.lombardrisk.ignis.web.common.response.EitherResponse;
import com.lombardrisk.ignis.web.common.response.FcrResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@AllArgsConstructor
@Slf4j
@RestController
public class PipelineController {

    private final PipelineService pipelineService;

    @GetMapping(path = design.api.v1.Pipelines)
    public List<PipelineView> getPipelines() {
        return pipelineService.findAllPipelines();
    }

    @GetMapping(path = design.api.v1.pipelines.ById)
    public FcrResponse<PipelineView> getPipeline(@PathVariable(design.api.Params.PIPELINE_ID) final Long pipelineId) {
        return pipelineService.findOne(pipelineId)
                .fold(FcrResponse::crudFailure, FcrResponse::okResponse);
    }

    @PostMapping(path = design.api.v1.Pipelines)
    public FcrResponse<PipelineView> savePipeline(@Valid @RequestBody final CreatePipelineRequest createPipelineRequest) {
        log.debug("Save pipeline [{}]", createPipelineRequest.getName());

        return pipelineService.saveNewPipeline(createPipelineRequest)
                .fold(FcrResponse::badRequest, FcrResponse::okResponse);
    }

    @PatchMapping(path = design.api.v1.pipelines.ById)
    public FcrResponse<PipelineView> updatePipeline(
            @PathVariable(design.api.Params.PIPELINE_ID) final Long pipelineId,
            @Valid @RequestBody final UpdatePipelineRequest updatePipelineRequest) {

        log.debug("Save pipeline [{}]", updatePipelineRequest.getName());

        return pipelineService.updatePipeline(pipelineId, updatePipelineRequest)
                .fold(FcrResponse::crudFailure, FcrResponse::okResponse);
    }

    @GetMapping(path = design.api.v1.pipelines.pipelineId.Edges)
    public EitherResponse<PipelineDisplayErrorView, PipelineConnectedGraphs> getPipelineEdgeData(
            @PathVariable(design.api.Params.PIPELINE_ID) final Long pipelineId) {

        log.debug("Get pipeline edge data [{}]", pipelineId);

        return pipelineService.calculatePipelineEdgeData(pipelineId)
                .mapError(this::logEdgesError)
                .fold(EitherResponse::badRequest, EitherResponse::okResponse);
    }

    private PipelineDisplayErrorView logEdgesError(final PipelineDisplayErrorView error) {
        log.error("Error retrieving pipeline edges {}", error);
        return error;
    }

    @DeleteMapping(path = design.api.v1.pipelines.ById)
    public FcrResponse<Identifiable> deletePipeline(@PathVariable(design.api.Params.PIPELINE_ID) final Long id) {
        log.debug("Delete pipeline with id [{}]", id);

        return pipelineService.deleteById(id)
                .fold(FcrResponse::crudFailure, FcrResponse::okResponse);
    }
}
