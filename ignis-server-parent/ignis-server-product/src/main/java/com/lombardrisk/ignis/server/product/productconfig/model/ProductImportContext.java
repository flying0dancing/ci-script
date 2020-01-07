package com.lombardrisk.ignis.server.product.productconfig.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.lombardrisk.ignis.client.external.productconfig.export.SchemaPeriod;
import com.lombardrisk.ignis.pipeline.step.api.DrillbackColumnLink;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Map;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor(onConstructor = @__({ @JsonCreator }))
public class ProductImportContext {

    private final Map<String, Long> newSchemaNameToId;
    private final Map<String, Map<String, Long>> schemaNameToNewFields;
    private final Map<String, Set<DrillbackColumnLink>> schemaNameToDrillBackColumns;
    private final Map<Long, SchemaPeriod> existingSchemaIdToNewPeriod;
    private final Map<Long, SchemaPeriod> existingSchemaIdToOldPeriod;
}

