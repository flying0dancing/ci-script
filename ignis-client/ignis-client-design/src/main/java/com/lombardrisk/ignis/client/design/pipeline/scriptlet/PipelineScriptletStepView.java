package com.lombardrisk.ignis.client.design.pipeline.scriptlet;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.lombardrisk.ignis.client.design.pipeline.PipelineStepView;
import com.lombardrisk.ignis.client.external.pipeline.export.TransformationType;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class PipelineScriptletStepView extends PipelineStepView {

    private final String jarFile;
    private final String javaClass;
    private final Set<ScriptletInputView> scriptletInputs;
    private final Long schemaOutId;

    @Builder
    @JsonCreator
    public PipelineScriptletStepView(
            @JsonProperty("id") final Long id,
            @JsonProperty("name") final String name,
            @JsonProperty("description") final String description,
            @JsonProperty("scriptletInputs") final Set<ScriptletInputView> scriptletInputs,
            @JsonProperty("schemaOutId") final Long schemaOutId,
            @JsonProperty("jarName") final String jarFile,
            @JsonProperty("className") final String javaClass) {
        super(id, name, description, TransformationType.SCRIPTLET);
        this.jarFile = jarFile;
        this.javaClass = javaClass;
        this.scriptletInputs = scriptletInputs;
        this.schemaOutId = schemaOutId;
    }
}