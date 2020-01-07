package com.lombardrisk.ignis.client.internal;

import com.lombardrisk.ignis.client.external.pipeline.view.PipelineStepStatus;
import lombok.Data;

@Data
public class UpdatePipelineStepStatusRequest {

    private final Long pipelineStepInvocationId;
    private final PipelineStepStatus pipelineStepStatus;
}
