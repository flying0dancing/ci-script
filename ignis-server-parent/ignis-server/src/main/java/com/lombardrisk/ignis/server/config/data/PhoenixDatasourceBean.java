package com.lombardrisk.ignis.server.config.data;

import com.lombardrisk.ignis.server.annotation.DataSourceQualifier;
import com.lombardrisk.ignis.server.batch.product.datasource.LocalMigrationTableService;
import com.lombardrisk.ignis.server.batch.product.datasource.MigrationTableService;
import com.lombardrisk.ignis.server.batch.product.datasource.PhoenixMigrationTableService;
import com.lombardrisk.ignis.server.dataset.result.FilterToSQLConverter;
import com.lombardrisk.ignis.server.dataset.result.LocalSQLFunctions;
import com.lombardrisk.ignis.server.dataset.result.PhoenixSQLFunctions;
import com.lombardrisk.ignis.server.dataset.result.SQLFunctions;
import com.lombardrisk.ignis.server.health.DatabaseHealthChecks;
import com.lombardrisk.ignis.server.health.MemoizedHealthCheck;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;
import java.sql.Driver;
import java.sql.DriverManager;
import java.time.Duration;

import static com.lombardrisk.ignis.server.annotation.DataSourceQualifier.Target.PHOENIX;

@Configuration
@Slf4j
public class PhoenixDatasourceBean {

    private static final Duration HEALTH_REFRESH_RATE = Duration.ofMinutes(5);
    private static final int QUERY_TIMEOUT = 2;

    @Value("${phoenix.datasource.url}")
    private String phoenixUrl;

    @Value("${phoenix.salt.bucket.count}")
    private int saltBuckets;

    @Value("${spark-defaults.conf.debug.mode}")
    private boolean localDataSource;


    @Bean
    @DataSourceQualifier(PHOENIX)
    @ConfigurationProperties(prefix = "phoenix.datasource")
    public DataSource phoenixDatasetSource() {
        log.info("Creating Phoenix datasource");

        try {
            Driver driver = DriverManager.getDriver(phoenixUrl);
            return new SimpleDriverDataSource(driver, phoenixUrl);
        } catch (Exception e) {
            throw new IllegalStateException("Error creating Phoenix datasource for url " + phoenixUrl, e);
        }
    }

    @Bean
    @DataSourceQualifier(PHOENIX)
    public JdbcTemplate phoenixJdbcTemplate() {
        return new JdbcTemplate(phoenixDatasetSource());
    }

    @Bean
    public MemoizedHealthCheck phoenixHealthCheck() {
        return DatabaseHealthChecks.dbHealthCheck(phoenixDatasetSource(), QUERY_TIMEOUT, HEALTH_REFRESH_RATE);
    }

    @Bean
    public MigrationTableService migrationTableService() {
        return localDataSource
                ? new LocalMigrationTableService(phoenixJdbcTemplate())
                : new PhoenixMigrationTableService(phoenixJdbcTemplate(), saltBuckets);
    }

    @Bean
    public FilterToSQLConverter filterToSQLConverter() {
        return new FilterToSQLConverter(sqlFunctions());
    }

    @Bean
    public SQLFunctions sqlFunctions() {
        return localDataSource
                ? new LocalSQLFunctions()
                : new PhoenixSQLFunctions();
    }
}
