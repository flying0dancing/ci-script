package com.lombardrisk.ignis.client.design.pipeline;

import com.lombardrisk.ignis.client.core.response.ApiErrorCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.List;
import java.util.Set;

@Data
@Builder
public class PipelinePlanErrorView {

    private final GraphNotConnectedView graphNotConnected;
    private final List<Long> selfJoiningSteps;
    @Singular
    private final List<MultipleStepsToSameOutputSchemaView> stepsToSameOutputSchemas;
    private final boolean hasCycles;
    private final List<ApiErrorCode> errors;

    @Data
    @AllArgsConstructor
    public static class GraphNotConnectedView {
        private final List<Set<Long>> connectedSets;
    }

    @Data
    @AllArgsConstructor
    public static class MultipleStepsToSameOutputSchemaView {

        private final List<Long> stepsIds;
        private final Long outputSchemaId;
    }
}
