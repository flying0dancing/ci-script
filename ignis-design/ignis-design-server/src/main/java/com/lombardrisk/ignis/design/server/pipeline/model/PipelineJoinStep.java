package com.lombardrisk.ignis.design.server.pipeline.model;

import com.lombardrisk.ignis.client.external.pipeline.export.TransformationType;
import com.lombardrisk.ignis.design.server.pipeline.model.join.Join;
import com.lombardrisk.ignis.design.server.pipeline.model.select.Select;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.validation.Valid;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "PIPELINE_JOIN_STEP")
@NoArgsConstructor
@DiscriminatorValue(value = "JOIN")
public class PipelineJoinStep extends PipelineStep {

    @Column(name = "SCHEMA_OUT_ID")
    private Long schemaOutId;

    @Valid
    @OrderBy("id ASC")
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "PIPELINE_STEP_ID", nullable = false)
    private Set<Join> joins = new LinkedHashSet<>();

    @Builder
    public PipelineJoinStep(
            final Long id,
            final Long pipelineId,
            final String name,
            final String description,
            final Set<Select> selects,
            final Long schemaOutId,
            final Set<Join> joins) {
        super(id, pipelineId, name, description, selects, Collections.emptySet(), TransformationType.JOIN);
        this.schemaOutId = schemaOutId;
        this.joins = joins;
    }

    public Stream<Long> allInputSchemaIds() {
        return joins.stream()
                .flatMap(join -> Stream.of(join.getLeftSchemaId(), join.getRightSchemaId()));
    }

    @Override
    public Set<Long> getInputs() {
        return allInputSchemaIds().collect(toSet());
    }

    @Override
    public Long getOutput() {
        return schemaOutId;
    }
}
