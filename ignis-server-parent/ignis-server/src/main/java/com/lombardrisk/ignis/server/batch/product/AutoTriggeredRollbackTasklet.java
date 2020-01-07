package com.lombardrisk.ignis.server.batch.product;

import com.lombardrisk.ignis.server.batch.SparkJobExecutor;
import com.lombardrisk.ignis.server.job.servicerequest.ServiceRequest;
import com.lombardrisk.ignis.server.product.productconfig.ProductConfigService;
import com.lombardrisk.ignis.server.product.productconfig.model.ImportStatus;
import com.lombardrisk.ignis.server.product.productconfig.model.ProductImportContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.StoppableTasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.lombardrisk.ignis.common.json.MapperWrapper.MAPPER;
import static com.lombardrisk.ignis.server.job.servicerequest.ServiceRequest.PRODUCT_CONFIG_ID;

@Slf4j
@Component
public class AutoTriggeredRollbackTasklet implements StoppableTasklet {

    private final SparkJobExecutor sparkJobExecutor;
    private final RollbackMigration rollbackMigration;
    private final ProductConfigService productConfigService;

    public AutoTriggeredRollbackTasklet(
            final SparkJobExecutor sparkJobExecutor,
            final RollbackMigration rollbackMigration,
            final ProductConfigService productConfigService) {
        this.sparkJobExecutor = sparkJobExecutor;
        this.rollbackMigration = rollbackMigration;
        this.productConfigService = productConfigService;
    }

    @Override
    public RepeatStatus execute(final StepContribution contribution, final ChunkContext chunkContext) throws Exception {
        try {
            ServiceRequest serviceRequest = sparkJobExecutor.setupRequest(chunkContext);
            ProductImportContext productImportContext = MAPPER.readValue(
                    serviceRequest.getRequestMessage(), ProductImportContext.class);

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
        } finally {
            setErrorImportStatus(chunkContext);
        }
    }

    private void setErrorImportStatus(final ChunkContext chunkContext) {
        Long productConfigId = Long.valueOf(
                (String) chunkContext.getStepContext().getJobParameters().get(PRODUCT_CONFIG_ID));

        productConfigService.updateImportStatus(productConfigId, ImportStatus.ERROR);
    }

    @Override
    public void stop() {
        log.warn("Stopping");
    }
}
