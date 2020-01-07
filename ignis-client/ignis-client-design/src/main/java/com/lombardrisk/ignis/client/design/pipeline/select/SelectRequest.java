package com.lombardrisk.ignis.client.design.pipeline.select;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Builder
@AllArgsConstructor
public class SelectRequest {

    @NotNull
    private final String select;
    @NotNull
    private final Long outputFieldId;
    private final Long order;
    private final boolean intermediateResult;
    private final boolean hasWindow;
    private final WindowRequest window;
}
