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
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Data
public class PipelineJoinStepExport extends PipelineStepExport {

    @NotNull(message = "output schema cannot be null")
    @Valid
    private SchemaReference schemaOut;

    @NotEmpty(message = "transformation is not defined")
    private List<SelectExport> selects;

    private List<@Valid JoinExport> joins;

    @Builder
    public PipelineJoinStepExport(
            final String name,
            final String description,
            final TransformationType type,
            final SchemaReference schemaOut,
            final List<SelectExport> selects,
            final List<JoinExport> joins) {
        super(name, description, TransformationType.JOIN);
        this.schemaOut = schemaOut;
        this.selects = selects;
        this.joins = joins;
    }

    @Override
    @JsonIgnore
    public Set<SchemaReference> getInputSchemas() {
        return joins.stream()
                .flatMap(join -> Stream.of(join.getLeft(), join.getRight()))
                .collect(toSet());
    }

    @Override
    @JsonIgnore
    public SchemaReference getOutputSchema() {
        return schemaOut;
    }

    @Override
    @JsonIgnore
    public List<SelectExport> getOutputFieldSelects() {
        return selects;
    }
}
