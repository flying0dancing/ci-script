package com.lombardrisk.ignis.server.product.productconfig.model;

import com.google.common.collect.ImmutableList;
import com.lombardrisk.ignis.client.external.productconfig.export.SchemaPeriod;
import com.lombardrisk.ignis.data.common.Versionable;
import com.lombardrisk.ignis.server.product.table.model.Field;
import com.lombardrisk.ignis.server.product.table.model.Table;
import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Data
@Builder
public class ProductImportDiff implements Versionable<String> {

    private final String productName;
    private final String productVersion;

    private final Set<Table> newSchemas;
    private final Set<Table> existingSchemas;
    private final Set<Table> newVersionedSchemas;

    private final Map<Table, SchemaPeriod> existingSchemaToNewPeriod;

    @Override
    public String getName() {
        return productName;
    }

    @Override
    public String getVersion() {
        return productVersion;
    }

    public Map<String, Map<String, Field>> physicalTableNameToFieldNameToField() {
        Map<String,  Map<String, Field>> physicalTableNameToFields = new HashMap<>();

        for (Table productSchema : ImmutableList.<Table>builder()
                .addAll(existingSchemas)
                .addAll(newSchemas)
                .build()) {

            String physicalTableName = productSchema.getPhysicalTableName();
            physicalTableNameToFields.putIfAbsent(physicalTableName, new HashMap<>());

            Map<String, Field> fieldNameToFields = physicalTableNameToFields.get(physicalTableName);
            productSchema.getFields().forEach(field -> fieldNameToFields.put(field.getName(), field));
        }


        return physicalTableNameToFields;
    }
}

