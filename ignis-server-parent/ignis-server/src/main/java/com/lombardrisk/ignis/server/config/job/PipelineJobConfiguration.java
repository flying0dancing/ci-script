package com.lombardrisk.ignis.server.config.job;

import com.lombardrisk.ignis.server.batch.AppId;
import com.lombardrisk.ignis.server.batch.pipeline.PipelineJobTasklet;
import com.lombardrisk.ignis.server.config.ApplicationConf;
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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

import static com.lombardrisk.ignis.server.job.servicerequest.ServiceRequestType.PIPELINE;

@Configuration
@Import(BatchConfiguration.class)
public class PipelineJobConfiguration {

    private final JobBuilderFactory jobs;
    private final StepBuilderFactory steps;
    private final Environment environment;
    private final SparkDefaultsConfiguration sparkDefaultsConfiguration;
    private final DatasetRepositoryConfiguration datasetRepositoryConfiguration;
    private final JobServiceConfiguration jobServiceConfiguration;
    private final ApplicationConf applicationConf;

    public PipelineJobConfiguration(
            final JobBuilderFactory jobs,
            final StepBuilderFactory steps,
            final Environment environment,
            final SparkDefaultsConfiguration sparkDefaultsConfiguration,
            final DatasetRepositoryConfiguration datasetRepositoryConfiguration,
            final JobServiceConfiguration jobServiceConfiguration,
            final ApplicationConf applicationConf) {
        this.jobs = jobs;
        this.steps = steps;
        this.environment = environment;
        this.sparkDefaultsConfiguration = sparkDefaultsConfiguration;
        this.datasetRepositoryConfiguration = datasetRepositoryConfiguration;
        this.jobServiceConfiguration = jobServiceConfiguration;
        this.applicationConf = applicationConf;
    }

    @Bean
    public Job pipelineJob() {
        return jobs.get(PIPELINE.name())
                .incrementer(new RunIdIncrementer())
                .listener(pipelineJobExecutionListener())
                .start(pipelineStep())
                .build();
    }

    @Bean
    public JobExecutionListener pipelineJobExecutionListener() {
        CompositeJobExecutionListener compositeJobExecutionListener = new CompositeJobExecutionListener();
        compositeJobExecutionListener.register(jobServiceConfiguration.jobExecutionStatusListener());
        compositeJobExecutionListener.register(jobServiceConfiguration.tempDirectoryJobListener());
        return compositeJobExecutionListener;
    }

    @Bean
    public Step pipelineStep() {
        return steps.get("pipelineStep")
                .tasklet(pipelineJobTasklet())
                .transactionAttribute(BatchConfiguration.NOT_SUPPORTED_TRANSACTION)
                .build();
    }

    @Bean
    public PipelineJobTasklet pipelineJobTasklet() {
        return new PipelineJobTasklet(
                environment,
                sparkDefaultsConfiguration.sparkConfFactory(),
                datasetRepositoryConfiguration.sparkJobExecutor(),
                appId(),
                applicationConf.objectMapper());
    }

    @Bean
    @StepScope
    public AppId appId() {
        return new AppId();
    }
}
