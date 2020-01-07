package com.lombardrisk.ignis.server.config.job;

import com.lombardrisk.ignis.common.time.TimeSource;
import com.lombardrisk.ignis.server.batch.JobExecutionStatusListener;
import com.lombardrisk.ignis.server.batch.JobOperatorImpl;
import com.lombardrisk.ignis.server.batch.JobService;
import com.lombardrisk.ignis.server.batch.TempDirectoryJobListener;
import com.lombardrisk.ignis.server.batch.staging.JobExecutionStatusService;
import com.lombardrisk.ignis.server.config.ApplicationConf;
import com.lombardrisk.ignis.server.config.calendar.CalendarConfiguration;
import com.lombardrisk.ignis.server.config.dataset.DatasetRepositoryConfiguration;
import com.lombardrisk.ignis.server.config.dataset.DatasetServiceConfiguration;
import com.lombardrisk.ignis.server.config.datasource.DatasourceFileConfiguration;
import com.lombardrisk.ignis.server.config.pipeline.PipelineServiceConfiguration;
import com.lombardrisk.ignis.server.config.product.ProductServiceConfiguration;
import com.lombardrisk.ignis.server.config.table.TableServiceConfiguration;
import com.lombardrisk.ignis.server.job.pipeline.PipelineJobService;
import com.lombardrisk.ignis.server.job.product.ProductConfigImportJobService;
import com.lombardrisk.ignis.server.job.servicerequest.ServiceRequestSpecificationBuilder;
import com.lombardrisk.ignis.server.job.staging.StagingDatasetService;
import com.lombardrisk.ignis.server.job.staging.StagingSparkConfigService;
import com.lombardrisk.ignis.server.job.staging.validate.StagingRequestCommonValidator;
import com.lombardrisk.ignis.server.job.staging.validate.StagingRequestV1Validator;
import com.lombardrisk.ignis.server.job.staging.validate.StagingRequestV2Validator;
import com.lombardrisk.ignis.web.common.config.FeatureFlagConfiguration;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ CalendarConfiguration.class })
public class JobServiceConfiguration {

    private final ApplicationConf applicationConf;
    private final DatasourceFileConfiguration datasourceFileConfiguration;
    private final DatasetRepositoryConfiguration datasetRepositoryConfiguration;
    private final PipelineServiceConfiguration pipelineServiceConfiguration;
    private final ProductServiceConfiguration productServiceConfiguration;
    private final CalendarConfiguration calendarConfiguration;
    private final org.springframework.batch.core.launch.JobOperator jobOperator;
    private final JobExplorer jobExplorer;
    private final TableServiceConfiguration tableServiceConfiguration;
    private final DatasetServiceConfiguration datasetServiceConfiguration;
    private final TimeSource timeSource;
    private final FeatureFlagConfiguration featureFlagConfiguration;

    @Autowired
    public JobServiceConfiguration(
            final ApplicationConf applicationConf,
            final DatasourceFileConfiguration datasourceFileConfiguration,
            final DatasetRepositoryConfiguration datasetRepositoryConfiguration,
            final PipelineServiceConfiguration pipelineServiceConfiguration,
            final ProductServiceConfiguration productServiceConfiguration,
            final CalendarConfiguration calendarConfiguration,
            final JobOperator jobOperator,
            final TableServiceConfiguration tableServiceConfiguration,
            final JobExplorer jobExplorer,
            final DatasetServiceConfiguration datasetServiceConfiguration,
            final TimeSource timeSource,
            final FeatureFlagConfiguration featureFlagConfiguration) {
        this.applicationConf = applicationConf;
        this.datasourceFileConfiguration = datasourceFileConfiguration;
        this.datasetRepositoryConfiguration = datasetRepositoryConfiguration;
        this.pipelineServiceConfiguration = pipelineServiceConfiguration;
        this.productServiceConfiguration = productServiceConfiguration;
        this.calendarConfiguration = calendarConfiguration;
        this.jobOperator = jobOperator;
        this.jobExplorer = jobExplorer;
        this.tableServiceConfiguration = tableServiceConfiguration;
        this.datasetServiceConfiguration = datasetServiceConfiguration;
        this.timeSource = timeSource;
        this.featureFlagConfiguration = featureFlagConfiguration;
    }

    @Bean
    public StagingDatasetService stagingDatasetService() {
        JobOperatorImpl jobOperatorImpl = ignisJobOperator();

        return new StagingDatasetService(
                stagingRequestValidator(),
                stagingRequestValidatorV2(),
                datasetRepositoryConfiguration.stagingDatasetRepository(),
                datasetRepositoryConfiguration.serviceRequestRepository(),
                jobOperatorImpl,
                stagingSparkConfigService(),
                timeSource,
                datasourceFileConfiguration.dataSourceService(),
                datasourceFileConfiguration.errorFileService());
    }

    @Bean
    public StagingSparkConfigService stagingSparkConfigService() {
        return new StagingSparkConfigService(
                datasourceFileConfiguration.dataSourceService(), datasourceFileConfiguration.errorFileService());
    }

    @Bean
    public StagingRequestV1Validator stagingRequestValidator() {
        return new StagingRequestV1Validator(commonValidator());
    }

    @Bean
    public StagingRequestV2Validator stagingRequestValidatorV2() {
        return new StagingRequestV2Validator(
                commonValidator(),
                featureFlagConfiguration.featureManager(),
                datasetServiceConfiguration.datasetService(),
                pipelineServiceConfiguration.pipelineService());
    }

    @Bean
    public StagingRequestCommonValidator commonValidator() {
        return new StagingRequestCommonValidator(tableServiceConfiguration.tableService());
    }

    @Bean
    public JobOperatorImpl ignisJobOperator() {
        return new JobOperatorImpl(
                jobOperator, datasetRepositoryConfiguration.serviceRequestRepository());
    }

    @Bean
    public JobService jobService() {
        return new JobService(
                datasetRepositoryConfiguration.datasetRepository(),
                datasetRepositoryConfiguration.serviceRequestRepository(),
                serviceRequestSpecificationBuilder(),
                jobExplorer,
                ignisJobOperator(),
                timeSource);
    }

    @Bean
    public PipelineJobService pipelineJobService() {
        return new PipelineJobService(
                pipelineServiceConfiguration.pipelineInvocationService(),
                ignisJobOperator(),
                datasetRepositoryConfiguration.serviceRequestRepository(),
                pipelineServiceConfiguration.pipelineInputValidator(),
                calendarConfiguration.calendarService(),
                timeSource,
                applicationConf.objectMapper());
    }

    @Bean
    public TempDirectoryJobListener tempDirectoryJobListener() {
        return new TempDirectoryJobListener(applicationConf.tempDirectory());
    }

    @Bean
    public JobExecutionStatusListener jobExecutionStatusListener() {
        return new JobExecutionStatusListener(jobExecutionStatusService());
    }

    @Bean
    public JobExecutionStatusService jobExecutionStatusService() {
        return new JobExecutionStatusService(
                datasetRepositoryConfiguration.serviceRequestRepository(),
                datasetRepositoryConfiguration.datasetRepository(),
                datasetRepositoryConfiguration.stagingDatasetRepository());
    }

    @Bean
    public ServiceRequestSpecificationBuilder serviceRequestSpecificationBuilder() {
        return new ServiceRequestSpecificationBuilder(timeSource);
    }

    @Bean
    public ProductConfigImportJobService productConfigImporter() {
        return new ProductConfigImportJobService(
                productServiceConfiguration.getProductRepositoryConfiguration().productConfigRepository(),
                productServiceConfiguration.productConfigImportValidator(),
                productServiceConfiguration.productConfigImportDiff(),
                productServiceConfiguration.getProductConfigViewConverterConfiguration().productConfigViewConverter(),
                tableServiceConfiguration.tableService(),
                pipelineServiceConfiguration.pipelineService(),
                pipelineServiceConfiguration.pipelineStepService(),
                productServiceConfiguration.productConfigImportEntityService(),
                productServiceConfiguration.getServiceRequestRepository(),
                ignisJobOperator());
    }
}
