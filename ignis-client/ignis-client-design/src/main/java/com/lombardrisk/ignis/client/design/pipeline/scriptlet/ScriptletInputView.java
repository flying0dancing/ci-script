package com.lombardrisk.ignis.client.design.pipeline.scriptlet;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
public class ScriptletInputView {

    private final String metadataInput;
    private final Long schemaInId;

    @Builder
    @JsonCreator
    public ScriptletInputView(
            @JsonProperty("metadataInput") final String metadataInput,
            @JsonProperty("schemaInId") final Long schemaInId) {
        this.metadataInput = metadataInput;
        this.schemaInId = schemaInId;
    }
}
