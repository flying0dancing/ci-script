package com.lombardrisk.ignis.server.config.job;

import com.lombardrisk.ignis.server.config.product.ProductConfigConverterConfiguration;
import com.lombardrisk.ignis.server.controller.ServletUriUtils;
import com.lombardrisk.ignis.server.controller.jobs.JobsController;
import com.lombardrisk.ignis.server.controller.staging.StagingController;
import com.lombardrisk.ignis.server.controller.staging.StagingDatasetConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JobControllerConfiguration {

    private final JobServiceConfiguration jobServiceConfiguration;
    private final String servletPath;
    private final ProductConfigConverterConfiguration productConfigConverterConfiguration;

    @Autowired
    public JobControllerConfiguration(
            @Value("${server.servlet.contextPath}") final String servletPath,
            final JobServiceConfiguration jobServiceConfiguration,
            final ProductConfigConverterConfiguration productConfigConverterConfiguration) {
        this.jobServiceConfiguration = jobServiceConfiguration;
        this.servletPath = servletPath;
        this.productConfigConverterConfiguration = productConfigConverterConfiguration;
    }

    @Bean
    public StagingController stagingController() {
        return new StagingController(
                jobServiceConfiguration.stagingDatasetService(), stagingDatasetConverter());
    }

    @Bean
    public StagingDatasetConverter stagingDatasetConverter() {
        return new StagingDatasetConverter(servletPath, ServletUriUtils::getContextPath);
    }

    @Bean
    public JobsController jobsController() {
        return new JobsController(
                jobServiceConfiguration.jobService(),
                jobServiceConfiguration.pipelineJobService(),
                productConfigConverterConfiguration.pageToViewConverter(),
                jobServiceConfiguration.stagingDatasetService(),
                jobServiceConfiguration.ignisJobOperator());
    }
}
