package com.lombardrisk.ignis.server.config;

import com.lombardrisk.ignis.server.config.batch.BatchConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class BatchConfigurationTest {

    @Mock
    private JobRepository jobRepository;

    @InjectMocks
    private BatchConfiguration batchConfiguration;

    @Test
    public void jobLauncher() {
        JobLauncher jobLauncher = batchConfiguration.jobLauncher();
        assertThat(jobLauncher).isNotNull();
    }

    @Test
    public void jobRegistryBeanPostProcessor() {
        JobRegistry jobRegistry = Mockito.mock(JobRegistry.class);
        assertThat(batchConfiguration.jobRegistryBeanPostProcessor(jobRegistry)).isNotNull();
    }
}