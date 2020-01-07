package com.lombardrisk.ignis.pipeline.step.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JoinAppConfig {

    private String leftSchemaName;
    private String rightSchemaName;

    private List<JoinFieldConfig> joinFields;

    private JoinType joinType;

    @AllArgsConstructor
    @Getter
    public enum JoinType {
        INNER("INNER"),
        LEFT("LEFT"),
        FULL_OUTER("FULL OUTER");

        private final String sqlSyntax;
    }
}
