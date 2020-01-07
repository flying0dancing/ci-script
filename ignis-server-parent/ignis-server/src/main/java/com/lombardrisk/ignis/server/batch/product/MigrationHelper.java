package com.lombardrisk.ignis.server.batch.product;

import lombok.Getter;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

@Getter
public class MigrationHelper {

    private final JdbcTemplate phoenixJdbcTemplate;

    public MigrationHelper(JdbcTemplate phoenixJdbcTemplate) {
        this.phoenixJdbcTemplate = phoenixJdbcTemplate;
    }

    boolean isPhysicalSchemaMissing(final String physicalSchemaName) {
        List<String> existingPhysicalSchema = phoenixJdbcTemplate.queryForList(
                "select TABLE_NAME from SYSTEM.CATALOG "
                        + "where TABLE_NAME = '" + physicalSchemaName + "' "
                        + "limit 1",
                String.class);

        return existingPhysicalSchema.isEmpty();
    }

    boolean isPhysicalSchemaPresent(final String physicalSchemaName) {
        return !isPhysicalSchemaMissing(physicalSchemaName);
    }
}
