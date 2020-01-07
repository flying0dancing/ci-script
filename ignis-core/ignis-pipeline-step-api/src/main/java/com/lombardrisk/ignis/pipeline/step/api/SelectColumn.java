package com.lombardrisk.ignis.pipeline.step.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SelectColumn {

    private String select;
    private String as;
    private boolean intermediateResult;
    private WindowSpec over;
    private UnionSpec union;

    public Long unionSchemaId() {
        if (union == null) {
            return null;
        }

        return union.getSchemaId();
    }
}
