package com.lombardrisk.ignis.client.external.pipeline.export.step.select;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class OrderExport {

    private String fieldName;
    private String direction;
    private int priority;
}
