package com.lombardrisk.ignis.server.batch.product.datasource;

import com.lombardrisk.ignis.server.product.table.model.Field;
import com.lombardrisk.ignis.server.product.table.model.Table;

import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.joining;

public interface MigrationTableService {

    boolean isPhysicalSchemaPresent(String physicalSchemaName);

    default boolean isPhysicalSchemaMissing(final String physicalSchemaName) {
        return !isPhysicalSchemaPresent(physicalSchemaName);
    }

    void createTable(Table schema);

    void addColumnsToTable(String tableName, final List<String> columnDefs);

    Set<String> findPhysicalColumns(String physicalTableName);

    static String toColumnsDefSql(final Set<Field> fields) {
        return fields.stream()
                .map(Field::toColumnDef)
                .collect(joining(","));
    }
}
