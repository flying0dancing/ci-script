package com.lombardrisk.ignis.client.design.pipeline;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Set;

@AllArgsConstructor
@Data
public class PipelineConnectedGraphs {
    private final List<Set<PipelineEdgeView>> connectedSets;
}
