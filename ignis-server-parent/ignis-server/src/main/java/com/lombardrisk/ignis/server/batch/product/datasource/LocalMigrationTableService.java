package com.lombardrisk.ignis.server.batch.product.datasource;

import com.lombardrisk.ignis.server.product.table.model.Table;
import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Set;

import static com.google.common.collect.Sets.newLinkedHashSet;
import static com.lombardrisk.ignis.pipeline.step.api.DatasetFieldName.ROW_KEY;

@AllArgsConstructor
public class LocalMigrationTableService implements MigrationTableService {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public boolean isPhysicalSchemaPresent(final String physicalSchemaName) {
        List<String> existingPhysicalSchema = jdbcTemplate.queryForList(
                "select TABLE_NAME from INFORMATION_SCHEMA.TABLES "
                        + "where TABLE_NAME = '" + physicalSchemaName + "' "
                        + "limit 1",
                String.class);

        return !existingPhysicalSchema.isEmpty();
    }

    @Override
    public void createTable(final Table schema) {
        String createTableSql =
                "create table " + schema.getPhysicalTableName() + " ("
                        + ROW_KEY.getName() + " BIGINT not null primary key, "
                        + MigrationTableService.toColumnsDefSql(schema.getFields())
                        + ") ";

        jdbcTemplate.execute(createTableSql);
    }

    @Override
    public void addColumnsToTable(final String tableName, final List<String> columnDefs) {
        String addNewFieldsSql = "ALTER TABLE " + tableName + " ADD (" + String.join("," ,columnDefs) + ")";
        jdbcTemplate.execute(addNewFieldsSql);
    }

    @Override
    public Set<String> findPhysicalColumns(final String physicalTableName) {
        List<String> existingPhysicalSchema = jdbcTemplate.queryForList(
                "select COLUMN_NAME from INFORMATION_SCHEMA.COLUMNS "
                        + "where TABLE_NAME = '" + physicalTableName + "' ",
                String.class);

        return newLinkedHashSet(existingPhysicalSchema);
    }
}
