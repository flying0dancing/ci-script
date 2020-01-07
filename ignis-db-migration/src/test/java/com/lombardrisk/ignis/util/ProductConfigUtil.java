package com.lombardrisk.ignis.util;

import com.lombardrisk.ignis.server.product.productconfig.model.ProductConfig;
import org.springframework.jdbc.core.JdbcTemplate;

public class ProductConfigUtil {

    private final JdbcTemplate jdbcTemplate;

    public ProductConfigUtil(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(final ProductConfig productConfig) {
        String sql = "INSERT INTO PRODUCT_CONFIG"
                + " (ID, NAME, VERSION, CREATED_TIME, IMPORT_STATUS, IMPORT_REQUEST_ID)"
                + " VALUES (?, ?, ?, ?, ?, ?)";

        jdbcTemplate.update(
                sql,
                productConfig.getId(),
                productConfig.getName(),
                productConfig.getVersion(),
                productConfig.getCreatedTime(),
                productConfig.getImportStatus().toString(),
                productConfig.getImportRequestId());
    }
}
