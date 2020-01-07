package com.lombardrisk.ignis.functional.test.config.data;

import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

public interface TableFinder {

    List<String> queryTablesByName(final String physicalSchema);

    @AllArgsConstructor
    class PhoenixTableFinder implements TableFinder {
        private final JdbcTemplate phoenixJdbcTemplate;

        @Override
        public List<String> queryTablesByName(final String physicalSchema) {
            return phoenixJdbcTemplate.queryForList(
                    "select TABLE_NAME from SYSTEM.CATALOG "
                            + "where TABLE_NAME = '" + physicalSchema.toUpperCase() + "' "
                            + "limit 1",
                    String.class);
        }
    }

    @AllArgsConstructor
    class H2TableFinder implements TableFinder {
        private final JdbcTemplate phoenixJdbcTemplate;

        @Override
        public List<String> queryTablesByName(final String physicalSchema) {
            return phoenixJdbcTemplate.queryForList(
                    "select TABLE_NAME from INFORMATION_SCHEMA.TABLES "
                            + "where TABLE_NAME = '" + physicalSchema.toUpperCase() + "' "
                            + "limit 1",
                    String.class);
        }
    }
}
