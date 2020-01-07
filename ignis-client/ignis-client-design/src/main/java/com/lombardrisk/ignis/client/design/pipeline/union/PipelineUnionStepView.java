package com.lombardrisk.ignis.client.design.pipeline.union;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.lombardrisk.ignis.client.design.pipeline.PipelineStepView;
import com.lombardrisk.ignis.client.external.pipeline.export.TransformationType;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
public class PipelineUnionStepView extends PipelineStepView {

    private final Map<Long, UnionView> unions;
    private final Long schemaOutId;

    @JsonCreator
    @Builder
    public PipelineUnionStepView(
            @JsonProperty("id") final Long id,
            @JsonProperty("name") final String name,
            @JsonProperty("description") final String description,
            @JsonProperty("unions") final Map<Long, UnionView> unions,
            @JsonProperty("schemaOutId") final Long schemaOutId) {
        super(id, name, description, TransformationType.UNION);
        this.unions = unions;
        this.schemaOutId = schemaOutId;
    }
}
