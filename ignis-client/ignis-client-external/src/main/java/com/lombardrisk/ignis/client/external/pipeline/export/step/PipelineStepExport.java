package com.lombardrisk.ignis.client.external.pipeline.export.step;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.lombardrisk.ignis.client.external.pipeline.export.SchemaReference;
import com.lombardrisk.ignis.client.external.pipeline.export.TransformationType;
import com.lombardrisk.ignis.client.external.pipeline.export.step.select.SelectExport;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = PipelineMapStepExport.class, name = "MAP"),
        @JsonSubTypes.Type(value = PipelineAggregationStepExport.class, name = "AGGREGATION"),
        @JsonSubTypes.Type(value = PipelineJoinStepExport.class, name = "JOIN"),
        @JsonSubTypes.Type(value = PipelineWindowStepExport.class, name = "WINDOW"),
        @JsonSubTypes.Type(value = PipelineUnionStepExport.class, name = "UNION"),
        @JsonSubTypes.Type(value = PipelineScriptletStepExport.class, name = "SCRIPTLET"),
})
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class PipelineStepExport {

    @NotEmpty(message = "step name cannot be empty")
    private String name;

    private String description;

    @NotNull(message = "transformation type is not defined")
    private TransformationType type;

    @JsonIgnore
    public abstract Set<SchemaReference> getInputSchemas();

    @JsonIgnore
    public abstract SchemaReference getOutputSchema();

    @JsonIgnore
    public abstract List<SelectExport> getOutputFieldSelects();
}
