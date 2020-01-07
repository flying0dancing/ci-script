package com.lombardrisk.ignis.client.design.productconfig.validation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
public class ProductConfigTaskList {

    private final Long productId;
    private final Map<Long, PipelineTask> pipelineTasks;
}
