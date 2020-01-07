package com.lombardrisk.ignis.client.design.pipeline;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
@AllArgsConstructor(onConstructor = @__({@JsonCreator }))
public class PipelineView {

    private final Long id;
    private final String name;
    private final Long productId;
    private final Set<PipelineStepView> steps;
    private final PipelinePlanErrorView error;
}
