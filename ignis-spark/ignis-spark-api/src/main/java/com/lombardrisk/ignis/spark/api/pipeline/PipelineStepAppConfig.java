package com.lombardrisk.ignis.spark.api.pipeline;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.lombardrisk.ignis.client.external.pipeline.export.TransformationType;
import com.lombardrisk.ignis.spark.api.staging.StagingDatasetConfig;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = MapStepAppConfig.class, name = "map"),
        @JsonSubTypes.Type(value = AggregateStepAppConfig.class, name = "aggregate"),
        @JsonSubTypes.Type(value = JoinStepAppConfig.class, name = "join"),
        @JsonSubTypes.Type(value = WindowStepAppConfig.class, name = "window"),
        @JsonSubTypes.Type(value = UnionStepAppConfig.class, name = "union"),
        @JsonSubTypes.Type(value = ScriptletStepAppConfig.class, name = "scriptlet")
})
public abstract class PipelineStepAppConfig {

    private Long pipelineStepInvocationId;
    private List<PipelineStepDatasetLookup> pipelineStepDatasetLookups;
    private StagingDatasetConfig outputDataset;

    public abstract TransformationType getTransformationType();
}
