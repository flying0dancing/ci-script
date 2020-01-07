package com.lombardrisk.ignis.server.batch;

import com.lombardrisk.ignis.server.util.TempDirectory;
import io.vavr.control.Try;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;

import java.nio.file.Paths;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TempDirectoryJobListenerTest {

    @Mock
    private JobExecution jobExecution;
    @Mock
    private JobParameters jobParameters;
    @Mock
    private TempDirectory tempDirectory;

    @InjectMocks
    private TempDirectoryJobListener jobListener;

    @Before
    public void setUp() {
        when(jobExecution.getJobParameters())
                .thenReturn(jobParameters);

        when(jobParameters.getString(any()))
                .thenReturn("123456");
    }

    @Test
    public void beforeJob_ServiceRequestIdNotSet_StopsJobExecution() {
        when(jobParameters.getString("serviceRequestId"))
                .thenReturn(null);

        jobListener.beforeJob(jobExecution);

        verify(jobExecution).stop();
        verifyZeroInteractions(tempDirectory);
    }

    @Test
    public void beforeJob_ExceptionThrownWhenCreatingDirectory_StopsJobExecution() {
        when(tempDirectory.createDirectory(any()))
                .thenReturn(Try.failure(new RuntimeException("Failed to created directory")));

        jobListener.beforeJob(jobExecution);

        verify(jobExecution).stop();
    }

    @Test
    public void beforeJob_ServiceRequestIdSet_CreatesDirectory() {
        when(jobParameters.getString("serviceRequestId"))
                .thenReturn("123456789");

        when(tempDirectory.createDirectory(any()))
                .thenReturn(Try.success(Paths.get("123456789")));

        jobListener.beforeJob(jobExecution);

        verify(tempDirectory).createDirectory("123456789");
        verify(jobExecution, never()).stop();
    }

    @Test
    public void afterJob_ServiceRequestIdNotSet_StopsJobExecution() {
        when(jobParameters.getString("serviceRequestId"))
                .thenReturn(null);

        jobListener.afterJob(jobExecution);

        verify(jobExecution).stop();
        verifyZeroInteractions(tempDirectory);
    }

    @Test
    public void afterJob_ExceptionThrownWhenDeletingDirectory_StopsJobExecution() {
        when(tempDirectory.delete(any()))
                .thenReturn(Try.failure(new RuntimeException("Failed to delete directory")));

        jobListener.afterJob(jobExecution);

        verify(jobExecution).stop();
    }

    @Test
    public void afterJob_ServiceRequestIdSet_DeletesDirectory() {
        when(jobParameters.getString("serviceRequestId"))
                .thenReturn("123456789");

        when(tempDirectory.delete(any()))
                .thenReturn(Try.success(true));

        jobListener.afterJob(jobExecution);

        verify(tempDirectory).delete("123456789");
        verify(jobExecution, never()).stop();
    }
}