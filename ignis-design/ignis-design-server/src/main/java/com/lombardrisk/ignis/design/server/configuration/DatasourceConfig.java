package com.lombardrisk.ignis.design.server.configuration;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Slf4j
@Getter
@Configuration
public class DatasourceConfig {

    @Bean
    @Primary
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
}
