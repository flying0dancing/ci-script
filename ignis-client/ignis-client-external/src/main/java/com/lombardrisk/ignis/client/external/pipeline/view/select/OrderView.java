package com.lombardrisk.ignis.client.external.pipeline.view.select;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor(onConstructor = @__({ @JsonCreator }))
public class OrderView {

    private final String fieldName;
    private final OrderDirection direction;
    private final int priority;
}
