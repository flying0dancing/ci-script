package com.lombardrisk.ignis.server.controller.pipeline;

import com.lombardrisk.ignis.client.core.view.IdView;
import com.lombardrisk.ignis.client.external.pipeline.view.PipelineStepStatus;
import com.lombardrisk.ignis.client.internal.UpdatePipelineStepStatusRequest;
import com.lombardrisk.ignis.client.internal.path.api;
import com.lombardrisk.ignis.data.common.Identifiable;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.server.dataset.pipeline.invocation.PipelineInvocationService;
import com.lombardrisk.ignis.server.dataset.pipeline.invocation.model.PipelineInvocation;
import com.lombardrisk.ignis.web.common.response.FcrResponse;
import io.vavr.control.Validation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@AllArgsConstructor
public class PipelineStatusController {

    private final PipelineInvocationService pipelineInvocationService;

    @PostMapping(
            path = api.internal.pipelineInvocations.ById,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public FcrResponse<IdView> updatePipelineStepInvocationStatus(
            @PathVariable(value = api.Params.ID) final Long pipelineInvocationId,
            @RequestBody final UpdatePipelineStepStatusRequest updatePipelineStepStatusRequest) {

        Long pipelineStepInvocationId = updatePipelineStepStatusRequest.getPipelineStepInvocationId();
        PipelineStepStatus pipelineStepStatus = updatePipelineStepStatusRequest.getPipelineStepStatus();

        log.info("Update pipeline invocation [{}], step invocation [{}] with status [{}]",
                pipelineInvocationId, pipelineStepInvocationId, pipelineStepStatus);

        Validation<CRUDFailure, PipelineInvocation> pipelineInvocation = pipelineInvocationService.updateStepInvocationStatus(
                pipelineInvocationId,
                pipelineStepInvocationId,
                pipelineStepStatus.toString());

        return pipelineInvocation
                .map(Identifiable::toIdentifiable)
                .map(identifiable -> new IdView(identifiable.getId()))
                .fold(FcrResponse::crudFailure, FcrResponse::okResponse);
    }
}
