package com.lombardrisk.ignis.server.dataset.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "phoenix.datasource")
    public DataSource phoenixDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    public JdbcTemplate phoenixJdbcTemplate() {
        return new JdbcTemplate(phoenixDataSource());
    }

}
