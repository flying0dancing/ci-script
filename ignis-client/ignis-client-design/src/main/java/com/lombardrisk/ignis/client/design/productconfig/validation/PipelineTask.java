package com.lombardrisk.ignis.client.design.productconfig.validation;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Wither;

import java.util.List;

@Data
@Builder
@Wither
public class PipelineTask implements ValidationTask {

    private final Long pipelineId;
    private final String name;
    private final String message;
    private final TaskStatus status;
    private final List<PipelineCheck> tasks;

    @Override
    public TaskType getType() {
        return TaskType.PIPELINE;
    }
}
