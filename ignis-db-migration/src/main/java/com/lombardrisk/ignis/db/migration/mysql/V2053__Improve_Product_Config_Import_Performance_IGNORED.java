package com.lombardrisk.ignis.db.migration.mysql;

import org.flywaydb.core.api.migration.jdbc.BaseJdbcMigration;

import java.sql.Connection;

@SuppressWarnings("squid:S00101")
public class V2053__Improve_Product_Config_Import_Performance_IGNORED extends BaseJdbcMigration {

    @Override
    public void migrate(final Connection connection) {
        // No-op migration since MySQL doesn't support sequences
    }
}
