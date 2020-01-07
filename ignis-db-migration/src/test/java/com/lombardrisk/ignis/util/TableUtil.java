package com.lombardrisk.ignis.util;

import com.lombardrisk.ignis.server.product.table.model.Table;
import org.springframework.jdbc.core.JdbcTemplate;

public class TableUtil {
    private final JdbcTemplate jdbcTemplate;
    private final FieldUtil fieldUtil;

    public TableUtil(final JdbcTemplate jdbcTemplate, final FieldUtil fieldUtil) {
        this.jdbcTemplate = jdbcTemplate;
        this.fieldUtil = fieldUtil;
    }

    public void save(final Table table) {
        String sql = "INSERT INTO DATASET_SCHEMA"
                + " (ID, PRODUCT_ID, PHYSICAL_TABLE_NAME, DISPLAY_NAME, VERSION,"
                + " START_DATE, END_DATE, CREATED_TIME, CREATED_BY, HAS_DATASETS)"
                + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.update(
                sql,
                table.getId(),
                table.getProductId(),
                table.getPhysicalTableName(),
                table.getDisplayName(),
                table.getVersion(),
                table.getStartDate(),
                table.getEndDate(),
                table.getCreatedTime(),
                table.getCreatedBy(),
                table.getHasDatasets());

        fieldUtil.insertAll(table.getFields(), table.getId());
    }
}
