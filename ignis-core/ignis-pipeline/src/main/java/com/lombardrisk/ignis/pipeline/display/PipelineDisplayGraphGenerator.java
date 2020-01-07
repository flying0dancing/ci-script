package com.lombardrisk.ignis.pipeline.display;

import com.google.common.collect.HashMultimap;
import com.lombardrisk.ignis.pipeline.TransformationStep;
import io.vavr.control.Validation;
import org.jgrapht.graph.DirectedAcyclicGraph;

import java.io.Serializable;
import java.util.Set;

public class PipelineDisplayGraphGenerator<T, STEP extends TransformationStep<T>> implements Serializable {

    private static final long serialVersionUID = 2872484513021627415L;

    public Validation<PipelineCycleError<T, STEP>, PipelineDisplayGraph<T, STEP>> generateDisplayGraph(final Set<STEP> steps) {

        DirectedAcyclicGraph<T, PipelineEdge<T, STEP>> pipelineGraph = new DirectedAcyclicGraph(PipelineEdge.class);

        HashMultimap<T, T> nodeToNodeMap = HashMultimap.create();

        for (STEP step : steps) {
            T outputId = step.getOutput();
            pipelineGraph.addVertex(outputId);

            for (T inputId : step.getInputs()) {
                pipelineGraph.addVertex(inputId);
                nodeToNodeMap.put(inputId, outputId);
            }
        }

        for (STEP step : steps) {
            T outputId = step.getOutput();
            for (T inputId : step.getInputs()) {

                if (doAncestorsContainChildren(pipelineGraph, inputId, nodeToNodeMap)) {
                    return Validation.invalid(
                            new PipelineCycleError<>(
                                    pipelineGraph.getAncestors(inputId), step));
                }

                pipelineGraph.addEdge(inputId, outputId, new PipelineEdge<>(inputId, outputId, step));
            }
        }

        return Validation.valid(new PipelineDisplayGraph<>(pipelineGraph));
    }

    private boolean doAncestorsContainChildren(
            final DirectedAcyclicGraph<T, PipelineEdge<T, STEP>> pipelineGraph,
            final T inputId,
            final HashMultimap<T, T> nodeToNodeMap) {

        Set<T> children = nodeToNodeMap.get(inputId);
        return pipelineGraph.getAncestors(inputId).stream()
                .anyMatch(children::contains);
    }
}
