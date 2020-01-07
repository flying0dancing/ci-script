package com.lombardrisk.ignis.client.design.pipeline.join;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lombardrisk.ignis.client.design.pipeline.PipelineStepRequest;
import com.lombardrisk.ignis.client.design.pipeline.select.SelectRequest;
import com.lombardrisk.ignis.client.external.pipeline.export.TransformationType;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.LinkedHashSet;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Data
public class PipelineJoinStepRequest extends PipelineStepRequest {

    @NotNull
    private final Long schemaOutId;
    @NotEmpty
    private final Set<SelectRequest> selects;

    @NotEmpty
    @JsonDeserialize(as = LinkedHashSet.class)
    private final Set<JoinRequest> joins;

    @JsonCreator
    @Builder
    public PipelineJoinStepRequest(
            @JsonProperty("name") final String name,
            @JsonProperty("description") final String description,
            @JsonProperty("schemaOutId") @NotNull final Long schemaOutId,
            @JsonProperty("selects") @NotEmpty final Set<SelectRequest> selects,
            @JsonProperty("joins")
            @JsonDeserialize(as = LinkedHashSet.class)
            @NotEmpty final Set<JoinRequest> joins) {
        super(name, description, TransformationType.JOIN);
        this.schemaOutId = schemaOutId;
        this.selects = selects;
        this.joins = joins;
    }
}
