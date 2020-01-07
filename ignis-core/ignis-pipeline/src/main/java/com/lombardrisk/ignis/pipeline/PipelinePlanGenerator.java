package com.lombardrisk.ignis.pipeline;

import com.google.common.collect.ImmutableList;
import com.lombardrisk.ignis.pipeline.PipelinePlanError.MultipleStepsToSameOutput;
import com.lombardrisk.ignis.data.common.error.ErrorResponse;
import io.vavr.control.Validation;
import lombok.extern.slf4j.Slf4j;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;

@Slf4j
public class PipelinePlanGenerator<T, STEP extends TransformationStep<T>> implements Serializable {

    private static final long serialVersionUID = 6256510119662933104L;

    private static final String NON_UNIQUE_OUTPUT_SCHEMA = "NON_UNIQUE_OUTPUT_SCHEMA";
    private static final String PIPELINE_GRAPH_NOT_CONNECTED = "PIPELINE_GRAPH_NOT_CONNECTED";
    private static final String PIPELINE_STEP_SELF_JOIN = "PIPELINE_STEP_SELF_JOIN";
    private static final String PIPELINE_GRAPH_CYCLICAL = "PIPELINE_GRAPH_CYCLICAL";

    @Transactional
    public Validation<PipelinePlanError<T, STEP>, PipelinePlan<T, STEP>> generateExecutionPlan(
            final TransformationPipeline<STEP> pipeline) {

        if (pipeline.getSteps() == null || pipeline.getSteps().isEmpty()) {
            return Validation.valid(PipelinePlan.empty());
        }

        DirectedAcyclicGraph<STEP, DefaultEdge> pipelineGraph = new DirectedAcyclicGraph<>(DefaultEdge.class);

        Map<T, STEP> stepOutputsToSteps = new HashMap<>();

        PipelinePlanError errors = validatePipelineSteps(pipeline.getSteps());

        if (!errors.getErrors().isEmpty()) {
            return Validation.invalid(errors);
        }

        for (STEP step : pipeline.getSteps()) {
            stepOutputsToSteps.put(step.getOutput(), step);

            pipelineGraph.addVertex(step);
        }

        for (STEP currentStep : pipeline.getSteps()) {
            for (T stepInput : currentStep.getInputs()) {
                if (stepOutputsToSteps.containsKey(stepInput)) {
                    STEP dependentStep = stepOutputsToSteps.get(stepInput);

                    if (pipelineGraph.getAncestors(dependentStep).contains(currentStep)) {
                        log.debug("Pipeline graph has cycle {} -> {}", currentStep, dependentStep);
                        return Validation.invalid(PipelinePlanError.builder()
                                .hasCycles(true)
                                .build());
                    }

                    pipelineGraph.addEdge(dependentStep, currentStep);
                }
            }
        }

        List<Set<T>> connectedNodes = connectedNodes(pipelineGraph);
        if (connectedNodes.size() > 1) {
            return Validation.invalid(PipelinePlanError.<T, STEP>builder()
                    .graphNotConnected(new PipelinePlanError.GraphNotConnected<>(connectedNodes))
                    .build());
        }

        return Validation.valid(PipelinePlan.fromGraph(pipelineGraph));
    }

    private List<Set<T>> connectedNodes(final DirectedAcyclicGraph<STEP, DefaultEdge> pipelineGraph) {
        return new ConnectivityInspector<>(pipelineGraph)
                .connectedSets()
                .stream()
                .map(connectedSet -> connectedSet.stream()
                        .flatMap(step -> ImmutableList.<T>builder()
                                .addAll(step.getInputs())
                                .add(step.getOutput())
                                .build()
                                .stream())
                        .collect(toSet()))
                .collect(Collectors.toList());
    }

    private PipelinePlanError validatePipelineSteps(final Set<STEP> pipelineSteps) {
        PipelinePlanError.PipelinePlanErrorBuilder errorBuilder = PipelinePlanError.builder();

        Map<T, List<STEP>> stepOutputsToSteps = new HashMap<>();
        for (STEP step : pipelineSteps) {
            if (pipelineStepIsSelfJoining(step)) {
                errorBuilder.selfJoiningStep(step);
            }

            T stepOutput = step.getOutput();

            if (stepOutputsToSteps.containsKey(stepOutput)) {
                stepOutputsToSteps.get(stepOutput).add(step);
            } else {
                List<STEP> steps = new ArrayList<>();
                steps.add(step);
                stepOutputsToSteps.put(stepOutput, steps);
            }
        }

        stepOutputsToSteps.values().stream()
                .filter(list -> list.size() > 1)
                .map(steps -> new MultipleStepsToSameOutput<>(steps, steps.get(0).getOutput()))
                .forEach(errorBuilder::stepsToSameOutputSchema);

        return errorBuilder.build();
    }

    private boolean pipelineStepIsSelfJoining(final STEP step) {
        return step.getInputs().contains(step.getOutput());
    }

    private ErrorResponse pipelineSelfJoinError(final STEP step) {
        String errorMessage = String.format("Self-join on pipeline steps not allowed [%s]", step.getName());
        return ErrorResponse.valueOf(errorMessage, PIPELINE_STEP_SELF_JOIN);
    }

    static ErrorResponse pipelineCyclicalError() {
        return ErrorResponse.valueOf("Circular dependency in pipeline", PIPELINE_GRAPH_CYCLICAL);
    }

    private static ErrorResponse pipelineNotConnectedError() {
        return ErrorResponse.valueOf("Pipeline is not fully connected", PIPELINE_GRAPH_NOT_CONNECTED);
    }

    private ErrorResponse multipleOutputSchemas(final List<STEP> steps) {
        String stepNames = steps.stream().map(TransformationStep::getName).collect(Collectors.joining(", "));
        String errorMessage = String.format("Multiple pipeline steps output to the same schema [%s]", stepNames);
        return ErrorResponse.valueOf(errorMessage, NON_UNIQUE_OUTPUT_SCHEMA);
    }
}
