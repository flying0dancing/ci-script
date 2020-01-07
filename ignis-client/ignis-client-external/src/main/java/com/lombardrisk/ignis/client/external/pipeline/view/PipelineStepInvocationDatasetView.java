package com.lombardrisk.ignis.client.external.pipeline.view;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Builder
@Data
public class PipelineStepInvocationDatasetView {

    private final Long datasetId;
    private final Long datasetRunKey;
}
