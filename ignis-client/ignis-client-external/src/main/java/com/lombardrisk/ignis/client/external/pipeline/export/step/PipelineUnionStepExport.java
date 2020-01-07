package com.lombardrisk.ignis.client.external.pipeline.export.step;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Data
public class PipelineUnionStepExport extends PipelineStepExport {

    @NotNull(message = "output schema cannot be null")
    @Valid
    private SchemaReference schemaOut;

    @NotEmpty(message = "union is not defined")
    private List<UnionExport> unions;

    @Builder
    public PipelineUnionStepExport(
            @NotEmpty(message = "step name cannot be empty") final String name,
            final String description,
            @NotNull(message = "output schema cannot be null") @Valid final SchemaReference schemaOut,
            @NotEmpty(message = "union is not defined") final List<UnionExport> unions) {
        super(name, description, TransformationType.UNION);
        this.schemaOut = schemaOut;
        this.unions = unions;
    }

    @Override
    @JsonIgnore
    public Set<SchemaReference> getInputSchemas() {
        return unions.stream().map(UnionExport::getUnionInSchema).collect(toSet());
    }

    @Override
    @JsonIgnore
    public SchemaReference getOutputSchema() {
        return schemaOut;
    }

    @Override
    @JsonIgnore
    public List<SelectExport> getOutputFieldSelects() {
        return unions.stream().flatMap(union -> union.getSelects().stream()).collect(toList());
    }
}


