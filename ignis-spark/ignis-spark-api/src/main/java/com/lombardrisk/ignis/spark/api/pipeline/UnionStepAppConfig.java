package com.lombardrisk.ignis.spark.api.pipeline;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lombardrisk.ignis.client.external.pipeline.export.TransformationType;
import com.lombardrisk.ignis.pipeline.step.api.UnionAppConfig;
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
public class UnionStepAppConfig extends PipelineStepAppConfig {

    @JsonDeserialize(as = LinkedHashSet.class)
    private Set<UnionAppConfig> unions;

    @Builder
    public UnionStepAppConfig(
            final Long pipelineStepInvocationId,
            final List<PipelineStepDatasetLookup> pipelineStepDatasetLookups,
            final StagingDatasetConfig outputDataset,
            final Set<UnionAppConfig> unions) {

        super(pipelineStepInvocationId, pipelineStepDatasetLookups, outputDataset);
        this.unions = unions;
    }

    @Override
    public TransformationType getTransformationType() {
        return TransformationType.UNION;
    }
}
