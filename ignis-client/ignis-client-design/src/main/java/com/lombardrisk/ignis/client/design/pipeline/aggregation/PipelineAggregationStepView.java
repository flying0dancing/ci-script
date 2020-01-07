package com.lombardrisk.ignis.client.design.pipeline.aggregation;

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
public class PipelineAggregationStepView extends PipelineStepView {

    private final List<SelectView> selects;
    private final List<String> filters;
    private final List<String> groupings;
    private final Long schemaInId;
    private final Long schemaOutId;

    @Builder
    @JsonCreator
    public PipelineAggregationStepView(
            @JsonProperty("id") final Long id,
            @JsonProperty("name") final String name,
            @JsonProperty("description") final String description,
            @JsonProperty("selects") final List<SelectView> selects,
            @JsonProperty("filters") final List<String> filters,
            @JsonProperty("groupings") final List<String> groupings,
            @JsonProperty("schemaInId") final Long schemaInId,
            @JsonProperty("schemaOutId") final Long schemaOutId) {
        super(id, name, description, TransformationType.AGGREGATION);
        this.selects = selects;
        this.filters = filters;
        this.groupings = groupings;
        this.schemaInId = schemaInId;
        this.schemaOutId = schemaOutId;
    }
}
