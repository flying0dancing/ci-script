package com.lombardrisk.ignis.client.design.productconfig.validation;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Wither;

@Data
@Builder
@Wither
public class PipelineCheck implements ValidationTask {

    private final Long pipelineId;
    private final Long pipelineStepId;
    private final String name;
    private final String message;
    private final TaskStatus status;
    private final TaskType type;
}
