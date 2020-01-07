package com.lombardrisk.ignis.client.design.pipeline.join;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.lombardrisk.ignis.client.design.pipeline.PipelineStepView;
import com.lombardrisk.ignis.client.design.pipeline.select.SelectView;
import com.lombardrisk.ignis.client.external.pipeline.export.TransformationType;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class PipelineJoinStepView extends PipelineStepView {

    private final List<SelectView> selects;
    private final Long schemaOutId;
    private final List<JoinView> joins;

    @Builder
    @JsonCreator
    public PipelineJoinStepView(
            @JsonProperty("id") final Long id,
            @JsonProperty("name") final String name,
            @JsonProperty("description") final String description,
            @JsonProperty("selects") final List<SelectView> selects,
            @JsonProperty("filters") final List<String> filters,
            @JsonProperty("schemaOutId") final Long schemaOutId,
            @JsonProperty("joins") final List<JoinView> joins) {
        super(id, name, description, TransformationType.JOIN);
        this.selects = selects;
        this.schemaOutId = schemaOutId;
        this.joins = joins;
    }
}
