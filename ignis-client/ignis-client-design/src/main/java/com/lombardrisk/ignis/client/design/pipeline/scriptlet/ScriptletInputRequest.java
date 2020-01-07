package com.lombardrisk.ignis.client.design.pipeline.scriptlet;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
@Builder
@AllArgsConstructor(onConstructor = @__({ @JsonCreator }))
public class ScriptletInputRequest {

    private final Long schemaInId;
    @NotNull
    @Valid
    private String metadataInput;
}