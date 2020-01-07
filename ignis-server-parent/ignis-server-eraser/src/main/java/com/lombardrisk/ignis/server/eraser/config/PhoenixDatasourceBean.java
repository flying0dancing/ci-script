package com.lombardrisk.ignis.server.eraser.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.DriverManager;

import static com.lombardrisk.ignis.server.eraser.config.DataSourceQualifier.Target.PHOENIX;

@Configuration
public class PhoenixDatasourceBean {

    @Value("${phoenix.datasource.url}")
    private String phoenixUrl;

    @Value("${phoenix.salt.bucket.count}")
    private int saltBuckets;

    @Value("${phoenix.local}")
    private boolean localDataSource;

    @Bean
    @DataSourceQualifier(PHOENIX)
    @ConfigurationProperties(prefix = "phoenix.datasource")
    public DataSource phoenixDatasetSource() {
        try {
            DriverManager.getDriver(phoenixUrl);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot find driver for phoenix url " + phoenixUrl, e);
        }

        return DataSourceBuilder.create()
                .type(org.apache.tomcat.jdbc.pool.DataSource.class)
                .build();
    }

    @Bean
    @DataSourceQualifier(PHOENIX)
    public JdbcTemplate phoenixJdbcTemplate() {
        return new JdbcTemplate(phoenixDatasetSource());
    }
}
