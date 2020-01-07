package com.lombardrisk.ignis.client.external.pipeline.export.step;

import com.lombardrisk.ignis.client.external.pipeline.export.SchemaReference;
import com.lombardrisk.ignis.client.external.pipeline.export.TransformationType;
import com.lombardrisk.ignis.client.external.pipeline.export.step.select.SelectExport;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Data
public class PipelineScriptletStepExport extends PipelineStepExport {

    @NotNull(message = "jar file cannot be null")
    private String jarFile;

    @NotNull(message = "class name cannot be null")
    private String className;

    @NotEmpty(message = "input schemas cannot be empty")
    private List<@Valid ScriptletInputExport> schemasIn;

    @NotNull(message = "output schema cannot be null")
    @Valid
    private SchemaReference schemaOut;

    @Builder
    public PipelineScriptletStepExport(
            final String name,
            final String description,
            final String jarFile,
            final String className,
            final List<ScriptletInputExport> schemasIn,
            final SchemaReference schemaOut) {
        super(name, description, TransformationType.SCRIPTLET);
        this.jarFile = jarFile;
        this.className = className;
        this.schemasIn = schemasIn;
        this.schemaOut = schemaOut;
    }

    @Override
    public Set<SchemaReference> getInputSchemas() {
        return schemasIn.stream()
                .map(ScriptletInputExport::getInputSchema)
                .collect(toSet());
    }

    @Override
    public SchemaReference getOutputSchema() {
        return schemaOut;
    }

    @Override
    public List<SelectExport> getOutputFieldSelects() {
        return null;
    }
}
