package com.lombardrisk.ignis.server.batch.product;

import com.lombardrisk.ignis.server.batch.SparkJobExecutor;
import com.lombardrisk.ignis.server.job.servicerequest.ServiceRequest;
import com.lombardrisk.ignis.server.job.servicerequest.ServiceRequestRepository;
import com.lombardrisk.ignis.server.product.productconfig.model.ProductImportContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.StoppableTasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

import static com.lombardrisk.ignis.common.json.MapperWrapper.MAPPER;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Slf4j
@Component
public class ManuallyTriggeredRollbackTasklet implements StoppableTasklet {

    private final SparkJobExecutor sparkJobExecutor;
    private final ServiceRequestRepository serviceRequestRepository;
    private final RollbackMigration rollbackMigration;

    public ManuallyTriggeredRollbackTasklet(
            final SparkJobExecutor sparkJobExecutor,
            final ServiceRequestRepository serviceRequestRepository,
            final RollbackMigration rollbackMigration) {
        this.sparkJobExecutor = sparkJobExecutor;
        this.serviceRequestRepository = serviceRequestRepository;
        this.rollbackMigration = rollbackMigration;
    }

    @Override
    public RepeatStatus execute(final StepContribution contribution, final ChunkContext chunkContext) throws Exception {
        ServiceRequest serviceRequest = sparkJobExecutor.setupRequest(chunkContext);
        ProductImportContext productImportContext = findProductImportContext(serviceRequest);

        Optional<String> optionalError = rollbackMigration.execute(productImportContext);
        if (optionalError.isPresent()) {
            String errorMsg = optionalError.get();

            log.error(errorMsg);
            throw new JobExecutionException(errorMsg);
        }
        log.info(
                "Finished tasklet for job [{}] with id [{}]",
                serviceRequest.getName(),
                serviceRequest.getJobExecutionId());
        return RepeatStatus.FINISHED;
    }

    private ProductImportContext findProductImportContext(final ServiceRequest serviceRequest)
            throws JobExecutionException, IOException {
        String importRequestIdMessage = serviceRequest.getRequestMessage();

        if (isBlank(importRequestIdMessage)) {
            throw new JobExecutionException("Could not find import product request");
        }
        Long importRequestId = Long.valueOf(importRequestIdMessage);
        Optional<ServiceRequest> optionalImportServiceRequest =
                serviceRequestRepository.findById(importRequestId);

        if (!optionalImportServiceRequest.isPresent()) {
            String errorMsg = "Could not find import product request with id [" + importRequestId + "]";

            log.error(errorMsg);
            throw new JobExecutionException(errorMsg);
        }

        ServiceRequest importRequest = optionalImportServiceRequest.get();
        return MAPPER.readValue(
                importRequest.getRequestMessage(), ProductImportContext.class);
    }

    @Override
    public void stop() {
        log.warn("Stopping");
    }
}
