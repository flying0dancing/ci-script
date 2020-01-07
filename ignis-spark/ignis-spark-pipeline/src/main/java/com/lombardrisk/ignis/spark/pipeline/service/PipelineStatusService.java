package com.lombardrisk.ignis.spark.pipeline.service;

import com.lombardrisk.ignis.client.external.pipeline.view.PipelineStepStatus;
import com.lombardrisk.ignis.client.internal.PipelineStatusClient;
import com.lombardrisk.ignis.client.internal.UpdatePipelineStepStatusRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
@AllArgsConstructor
public class PipelineStatusService {

    private final PipelineStatusClient pipelineStatusClient;

    public void setPipelineStepRunning(
            final Long pipelineInvocationId,
            final Long pipelineStepInvocationId) throws IOException {

        updatePipelineStepStatus(pipelineInvocationId, pipelineStepInvocationId, PipelineStepStatus.RUNNING);
    }

    public void setPipelineStepSuccess(
            final Long pipelineInvocationId,
            final Long pipelineStepInvocationId) throws IOException {

        updatePipelineStepStatus(pipelineInvocationId, pipelineStepInvocationId, PipelineStepStatus.SUCCESS);
    }

    public void setPipelineStepFailed(
            final Long pipelineInvocationId,
            final Long pipelineStepInvocationId) throws IOException {

        updatePipelineStepStatus(pipelineInvocationId, pipelineStepInvocationId, PipelineStepStatus.FAILED);
    }

    private void updatePipelineStepStatus(
            final Long invocationId,
            final Long stepInvocationId,
            final PipelineStepStatus stepStatus) throws IOException {

        log.info("Updating pipeline invocation [{}], step invocation [{}] with status [{}]",
                invocationId, stepInvocationId, stepStatus);

        UpdatePipelineStepStatusRequest request = new UpdatePipelineStepStatusRequest(stepInvocationId, stepStatus);

        pipelineStatusClient.updatePipelineStepInvocationStatus(invocationId, request).execute();

        log.info("Successfully updated pipeline invocation [{}], step invocation [{}]", invocationId, stepInvocationId);
    }
}
