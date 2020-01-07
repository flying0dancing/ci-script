package com.lombardrisk.ignis.server.config.batch;

import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.support.JobRegistryBeanPostProcessor;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;
import org.springframework.transaction.interceptor.TransactionAttribute;
import org.springframework.transaction.support.DefaultTransactionDefinition;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {

    private final JobRepository jobRepository;

    public static final TransactionAttribute NOT_SUPPORTED_TRANSACTION =
            new DefaultTransactionAttribute(DefaultTransactionDefinition.PROPAGATION_NOT_SUPPORTED) {
                private static final long serialVersionUID = 7588176538755410912L;

                @Override
                public boolean rollbackOn(Throwable ex) {
                    return true;
                }
            };

    @Autowired
    public BatchConfiguration(final JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    @Bean
    public JobLauncher jobLauncher() {
        final SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
        jobLauncher.setJobRepository(jobRepository);

        final SimpleAsyncTaskExecutor taskExecutor = new SimpleAsyncTaskExecutor();
        jobLauncher.setTaskExecutor(taskExecutor);

        return jobLauncher;
    }

    @Bean
    public JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor(JobRegistry jobRegistry) {
        JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor = new JobRegistryBeanPostProcessor();
        jobRegistryBeanPostProcessor.setJobRegistry(jobRegistry);
        return jobRegistryBeanPostProcessor;
    }
}