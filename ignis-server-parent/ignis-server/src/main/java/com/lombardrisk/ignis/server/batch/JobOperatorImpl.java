package com.lombardrisk.ignis.server.batch;

import com.lombardrisk.ignis.common.log.CorrelationId;
import com.lombardrisk.ignis.data.common.error.ErrorResponse;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.data.common.service.RetrieveService;
import com.lombardrisk.ignis.server.job.exception.JobStartException;
import com.lombardrisk.ignis.server.job.servicerequest.ServiceRequest;
import com.lombardrisk.ignis.server.job.servicerequest.ServiceRequestRepository;
import com.lombardrisk.ignis.server.job.staging.JobStarter;
import io.vavr.control.Option;
import io.vavr.control.Validation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobExecutionNotRunningException;
import org.springframework.batch.core.launch.JobInstanceAlreadyExistsException;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.launch.NoSuchJobExecutionException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static com.lombardrisk.ignis.server.job.servicerequest.ServiceRequest.CORRELATION_ID;
import static com.lombardrisk.ignis.server.job.servicerequest.ServiceRequest.SERVICE_REQUEST_ID;
import static java.util.Collections.singletonList;
import static org.springframework.batch.support.PropertiesConverter.propertiesToString;

@Slf4j
public class JobOperatorImpl implements RetrieveService<ServiceRequest>, JobStarter {

    private static final String NOT_RUNNING = "NOT_RUNNING";
    private static final String ALREADY_RUNNING = "ALREADY_RUNNING";

    private final org.springframework.batch.core.launch.JobOperator jobOperator;
    private final ServiceRequestRepository serviceRequestRepository;

    public JobOperatorImpl(
            final org.springframework.batch.core.launch.JobOperator jobOperator,
            final ServiceRequestRepository serviceRequestRepository) {

        this.jobOperator = jobOperator;
        this.serviceRequestRepository = serviceRequestRepository;
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

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public long startJob(
            final String jobName,
            final ServiceRequest serviceRequest,
            final Properties properties) throws JobStartException {

        properties.setProperty(SERVICE_REQUEST_ID, String.valueOf(serviceRequest.getId()));
        properties.setProperty(CORRELATION_ID, CorrelationId.getCorrelationId());
        try {
            long jobExecutionId = jobOperator.start(jobName, propertiesToString(properties));
            log.debug("Start job with name [{}] id [{}]", jobName, jobExecutionId);

            return serviceRequest.getId();
        } catch (NoSuchJobException | JobInstanceAlreadyExistsException | JobParametersInvalidException e) {
            throw new JobStartException(e);
        }
    }

    public Validation<List<ErrorResponse>, Boolean> stop(final long executionId) {
        Validation<CRUDFailure, ServiceRequest> serviceRequest = findWithValidation(executionId);

        if (serviceRequest.isInvalid()) {
            return Validation.invalid(singletonList(
                    serviceRequest.getError().toErrorResponse()));
        }

        try {
            boolean stopped = jobOperator.stop(serviceRequest.get().getJobExecutionId());
            return Validation.valid(stopped);
        } catch (final NoSuchJobExecutionException e) {
            log.error("Job not found in spring batch for id [{}]", executionId, e);
            return Validation.invalid(singletonList(
                    notFound(executionId).toErrorResponse()));
        } catch (final JobExecutionNotRunningException e) {
            log.error("Job to be stopped is not running [{}]", executionId, e);

            return abandon(serviceRequest.get())
                    .mapError(errorResponse -> Arrays.asList(
                            ErrorResponse.valueOf("Job [" + executionId + "] was not running", NOT_RUNNING),
                            errorResponse));
        }
    }

    private Validation<ErrorResponse, Boolean> abandon(final ServiceRequest serviceRequest) {
        Long executionId = serviceRequest.getJobExecutionId();
        try {
            jobOperator.abandon(executionId);
            return Validation.valid(true);
        } catch (final NoSuchJobExecutionException e) {
            log.error("Job not found in spring batch for id [{}]", executionId, e);
            return Validation.invalid(notFound(executionId).toErrorResponse());
        } catch (JobExecutionAlreadyRunningException e) {
            log.error("Job to be stopped is already running [{}]", executionId, e);
            return Validation.invalid(
                    ErrorResponse.valueOf(
                            "Job [" + executionId + "] was already running, should be stopped first",
                            ALREADY_RUNNING));
        }
    }
}
