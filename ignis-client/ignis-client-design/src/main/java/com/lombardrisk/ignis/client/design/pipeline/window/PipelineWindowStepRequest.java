package com.lombardrisk.ignis.client.design.pipeline.window;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.lombardrisk.ignis.client.design.pipeline.PipelineStepRequest;
import com.lombardrisk.ignis.client.design.pipeline.select.SelectRequest;
import com.lombardrisk.ignis.client.external.pipeline.export.TransformationType;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Data
public class PipelineWindowStepRequest extends PipelineStepRequest {

    private final Set<SelectRequest> selects;
    private final Set<String> filters;
    private Long schemaInId;
    private Long schemaOutId;

    @JsonCreator
    @Builder
    public PipelineWindowStepRequest(
            @JsonProperty("name") final String name,
            @JsonProperty("description") final String description,
            @JsonProperty("selects") final Set<SelectRequest> selects,
            @JsonProperty("filters") final Set<String> filters,
            @JsonProperty("schemaInId") final Long schemaInId,
            @JsonProperty("schemaOutId") final Long schemaOutId) {

        super(name, description, TransformationType.WINDOW);
        this.selects = selects;
        this.filters = filters;
        this.schemaInId = schemaInId;
        this.schemaOutId = schemaOutId;
    }
}
