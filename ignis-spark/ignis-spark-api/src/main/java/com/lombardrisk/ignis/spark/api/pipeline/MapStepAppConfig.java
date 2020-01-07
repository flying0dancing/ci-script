package com.lombardrisk.ignis.spark.api.pipeline;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lombardrisk.ignis.client.external.pipeline.export.TransformationType;
import com.lombardrisk.ignis.pipeline.step.api.SelectColumn;
import com.lombardrisk.ignis.spark.api.staging.StagingDatasetConfig;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.LinkedHashSet;
import java.util.Set;

import static java.util.Collections.singletonList;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MapStepAppConfig extends PipelineStepAppConfig {

    @JsonDeserialize(as = LinkedHashSet.class)
    private Set<SelectColumn> selects;

    @JsonDeserialize(as = LinkedHashSet.class)
    private Set<String> filters;

    @Builder
    public MapStepAppConfig(
            final Long pipelineStepInvocationId,
            final PipelineStepDatasetLookup pipelineStepDatasetLookup,
            final StagingDatasetConfig outputDataset,
            final Set<SelectColumn> selects,
            final Set<String> filters) {

        super(pipelineStepInvocationId, singletonList(pipelineStepDatasetLookup), outputDataset);
        this.selects = selects;
        this.filters = filters;
    }

    @Override
    public TransformationType getTransformationType() {
        return TransformationType.MAP;
    }
}
