package com.lombardrisk.ignis.client.external.pipeline.view;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class PipelineStepInvocationView {

    private Long id;
    private Set<PipelineStepInvocationDatasetView> datasetsIn;
    private Long datasetOutId;
    private Set<Long> inputPipelineStepIds;
    private PipelineStepView pipelineStep;
    private PipelineStepStatus status;
}
