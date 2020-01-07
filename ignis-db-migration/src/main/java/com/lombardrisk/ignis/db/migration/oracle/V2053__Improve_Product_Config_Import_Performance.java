package com.lombardrisk.ignis.db.migration.oracle;

import com.lombardrisk.ignis.db.migration.common.MigrationV2053;
import org.flywaydb.core.api.migration.jdbc.BaseJdbcMigration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import java.sql.Connection;

@SuppressWarnings("squid:S00101")
public class V2053__Improve_Product_Config_Import_Performance extends BaseJdbcMigration {

    @Override
    public void migrate(final Connection connection) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(new SingleConnectionDataSource(connection, true));
        MigrationV2053 v2053 = new MigrationV2053(jdbcTemplate, new OracleSyntax());
        v2053.migrate();
    }
}
