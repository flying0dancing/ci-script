package com.lombardrisk.ignis.server.config.data;

import com.lombardrisk.ignis.server.annotation.DataSourceQualifier;
import com.lombardrisk.ignis.server.annotation.DataSourceQualifier.Target;
import com.lombardrisk.ignis.server.health.DatabaseHealthChecks;
import com.lombardrisk.ignis.server.health.MemoizedHealthCheck;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.time.Duration;

@Configuration
public class DatasourceBean {

    private static final Duration HEALTH_REFRESH_RATE = Duration.ofMinutes(1);
    private static final int QUERY_TIMEOUT = 2;

    @Bean
    @Primary
    @DataSourceQualifier(Target.MASTER)
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource defaultDataSource(
            @Value("${spring.datasource.db.schema}") final String schema,
            @Value("${spring.datasource.url}") final String url,
            @Value("${spring.datasource.tomcat.validationQuery}") final String validationQuery) {

        org.apache.tomcat.jdbc.pool.DataSource datasource = new org.apache.tomcat.jdbc.pool.DataSource();

        if (isOracleSchemaQualify(schema, url)) {
            datasource.setInitSQL("ALTER SESSION SET CURRENT_SCHEMA=" + schema);
        }

        datasource.setTestOnBorrow(true);
        datasource.setValidationQuery(validationQuery);

        return datasource;
    }

    private boolean isOracleSchemaQualify(final String schema, final String datasourceUrl) {
        return !(StringUtils.isEmpty(schema) || !datasourceUrl.startsWith("jdbc:oracle:"));
    }

    @Bean
    @DataSourceQualifier(Target.MASTER)
    @Primary
    public JdbcTemplate jdbcTemplate(@DataSourceQualifier(Target.MASTER) final DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    public MemoizedHealthCheck dbHealthCheck(
            @DataSourceQualifier(Target.MASTER) final DataSource defaultDataSource) {
        return DatabaseHealthChecks.dbHealthCheck(defaultDataSource,  QUERY_TIMEOUT, HEALTH_REFRESH_RATE);
    }
}
