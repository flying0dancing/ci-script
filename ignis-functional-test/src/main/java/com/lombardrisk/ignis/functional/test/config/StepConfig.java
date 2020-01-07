package com.lombardrisk.ignis.functional.test.config;

import com.lombardrisk.ignis.functional.test.config.data.DataSourceConfig;
import com.lombardrisk.ignis.functional.test.config.properties.CsvDataSourceProperties;
import com.lombardrisk.ignis.functional.test.config.properties.NameNodeProperties;
import com.lombardrisk.ignis.functional.test.config.properties.Timeouts;
import com.lombardrisk.ignis.functional.test.dsl.FcrEngine;
import com.lombardrisk.ignis.functional.test.steps.CleanupSteps;
import com.lombardrisk.ignis.functional.test.steps.JobSteps;
import com.lombardrisk.ignis.functional.test.steps.PipelineSteps;
import com.lombardrisk.ignis.functional.test.steps.ProductConfigSteps;
import com.lombardrisk.ignis.functional.test.steps.StagingSteps;
import com.lombardrisk.ignis.functional.test.steps.ValidationSteps;
import com.lombardrisk.ignis.functional.test.steps.service.StagingServiceProvider;
import com.lombardrisk.ignis.functional.test.steps.service.StagingServiceV1;
import com.lombardrisk.ignis.functional.test.steps.service.StagingServiceV2;
import com.lombardrisk.ignis.functional.test.zip.ProductConfigZipService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        FunctionalTestClientConfig.class,
        DataSourceConfig.class,
        ExternalClientConfig.class,
        HadoopClientConfig.class,
        AssertionsConfig.class,
        FeaturesConfig.class,
})
@EnableConfigurationProperties(CsvDataSourceProperties.class)
@AllArgsConstructor(onConstructor = @__({ @Autowired }))
public class StepConfig {

    private final ExternalClientConfig externalClientConfig;
    private final FunctionalTestClientConfig ignisServerClientConfig;
    private final DataSourceConfig dataSourceConfig;
    private final HadoopClientConfig hadoopClientConfig;
    private final NameNodeProperties nameNodeProperties;
    private final CsvDataSourceProperties csvDataSourceProperties;
    private final Timeouts timeouts;

    private final AssertionsConfig assertionsConfig;

    @Bean
    public FcrEngine fcrEngine() {
        return new FcrEngine(
                jobSteps(),
                productConfigSteps(),
                stagingSteps(),
                pipelineSteps(),
                validationSteps(),
                cleanupSteps(),
                csvDataSourceProperties.csvFinder(),
                assertionsConfig.hdfsAssertions(),
                assertionsConfig.datasetAssertions()
        );
    }

    @Bean
    public StagingSteps stagingSteps() {
        return new StagingSteps(
                ignisServerClientConfig.stagingClient(),
                externalClientConfig.datasetService(),
                timeouts,
                ignisServerClientConfig.jobsClient(),
                stagingServiceProvider());
    }

    @Bean
    public PipelineSteps pipelineSteps() {
        return new PipelineSteps(
                ignisServerClientConfig.pipelineClient(),
                ignisServerClientConfig.drillbackClient(),
                externalClientConfig.datasetService(),
                timeouts,
                ignisServerClientConfig.jobsClient());
    }

    @Bean
    public CleanupSteps cleanupSteps() {
        return new CleanupSteps(
                dataSourceConfig.ignisServerJdbcTemplate(),
                dataSourceConfig.phoenixQueryServerJdbcTemplate(),
                hadoopClientConfig.fileSystemTemplate(),
                nameNodeProperties);
    }

    @Bean
    public ValidationSteps validationSteps() {
        return new ValidationSteps(
                ignisServerClientConfig.jobsClient(),
                ignisServerClientConfig.externalDatasetClient(),
                externalClientConfig.datasetService(),
                timeouts);
    }

    @Bean
    public JobSteps jobSteps() {
        return new JobSteps(ignisServerClientConfig.jobsClient(), timeouts);
    }

    @Bean
    public ProductConfigSteps productConfigSteps() {
        return new ProductConfigSteps(
                ignisServerClientConfig.productConfigClient(),
                productConfigZipService(),
                dataSourceConfig.phoenixQueryServerJdbcTemplate(),
                dataSourceConfig.phoenixDataSource(),
                dataSourceConfig.dataSourceUpdateSyntax());
    }

    @Bean
    public ProductConfigZipService productConfigZipService() {
        return new ProductConfigZipService();
    }

    @Bean
    public StagingServiceProvider stagingServiceProvider() {
        return new StagingServiceProvider(
                new StagingServiceV1(ignisServerClientConfig.jobsClient()),
                new StagingServiceV2(ignisServerClientConfig.jobsClient()));
    }
}
