package com.lombardrisk.ignis.client.design.pipeline.aggregation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.lombardrisk.ignis.client.design.pipeline.PipelineStepRequest;
import com.lombardrisk.ignis.client.design.pipeline.select.SelectRequest;
import com.lombardrisk.ignis.client.external.pipeline.export.TransformationType;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Data
public class PipelineAggregationStepRequest extends PipelineStepRequest {

    private final Long schemaInId;
    private final Long schemaOutId;
    private final Set<SelectRequest> selects;
    private final List<String> filters;
    private final List<String> groupings;

    @Builder
    @JsonCreator
    public PipelineAggregationStepRequest(
            @JsonProperty("name") final String name,
            @JsonProperty("description") final String description,
            @JsonProperty("schemaInId") final Long schemaInId,
            @JsonProperty("schemaOutId") final Long schemaOutId,
            @JsonProperty("selects") final Set<SelectRequest> selects,
            @JsonProperty("filters") final List<String> filters,
            @JsonProperty("groupings")final List<String> groupings) {
        super(name, description, TransformationType.AGGREGATION);

        this.schemaInId = schemaInId;
        this.schemaOutId = schemaOutId;
        this.selects = selects;
        this.filters = filters;
        this.groupings = groupings;
    }
}
