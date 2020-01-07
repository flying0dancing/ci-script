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

import static java.util.Collections.singleton;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Data
public class PipelineAggregationStepExport extends PipelineStepExport {

    @NotNull(message = "input schema cannot be null")
    @Valid
    private SchemaReference schemaIn;

    @NotNull(message = "output schema cannot be null")
    @Valid
    private SchemaReference schemaOut;

    @NotEmpty(message = "transformation is not defined")
    private List<SelectExport> selects;

    private List<String> filters;

    private List<String> groupings;

    @Builder
    public PipelineAggregationStepExport(
            final String name,
            final String description,
            final SchemaReference schemaIn,
            final SchemaReference schemaOut,
            final List<SelectExport> selects,
            final List<String> filters,
            final List<String> groupings) {
        super(name, description, TransformationType.AGGREGATION);
        this.schemaIn = schemaIn;
        this.schemaOut = schemaOut;
        this.selects = selects;
        this.filters = filters;
        this.groupings = groupings;
    }

    @Override
    @JsonIgnore
    public Set<SchemaReference> getInputSchemas() {
        return singleton(schemaIn);
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
