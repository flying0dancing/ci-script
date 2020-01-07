package com.lombardrisk.ignis.client.design.pipeline;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PipelineEdgeView {

    private static final long serialVersionUID = -1294996681828766630L;

    private final Long source;
    private final Long target;
    private final PipelineStepView pipelineStep;
}
