package com.lombardrisk.ignis.client.design.pipeline;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

@Data
@AllArgsConstructor
public class PipelineDisplayErrorView {

    private final String errorCode;
    private final String errorMessage;
    private final PipelineCycleErrorView cycleError;

    public static PipelineDisplayErrorView notFound(final String errorCode, final String errorMessage) {
        return new PipelineDisplayErrorView(errorCode, errorMessage, null);
    }

    public static PipelineDisplayErrorView hasCyclesError(
            final Set<Long> parentChain,
            final Long cyclicalStep) {

        return new PipelineDisplayErrorView(
                "PIPELINE_GRAPH_CYCLICAL",
                "Pipeline graph cannot contain cycles and could not be rendered",
                new PipelineCycleErrorView(parentChain, cyclicalStep));
    }

    @Data
    public static class PipelineCycleErrorView {

        private final Set<Long> parentChain;
        private final Long cyclicalStep;
    }
}
