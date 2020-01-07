package com.lombardrisk.ignis.client.design.pipeline.test.view;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class StepTestRowView {
    private final Long id;
    private final List<CellIdAndField> cells;

    @AllArgsConstructor
    @Builder
    @Data
    public static class CellIdAndField {
        private final Long id;
        private final String cellData;
        private final Long fieldId;
    }
}
