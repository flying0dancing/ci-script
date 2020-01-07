package com.lombardrisk.ignis.server.health;

import io.vavr.control.Option;
import lombok.experimental.UtilityClass;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.jdbc.DatabaseDriver;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;

@UtilityClass
public final class DatabaseHealthChecks {

    private static final String DEFAULT_QUERY = "SELECT 1";

    public static MemoizedHealthCheck dbHealthCheck(
            final DataSource dataSource,
            final int queryTimeout,
            final Duration healthRefreshRate) {
        return MemoizedHealthCheck.create(() -> validateDataSource(dataSource, queryTimeout), healthRefreshRate);
    }

    private static Health validateDataSource(final DataSource dataSource, final int queryTimeout) {
        try {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            jdbcTemplate.setQueryTimeout(queryTimeout);

            String product = getProduct(jdbcTemplate);
            String validationQuery = Option.of(getValidationQuery(product))
                    .getOrElse(DEFAULT_QUERY);

            jdbcTemplate.execute(validationQuery);

            return Health.up()
                    .withDetail("database", product)
                    .build();
        } catch (DataAccessException e) {
            return Health.down(e)
                    .build();
        }
    }

    private static String getProduct(final JdbcTemplate jdbcTemplate) {
        return jdbcTemplate.execute((ConnectionCallback<String>) DatabaseHealthChecks::getProduct);
    }

    private static String getProduct(final Connection connection) throws SQLException {
        return connection.getMetaData().getDatabaseProductName();
    }

    private static String getValidationQuery(final String product) {
        DatabaseDriver specific = DatabaseDriver.fromProductName(product);
        return specific.getValidationQuery();
    }
}
