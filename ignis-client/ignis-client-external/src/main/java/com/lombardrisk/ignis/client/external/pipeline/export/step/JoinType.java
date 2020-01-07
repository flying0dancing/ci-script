package com.lombardrisk.ignis.client.external.pipeline.export.step;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum JoinType {
    INNER("INNER"),
    LEFT("LEFT"),
    FULL_OUTER("FULL OUTER");

    private final String sqlSyntax;
}