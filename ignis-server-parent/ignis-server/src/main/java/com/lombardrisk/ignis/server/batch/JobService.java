package com.lombardrisk.ignis.server.batch;

import com.lombardrisk.ignis.client.external.job.JobExecutionView;
import com.lombardrisk.ignis.client.external.job.JobType;
import com.lombardrisk.ignis.common.time.TimeSource;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.data.common.search.FilterExpression;
import com.lombardrisk.ignis.data.common.service.RetrieveService;
import com.lombardrisk.ignis.server.dataset.DatasetJpaRepository;
import com.lombardrisk.ignis.server.job.JobFailure;
import com.lombardrisk.ignis.server.job.servicerequest.ServiceRequest;
import com.lombardrisk.ignis.server.job.servicerequest.ServiceRequestRepository;
import com.lombardrisk.ignis.server.job.servicerequest.ServiceRequestSpecificationBuilder;
import com.lombardrisk.ignis.server.job.servicerequest.ServiceRequestType;
import com.lombardrisk.ignis.server.job.staging.JobStarter;
import com.lombardrisk.ignis.server.util.converter.StringToFilterConverter;
import io.vavr.control.Either;
import io.vavr.control.Option;
import io.vavr.control.Try;
import io.vavr.control.Validation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import static com.lombardrisk.ignis.server.job.JobFailure.DATASET_NOT_FOUND;
import static com.lombardrisk.ignis.server.job.JobFailure.JOB_COULD_NOT_START;
import static com.lombardrisk.ignis.server.job.servicerequest.ServiceRequestType.VALIDATION;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

@Slf4j
public class JobService implements RetrieveService<ServiceRequest> {

    private final DatasetJpaRepository datasetRepository;
    private final ServiceRequestRepository serviceRequestRepository;
    private final ServiceRequestSpecificationBuilder specificationBuilder;
    private final JobExplorer jobExplorer;
    private final JobStarter jobStarter;
    private final TimeSource timeSource;

    public JobService(
            final DatasetJpaRepository datasetRepository,
            final ServiceRequestRepository serviceRequestRepository,
            final ServiceRequestSpecificationBuilder specificationBuilder,
            final JobExplorer jobExplorer,
            final JobStarter jobStarter,
            final TimeSource timeSource) {
        this.datasetRepository = datasetRepository;
        this.serviceRequestRepository = serviceRequestRepository;
        this.specificationBuilder = specificationBuilder;
        this.jobExplorer = jobExplorer;
        this.jobStarter = jobStarter;
        this.timeSource = timeSource;
    }

    @Override
    public String entityName() {
        return ServiceRequest.class.getSimpleName();
    }

    @Override
    public Option<ServiceRequest> findById(final long id) {
        return Option.ofOptional(serviceRequestRepository.findById(id));
    }

    @Override
    public List<ServiceRequest> findAllByIds(final Iterable<Long> ids) {
        return serviceRequestRepository.findAllById(ids);
    }

    @Override
    public List<ServiceRequest> findAll() {
        return serviceRequestRepository.findAll();
    }

    public Either<JobFailure, Long> startValidationJob(final Long datasetId, final String name, final String userName) {
        if (!datasetRepository.findById(datasetId).isPresent()) {
            return Either.left(DATASET_NOT_FOUND);
        }
        ServiceRequest serviceRequest = serviceRequestRepository.save(
                ServiceRequest.builder()
                        .name(name)
                        .requestMessage(datasetId.toString())
                        .serviceRequestType(VALIDATION)
                        .createdBy(userName)
                        .build());

        Long serviceRequestId = serviceRequest.getId();
        log.debug("Start validation job from service request [{}]", serviceRequestId);

        return Try.of(() -> jobStarter.startJob(VALIDATION.name(), serviceRequest, new Properties()))
                .onFailure(this::logError)
                .toRight(JOB_COULD_NOT_START);
    }

    private void logError(final Throwable exception) {
        log.error("Could not start job", exception);
    }

    public Validation<CRUDFailure, JobExecutionView> getJob(final Long id) {
        Validation<CRUDFailure, ServiceRequest> serviceRequestValidation = findWithValidation(id);

        if (serviceRequestValidation.isInvalid()) {
            log.warn("Service request not found for ID [{}]", id);
            return Validation.invalid(serviceRequestValidation.getError());
        }

        ServiceRequest serviceRequest = serviceRequestValidation.get();

        JobExecution jobExecution = jobExplorer.getJobExecution(serviceRequest.getJobExecutionId());

        if (jobExecution == null) {
            log.warn("Job execution not found for service request [{}], job execution id [{}]",
                    serviceRequest.getId(), serviceRequest.getJobExecutionId());

            return Validation.invalid(notFound(serviceRequest.getId()));
        }

        return Validation.valid(toResponseWithErrors(serviceRequest, jobExecution));
    }

    public Page<JobExecutionView> getAllJobs(final Pageable pageable) {
        Page<ServiceRequest> serviceRequestPage = serviceRequestRepository.findAll(pageable);
        return serviceRequestPage.map(this::toResponse);
    }

    public Page<JobExecutionView> searchJobs(
            final Pageable pageable, final String searchCriteria) {

        FilterExpression filter = StringToFilterConverter.toFilter(searchCriteria);
        Validation<List<CRUDFailure>, Specification<ServiceRequest>> specification = specificationBuilder.build(filter);

        if (specification.isInvalid()) {
            log.warn("Failed to validate search parameters: {}", formatCrudFailures(specification.getError()));
            return Page.empty(pageable);
        }

        Page<ServiceRequest> serviceRequestPage = serviceRequestRepository.findAll(specification.get(), pageable);
        return serviceRequestPage.map(this::toResponse);
    }

    private JobExecutionView toResponse(final ServiceRequest serviceRequest) {
        return baseJobExecutionView(serviceRequest).build();
    }

    private JobExecutionView toResponseWithErrors(
            final ServiceRequest serviceRequest, final JobExecution jobExecution) {

        return baseJobExecutionView(serviceRequest)
                .errors(toErrors(jobExecution.getStepExecutions()))
                .build();
    }

    private JobExecutionView.JobExecutionViewBuilder baseJobExecutionView(final ServiceRequest serviceRequest) {
        JobExecutionView.JobExecutionViewBuilder jobExecutionBuilder = JobExecutionView.builder()
                .yarnApplicationTrackingUrl(serviceRequest.getTrackingUrl())
                .id(serviceRequest.getId())
                .name(serviceRequest.getName())
                .errors(emptyList())
                .serviceRequestType(JobType.valueOf(serviceRequest.getServiceRequestType().name()))
                .exitCode(serviceRequest.getExitCode())
                .status(serviceRequest.getStatus())
                .startTime(toZonedDateTime(serviceRequest.getStartTime()))
                .endTime(toZonedDateTime(serviceRequest.getEndTime()))
                .createUser(serviceRequest.getCreatedBy());

        if (ServiceRequestType.VALIDATION == serviceRequest.getServiceRequestType()) {
            jobExecutionBuilder.requestMessage(serviceRequest.getRequestMessage());
        }
        return jobExecutionBuilder;
    }

    private static List<String> toErrors(final Collection<StepExecution> stepExecutions) {
        String jobExecutionException = JobExecutionException.class.getSimpleName();

        return stepExecutions.stream()
                .map(stepExecution -> stepExecution.getExitStatus().getExitDescription())
                .filter(StringUtils::isNotBlank)
                .filter(exitDescription -> exitDescription.contains(jobExecutionException))
                .map(exitDescription -> exitDescription
                        .substring(exitDescription.indexOf(jobExecutionException) + jobExecutionException.length() + 1)
                        .split("\n")[0]
                        .trim())
                .collect(toList());
    }

    private String formatCrudFailures(final List<CRUDFailure> crudFailures) {
        return crudFailures.stream()
                .map(failure -> String.format("[%s: %s]",
                        failure.getErrorCode(), failure.getErrorMessage()))
                .collect(Collectors.joining(", "));
    }

    private ZonedDateTime toZonedDateTime(final Date date) {
        return Option.of(date)
                .map(timeSource::toZonedDateTime)
                .getOrNull();
    }
}
