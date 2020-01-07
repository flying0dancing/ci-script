package com.lombardrisk.ignis.client.design.pipeline.select;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
public class WindowRequest {

    private final Set<String> partitionBy;
    private final List<OrderRequest> orderBy;
}
