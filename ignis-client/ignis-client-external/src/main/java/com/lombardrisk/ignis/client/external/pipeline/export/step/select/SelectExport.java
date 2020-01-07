package com.lombardrisk.ignis.client.external.pipeline.export.step.select;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class SelectExport {

    private String select;
    private String outputFieldName;
    private Long order;
    private boolean intermediateResult;
    private Boolean isWindow;
    private WindowExport window;
}
