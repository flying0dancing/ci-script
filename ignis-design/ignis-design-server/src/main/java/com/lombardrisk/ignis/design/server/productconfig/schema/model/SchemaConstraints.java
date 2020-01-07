package com.lombardrisk.ignis.design.server.productconfig.schema.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;

@Value
@Builder
@AllArgsConstructor
public class SchemaConstraints {

    private final String physicalTableName;
    private final String displayName;
    private final Integer majorVersion;
    private final LocalDate startDate;
    private final LocalDate endDate;
}
