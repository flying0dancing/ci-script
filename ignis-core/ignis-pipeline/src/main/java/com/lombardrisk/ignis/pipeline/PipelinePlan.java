package com.lombardrisk.ignis.pipeline;

import com.google.common.collect.ImmutableList;
import io.vavr.Tuple;
import io.vavr.control.Either;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PipelinePlan<T, STEP extends TransformationStep<T>> {

    private final DirectedAcyclicGraph<STEP, DefaultEdge> graph;

    static <T, STEP extends TransformationStep<T>> PipelinePlan<T, STEP> empty() {
        return new PipelinePlan<>(new DirectedAcyclicGraph<>(DefaultEdge.class));
    }

    static <T, STEP extends TransformationStep<T>> PipelinePlan<T, STEP> fromGraph(
            final DirectedAcyclicGraph<STEP, DefaultEdge> graph) {

        return new PipelinePlan<>(graph);
    }

    /**
     * Return an immutable list of steps to be executed in order based on each steps dependencies
     *
     * @return list of steps
     */
    public List<STEP> getPipelineStepsInOrderOfExecution() {
        TopologicalOrderIterator<STEP, DefaultEdge> orderIterator = new TopologicalOrderIterator<>(graph);
        return ImmutableList.copyOf(orderIterator);
    }

    /**
     * Return a list of dependencies for a step to be able to execute. Steps can depend on a combination of
     * other steps or staged datasets.
     *
     * @param step to get dependencies for
     * @return list of either schema IDs or pipeline steps
     */
    public List<Either<T, STEP>> getStepDependencies(final STEP step) {
        Map<T, STEP> dependentStepOutputs = dependentStepOutputsToDependentStep(step);
        Set<T> stepInputs = step.getInputs();

        List<Either<T, STEP>> stepDependencies = new ArrayList<>();

        for (T stepInput : stepInputs) {
            if (dependentStepOutputs.containsKey(stepInput)) {
                stepDependencies.add(Either.right(dependentStepOutputs.get(stepInput)));
            } else {
                stepDependencies.add(Either.left(stepInput));
            }
        }

        return stepDependencies;
    }

    public Set<T> getRequiredPipelineInputs() {
        return graph.vertexSet().stream()
                .flatMap(step -> getStepDependencies(step).stream())
                .filter(Either::isLeft)
                .map(Either::getLeft)
                .collect(toSet());
    }

    private Map<T, STEP> dependentStepOutputsToDependentStep(final STEP step) {
        return graph.incomingEdgesOf(step).stream()
                .map(graph::getEdgeSource)
                .map(dependentStep -> Tuple.of(dependentStep.getOutput(), dependentStep))
                .collect(toMap(outputSchemaId -> outputSchemaId._1, dependentStep -> dependentStep._2));
    }
}
