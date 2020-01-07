package com.lombardrisk.ignis.server.controller.pipeline;

import com.lombardrisk.ignis.client.external.path.api;
import com.lombardrisk.ignis.client.external.pipeline.view.DownstreamPipelineView;
import com.lombardrisk.ignis.client.external.pipeline.view.PipelineEdgeView;
import com.lombardrisk.ignis.client.external.pipeline.view.PipelineInvocationView;
import com.lombardrisk.ignis.client.external.pipeline.view.PipelineView;
import com.lombardrisk.ignis.client.external.pipeline.view.SchemaDetailsView;
import com.lombardrisk.ignis.server.dataset.pipeline.invocation.PipelineInvocationService;
import com.lombardrisk.ignis.server.product.pipeline.PipelineService;
import com.lombardrisk.ignis.web.common.response.FcrResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@AllArgsConstructor
public class PipelineController {

    private final PipelineService pipelineService;
    private final PipelineInvocationService pipelineInvocationService;

    @GetMapping(path = api.external.v1.Pipelines, produces = APPLICATION_JSON_VALUE)
    public FcrResponse<List<PipelineView>> getPipelines() {
        log.debug("Find all pipelines");

        return FcrResponse.okResponse(pipelineService.findAll());
    }

    @GetMapping(path = api.external.v1.PipelinesDownstreams, produces = APPLICATION_JSON_VALUE)
    public FcrResponse<Set<DownstreamPipelineView>> getPipelineDownstreams() {
        log.debug("Find all pipelines downstreams");

        return pipelineService.findDownstreamPipelines()
                .fold(FcrResponse::crudFailure, FcrResponse::okResponse);
    }

    @GetMapping(path = api.external.v1.pipelines.ByID.schemas, produces = APPLICATION_JSON_VALUE)
    public FcrResponse<Set<SchemaDetailsView>> getRequiredSchemas(
            @PathVariable(api.Params.ID) final Long pipelineId) {

        return pipelineService.getRequiredSchemas(pipelineId)
                .fold(FcrResponse::badRequest, FcrResponse::okResponse);
    }

    @GetMapping(path = api.external.v1.pipelines.ByID.edges, produces = APPLICATION_JSON_VALUE)
    public FcrResponse<List<PipelineEdgeView>> getEdges(
            @PathVariable(api.Params.ID) final Long pipelineId) {

        return pipelineService.findPipelineEdgeData(pipelineId)
                .fold(FcrResponse::crudFailure, FcrResponse::okResponse);
    }

    @GetMapping(path = api.external.v1.PipelineInvocations, produces = APPLICATION_JSON_VALUE)
    public FcrResponse<List<PipelineInvocationView>> getPipelineInvocations(
            @RequestParam(value = api.Params.JOB_ID, required = false) final Long jobId) {

        if (jobId != null) {
            log.debug("Find all pipeline invocations with job ID [" + jobId + "]");
            return FcrResponse.okResponse(pipelineInvocationService.findInvocationsByServiceRequestId(jobId));
        }

        log.debug("Find all pipeline invocations");
        return FcrResponse.okResponse(pipelineInvocationService.findAllInvocations());
    }
}
