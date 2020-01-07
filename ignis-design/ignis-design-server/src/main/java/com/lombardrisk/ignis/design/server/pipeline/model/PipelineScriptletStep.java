package com.lombardrisk.ignis.design.server.pipeline.model;

import com.lombardrisk.ignis.client.external.pipeline.export.TransformationType;
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
import javax.persistence.Table;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "PIPELINE_SCRIPTLET_STEP")
@NoArgsConstructor
@DiscriminatorValue(value = "SCRIPTLET")
public class PipelineScriptletStep extends PipelineStep {

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "PIPELINE_STEP_ID", nullable = false)
    private Set<ScriptletInput> schemaIns;

    @Column(name = "SCHEMA_OUT_ID")
    private Long schemaOutId;

    @Column(name = "JAR_FILE")
    private String jarFile;

    @Column(name = "CLASS_NAME")
    private String className;

    @Builder
    public PipelineScriptletStep(
            final Long id,
            final Long pipeline,
            final String name,
            final String description,
            final Set<ScriptletInput> schemaIns,
            final Long schemaOutId,
            final String jarFile,
            final String className) {
        super(id, pipeline, name, description, null, null, TransformationType.SCRIPTLET);
        this.schemaIns = schemaIns;
        this.schemaOutId = schemaOutId;
        this.jarFile = jarFile;
        this.className = className;
    }

    @Override
    public Set<Long> getInputs() {
        return schemaIns.stream()
                .map(ScriptletInput::getSchemaInId)
                .collect(toSet());
    }

    @Override
    public Long getOutput() {
        return schemaOutId;
    }
}