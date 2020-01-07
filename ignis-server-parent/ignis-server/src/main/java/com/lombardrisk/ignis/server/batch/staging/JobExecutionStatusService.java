package com.lombardrisk.ignis.server.batch.staging;

import com.lombardrisk.ignis.api.dataset.DatasetState;
import com.lombardrisk.ignis.api.dataset.ValidationStatus;
import com.lombardrisk.ignis.client.external.job.ExternalExitStatus;
import com.lombardrisk.ignis.client.external.job.JobStatus;
import com.lombardrisk.ignis.server.dataset.DatasetJpaRepository;
import com.lombardrisk.ignis.server.dataset.model.Dataset;
import com.lombardrisk.ignis.server.job.servicerequest.ServiceRequest;
import com.lombardrisk.ignis.server.job.servicerequest.ServiceRequestRepository;
import com.lombardrisk.ignis.server.job.servicerequest.ServiceRequestType;
import com.lombardrisk.ignis.server.job.staging.StagingDatasetRepository;
import com.lombardrisk.ignis.server.job.staging.model.StagingDataset;
import io.vavr.control.Option;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.lombardrisk.ignis.api.dataset.ValidationStatus.VALIDATED;
import static com.lombardrisk.ignis.api.dataset.ValidationStatus.VALIDATING;
import static com.lombardrisk.ignis.api.dataset.ValidationStatus.VALIDATION_FAILED;
import static com.lombardrisk.ignis.server.job.servicerequest.ServiceRequest.SERVICE_REQUEST_ID;
import static java.util.Arrays.asList;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

@Slf4j
public class JobExecutionStatusService {
    private static final List<ServiceRequestType> REQUEST_TYPES_VALUES =
            asList(ServiceRequestType.STAGING, ServiceRequestType.VALIDATION);

    private static final List<DatasetState> FAIL_STATUS_VALUES = asList(
            DatasetState.VALIDATION_FAILED,
            DatasetState.UPLOAD_FAILED,
            DatasetState.REGISTRATION_FAILED);

    private final ServiceRequestRepository serviceRequestRepository;
    private final DatasetJpaRepository datasetRepository;
    private final StagingDatasetRepository stagingDatasetRepository;

    public JobExecutionStatusService(
            final ServiceRequestRepository serviceRequestRepository,
            final DatasetJpaRepository datasetRepository,
            final StagingDatasetRepository stagingDatasetRepository) {
        this.serviceRequestRepository = serviceRequestRepository;
        this.datasetRepository = datasetRepository;
        this.stagingDatasetRepository = stagingDatasetRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Option<ServiceRequest> updateServiceRequest(final JobExecution jobExecution) {
        Option<String> serviceRequestId = Option.of(jobExecution.getJobParameters().getString(SERVICE_REQUEST_ID));
        Option<ServiceRequest> serviceRequest = serviceRequestId
                .map(Long::valueOf)
                .flatMap(id -> Option.ofOptional(serviceRequestRepository.findById(id)));

        if (serviceRequest.isDefined()) {
            updateServiceRequestStatus(serviceRequest.get(), jobExecution);
        }

        return serviceRequest;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Option<Dataset> updateDatasetStatus(final JobExecution jobExecution) {
        Option<ServiceRequest> serviceRequest = updateServiceRequest(jobExecution);

        if (!serviceRequest.isDefined()
                || !ServiceRequestType.VALIDATION.equals(serviceRequest.get().getServiceRequestType())) {
            return Option.none();
        }

        Option<Dataset> dataset = Option.ofOptional(
                datasetRepository.findById(Long.valueOf(serviceRequest.get().getRequestMessage())));

        if (dataset.isDefined()) {
            updateDataset(dataset.get(), jobExecution.getStatus(), jobExecution.getExitStatus());
        }

        return dataset;
    }

    private void updateServiceRequestStatus(
            final ServiceRequest serviceRequest,
            final JobExecution jobExecution) {

        final JobStatus serviceRequestStatus = convertToJobStatus(jobExecution,serviceRequest,
                stagingDatasetRepository);

        serviceRequest.setStatus(serviceRequestStatus);
        serviceRequest.setExitCode(ExternalExitStatus.valueOf(jobExecution.getExitStatus().getExitCode()));
        serviceRequest.setStartTime(jobExecution.getStartTime());
        serviceRequest.setEndTime(jobExecution.getEndTime());
        serviceRequest.setJobExecutionId(jobExecution.getJobId());

        log.debug("Service request before update [{}]", serviceRequest);
        ServiceRequest updatedServiceRequest = serviceRequestRepository.save(serviceRequest);
        log.debug("Service request after update  [{}]", updatedServiceRequest);
    }

    private JobStatus convertToJobStatus(final JobExecution jobExecution,
                                         final ServiceRequest serviceRequest,
                             final StagingDatasetRepository stagingDatasetRepository) {
        if(BatchStatus.COMPLETED.equals(jobExecution.getStatus()) &&
                REQUEST_TYPES_VALUES.contains(serviceRequest.getServiceRequestType())) {
            List<StagingDataset> stagingDataSets =  stagingDatasetRepository.findByServiceRequestId(serviceRequest.getId());

            List<StagingDataset> failedStagingDataSets =  stagingDataSets.stream()
                    .filter(s-> FAIL_STATUS_VALUES.contains(s.getStatus()))
                    .collect(Collectors.toList());

            if(isNotEmpty(failedStagingDataSets)) {
                return JobStatus.FAILED;
            }
        }
        return JobStatus.valueOf(jobExecution.getStatus().toString());
    }

    private void updateDataset(
            final Dataset dataset,
            final BatchStatus batchStatus,
            final ExitStatus exitStatus) {

        ValidationStatus currentStatus = dataset.getValidationStatus();
        ValidationStatus newStatus = getNewDatasetStatus(batchStatus, exitStatus);
        dataset.setValidationStatus(newStatus);

        log.info("Change status [{}] to [{}] for dataset [{}]", currentStatus, newStatus, dataset.getId());
        datasetRepository.save(dataset);
    }

    private ValidationStatus getNewDatasetStatus(final BatchStatus batchStatus, final ExitStatus exitStatus) {
        switch (batchStatus) {
            case STARTING:
            case STARTED:
                return VALIDATING;
            case COMPLETED:
                return exitStatus.equals(ExitStatus.COMPLETED)
                        ? VALIDATED
                        : VALIDATION_FAILED;
            default:
                return VALIDATION_FAILED;
        }
    }
}
