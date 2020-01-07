package com.lombardrisk.ignis.spark.api.pipeline;

import com.lombardrisk.ignis.client.external.pipeline.export.TransformationType;
import com.lombardrisk.ignis.spark.api.staging.StagingDatasetConfig;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ScriptletStepAppConfig extends PipelineStepAppConfig {

    private String jarFile;
    private String className;
    private Map<String, String> inputSchemaMappings;

    @Builder
    public ScriptletStepAppConfig(
            final String jarFile,
            final String className,
            final Map<String, String> inputSchemaMappings,
            final Long pipelineStepInvocationId,
            final List<PipelineStepDatasetLookup> pipelineStepDatasetLookups,
            final StagingDatasetConfig outputDataset) {

        super(pipelineStepInvocationId, pipelineStepDatasetLookups, outputDataset);
        this.jarFile = jarFile;
        this.className = className;
        this.inputSchemaMappings = inputSchemaMappings;
    }

    @Override
    public TransformationType getTransformationType() {
        return TransformationType.SCRIPTLET;
    }
}