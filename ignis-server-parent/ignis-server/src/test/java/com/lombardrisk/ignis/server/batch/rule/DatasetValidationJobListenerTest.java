package com.lombardrisk.ignis.server.batch.rule;

import com.google.common.collect.ImmutableMap;
import com.lombardrisk.ignis.server.batch.staging.JobExecutionStatusService;
import com.lombardrisk.ignis.server.dataset.fixture.DatasetPopulated;
import io.vavr.control.Option;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;

import static com.lombardrisk.ignis.server.job.servicerequest.ServiceRequest.CORRELATION_ID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DatasetValidationJobListenerTest {

    @InjectMocks
    private DatasetValidationJobListener listener;
    @Mock
    private JobExecutionStatusService jobExecutionStatusService;

    @Test
    public void beforeJob_CallsJobStatusService() {
        JobExecution jobExecution = jobExecution(123);

        when(jobExecutionStatusService.updateDatasetStatus(any()))
                .thenReturn(Option.none());

        listener.beforeJob(jobExecution);

        verify(jobExecutionStatusService).updateDatasetStatus(jobExecution);
    }

    @Test
    public void beforeJob_DatasetNotFound_StopsJob() {
        JobExecution jobExecution = mock(JobExecution.class);
        when(jobExecution.getJobParameters())
                .thenReturn(new JobParameters(ImmutableMap.of(CORRELATION_ID, new JobParameter("cor"))));

        when(jobExecutionStatusService.updateDatasetStatus(any()))
                .thenReturn(Option.none());

        listener.beforeJob(jobExecution);

        verify(jobExecution).stop();
    }

    @Test
    public void beforeJob_DatasetFound_DoesNotStopJob() {
        JobExecution jobExecution = mock(JobExecution.class);
        when(jobExecution.getJobParameters())
                .thenReturn(new JobParameters(ImmutableMap.of(CORRELATION_ID, new JobParameter("cor"))));

        when(jobExecutionStatusService.updateDatasetStatus(any()))
                .thenReturn(Option.of(DatasetPopulated.dataset().build()));

        listener.beforeJob(jobExecution);

        verify(jobExecution, never()).stop();
    }

    @Test
    public void afterJob_CallsJobStatusService() {
        JobExecution jobExecution = jobExecution(123);

        listener.afterJob(jobExecution);

        verify(jobExecutionStatusService).updateDatasetStatus(jobExecution);
    }

    private static JobExecution jobExecution(final long id) {
        JobExecution jobExecution = new JobExecution(id);
        jobExecution.getJobParameters()
                .getParameters()
                .put(CORRELATION_ID, new JobParameter("cid"));
        return jobExecution;
    }
}
