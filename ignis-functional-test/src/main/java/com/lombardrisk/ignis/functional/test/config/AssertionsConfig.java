package com.lombardrisk.ignis.functional.test.config;

import com.lombardrisk.ignis.functional.test.assertions.HdfsAssertions;
import com.lombardrisk.ignis.functional.test.assertions.PhysicalSchemaAssertions;
import com.lombardrisk.ignis.functional.test.config.data.DataSourceConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AssertionsConfig {

    private final DataSourceConfig dataSourceConfig;
    private final HadoopClientConfig hadoopClientConfig;

    @Autowired
    public AssertionsConfig(
            final DataSourceConfig dataSourceConfig,
            final HadoopClientConfig hadoopClientConfig) {
        this.dataSourceConfig = dataSourceConfig;
        this.hadoopClientConfig = hadoopClientConfig;
    }

    @Bean
    public PhysicalSchemaAssertions datasetAssertions() {
        return new PhysicalSchemaAssertions(dataSourceConfig.phoenixQueryServerJdbcTemplate(),
                dataSourceConfig.dataSourceFieldTypes(),
                dataSourceConfig.tableFinder());
    }

    @Bean
    public HdfsAssertions hdfsAssertions() {
        return new HdfsAssertions(hadoopClientConfig.fileSystemTemplate());
    }
}
