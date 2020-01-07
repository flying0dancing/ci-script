package com.lombardrisk.ignis.server.product.pipeline.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lombardrisk.ignis.server.product.pipeline.details.SchemaDetails;
import com.lombardrisk.ignis.server.product.pipeline.select.PipelineFilter;
import com.lombardrisk.ignis.server.product.pipeline.select.Select;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Set;

import static com.lombardrisk.ignis.server.product.pipeline.model.TransformationType.UNION;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "PIPELINE_UNION_STEP")
@NoArgsConstructor
@DiscriminatorValue(value = "UNION")
public class PipelineUnionStep extends PipelineStep {

    @Column(name = "SCHEMA_IN_ID")
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "PIPELINE_STEP_UNION", joinColumns = {
            @JoinColumn(name = "PIPELINE_STEP_ID", referencedColumnName = "ID") })
    private Set<Long> schemaInIds;

    @Column(name = "SCHEMA_IN_ID")
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "PIPELINE_STEP_UNION",
            joinColumns = @JoinColumn(name = "PIPELINE_STEP_ID", referencedColumnName = "ID", insertable = false, updatable = false),
            inverseJoinColumns = @JoinColumn(name = "SCHEMA_IN_ID", referencedColumnName = "ID", insertable = false, updatable = false))
    private Set<SchemaDetails> schemasIn;

    @Column(name = "SCHEMA_OUT_ID")
    private Long schemaOutId;

    @ManyToOne
    @JoinColumn(name = "SCHEMA_OUT_ID", insertable = false, updatable = false)
    @JsonIgnore
    private SchemaDetails schemaOut;

    @Builder
    public PipelineUnionStep(
            final Long id,
            final Pipeline pipeline,
            final String name,
            final String description,
            final Set<Long> schemaInIds,
            final Long schemaOutId,
            final Set<Select> selects,
            final Set<PipelineFilter> filters,
            final Set<SchemaDetails> schemasIn,
            final SchemaDetails schemaOut) {
        super(id, pipeline, name, description, selects, filters, UNION);
        this.schemaInIds = schemaInIds;
        this.schemasIn = schemasIn;
        this.schemaOutId = schemaOutId;
        this.schemaOut = schemaOut;
    }

    @Override
    public Set<SchemaDetails> getInputs() {
        return schemasIn;
    }

    @Override
    public SchemaDetails getOutput() {
        return schemaOut;
    }
}
