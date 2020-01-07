package com.lombardrisk.ignis.functional.test.config.data;

import com.google.common.collect.ImmutableMap;
import com.lombardrisk.ignis.client.external.productconfig.view.FieldView;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Types;
import java.util.Map;

import static com.lombardrisk.ignis.functional.test.config.data.TableFinder.*;

@Configuration
public class DataSourceConfig {

    private static final Map<FieldView.Type, Integer> PHOENIX_FIELD_TYPES =
            ImmutableMap.<FieldView.Type, Integer>builder()
                    .put(FieldView.Type.STRING, Types.VARCHAR)
                    .put(FieldView.Type.DOUBLE, Types.DOUBLE)
                    .put(FieldView.Type.DECIMAL, Types.DECIMAL)
                    .put(FieldView.Type.INTEGER, Types.INTEGER)
                    .put(FieldView.Type.LONG, Types.BIGINT)
                    .put(FieldView.Type.DATE, Types.DATE)
                    .put(FieldView.Type.FLOAT, Types.FLOAT)
                    .put(FieldView.Type.BOOLEAN, Types.BOOLEAN)
                    .put(FieldView.Type.TIMESTAMP, Types.TIMESTAMP)
                    .build();

    private static final Map<FieldView.Type, Integer> H2_FIELD_TYPES =
            ImmutableMap.<FieldView.Type, Integer>builder()
                    .put(FieldView.Type.STRING, Types.VARCHAR)
                    .put(FieldView.Type.DOUBLE, Types.DOUBLE)
                    .put(FieldView.Type.DECIMAL, Types.DECIMAL)
                    .put(FieldView.Type.INTEGER, Types.INTEGER)
                    .put(FieldView.Type.LONG, Types.BIGINT)
                    .put(FieldView.Type.DATE, Types.DATE)
                    .put(FieldView.Type.FLOAT, Types.DOUBLE)
                    .put(FieldView.Type.BOOLEAN, Types.BOOLEAN)
                    .put(FieldView.Type.TIMESTAMP, Types.TIMESTAMP)
                    .build();

    @Value(value = "${spring.datasource.phoenix.driver-class-name}")
    private String phoenixDriverClass;

    @IgnisServer
    @Bean
    public JdbcTemplate ignisServerJdbcTemplate() {
        return new JdbcTemplate(ignisDataSource());
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.ignis-server")
    public DataSource ignisDataSource() {
        return new org.apache.tomcat.jdbc.pool.DataSource();
    }

    @Phoenix
    @Bean
    public JdbcTemplate phoenixQueryServerJdbcTemplate() {
        return new JdbcTemplate(phoenixDataSource());
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.phoenix")
    public DataSource phoenixDataSource() {
        org.apache.tomcat.jdbc.pool.DataSource dataSource = new org.apache.tomcat.jdbc.pool.DataSource();
        dataSource.setDefaultAutoCommit(true);
        return dataSource;
    }

    @Bean
    public Map<FieldView.Type, Integer> dataSourceFieldTypes() {
        return isH2() ? H2_FIELD_TYPES : PHOENIX_FIELD_TYPES;
    }

    @Bean
    public String dataSourceUpdateSyntax() {
        return isH2() ? "INSERT" : "UPSERT";
    }

    @Bean
    public TableFinder tableFinder() {
        return isH2()
                ? new H2TableFinder(phoenixQueryServerJdbcTemplate())
                : new PhoenixTableFinder(phoenixQueryServerJdbcTemplate());
    }

    private boolean isH2() {
        return phoenixDriverClass.equals(org.h2.Driver.class.getName());
    }
}
