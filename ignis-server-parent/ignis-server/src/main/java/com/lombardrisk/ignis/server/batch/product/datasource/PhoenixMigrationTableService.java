package com.lombardrisk.ignis.server.batch.product.datasource;

import com.lombardrisk.ignis.server.product.table.model.Table;
import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

import static com.google.common.collect.Sets.newLinkedHashSet;
import static com.lombardrisk.ignis.pipeline.step.api.DatasetFieldName.ROW_KEY;
import static org.apache.commons.lang3.StringUtils.EMPTY;

@AllArgsConstructor
public class PhoenixMigrationTableService implements MigrationTableService {

    private final JdbcTemplate phoenixJdbcTemplate;
    private final int saltBuckets;

    @Override
    public boolean isPhysicalSchemaPresent(final String physicalSchemaName) {
        List<String> existingPhysicalSchema = phoenixJdbcTemplate.queryForList(
                "select TABLE_NAME from SYSTEM.CATALOG "
                        + "where TABLE_NAME = '" + physicalSchemaName + "' "
                        + "limit 1",
                String.class);

        return !existingPhysicalSchema.isEmpty();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createTable(final Table schema) {
        String saltedBucketsAttribute = saltBuckets > 0 ? "SALT_BUCKETS=" + saltBuckets : EMPTY;

        String createTableSql =
                "create table " + schema.getPhysicalTableName() + " ("
                        + ROW_KEY.getName() + " BIGINT not null primary key, "
                        + MigrationTableService.toColumnsDefSql(schema.getFields())
                        + ") " + saltedBucketsAttribute;

        phoenixJdbcTemplate.execute(createTableSql);
    }

    @Override
    public void addColumnsToTable(final String tableName, final List<String> columnDefs) {
        String addNewFieldsSql = "ALTER TABLE " + tableName + " ADD " + String.join("," ,columnDefs) + "";
        phoenixJdbcTemplate.execute(addNewFieldsSql);
    }

    @Override
    public Set<String> findPhysicalColumns(final String physicalTableName) {
        List<String> existingPhysicalSchema = phoenixJdbcTemplate.queryForList(
                "select COLUMN_NAME from SYSTEM.CATALOG "
                        + "where TABLE_NAME = '" + physicalTableName + "' ",
                String.class);

        return newLinkedHashSet(existingPhysicalSchema);
    }
}
