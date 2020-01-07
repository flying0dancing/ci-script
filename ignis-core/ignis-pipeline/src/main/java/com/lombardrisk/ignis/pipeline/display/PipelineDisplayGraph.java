package com.lombardrisk.ignis.pipeline.display;

import com.lombardrisk.ignis.pipeline.TransformationStep;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.graph.DirectedAcyclicGraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class PipelineDisplayGraph<T, STEP extends TransformationStep<T>> {

    private final DirectedAcyclicGraph<T, PipelineEdge<T, STEP>> pipelineGraph;

    public List<PipelineEdge<T, STEP>> edges() {
        return new ArrayList<>(pipelineGraph.edgeSet());
    }

    public List<Set<PipelineEdge<T, STEP>>> connectedSets() {
        List<Set<PipelineEdge<T, STEP>>> connectedSets = new ArrayList<>();

        for (Set<T> connectedSet : new ConnectivityInspector<>(pipelineGraph).connectedSets()) {
            Set<PipelineEdge<T, STEP>> edgesInConnectedSet = connectedSet.stream()
                    .map(pipelineGraph::edgesOf)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toSet());

            connectedSets.add(edgesInConnectedSet);

        }

        return connectedSets;
    }
}
