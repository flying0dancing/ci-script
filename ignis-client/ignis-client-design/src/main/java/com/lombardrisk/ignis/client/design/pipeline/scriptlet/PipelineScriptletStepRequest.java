package com.lombardrisk.ignis.client.design.pipeline.scriptlet;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.lombardrisk.ignis.client.design.pipeline.PipelineStepRequest;
import com.lombardrisk.ignis.client.external.pipeline.export.TransformationType;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Data
public class PipelineScriptletStepRequest extends PipelineStepRequest {

    private final String jarFile;
    private final String javaClass;
    private Set<ScriptletInputRequest> scriptletInputs;
    private Long schemaOutId;

    @JsonCreator
    @Builder
    public PipelineScriptletStepRequest(
            @JsonProperty("name") final String name,
            @JsonProperty("description") final String description,
            @JsonProperty("scriptletInputs") final Set<ScriptletInputRequest> scriptletInputs,
            @JsonProperty("schemaOutId") final Long schemaOutId,
            @JsonProperty("jarName") final String jarFile,
            @JsonProperty("className") final String javaClass) {
        super(name, description, TransformationType.SCRIPTLET);

        this.scriptletInputs = scriptletInputs;
        this.schemaOutId = schemaOutId;
        this.jarFile = jarFile;
        this.javaClass = javaClass;
    }
}