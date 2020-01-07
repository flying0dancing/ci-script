package com.lombardrisk.ignis.spark.api.pipeline;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lombardrisk.ignis.client.external.pipeline.export.TransformationType;
import com.lombardrisk.ignis.pipeline.step.api.SelectColumn;
import com.lombardrisk.ignis.pipeline.step.api.JoinAppConfig;
import com.lombardrisk.ignis.spark.api.staging.StagingDatasetConfig;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class JoinStepAppConfig extends PipelineStepAppConfig {

    @JsonDeserialize(as = LinkedHashSet.class)
    private Set<SelectColumn> selects;

    @JsonDeserialize(as = LinkedHashSet.class)
    private Set<JoinAppConfig> joinAppConfigs;

    @Builder
    public JoinStepAppConfig(
            final Long pipelineStepInvocationId,
            final List<PipelineStepDatasetLookup> pipelineStepDatasetLookups,
            final StagingDatasetConfig outputDataset,
            final Set<SelectColumn> selects,
            final Set<JoinAppConfig> joinAppConfigs) {

        super(pipelineStepInvocationId, pipelineStepDatasetLookups, outputDataset);
        this.selects = selects;
        this.joinAppConfigs = joinAppConfigs;
    }

    @Override
    public TransformationType getTransformationType() {
        return TransformationType.JOIN;
    }
}
