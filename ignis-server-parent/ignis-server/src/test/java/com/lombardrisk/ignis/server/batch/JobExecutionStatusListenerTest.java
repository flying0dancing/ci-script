package com.lombardrisk.ignis.server.batch;

import com.lombardrisk.ignis.common.log.CorrelationId;
import com.lombardrisk.ignis.server.batch.staging.JobExecutionStatusService;
import com.lombardrisk.ignis.server.job.fixture.JobPopulated;
import io.vavr.control.Option;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class JobExecutionStatusListenerTest {

    @Mock
    private JobExecutionStatusService jobExecutionStatusService;
    @Mock
    private JobExecution jobExecution;
    @Mock
    private JobParameters jobParameters;

    @InjectMocks
    private JobExecutionStatusListener listener;

    @Before
    public void setUp() {
        when(jobExecution.getJobParameters())
                .thenReturn(jobParameters);

        when(jobExecutionStatusService.updateServiceRequest(any()))
                .thenReturn(Option.none());
    }

    @Test
    public void beforeJob_SetsCorrelationId() {
        when(jobParameters.getString("correlationId"))
                .thenReturn("abcdef12345");

        listener.beforeJob(jobExecution);

        assertThat(CorrelationId.getCorrelationId())
                .isEqualTo("abcdef12345");
    }

    @Test
    public void beforeJob_ServiceRequestNotFound_StopsJob() {
        when(jobExecutionStatusService.updateServiceRequest(any()))
                .thenReturn(Option.none());

        listener.beforeJob(jobExecution);

        verify(jobExecution).stop();
    }

    @Test
    public void beforeJob_ServiceRequestFound_DoesNotStopJob() {
        when(jobExecutionStatusService.updateServiceRequest(any()))
                .thenReturn(Option.of(JobPopulated.stagingJobServiceRequest().build()));

        listener.beforeJob(jobExecution);

        verify(jobExecution, never()).stop();
    }

    @Test
    public void beforeJob_UpdatesServiceRequestStatus() {
        when(jobExecutionStatusService.updateServiceRequest(any()))
                .thenReturn(Option.none());

        listener.beforeJob(jobExecution);

        verify(jobExecutionStatusService).updateServiceRequest(jobExecution);
    }

    @Test
    public void afterJob_SetsCorrelationId() {
        when(jobParameters.getString("correlationId"))
                .thenReturn("abcdef12345");

        listener.afterJob(jobExecution);

        assertThat(CorrelationId.getCorrelationId())
                .isEqualTo("abcdef12345");
    }

    @Test
    public void afterJob_UpdatesServiceRequestStatus() {
        listener.afterJob(jobExecution);

        verify(jobExecutionStatusService).updateServiceRequest(jobExecution);
    }
}