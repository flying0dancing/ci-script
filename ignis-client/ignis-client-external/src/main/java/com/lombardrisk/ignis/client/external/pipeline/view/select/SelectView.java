package com.lombardrisk.ignis.client.external.pipeline.view.select;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor(onConstructor = @__({ @JsonCreator }))
public class SelectView {

    private final Long id;
    private final String select;
    private final Long outputFieldId;
    private final Boolean isWindow;
    private final WindowView window;
}

