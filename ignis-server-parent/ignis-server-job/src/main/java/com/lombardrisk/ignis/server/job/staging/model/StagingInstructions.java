package com.lombardrisk.ignis.server.job.staging.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
@AllArgsConstructor
public class StagingInstructions {

    private final String jobName;
    private final Set<StagingDatasetInstruction> stagingDatasetInstructions;
    private final Set<DownstreamPipelineInstruction> downstreamPipelineInstructions;
}
