package com.lombardrisk.ignis.server.batch;

import com.google.common.collect.ImmutableMap;
import com.lombardrisk.ignis.api.dataset.DatasetState;
import com.lombardrisk.ignis.client.external.job.ExternalExitStatus;
import com.lombardrisk.ignis.client.external.job.JobStatus;
import com.lombardrisk.ignis.server.batch.staging.JobExecutionStatusService;
import com.lombardrisk.ignis.server.dataset.DatasetJpaRepository;
import com.lombardrisk.ignis.server.dataset.fixture.DatasetPopulated;
import com.lombardrisk.ignis.server.dataset.model.Dataset;
import com.lombardrisk.ignis.server.job.fixture.JobPopulated;
import com.lombardrisk.ignis.server.job.servicerequest.ServiceRequest;
import com.lombardrisk.ignis.server.job.servicerequest.ServiceRequestRepository;
import com.lombardrisk.ignis.server.job.servicerequest.ServiceRequestType;
import com.lombardrisk.ignis.server.job.staging.StagingDatasetRepository;
import com.lombardrisk.ignis.server.job.staging.model.StagingDataset;
import io.vavr.control.Option;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;

import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

import static com.lombardrisk.ignis.api.dataset.ValidationStatus.NOT_VALIDATED;
import static com.lombardrisk.ignis.api.dataset.ValidationStatus.VALIDATED;
import static com.lombardrisk.ignis.api.dataset.ValidationStatus.VALIDATING;
import static com.lombardrisk.ignis.api.dataset.ValidationStatus.VALIDATION_FAILED;
import static com.lombardrisk.ignis.server.job.servicerequest.ServiceRequest.SERVICE_REQUEST_ID;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class JobExecutionStatusServiceTest {

    @InjectMocks
    private JobExecutionStatusService jobStatusService;
    @Mock
    private ServiceRequestRepository serviceRequestRepository;
    @Mock
    private DatasetJpaRepository datasetRepository;
    @Mock
    private StagingDatasetRepository stagingDatasetRepository;
    @Mock
    private JobExecution jobExecution;

    @Before
    public void setUp() {
        when(jobExecution.getStatus()).thenReturn(BatchStatus.COMPLETED);
        when(jobExecution.getExitStatus()).thenReturn(ExitStatus.COMPLETED);
        when(jobExecution.getJobParameters()).thenReturn(jobParameters(SERVICE_REQUEST_ID, "12345"));
    }

    @Test
    public void updateServiceRequest_ServiceRequestNotFound_ReturnsEmpty() {
        when(serviceRequestRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        Option<ServiceRequest> serviceRequest = jobStatusService.updateServiceRequest(jobExecution);

        assertThat(serviceRequest).isEmpty();

        verify(serviceRequestRepository).findById(12345L);
        verifyNoMoreInteractions(serviceRequestRepository);
    }

    @Test
    public void updateServiceRequest_ServiceRequestFound_UpdatesServiceRequest() {
        Date startTime = new Calendar.Builder().setDate(2018, 11, 31).build().getTime();
        Date endTime = new Calendar.Builder().setDate(2018, 12, 1).build().getTime();

        ServiceRequest serviceRequest = JobPopulated.stagingJobServiceRequest()
                .serviceRequestType(ServiceRequestType.VALIDATION)
                .id(12345L)
                .requestMessage("7777")
                .status(JobStatus.STARTED)
                .exitCode(ExternalExitStatus.UNKNOWN)
                .build();

        when(serviceRequestRepository.findById(anyLong()))
                .thenReturn(Optional.of(serviceRequest));

        when(jobExecution.getStatus())
                .thenReturn(BatchStatus.COMPLETED);

        when(jobExecution.getExitStatus())
                .thenReturn(ExitStatus.FAILED);

        when(jobExecution.getStartTime())
                .thenReturn(startTime);

        when(jobExecution.getEndTime())
                .thenReturn(endTime);

        jobStatusService.updateServiceRequest(jobExecution);

        ArgumentCaptor<ServiceRequest> captor = ArgumentCaptor.forClass(ServiceRequest.class);
        verify(serviceRequestRepository).findById(12345L);
        verify(serviceRequestRepository).save(captor.capture());
        verifyNoMoreInteractions(serviceRequestRepository);

        assertThat(captor.getValue().getId())
                .isEqualTo(12345L);

        assertThat(captor.getValue().getStatus())
                .isEqualTo(JobStatus.COMPLETED);

        assertThat(captor.getValue().getExitCode())
                .isEqualTo(ExternalExitStatus.FAILED);

        assertThat(captor.getValue().getStartTime())
                .isEqualTo(startTime);

        assertThat(captor.getValue().getEndTime())
                .isEqualTo(endTime);
    }

    @Test
    public void updateServiceRequest_ServiceRequestFound_ReturnsServiceRequest() {
        ServiceRequest serviceRequest = JobPopulated.stagingJobServiceRequest()
                .serviceRequestType(ServiceRequestType.VALIDATION)
                .id(12345L)
                .requestMessage("7777")
                .status(JobStatus.STARTED)
                .exitCode(ExternalExitStatus.UNKNOWN)
                .build();

        when(serviceRequestRepository.findById(anyLong()))
                .thenReturn(Optional.of(serviceRequest));

        Option<ServiceRequest> updatedServiceRequest = jobStatusService.updateServiceRequest(jobExecution);

        assertThat(updatedServiceRequest).isNotEmpty();
    }

    @Test
    public void updateServiceRequest_serviceRequestTypeStagingAndWithStagingDataSetItemFailed_savesStatusFailed() {
        ServiceRequest stagingRequest = JobPopulated.stagingJobServiceRequest()
                .id(1245L)
                .serviceRequestType(ServiceRequestType.STAGING)
                .build();
        when(serviceRequestRepository.findById(anyLong())).thenReturn(Optional.of(stagingRequest));

        StagingDataset stagingDataSet1 = JobPopulated.stagingDataset()
                .status(DatasetState.VALIDATED)
                .build();
        StagingDataset stagingDataSet2 = JobPopulated.stagingDataset()
                .status(DatasetState.VALIDATION_FAILED)
                .build();

        when(stagingDatasetRepository.findByServiceRequestId(anyLong())).thenReturn(asList(stagingDataSet1,
                stagingDataSet2));

        jobStatusService.updateServiceRequest(jobExecution);

        ArgumentCaptor<ServiceRequest> captor = ArgumentCaptor.forClass(ServiceRequest.class);
        verify(serviceRequestRepository).save(captor.capture());

        assertThat(captor.getValue().getStatus())
                .isEqualTo(JobStatus.FAILED);
    }

    @Test
    public void updateServiceRequest_serviceRequestTypeValidationAndWithStagingDataSetItemFailed_savesStatusFailed() {
        ServiceRequest stagingRequest = JobPopulated.stagingJobServiceRequest()
                .id(1245L)
                .serviceRequestType(ServiceRequestType.VALIDATION)
                .build();
        when(serviceRequestRepository.findById(anyLong())).thenReturn(Optional.of(stagingRequest));

        StagingDataset stagingDataSet1 = JobPopulated.stagingDataset()
                .status(DatasetState.VALIDATION_FAILED)
                .build();
        StagingDataset stagingDataSet2 = JobPopulated.stagingDataset()
                .status(DatasetState.VALIDATED)
                .build();

        when(stagingDatasetRepository.findByServiceRequestId(anyLong())).thenReturn(asList(stagingDataSet1,
                stagingDataSet2));

        jobStatusService.updateServiceRequest(jobExecution);

        ArgumentCaptor<ServiceRequest> captor = ArgumentCaptor.forClass(ServiceRequest.class);
        verify(serviceRequestRepository).save(captor.capture());

        assertThat(captor.getValue().getStatus())
                .isEqualTo(JobStatus.FAILED);
    }

    @Test
    public void updateServiceRequest_serviceRequestTypeStagingAndWithNoStagingDataSetItemFailedFound_savesStatusCompleted() {
        ServiceRequest stagingRequest = JobPopulated.stagingJobServiceRequest()
                .id(1245L)
                .serviceRequestType(ServiceRequestType.STAGING)
                .build();
        when(serviceRequestRepository.findById(anyLong())).thenReturn(Optional.of(stagingRequest));

        StagingDataset stagingDataSet1 = JobPopulated.stagingDataset()
                .status(DatasetState.VALIDATED)
                .build();
        StagingDataset stagingDataSet2 = JobPopulated.stagingDataset()
                .status(DatasetState.VALIDATED)
                .build();

        when(stagingDatasetRepository.findByServiceRequestId(anyLong())).thenReturn(asList(stagingDataSet1,
                stagingDataSet2));

        jobStatusService.updateServiceRequest(jobExecution);

        ArgumentCaptor<ServiceRequest> captor = ArgumentCaptor.forClass(ServiceRequest.class);
        verify(serviceRequestRepository).save(captor.capture());

        assertThat(captor.getValue().getStatus())
                .isEqualTo(JobStatus.COMPLETED);
    }

    @Test
    public void updateServiceRequest_serviceRequestTypeValidationAndWithNoStagingDataSetItemFailedFound_savesStatusCompleted() {
        ServiceRequest stagingRequest = JobPopulated.stagingJobServiceRequest()
                .id(1245L)
                .serviceRequestType(ServiceRequestType.VALIDATION)
                .build();
        when(serviceRequestRepository.findById(anyLong())).thenReturn(Optional.of(stagingRequest));

        StagingDataset stagingDataSet1 = JobPopulated.stagingDataset()
                .status(DatasetState.VALIDATED)
                .build();
        StagingDataset stagingDataSet2 = JobPopulated.stagingDataset()
                .status(DatasetState.VALIDATED)
                .build();

        when(stagingDatasetRepository.findByServiceRequestId(anyLong())).thenReturn(asList(stagingDataSet1,
                stagingDataSet2));

        jobStatusService.updateServiceRequest(jobExecution);

        ArgumentCaptor<ServiceRequest> captor = ArgumentCaptor.forClass(ServiceRequest.class);
        verify(serviceRequestRepository).save(captor.capture());

        assertThat(captor.getValue().getStatus())
                .isEqualTo(JobStatus.COMPLETED);
    }

    @Test
    public void updateServiceRequest_jobStatusDifferentThanCompleted_savesJobExecutionStatus() {
        when(jobExecution.getStatus()).thenReturn(BatchStatus.FAILED);

        when(serviceRequestRepository.findById(anyLong())).thenReturn(Optional.of(anyStagingRequest()));

        jobStatusService.updateServiceRequest(jobExecution);

        ArgumentCaptor<ServiceRequest> captor = ArgumentCaptor.forClass(ServiceRequest.class);
        verify(serviceRequestRepository).save(captor.capture());

        assertThat(captor.getValue().getStatus())
                .isEqualTo(JobStatus.FAILED);
    }

    @Test
    public void updateServiceRequest_jobStatusDifferentThanCompleted_neverDelegatesToStagingDatasetRepoitory() {
        when(jobExecution.getStatus()).thenReturn(BatchStatus.FAILED);

        when(serviceRequestRepository.findById(anyLong())).thenReturn(Optional.of(anyStagingRequest()));

        jobStatusService.updateServiceRequest(jobExecution);

        verify(stagingDatasetRepository, never()).findByServiceRequestId(anyLong());
    }

    @Test
    public void updateServiceRequest_serviceRequestTypeDifferentThanValidationAndStaging_savesJobExecutionStatus() {
        when(jobExecution.getStatus()).thenReturn(BatchStatus.FAILED);

        ServiceRequest serviceRequest = JobPopulated.stagingJobServiceRequest()
                .serviceRequestType(ServiceRequestType.IMPORT_PRODUCT)
                .build();
        when(serviceRequestRepository.findById(anyLong())).thenReturn(Optional.of(serviceRequest));

        jobStatusService.updateServiceRequest(jobExecution);

        ArgumentCaptor<ServiceRequest> captor = ArgumentCaptor.forClass(ServiceRequest.class);
        verify(serviceRequestRepository).save(captor.capture());

        assertThat(captor.getValue().getStatus())
                .isEqualTo(JobStatus.FAILED);
    }

    @Test
    public void updateServiceRequest_serviceRequestTypeDifferentThanValidationAndStaging_neverDelegatesToStagingDataSetRepoitory() {
        when(jobExecution.getStatus()).thenReturn(BatchStatus.COMPLETED);

        ServiceRequest serviceRequest = JobPopulated.stagingJobServiceRequest()
                .id(1245L)
                .serviceRequestType(ServiceRequestType.IMPORT_PRODUCT)
                .build();
        when(serviceRequestRepository.findById(anyLong())).thenReturn(Optional.of(serviceRequest));

        jobStatusService.updateServiceRequest(jobExecution);

        verify(stagingDatasetRepository, never()).findByServiceRequestId(anyLong());
    }

    private ServiceRequest anyStagingRequest() {
        return JobPopulated.stagingJobServiceRequest()
                .build();
    }

    @Test
    public void updateDatasetStatus_ServiceRequestParameterNotPassed_ReturnsEmpty() {
        when(jobExecution.getJobParameters())
                .thenReturn(new JobParameters(emptyMap()));

        Option<Dataset> dataset = jobStatusService.updateDatasetStatus(jobExecution);

        assertThat(dataset).isEmpty();
    }

    @Test
    public void updateDatasetStatus_ServiceRequestNotFound_ReturnsEmpty() {
        when(serviceRequestRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        Option<Dataset> dataset = jobStatusService.updateDatasetStatus(jobExecution);

        assertThat(dataset).isEmpty();
    }

    @Test
    public void updateDatasetStatus_ServiceRequestIdJobParameter_PassesIdToRepository() {
        when(jobExecution.getJobParameters())
                .thenReturn(jobParameters(SERVICE_REQUEST_ID, "999999"));

        when(serviceRequestRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        jobStatusService.updateDatasetStatus(jobExecution);

        verify(serviceRequestRepository).findById(999999L);
    }

    @Test
    public void updateDatasetStatus_ServiceRequestNotValidationType_ReturnsEmpty() {
        ServiceRequest stagingRequest = JobPopulated.stagingJobServiceRequest()
                .id(1245L)
                .serviceRequestType(ServiceRequestType.VALIDATION)
                .serviceRequestType(ServiceRequestType.STAGING)
                .build();
        when(serviceRequestRepository.findById(anyLong())).thenReturn(Optional.of(stagingRequest));

        Option<Dataset> dataset = jobStatusService.updateDatasetStatus(jobExecution);

        assertThat(dataset).isEmpty();
    }

    @Test
    public void updateDatasetStatus_DatasetNotFound_ReturnsEmpty() {
        ServiceRequest serviceRequest = JobPopulated.stagingJobServiceRequest()
                .id(1425L)
                .serviceRequestType(ServiceRequestType.VALIDATION)
                .serviceRequestType(ServiceRequestType.VALIDATION)
                .requestMessage("7777")
                .build();

        when(serviceRequestRepository.findById(anyLong()))
                .thenReturn(Optional.of(serviceRequest));

        when(datasetRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        Option<Dataset> dataset = jobStatusService.updateDatasetStatus(jobExecution);

        assertThat(dataset).isEmpty();
    }

    @Test
    public void updateDatasetStatus_DatasetIdOnServiceRequest_PassesIdToRepository() {
        ServiceRequest serviceRequest = JobPopulated.stagingJobServiceRequest()
                .id(1555L)
                .serviceRequestType(ServiceRequestType.VALIDATION)
                .serviceRequestType(ServiceRequestType.VALIDATION)
                .requestMessage("123456789")
                .build();

        when(serviceRequestRepository.findById(anyLong()))
                .thenReturn(Optional.of(serviceRequest));

        when(datasetRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        jobStatusService.updateDatasetStatus(jobExecution);

        verify(datasetRepository).findById(123456789L);
    }

    @Test
    public void updateDatasetStatus_JobStarting_SetsStatusToValidating() {
        ServiceRequest serviceRequest = JobPopulated.stagingJobServiceRequest()
                .serviceRequestType(ServiceRequestType.VALIDATION)
                .id(12345L)
                .requestMessage("7777")
                .build();

        Dataset previousDatasetState = DatasetPopulated.dataset()
                .id(7777L)
                .validationStatus(NOT_VALIDATED)
                .build();

        when(jobExecution.getStatus())
                .thenReturn(BatchStatus.STARTING);

        when(serviceRequestRepository.findById(anyLong()))
                .thenReturn(Optional.of(serviceRequest));

        when(datasetRepository.findById(anyLong()))
                .thenReturn(Optional.of(previousDatasetState));

        Option<Dataset> updatedDataset = jobStatusService.updateDatasetStatus(jobExecution);

        assertThat(updatedDataset.get().getValidationStatus()).isEqualTo(VALIDATING);
    }

    @Test
    public void updateDatasetStatus_JobStarted_SetsStatusToValidating() {
        ServiceRequest serviceRequest = JobPopulated.stagingJobServiceRequest()
                .serviceRequestType(ServiceRequestType.VALIDATION)
                .id(12345L)
                .requestMessage("7777")
                .build();

        Dataset previousDatasetState = DatasetPopulated.dataset()
                .id(7777L)
                .validationStatus(NOT_VALIDATED)
                .build();

        when(jobExecution.getStatus())
                .thenReturn(BatchStatus.STARTED);

        when(serviceRequestRepository.findById(anyLong()))
                .thenReturn(Optional.of(serviceRequest));

        when(datasetRepository.findById(anyLong()))
                .thenReturn(Optional.of(previousDatasetState));

        Option<Dataset> updatedDataset = jobStatusService.updateDatasetStatus(jobExecution);

        assertThat(updatedDataset.get().getValidationStatus()).isEqualTo(VALIDATING);
    }

    @Test
    public void updateDatasetStatus_JobCompleted_SetsStatusToValidated() {
        ServiceRequest serviceRequest = JobPopulated.stagingJobServiceRequest()
                .serviceRequestType(ServiceRequestType.VALIDATION)
                .id(12345L)
                .requestMessage("7777")
                .build();

        Dataset previousDatasetState = DatasetPopulated.dataset()
                .id(7777L)
                .validationStatus(NOT_VALIDATED)
                .build();

        when(jobExecution.getStatus())
                .thenReturn(BatchStatus.COMPLETED);

        when(serviceRequestRepository.findById(anyLong()))
                .thenReturn(Optional.of(serviceRequest));

        when(datasetRepository.findById(anyLong()))
                .thenReturn(Optional.of(previousDatasetState));

        Option<Dataset> updatedDataset = jobStatusService.updateDatasetStatus(jobExecution);

        assertThat(updatedDataset.get().getValidationStatus()).isEqualTo(VALIDATED);
    }

    @Test
    public void updateDatasetStatus_JobNotSuccessful_SetsStatusToValidationFailed() {
        ServiceRequest serviceRequest = JobPopulated.stagingJobServiceRequest()
                .serviceRequestType(ServiceRequestType.VALIDATION)
                .id(12345L)
                .requestMessage("7777")
                .build();

        Dataset previousDatasetState = DatasetPopulated.dataset()
                .id(7777L)
                .validationStatus(NOT_VALIDATED)
                .build();

        when(jobExecution.getStatus())
                .thenReturn(BatchStatus.FAILED);

        when(serviceRequestRepository.findById(anyLong()))
                .thenReturn(Optional.of(serviceRequest));

        when(datasetRepository.findById(anyLong()))
                .thenReturn(Optional.of(previousDatasetState));

        Option<Dataset> updatedDataset = jobStatusService.updateDatasetStatus(jobExecution);

        assertThat(updatedDataset.get().getValidationStatus()).isEqualTo(VALIDATION_FAILED);
    }

    @Test
    public void updateDatasetStatus_JobFinishedButExitStatusNotSuccessful_SetsStatusToValidationFailed() {
        ServiceRequest serviceRequest = JobPopulated.stagingJobServiceRequest()
                .serviceRequestType(ServiceRequestType.VALIDATION)
                .id(12345L)
                .requestMessage("7777")
                .build();

        Dataset previousDatasetState = DatasetPopulated.dataset()
                .id(7777L)
                .validationStatus(NOT_VALIDATED)
                .build();

        when(jobExecution.getStatus())
                .thenReturn(BatchStatus.COMPLETED);
        when(jobExecution.getExitStatus())
                .thenReturn(ExitStatus.FAILED);

        when(serviceRequestRepository.findById(anyLong()))
                .thenReturn(Optional.of(serviceRequest));

        when(datasetRepository.findById(anyLong()))
                .thenReturn(Optional.of(previousDatasetState));

        Option<Dataset> updatedDataset = jobStatusService.updateDatasetStatus(jobExecution);

        assertThat(updatedDataset.get().getValidationStatus()).isEqualTo(VALIDATION_FAILED);
    }

    @Test
    public void updateDatasetStatus_JobCompleted_SavesNewDatasetState() {
        ServiceRequest serviceRequest = JobPopulated.stagingJobServiceRequest()
                .serviceRequestType(ServiceRequestType.VALIDATION)
                .id(12345L)
                .requestMessage("7777")
                .build();

        Dataset previousDatasetState = DatasetPopulated.dataset()
                .id(7777L)
                .validationStatus(NOT_VALIDATED)
                .build();

        when(jobExecution.getStatus())
                .thenReturn(BatchStatus.COMPLETED);

        when(serviceRequestRepository.findById(anyLong()))
                .thenReturn(Optional.of(serviceRequest));

        when(datasetRepository.findById(anyLong()))
                .thenReturn(Optional.of(previousDatasetState));

        jobStatusService.updateDatasetStatus(jobExecution);

        ArgumentCaptor<Dataset> updatedDataset = ArgumentCaptor.forClass(Dataset.class);
        verify(datasetRepository).save(updatedDataset.capture());
        assertThat(updatedDataset.getValue().getId()).isEqualTo(7777L);
        assertThat(updatedDataset.getValue().getValidationStatus()).isEqualTo(VALIDATED);
    }

    @Test
    public void updateDatasetStatus_ServiceRequestFound_UpdatesServiceRequest() {
        ServiceRequest serviceRequest = JobPopulated.stagingJobServiceRequest()
                .serviceRequestType(ServiceRequestType.VALIDATION)
                .id(12345L)
                .requestMessage("7777")
                .build();

        Dataset previousDatasetState = DatasetPopulated.dataset()
                .id(7777L)
                .validationStatus(NOT_VALIDATED)
                .build();

        when(jobExecution.getStatus())
                .thenReturn(BatchStatus.COMPLETED);

        when(serviceRequestRepository.findById(anyLong()))
                .thenReturn(Optional.of(serviceRequest));

        when(datasetRepository.findById(anyLong()))
                .thenReturn(Optional.of(previousDatasetState));

        jobStatusService.updateDatasetStatus(jobExecution);

        ArgumentCaptor<ServiceRequest> updatedServiceRequest = ArgumentCaptor.forClass(ServiceRequest.class);
        verify(serviceRequestRepository).save(updatedServiceRequest.capture());
        assertThat(updatedServiceRequest.getValue().getId()).isEqualTo(12345L);
    }

    @SuppressWarnings("SameParameterValue")
    private static JobParameters jobParameters(final String key, final String value) {
        return new JobParameters(ImmutableMap.of(key, new JobParameter(value)));
    }
}
