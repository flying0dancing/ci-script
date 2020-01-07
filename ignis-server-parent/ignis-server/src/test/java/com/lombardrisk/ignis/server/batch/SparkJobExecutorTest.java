package com.lombardrisk.ignis.server.batch;

import com.google.common.collect.ImmutableMap;
import com.lombardrisk.ignis.common.log.CorrelationId;
import com.lombardrisk.ignis.server.fixtures.Populated;
import com.lombardrisk.ignis.server.job.fixture.JobPopulated;
import com.lombardrisk.ignis.server.job.servicerequest.ServiceRequest;
import com.lombardrisk.ignis.server.job.servicerequest.ServiceRequestRepository;
import com.lombardrisk.ignis.server.util.spark.AppSubmitter;
import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;

import java.io.IOException;
import java.util.Optional;

import static com.lombardrisk.ignis.server.job.servicerequest.ServiceRequest.CORRELATION_ID;
import static com.lombardrisk.ignis.server.job.servicerequest.ServiceRequest.SERVICE_REQUEST_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SparkJobExecutorTest {

    @Mock
    private YarnClient yarnClient;
    @Mock
    private ServiceRequestRepository serviceRequestRepository;
    @Mock
    private AppSubmitter appSubmitter;

    @InjectMocks
    private SparkJobExecutor sparkJobExecutor;

    private AppId appId = new AppId();

    @Captor
    private ArgumentCaptor<ApplicationId> appIdCaptor;
    @Captor
    private ArgumentCaptor<ServiceRequest> serviceRequestCaptor;

    private final StepExecution stepExecution = new StepExecution(
            "test",
            new JobExecution(0L, new JobParameters(ImmutableMap.of(SERVICE_REQUEST_ID, new JobParameter("0")))));
    private ChunkContext chunkContext = new ChunkContext(new StepContext(stepExecution));
    private StepContribution stepContribution = new StepContribution(stepExecution);

    @Before
    public void setup() throws IOException {
        when(serviceRequestRepository.findById(any()))
                .thenReturn(Optional.of(
                        JobPopulated.stagingJobServiceRequest().build()));

        when(appSubmitter.submit(any()))
                .thenReturn(Populated.appSession().build());
    }

    @Test
    public void setupRequest_SetsUpCorrelationId() throws JobExecutionException {
        ChunkContext chunkContext = new ChunkContext(new StepContext(new StepExecution(
                "staging",
                new JobExecution(
                        0L,
                        new JobParameters(ImmutableMap.of(
                                SERVICE_REQUEST_ID, new JobParameter("0"),
                                CORRELATION_ID, new JobParameter("999-A")))))));
        sparkJobExecutor.setupRequest(chunkContext);

        assertThat(CorrelationId.getCorrelationId())
                .isEqualTo("999-A");
    }

    @Test
    public void setupRequest_FindsServiceRequest() throws JobExecutionException {
        ServiceRequest serviceRequest = JobPopulated.stagingJobServiceRequest().build();
        when(serviceRequestRepository.findById(any()))
                .thenReturn(Optional.of(serviceRequest));

        ServiceRequest setUpServiceRequest = sparkJobExecutor.setupRequest(chunkContext);

        assertThat(setUpServiceRequest).isSameAs(serviceRequest);
    }

    @Test
    public void stop_TaskletAppIdSet_KillsYarnApplication() throws Exception {
        when(appSubmitter.submit(any()))
                .thenReturn(Populated.appSession()
                        .appId(22L, 55)
                        .build());

        AppId taskletScopedAppId = AppId.valueOf(22L, 55);
        sparkJobExecutor.executeSparkJob(
                stepContribution,
                chunkContext,
                taskletScopedAppId,
                Populated.sparkSubmitOption().build());

        sparkJobExecutor.stop(taskletScopedAppId);

        verify(yarnClient)
                .killApplication(appIdCaptor.capture());

        //noinspection unchecked
        assertThat(appIdCaptor.getValue())
                .extracting(ApplicationId::getClusterTimestamp, ApplicationId::getId)
                .containsSequence(22L, 55);
    }

    @Test
    public void executeSparkJob_FindsServiceRequest() throws JobExecutionException {
        ChunkContext chunkContext = new ChunkContext(new StepContext(new StepExecution(
                "test", new JobExecution(
                0L,
                new JobParameters(ImmutableMap.of(SERVICE_REQUEST_ID, new JobParameter("43")))))));

        sparkJobExecutor.executeSparkJob(
                stepContribution,
                chunkContext,
                appId, Populated.sparkSubmitOption().build());

        verify(serviceRequestRepository).findById(43L);
    }

    @Test
    public void executeSparkJob_SubmitsApp() throws Exception {
        sparkJobExecutor.executeSparkJob(
                stepContribution,
                chunkContext,
                appId,
                Populated.sparkSubmitOption().build());

        verify(appSubmitter).submit(any());
    }

    @Test
    public void executeSparkJob_SetsAppId() throws Exception {
        when(appSubmitter.submit(any()))
                .thenReturn(Populated.appSession()
                        .appId(53L, 7)
                        .build());

        sparkJobExecutor.executeSparkJob(
                stepContribution,
                chunkContext,
                appId,
                Populated.sparkSubmitOption().build());

        assertThat(appId)
                .extracting(AppId::getClusterTimestamp, AppId::getId)
                .containsSequence(53L, 7);
    }

    @Test
    public void executeSparkJob_SavesTrackingUrl() throws Exception {
        when(appSubmitter.submit(any()))
                .thenReturn(Populated.appSession()
                        .appId(5443L, 7778)
                        .build());

        sparkJobExecutor.executeSparkJob(
                stepContribution,
                chunkContext,
                appId,
                Populated.sparkSubmitOption().build());

        verify(serviceRequestRepository)
                .save(serviceRequestCaptor.capture());

        assertThat(serviceRequestCaptor.getValue().getTrackingUrl())
                .contains("5443", "7778");
    }

    @Test
    public void executeSparkJob_SetsAppStatus() throws JobExecutionException {
        sparkJobExecutor.executeSparkJob(
                stepContribution,
                chunkContext,
                appId,
                Populated.sparkSubmitOption().build());

        assertThat(stepContribution.getExitStatus())
                .isEqualTo(ExitStatus.FAILED);
    }
}
