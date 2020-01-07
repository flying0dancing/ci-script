package com.lombardrisk.ignis.server.batch;

import com.lombardrisk.ignis.common.assertions.VavrAssert;
import com.lombardrisk.ignis.common.log.CorrelationId;
import com.lombardrisk.ignis.data.common.error.ErrorResponse;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.server.job.fixture.JobPopulated;
import com.lombardrisk.ignis.server.job.servicerequest.ServiceRequest;
import com.lombardrisk.ignis.server.job.servicerequest.ServiceRequestRepository;
import io.vavr.control.Validation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobExecutionNotRunningException;
import org.springframework.batch.core.launch.NoSuchJobExecutionException;

import java.util.List;
import java.util.Optional;
import java.util.Properties;

import static com.lombardrisk.ignis.server.job.servicerequest.ServiceRequest.CORRELATION_ID;
import static com.lombardrisk.ignis.server.job.servicerequest.ServiceRequest.SERVICE_REQUEST_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class JobOperatorImplTest {

    @Mock
    private org.springframework.batch.core.launch.JobOperator springJobOperator;
    @Mock
    private ServiceRequestRepository serviceRequestRepository;
    @Mock
    private JobExplorer jobExplorer;

    @InjectMocks
    private JobOperatorImpl jobOperatorImpl;

    @Before
    public void setUp() {
        CorrelationId.setCorrelationId("CorrelationId");
    }

    @Test
    public void startJob_ReturnsServiceRequestId() throws Exception {
        ServiceRequest serviceRequest = JobPopulated.stagingJobServiceRequest().id(7777L).build();

        when(springJobOperator.start(anyString(), anyString()))
                .thenReturn(1234567L);

        long jobExecutionId =
                jobOperatorImpl.startJob("job name", serviceRequest, new Properties());

        assertThat(jobExecutionId).isEqualTo(7777L);
    }

    @Test
    public void startJob_SetsCorrelationIdProperty() throws Exception {
        CorrelationId.setCorrelationId("1237");

        ServiceRequest serviceRequest = JobPopulated.stagingJobServiceRequest().id(7777L).build();

        when(springJobOperator.start(anyString(), anyString()))
                .thenReturn(1234567L);

        Properties properties = new Properties();

        jobOperatorImpl.startJob("job name", serviceRequest, properties);

        assertThat(properties.get(CORRELATION_ID)).isEqualTo("1237");
    }

    @Test
    public void startJob_SetsServiceRequestIdProperty() throws Exception {
        ServiceRequest serviceRequest = JobPopulated.stagingJobServiceRequest().id(7777L).build();

        when(springJobOperator.start(anyString(), anyString()))
                .thenReturn(1234567L);

        Properties properties = new Properties();

        jobOperatorImpl.startJob("job name", serviceRequest, properties);

        assertThat(properties.get(SERVICE_REQUEST_ID)).isEqualTo("7777");
    }

    @Test
    public void stop_JobServiceRequestNotFound_ReturnsNotFound() {
        when(serviceRequestRepository.findById(any()))
                .thenReturn(Optional.empty());

        Validation<List<ErrorResponse>, Boolean> result = jobOperatorImpl.stop(100);

        VavrAssert.assertCollectionFailure(result)
                .withFailure(CRUDFailure.notFoundIds("ServiceRequest", 100L).toErrorResponse());
    }

    @Test
    public void stop_JobStopThrowsNoSuchJobException_ReturnsNotFound() throws Exception {
        when(serviceRequestRepository.findById(any()))
                .thenReturn(Optional.of(JobPopulated.stagingJobServiceRequest().build()));

        when(springJobOperator.stop(anyLong()))
                .thenThrow(new NoSuchJobExecutionException("Job does not exist"));

        Validation<List<ErrorResponse>, Boolean> result = jobOperatorImpl.stop(100);

        VavrAssert.assertCollectionFailure(result)
                .withFailure(CRUDFailure.notFoundIds("ServiceRequest", 100L).toErrorResponse());
    }

    @Test
    public void stop_JobNotRunning_CallsAbandon() throws Exception {
        when(serviceRequestRepository.findById(any()))
                .thenReturn(Optional.of(JobPopulated.stagingJobServiceRequest().build()));

        when(springJobOperator.stop(anyLong()))
                .thenThrow(new JobExecutionNotRunningException("Job not running"));
        when(springJobOperator.abandon(anyLong()))
                .thenReturn(new JobExecution(100L));

        Validation<List<ErrorResponse>, Boolean> result = jobOperatorImpl.stop(100);

        VavrAssert.assertValid(result)
                .withResult(true);

        verify(springJobOperator).abandon(100);
    }

    @Test
    public void stop_JobNotRunningAbandonFails_ReturnsErrors() throws Exception {
        when(serviceRequestRepository.findById(any()))
                .thenReturn(Optional.of(JobPopulated.stagingJobServiceRequest().build()));

        when(springJobOperator.stop(anyLong()))
                .thenThrow(new JobExecutionNotRunningException("Job not running"));
        when(springJobOperator.abandon(anyLong()))
                .thenThrow(new NoSuchJobExecutionException("Ooops"));

        Validation<List<ErrorResponse>, Boolean> result = jobOperatorImpl.stop(100);

        VavrAssert.assertCollectionFailure(result)
                .withFailure(ErrorResponse.valueOf("Job [100] was not running", "NOT_RUNNING"))
                .withFailure(ErrorResponse.valueOf("Could not find ServiceRequest for ids [100]", "NOT_FOUND"));
    }

    @Test
    public void stop_JobStopped_ReturnsTrue() throws Exception {
        when(serviceRequestRepository.findById(any()))
                .thenReturn(Optional.of(JobPopulated.stagingJobServiceRequest().build()));

        when(springJobOperator.stop(anyLong()))
                .thenReturn(true);

        Validation<List<ErrorResponse>, Boolean> result = jobOperatorImpl.stop(100);

        assertThat(result.get())
                .isTrue();
    }
}
