package com.lombardrisk.ignis.server.eraser.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

import static com.lombardrisk.ignis.server.eraser.config.DataSourceQualifier.Target.MASTER;

@Configuration
public class DatasourceBean {

    @Bean
    @Primary
    @DataSourceQualifier(MASTER)
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
    @DataSourceQualifier(MASTER)
    @Primary
    public JdbcTemplate jdbcTemplate(@DataSourceQualifier(MASTER) final DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
