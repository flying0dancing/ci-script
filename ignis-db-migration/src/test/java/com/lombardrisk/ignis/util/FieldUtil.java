package com.lombardrisk.ignis.util;

import com.lombardrisk.ignis.server.product.table.model.Field;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Collection;

public class FieldUtil {

    private final JdbcTemplate jdbcTemplate;

    public FieldUtil(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final Field field, final Long tableId) {
        String sql = "INSERT INTO DATASET_SCHEMA_FIELD (ID, NAME, FIELD_TYPE, NULLABLE, DATASET_SCHEMA_ID)"
                + " VALUES(?, ?, ?, ?, ?)";

        jdbcTemplate.update(sql, field.getId(), field.getName(), field.getFieldType(), field.isNullable(), tableId);
    }

    public void insertAll(final Collection<Field> fields, final Long tableId) {
        for (Field field : fields) {
            insert(field, tableId);
        }
    }
}
