package com.lombardrisk.ignis.client.design.pipeline;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.lombardrisk.ignis.client.design.pipeline.aggregation.PipelineAggregationStepView;
import com.lombardrisk.ignis.client.design.pipeline.join.PipelineJoinStepView;
import com.lombardrisk.ignis.client.design.pipeline.map.PipelineMapStepView;
import com.lombardrisk.ignis.client.design.pipeline.scriptlet.PipelineScriptletStepView;
import com.lombardrisk.ignis.client.design.pipeline.union.PipelineUnionStepView;
import com.lombardrisk.ignis.client.design.pipeline.window.PipelineWindowStepView;
import com.lombardrisk.ignis.client.external.pipeline.export.TransformationType;
import lombok.AllArgsConstructor;
import lombok.Data;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = PipelineMapStepView.class, name = "MAP"),
        @JsonSubTypes.Type(value = PipelineAggregationStepView.class, name = "AGGREGATION"),
        @JsonSubTypes.Type(value = PipelineJoinStepView.class, name = "JOIN"),
        @JsonSubTypes.Type(value = PipelineWindowStepView.class, name = "WINDOW"),
        @JsonSubTypes.Type(value = PipelineUnionStepView.class, name = "UNION"),
        @JsonSubTypes.Type(value = PipelineScriptletStepView.class, name = "SCRIPTLET")
})
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@AllArgsConstructor(onConstructor = @__({ @JsonCreator }))
public abstract class PipelineStepView {

    private final Long id;
    private final String name;
    private final String description;
    private final TransformationType type;
}
