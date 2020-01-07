package com.lombardrisk.ignis.server.product.pipeline.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lombardrisk.ignis.common.MapperUtils;
import com.lombardrisk.ignis.server.product.pipeline.details.SchemaDetails;
import com.lombardrisk.ignis.server.product.pipeline.select.PipelineFilter;
import com.lombardrisk.ignis.server.product.pipeline.select.Select;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Set;

import static java.util.Collections.singleton;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@Entity
@Table(name = "PIPELINE_MAP_STEP")
@NoArgsConstructor
@DiscriminatorValue(value = "MAP")
public class PipelineMapStep extends PipelineStep {

    @Column(name = "SCHEMA_IN_ID")
    private Long schemaInId;

    @Column(name = "SCHEMA_OUT_ID")
    private Long schemaOutId;

    @ManyToOne
    @JoinColumn(name = "SCHEMA_IN_ID", insertable = false, updatable = false)
    @JsonIgnore
    private SchemaDetails schemaIn;

    @ManyToOne
    @JoinColumn(name = "SCHEMA_OUT_ID", insertable = false, updatable = false)
    @JsonIgnore
    private SchemaDetails schemaOut;

    @Builder
    public PipelineMapStep(
            final Long id,
            final Pipeline pipeline,
            final String name,
            final String description,
            final Set<Select> selects,
            final Long schemaInId,
            final Long schemaOutId,
            final Set<String> filters,
            final SchemaDetails schemaIn,
            final SchemaDetails schemaOut) {
        super(id, pipeline, name, description, selects,
                MapperUtils.mapOrEmptySet(filters, filter -> PipelineFilter.builder()
                        .filter(filter)
                        .build()), TransformationType.MAP);
        this.schemaInId = schemaInId;
        this.schemaOutId = schemaOutId;
        this.schemaIn = schemaIn;
        this.schemaOut = schemaOut;
    }

    @Override
    public Set<SchemaDetails> getInputs() {
        return singleton(schemaIn);
    }

    @Override
    public SchemaDetails getOutput() {
        return schemaOut;
    }
}
