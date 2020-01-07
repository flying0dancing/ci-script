package com.lombardrisk.ignis.server.config.job;

import com.lombardrisk.ignis.server.batch.AppId;
import com.lombardrisk.ignis.server.batch.DatasetId;
import com.lombardrisk.ignis.server.batch.rule.DatasetValidationJobListener;
import com.lombardrisk.ignis.server.batch.rule.DatasetValidationTasklet;
import com.lombardrisk.ignis.server.config.batch.BatchConfiguration;
import com.lombardrisk.ignis.server.config.dataset.DatasetRepositoryConfiguration;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.listener.CompositeJobExecutionListener;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

import static com.lombardrisk.ignis.server.job.servicerequest.ServiceRequestType.VALIDATION;

/**
 * Job configuration for data quality job
 */
@Configuration
@Import(BatchConfiguration.class)
public class DatasetValidationJobConfiguration {

    private final JobBuilderFactory jobs;
    private final StepBuilderFactory steps;
    private final DatasetRepositoryConfiguration datasetRepositoryConfiguration;
    private final JobServiceConfiguration jobServiceConfiguration;
    private final SparkDefaultsConfiguration sparkDefaultsConfiguration;
    private final Environment environment;

    public DatasetValidationJobConfiguration(
            final JobBuilderFactory jobs,
            final StepBuilderFactory steps,
            final DatasetRepositoryConfiguration datasetRepositoryConfiguration,
            final JobServiceConfiguration jobServiceConfiguration,
            final SparkDefaultsConfiguration sparkDefaultsConfiguration,
            final Environment environment) {
        this.jobs = jobs;
        this.steps = steps;
        this.datasetRepositoryConfiguration = datasetRepositoryConfiguration;
        this.jobServiceConfiguration = jobServiceConfiguration;
        this.sparkDefaultsConfiguration = sparkDefaultsConfiguration;
        this.environment = environment;
    }

    @Bean
    public Job datasetValidationJob() {
        return jobs.get(VALIDATION.name())
                .incrementer(new RunIdIncrementer())
                .listener(datasetValidationJobExecutionListener())
                .start(validateDatasetStep())
                .build();
    }

    @Bean
    public JobExecutionListener datasetValidationJobExecutionListener() {
        CompositeJobExecutionListener compositeJobExecutionListener = new CompositeJobExecutionListener();
        compositeJobExecutionListener.register(datasetValidationJobListener());
        compositeJobExecutionListener.register(jobServiceConfiguration.tempDirectoryJobListener());
        return compositeJobExecutionListener;
    }

    @Bean
    public DatasetValidationJobListener datasetValidationJobListener() {
        return new DatasetValidationJobListener(jobServiceConfiguration.jobExecutionStatusService());
    }

    @Bean
    public Step validateDatasetStep() {
        return steps.get("validationStep")
                .tasklet(validationTasklet())
                .transactionAttribute(BatchConfiguration.NOT_SUPPORTED_TRANSACTION)
                .build();
    }

    @Bean
    public Tasklet validationTasklet() {
        return new DatasetValidationTasklet(
                sparkDefaultsConfiguration.sparkConfFactory(),
                datasetRepositoryConfiguration.sparkJobExecutor(),
                datasetRepositoryConfiguration.datasetRepository(),
                taskletScopedAppId(),
                datasetId(),
                environment);
    }

    @Bean
    @StepScope
    public AppId taskletScopedAppId() {
        return new AppId();
    }

    @Bean
    @StepScope
    public DatasetId datasetId() {
        return new DatasetId();
    }
}
