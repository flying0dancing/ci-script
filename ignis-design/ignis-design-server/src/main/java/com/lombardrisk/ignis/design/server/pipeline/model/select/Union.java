package com.lombardrisk.ignis.design.server.pipeline.model.select;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Data
@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Union {
    @Column(name = "UNION_INPUT_SCHEMA_ID")
    private Long unionSchemaId;

    public static Union forSchema(final Long schemaId) {
        return Union.builder().unionSchemaId(schemaId).build();
    }
}
