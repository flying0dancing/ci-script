package com.lombardrisk.ignis.client.design.pipeline;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.lombardrisk.ignis.client.design.pipeline.aggregation.PipelineAggregationStepRequest;
import com.lombardrisk.ignis.client.design.pipeline.join.PipelineJoinStepRequest;
import com.lombardrisk.ignis.client.design.pipeline.map.PipelineMapStepRequest;
import com.lombardrisk.ignis.client.design.pipeline.scriptlet.PipelineScriptletStepRequest;
import com.lombardrisk.ignis.client.design.pipeline.union.PipelineUnionStepRequest;
import com.lombardrisk.ignis.client.design.pipeline.window.PipelineWindowStepRequest;
import com.lombardrisk.ignis.client.external.pipeline.export.TransformationType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = PipelineMapStepRequest.class, name = "MAP"),
        @JsonSubTypes.Type(value = PipelineAggregationStepRequest.class, name = "AGGREGATION"),
        @JsonSubTypes.Type(value = PipelineJoinStepRequest.class, name = "JOIN"),
        @JsonSubTypes.Type(value = PipelineWindowStepRequest.class, name = "WINDOW"),
        @JsonSubTypes.Type(value = PipelineUnionStepRequest.class, name = "UNION"),
        @JsonSubTypes.Type(value = PipelineScriptletStepRequest.class, name = "SCRIPTLET")
})
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class PipelineStepRequest {

    private final String name;
    private final String description;
    private final TransformationType type;
}
