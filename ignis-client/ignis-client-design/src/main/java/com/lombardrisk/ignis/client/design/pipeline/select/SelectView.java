package com.lombardrisk.ignis.client.design.pipeline.select;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor(onConstructor = @__({ @JsonCreator }))
@JsonInclude
public class SelectView {

    private final String select;
    private final Long outputFieldId;
    private final Long order;
    private final boolean intermediateResult;
    private final boolean hasWindow;
    private final WindowView window;
}

