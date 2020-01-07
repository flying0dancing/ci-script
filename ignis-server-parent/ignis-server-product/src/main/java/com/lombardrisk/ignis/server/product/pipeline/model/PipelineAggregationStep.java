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

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.LinkedHashSet;
import java.util.Set;

import static java.util.Collections.singleton;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@Entity
@Table(name = "PIPELINE_AGGREGATION_STEP")
@NoArgsConstructor
@DiscriminatorValue(value = "AGGREGATION")
public class PipelineAggregationStep extends PipelineStep {

    @Column(name = "SCHEMA_IN_ID")
    private Long schemaInId;

    @Column(name = "SCHEMA_OUT_ID")
    private Long schemaOutId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "PIPELINE_STEP_GROUPING", joinColumns = { @JoinColumn(name = "PIPELINE_STEP_ID", referencedColumnName = "ID") })
    @Column(name = "GROUPING")
    private Set<String> groupings = new LinkedHashSet<>();

    @ManyToOne
    @JoinColumn(name = "SCHEMA_IN_ID", insertable = false, updatable = false)
    @JsonIgnore
    private SchemaDetails schemaIn;

    @ManyToOne
    @JoinColumn(name = "SCHEMA_OUT_ID", insertable = false, updatable = false)
    @JsonIgnore
    private SchemaDetails schemaOut;

    @Builder
    public PipelineAggregationStep(
            final Long id,
            final Pipeline pipeline,
            final String name,
            final String description,
            final Set<Select> selects,
            final Long schemaInId,
            final Long schemaOutId,
            final Set<String> filters,
            final Set<String> groupings,
            final SchemaDetails schemaIn,
            final SchemaDetails schemaOut) {
        super(id, pipeline, name, description, selects,
                MapperUtils.mapOrEmptySet(filters, filter -> PipelineFilter.builder()
                        .filter(filter)
                        .build()), TransformationType.AGGREGATION);
        this.schemaInId = schemaInId;
        this.schemaOutId = schemaOutId;
        this.groupings = groupings;
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
