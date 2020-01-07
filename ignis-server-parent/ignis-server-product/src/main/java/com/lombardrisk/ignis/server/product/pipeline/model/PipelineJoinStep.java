package com.lombardrisk.ignis.server.product.pipeline.model;

import com.lombardrisk.ignis.server.product.pipeline.details.SchemaDetails;
import com.lombardrisk.ignis.server.product.pipeline.select.Select;
import com.lombardrisk.ignis.server.product.pipeline.transformation.Join;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.Valid;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@Entity
@Table(name = "PIPELINE_JOIN_STEP")
@NoArgsConstructor
@DiscriminatorValue(value = "JOIN")
public class PipelineJoinStep extends PipelineStep {

    @Column(name = "SCHEMA_OUT_ID")
    private Long schemaOutId;

    @Valid
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "PIPELINE_STEP_ID", nullable = false)
    private Set<Join> joins = new LinkedHashSet<>();

    @ManyToOne
    @JoinColumn(name = "SCHEMA_OUT_ID", insertable = false, updatable = false)
    private SchemaDetails schemaOut;

    @Builder
    public PipelineJoinStep(
            final Long id,
            final Pipeline pipeline,
            final String name,
            final String description,
            final Set<Select> selects,
            final Long schemaOutId,
            final Set<Join> joins,
            final SchemaDetails schemaOut) {
        super(id, pipeline, name, description, selects, Collections.emptySet(), TransformationType.JOIN);
        this.schemaOutId = schemaOutId;
        this.joins = joins;
        this.schemaOut = schemaOut;
    }

    public Set<Long> findInputSchemas() {
        return joins.stream()
                .flatMap(join -> Stream.of(join.getLeftSchemaId(), join.getRightSchemaId()))
                .collect(toSet());
    }

    @Override
    public Set<String> getFilters() {
        return Collections.emptySet();
    }

    @Override
    public Set<SchemaDetails> getInputs() {
        return joins.stream()
                .flatMap(join -> Stream.of(join.getLeftSchema(), join.getRightSchema()))
                .collect(toSet());
    }

    @Override
    public SchemaDetails getOutput() {
        return schemaOut;
    }
}
