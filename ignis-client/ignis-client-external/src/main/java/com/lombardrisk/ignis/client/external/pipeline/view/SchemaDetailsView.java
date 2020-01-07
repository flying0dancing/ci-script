package com.lombardrisk.ignis.client.external.pipeline.view;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class SchemaDetailsView {

    private final Long id;
    private final String displayName;
    private final String physicalTableName;
    private final Integer version;
}
