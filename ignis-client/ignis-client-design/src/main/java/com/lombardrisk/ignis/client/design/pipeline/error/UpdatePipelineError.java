package com.lombardrisk.ignis.client.design.pipeline.error;

import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdatePipelineError {

    private final CRUDFailure pipelineNotFoundError;
    private final StepExecutionResult stepExecutionResult;
}
