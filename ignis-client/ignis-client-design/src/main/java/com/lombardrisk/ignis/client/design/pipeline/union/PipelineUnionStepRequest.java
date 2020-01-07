package com.lombardrisk.ignis.client.design.pipeline.union;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.lombardrisk.ignis.client.design.pipeline.PipelineStepRequest;
import com.lombardrisk.ignis.client.design.pipeline.select.UnionRequest;
import com.lombardrisk.ignis.client.external.pipeline.export.TransformationType;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
public class PipelineUnionStepRequest extends PipelineStepRequest {

    @Size(min = 2)
    @Valid
    private final Map<Long, @Valid UnionRequest> unionSchemas;
    private final Long schemaOutId;

    @JsonCreator
    @Builder
    public PipelineUnionStepRequest(
            @JsonProperty("name") final String name,
            @JsonProperty("description") final String description,
            @JsonProperty("unions") @Size(min = 2) @Valid final Map<Long, @Valid UnionRequest> unionSchemas,
            @JsonProperty("schemaOutId") final Long schemaOutId) {
        super(name, description, TransformationType.UNION);

        this.unionSchemas = unionSchemas;
        this.schemaOutId = schemaOutId;
    }
}
