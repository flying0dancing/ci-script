package com.lombardrisk.ignis.design.server.pipeline.model;

import com.lombardrisk.ignis.client.external.pipeline.export.TransformationType;
import com.lombardrisk.ignis.design.server.pipeline.model.select.PipelineFilter;
import com.lombardrisk.ignis.design.server.pipeline.model.select.Select;
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
import javax.persistence.Table;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "PIPELINE_UNION_STEP")
@NoArgsConstructor
@DiscriminatorValue(value = "UNION")
public class PipelineUnionStep extends PipelineStep {

    @Column(name = "SCHEMA_IN_ID")
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "PIPELINE_STEP_UNION", joinColumns = {
            @JoinColumn(name = "PIPELINE_STEP_ID", referencedColumnName = "ID") })
    private Set<Long> schemaInIds;

    @Column(name = "SCHEMA_OUT_ID")
    private Long schemaOutId;

    @Builder
    public PipelineUnionStep(
            final Long id,
            final Long pipelineId,
            final String name,
            final String description,
            final Set<Select> selects,
            final Set<Long> schemaInIds,
            final Long schemaOutId,
            final Set<PipelineFilter> filters) {
        super(id, pipelineId, name, description, selects, filters, TransformationType.UNION);
        this.schemaInIds = schemaInIds;
        this.schemaOutId = schemaOutId;
    }

    @Override
    public Set<Long> getInputs() {
        return schemaInIds;
    }

    @Override
    public Long getOutput() {
        return schemaOutId;
    }
}
