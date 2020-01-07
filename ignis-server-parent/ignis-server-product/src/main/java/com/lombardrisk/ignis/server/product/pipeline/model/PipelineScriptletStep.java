package com.lombardrisk.ignis.server.product.pipeline.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lombardrisk.ignis.server.product.pipeline.details.SchemaDetails;
import com.lombardrisk.ignis.server.product.pipeline.transformation.ScriptletInput;
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
import javax.persistence.ManyToOne;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SCHEMA_OUT_ID", insertable = false, updatable = false)
    @JsonIgnore
    private SchemaDetails schemaOut;

    @Column(name = "JAR_FILE")
    private String jarFile;

    @Column(name = "CLASS_NAME")
    private String className;

    @Builder
    public PipelineScriptletStep(
            final Long id,
            final Pipeline pipeline,
            final String name,
            final String description,
            final Set<ScriptletInput> schemaIns,
            final Long schemaOutId,
            final SchemaDetails schemaOut,
            final String jarFile,
            final String className) {
        super(id, pipeline, name, description, null, null, TransformationType.SCRIPTLET);
        this.schemaIns = schemaIns;
        this.schemaOutId = schemaOutId;
        this.schemaOut = schemaOut;
        this.jarFile = jarFile;
        this.className = className;
    }

    @Override
    public Set<SchemaDetails> getInputs() {
        return schemaIns.stream()
                .map(ScriptletInput::getSchemaIn)
                .collect(toSet());
    }

    @Override
    public SchemaDetails getOutput() {
        return schemaOut;
    }
}
