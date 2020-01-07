package com.lombardrisk.ignis.design.server.pipeline.model;

import com.lombardrisk.ignis.client.external.pipeline.export.TransformationType;
import com.lombardrisk.ignis.common.MapperUtils;
import com.lombardrisk.ignis.design.server.pipeline.model.select.PipelineFilter;
import com.lombardrisk.ignis.design.server.pipeline.model.select.Select;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Set;

import static java.util.Collections.singleton;

@EqualsAndHashCode(callSuper = true)
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

    @Builder
    public PipelineMapStep(
            final Long id,
            final Long pipelineId,
            final String name,
            final String description,
            final Set<Select> selects,
            final Long schemaInId,
            final Long schemaOutId,
            final Set<String> filters) {
        super(id, pipelineId, name, description, selects,
                MapperUtils.mapOrEmptySet(filters, filter -> PipelineFilter.builder()
                        .filter(filter)
                        .build()),
                TransformationType.MAP);
        this.schemaInId = schemaInId;
        this.schemaOutId = schemaOutId;
    }

    @Override
    public Set<Long> getInputs() {
        return singleton(schemaInId);
    }

    @Override
    public Long getOutput() {
        return schemaOutId;
    }

}
