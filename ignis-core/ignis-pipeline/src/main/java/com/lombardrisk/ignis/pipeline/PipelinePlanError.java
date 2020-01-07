package com.lombardrisk.ignis.pipeline;

import com.lombardrisk.ignis.data.common.error.ErrorResponse;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class PipelinePlanError<NODE, STEP extends TransformationStep<NODE>> {

    private static final String NON_UNIQUE_OUTPUT_SCHEMA = "NON_UNIQUE_OUTPUT_SCHEMA";
    private static final String PIPELINE_GRAPH_NOT_CONNECTED = "PIPELINE_GRAPH_NOT_CONNECTED";
    private static final String PIPELINE_STEP_SELF_JOIN = "PIPELINE_STEP_SELF_JOIN";
    private static final String PIPELINE_GRAPH_CYCLICAL = "PIPELINE_GRAPH_CYCLICAL";

    private final GraphNotConnected<NODE> graphNotConnected;
    private final List<STEP> selfJoiningSteps;
    private final List<MultipleStepsToSameOutput<NODE, STEP>> stepsToSameOutputSchemas;
    private final boolean hasCycles;
    private final List<ErrorResponse> errors;

    public static <T, V extends TransformationStep<T>> PipelinePlanErrorBuilder<T, V> builder() {
        return new PipelinePlanErrorBuilder<>();
    }

    private static ErrorResponse pipelineSelfJoinError(final TransformationStep step) {
        String errorMessage = String.format("Self-join on pipeline steps not allowed [%s]", step.getName());
        return ErrorResponse.valueOf(errorMessage, PIPELINE_STEP_SELF_JOIN);
    }

    private static ErrorResponse pipelineCyclicalError() {
        return ErrorResponse.valueOf("Circular dependency in pipeline", PIPELINE_GRAPH_CYCLICAL);
    }

    private static ErrorResponse pipelineNotConnectedError() {
        return ErrorResponse.valueOf("Pipeline is not fully connected", PIPELINE_GRAPH_NOT_CONNECTED);
    }

    private static ErrorResponse multipleOutputSchemas(final List<? extends TransformationStep> steps) {
        String stepNames = steps.stream()
                .map(TransformationStep::getName)
                .collect(Collectors.joining(", "));

        String errorMessage = String.format("Multiple pipeline steps output to the same schema [%s]", stepNames);
        return ErrorResponse.valueOf(errorMessage, NON_UNIQUE_OUTPUT_SCHEMA);
    }

    @Data
    @AllArgsConstructor
    public static class SelfJoiningStep {

        private final TransformationStep selfJoiningStep;
    }

    @Data
    @AllArgsConstructor
    public static class GraphNotConnected<NODE> {

        private final List<Set<NODE>> connectedSets;
    }

    @Data
    @AllArgsConstructor
    public static class MultipleStepsToSameOutput<T, STEP> {

        private final List<STEP> steps;
        private final T output;
    }

    public static final class PipelinePlanErrorBuilder<NODE, STEP extends TransformationStep<NODE>> {

        private final List<STEP> selfJoiningSteps = new ArrayList<>();
        private final List<MultipleStepsToSameOutput<NODE, STEP>> stepsToSameOutputSchemas = new ArrayList<>();
        private GraphNotConnected<NODE> graphNotConnected;
        private boolean hasCycles;

        private PipelinePlanErrorBuilder() {
        }

        public static PipelinePlanErrorBuilder aPipelinePlanError() {
            return new PipelinePlanErrorBuilder();
        }

        public PipelinePlanErrorBuilder<NODE, STEP> graphNotConnected(final GraphNotConnected<NODE> graphNotConnected) {
            this.graphNotConnected = graphNotConnected;
            return this;
        }

        public PipelinePlanErrorBuilder<NODE, STEP> selfJoiningStep(final STEP selfJoiningStep) {
            this.selfJoiningSteps.add(selfJoiningStep);
            return this;
        }

        public PipelinePlanErrorBuilder<NODE, STEP> stepsToSameOutputSchema(final MultipleStepsToSameOutput<NODE, STEP> steps) {
            this.stepsToSameOutputSchemas.add(steps);
            return this;
        }

        public PipelinePlanErrorBuilder<NODE, STEP> hasCycles(final boolean hasCycles) {
            this.hasCycles = hasCycles;
            return this;
        }

        public PipelinePlanError build() {
            return new PipelinePlanError(
                    graphNotConnected,
                    selfJoiningSteps,
                    stepsToSameOutputSchemas,
                    hasCycles,
                    findErrors());
        }

        private List<ErrorResponse> findErrors() {
            List<ErrorResponse> errorResponses = new ArrayList<>();
            if (hasCycles) {
                errorResponses.add(pipelineCyclicalError());
            }

            selfJoiningSteps.stream()
                    .map(PipelinePlanError::pipelineSelfJoinError)
                    .forEach(errorResponses::add);

            stepsToSameOutputSchemas.stream()
                    .map(MultipleStepsToSameOutput::getSteps)
                    .map(PipelinePlanError::multipleOutputSchemas)
                    .forEach(errorResponses::add);

            if (graphNotConnected != null) {
                errorResponses.add(pipelineNotConnectedError());
            }

            return errorResponses;
        }
    }
}
