package com.lombardrisk.ignis.server.config.job;

import com.lombardrisk.ignis.server.batch.AppId;
import com.lombardrisk.ignis.server.batch.JobService;
import com.lombardrisk.ignis.server.batch.staging.AutoValidateDatasetTasklet;
import com.lombardrisk.ignis.server.batch.staging.DatasetCleanupTasklet;
import com.lombardrisk.ignis.server.batch.staging.DatasetStagingTasklet;
import com.lombardrisk.ignis.server.batch.staging.DatasetUploadTasklet;
import com.lombardrisk.ignis.server.batch.staging.DownstreamPipelineTasklet;
import com.lombardrisk.ignis.server.batch.staging.HdfsDatasetConf;
import com.lombardrisk.ignis.server.config.ApplicationConf;
import com.lombardrisk.ignis.server.config.batch.BatchConfiguration;
import com.lombardrisk.ignis.server.config.dataset.DatasetRepositoryConfiguration;
import com.lombardrisk.ignis.server.config.datasource.DatasourceFileConfiguration;
import com.lombardrisk.ignis.server.job.pipeline.PipelineJobService;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.listener.CompositeJobExecutionListener;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

import static com.lombardrisk.ignis.server.job.servicerequest.ServiceRequestType.STAGING;

/**
 * Job configuration for staging dataset
 */
@Configuration
@Import({ BatchConfiguration.class })
public class StagingJobConfiguration {

    private static final String COMPLETE = ExitStatus.COMPLETED.getExitCode();
    private static final String FAIL = ExitStatus.FAILED.getExitCode();
    private static final String STOP = ExitStatus.STOPPED.getExitCode();
    private static final String ANY = "*";
    private static final String NOOP = ExitStatus.NOOP.getExitCode();
    private static final String UNKNOWN = ExitStatus.UNKNOWN.getExitCode();

    private final ApplicationConf applicationConf;
    private final DatasourceFileConfiguration datasourceFileConfiguration;
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory steps;
    private final Environment environment;
    private final HadoopConfiguration hadoopConfiguration;
    private final SparkDefaultsConfiguration sparkDefaultsConfiguration;
    private final DatasetRepositoryConfiguration datasetRepositoryConfiguration;
    private final JobServiceConfiguration jobServiceConfiguration;
    private final HdfsDatasetConf datasetConf;
    private final JobService jobService;
    private final PipelineJobService pipelineJobService;

    @Autowired
    public StagingJobConfiguration(
            final ApplicationConf applicationConf,
            final DatasourceFileConfiguration datasourceFileConfiguration,
            final JobBuilderFactory jobBuilderFactory,
            final StepBuilderFactory steps,
            final Environment environment,
            final HadoopConfiguration hadoopConfiguration,
            final SparkDefaultsConfiguration sparkDefaultsConfiguration,
            final DatasetRepositoryConfiguration datasetRepositoryConfiguration,
            final JobServiceConfiguration jobServiceConfiguration,
            final HdfsDatasetConf datasetConf,
            final JobService jobService,
            final PipelineJobService pipelineJobService) {
        this.applicationConf = applicationConf;
        this.datasourceFileConfiguration = datasourceFileConfiguration;
        this.jobBuilderFactory = jobBuilderFactory;
        this.steps = steps;
        this.environment = environment;
        this.hadoopConfiguration = hadoopConfiguration;
        this.sparkDefaultsConfiguration = sparkDefaultsConfiguration;
        this.datasetRepositoryConfiguration = datasetRepositoryConfiguration;
        this.jobServiceConfiguration = jobServiceConfiguration;
        this.datasetConf = datasetConf;
        this.jobService = jobService;
        this.pipelineJobService = pipelineJobService;
    }

    @Bean
    public Job stagingJob() {
        if (datasourceFileConfiguration.getDatasetFileSource()
                .equals(DatasourceFileConfiguration.Source.S3)) {
            return stagingJobWithoutUpload();
        }

        return stagingJobWithUpload();
    }

    private Job stagingJobWithoutUpload() {
        return jobBuilderFactory.get(STAGING.name())
                .incrementer(new RunIdIncrementer())
                .listener(stagingJobExecutionListener())
                .start(stagingStep()).on(COMPLETE).to(autoValidateDatasetStep())
                .from(autoValidateDatasetStep()).on(COMPLETE).to(downstreamPipelineStep())

                .from(stagingStep()).on(FAIL).fail()
                .from(stagingStep()).on(STOP).stop()
                .from(stagingStep()).on(UNKNOWN).end(UNKNOWN)

                .from(autoValidateDatasetStep()).on(FAIL).fail()
                .from(autoValidateDatasetStep()).on(STOP).stop()
                .from(autoValidateDatasetStep()).on(UNKNOWN).end(UNKNOWN)

                .from(downstreamPipelineStep()).on(FAIL).fail()
                .from(downstreamPipelineStep()).on(STOP).stop()
                .from(downstreamPipelineStep()).on(UNKNOWN).end(UNKNOWN)

                .build().build();
    }

    private Job stagingJobWithUpload() {
        return jobBuilderFactory.get(STAGING.name())
                .incrementer(new RunIdIncrementer())
                .listener(stagingJobExecutionListener())

                .start(uploadStep()).on(NOOP).end()
                .from(uploadStep()).on(FAIL).to(failedCleanupStep())
                .from(uploadStep()).on(STOP).to(stoppedCleanupStep())
                .from(uploadStep()).on(ANY).to(stagingStep())

                .from(stagingStep()).on(FAIL).to(failedCleanupStep())
                .from(stagingStep()).on(STOP).to(stoppedCleanupStep())
                .from(stagingStep()).on(ANY).to(autoValidateDatasetStep())

                .from(autoValidateDatasetStep()).on(FAIL).to(failedCleanupStep())
                .from(autoValidateDatasetStep()).on(STOP).to(stoppedCleanupStep())
                .from(autoValidateDatasetStep()).on(ANY).to(downstreamPipelineStep())

                .from(downstreamPipelineStep()).on(FAIL).to(failedCleanupStep())
                .from(downstreamPipelineStep()).on(STOP).to(stoppedCleanupStep())
                .from(downstreamPipelineStep()).on(ANY).to(cleanupStep())

                .from(failedCleanupStep()).on(ANY).fail()
                .from(stoppedCleanupStep()).on(ANY).stop()
                .from(cleanupStep()).on(ANY).end()

                .end()

                .build();
    }

    @Bean
    public JobExecutionListener stagingJobExecutionListener() {
        CompositeJobExecutionListener compositeJobExecutionListener = new CompositeJobExecutionListener();
        compositeJobExecutionListener.register(jobServiceConfiguration.jobExecutionStatusListener());
        compositeJobExecutionListener.register(jobServiceConfiguration.tempDirectoryJobListener());
        return compositeJobExecutionListener;
    }

    @Bean
    public Step cleanupStep() {
        return createCleanupStep("cleanup");
    }

    @Bean
    public Step failedCleanupStep() {
        return createCleanupStep("cleanup after step failed");
    }

    @Bean
    public Step stoppedCleanupStep() {
        return createCleanupStep("cleanup after step stopped");
    }

    private Step createCleanupStep(final String stepName) {
        return steps.get(stepName)
                .tasklet(cleanupTasklet())
                .transactionAttribute(BatchConfiguration.NOT_SUPPORTED_TRANSACTION)
                .build();
    }

    @Bean
    public Step uploadStep() {
        return steps.get("upload")
                .tasklet(uploadTaskLet())
                .transactionAttribute(BatchConfiguration.NOT_SUPPORTED_TRANSACTION)
                .build();
    }

    @Bean
    public Step stagingStep() {
        return steps.get("staging")
                .tasklet(stagingTasklet())
                .transactionAttribute(BatchConfiguration.NOT_SUPPORTED_TRANSACTION)
                .build();
    }

    @Bean
    public Step downstreamPipelineStep() {
        return steps.get("downstreamPipeline")
                .tasklet(downstreamPipelineTasklet())
                .transactionAttribute(BatchConfiguration.NOT_SUPPORTED_TRANSACTION)
                .build();
    }

    @Bean
    public Step autoValidateDatasetStep() {
        return steps.get("autoValidateDataset")
                .tasklet(autoValidateDatasetTasklet())
                .transactionAttribute(BatchConfiguration.NOT_SUPPORTED_TRANSACTION)
                .build();
    }

    @Bean
    public Tasklet uploadTaskLet() {
        return new DatasetUploadTasklet(
                datasetConf,
                hadoopConfiguration.fileSystemTemplate(),
                datasetRepositoryConfiguration.stagingDatasetRepository(),
                datasetRepositoryConfiguration.sparkJobExecutor(),
                applicationConf.objectMapper());
    }

    @Bean
    public Tasklet stagingTasklet() {
        return new DatasetStagingTasklet(
                environment,
                sparkDefaultsConfiguration.sparkConfFactory(),
                datasetRepositoryConfiguration.sparkJobExecutor(),
                appId());
    }

    @Bean
    @StepScope
    public AppId appId() {
        return new AppId();
    }

    @Bean
    public Tasklet autoValidateDatasetTasklet() {
        return new AutoValidateDatasetTasklet(
                datasetRepositoryConfiguration.sparkJobExecutor(),
                datasetRepositoryConfiguration.datasetRepository(),
                jobService);
    }

    @Bean
    public Tasklet downstreamPipelineTasklet() {
        return new DownstreamPipelineTasklet(
                datasetRepositoryConfiguration.sparkJobExecutor(), pipelineJobService, applicationConf.objectMapper());
    }

    @Bean
    public Tasklet cleanupTasklet() {
        return new DatasetCleanupTasklet(
                datasetRepositoryConfiguration.sparkJobExecutor(),
                hadoopConfiguration.fileSystemTemplate(),
                applicationConf.objectMapper());
    }
}
