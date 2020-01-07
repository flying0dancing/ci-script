package com.lombardrisk.ignis.server.product.pipeline.select;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lombardrisk.ignis.server.product.pipeline.details.SchemaDetails;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Data
@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Union {
    @Column(name = "UNION_INPUT_SCHEMA_ID")
    private Long unionSchemaId;

    @ManyToOne
    @JoinColumn(name = "UNION_INPUT_SCHEMA_ID", insertable = false, updatable = false)
    @JsonIgnore
    private SchemaDetails unionSchema;

    public static Union forSchema(final Long schemaId) {
        return Union.builder().unionSchemaId(schemaId).build();
    }
}
