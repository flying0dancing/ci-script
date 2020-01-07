package com.lombardrisk.ignis.client.design.pipeline.select;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class OrderRequest {

    private final String fieldName;
    private final OrderDirection direction;
    private final int priority;
}
