package com.lombardrisk.ignis.pipeline.step.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor(onConstructor = @__({ @JsonCreator }))
public class DrillbackColumnLink {

    private static final String FCR_SYSTEM_PREFIX = "FCR_SYS__";

    private final String inputSchema;
    private final String outputSchema;
    private final String inputColumn;
    private final String inputColumnSqlType;

    public String toDrillbackColumn() {
        return FCR_SYSTEM_PREFIX + inputSchema + "__" + inputColumn;
    }
}
