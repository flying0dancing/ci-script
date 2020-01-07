package com.lombardrisk.ignis.functional.test.config;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.lombardrisk.ignis.functional.test.config.properties.EnvironmentSystemProperties;
import com.lombardrisk.ignis.performance.test.config.properties.PerformanceTestProperties;
import com.lombardrisk.ignis.performance.test.steps.ReportingSteps;
import com.lombardrisk.ignis.performance.test.steps.service.reporting.StagingJobDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

import java.text.SimpleDateFormat;

@Configuration
@Profile("performance")
@PropertySource("file:${environment.system.properties}")
@EnableConfigurationProperties(PerformanceTestProperties.class)
@Import({ FunctionalTestClientConfig.class, HadoopClientConfig.class })
public class PerformanceTestConfig {

    private final FunctionalTestClientConfig functionalTestClientConfig;
    private final HadoopClientConfig hadoopClientConfig;
    private final PerformanceTestProperties performanceTestProperties;

    @Autowired
    public PerformanceTestConfig(
            final FunctionalTestClientConfig functionalTestClientConfig,
            final HadoopClientConfig hadoopClientConfig,
            final PerformanceTestProperties performanceTestProperties) {
        this.functionalTestClientConfig = functionalTestClientConfig;
        this.hadoopClientConfig = hadoopClientConfig;
        this.performanceTestProperties = performanceTestProperties;
    }

    @Bean
    public EnvironmentSystemProperties environmentSystemProperties() {
        return new EnvironmentSystemProperties();
    }

    @Bean
    public CsvMapper csvMapper() {
        CsvMapper csvMapper = new CsvMapper();
        csvMapper.setDateFormat(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss"));
        return csvMapper;
    }

    @Bean
    public ReportingSteps reportingSteps() {
        return new ReportingSteps(
                csvMapper(),
                performanceTestProperties);
    }

    @Bean
    public StagingJobDataService stagingJobDataService() {
        return new StagingJobDataService(
                environmentSystemProperties(),
                functionalTestClientConfig.jobsClient(),
                hadoopClientConfig.metricsClient());
    }
}
