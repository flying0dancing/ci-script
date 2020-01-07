package com.lombardrisk.ignis.client.external.pipeline.view;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
@AllArgsConstructor
public class DownstreamPipelineView {
    private final Long pipelineId;
    private final String pipelineName;
    private final Set<SchemaDetailsView> requiredSchemas;
}
