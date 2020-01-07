package com.lombardrisk.ignis.server.batch.staging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lombardrisk.ignis.fs.FileSystemTemplate;
import com.lombardrisk.ignis.server.batch.SparkJobExecutor;
import com.lombardrisk.ignis.server.job.servicerequest.ServiceRequest;
import com.lombardrisk.ignis.spark.api.staging.StagingAppConfig;
import com.lombardrisk.ignis.spark.api.staging.StagingDatasetConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.StoppableTasklet;
import org.springframework.batch.repeat.RepeatStatus;

@Slf4j
public class DatasetCleanupTasklet implements StoppableTasklet {

    private final SparkJobExecutor sparkJobExecutor;
    private final FileSystemTemplate fileSystemTemplate;
    private final ObjectMapper objectMapper;

    public DatasetCleanupTasklet(
            final SparkJobExecutor sparkJobExecutor,
            final FileSystemTemplate fileSystemTemplate, final ObjectMapper objectMapper) {
        this.sparkJobExecutor = sparkJobExecutor;
        this.fileSystemTemplate = fileSystemTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public RepeatStatus execute(final StepContribution contribution, final ChunkContext chunkContext) {
        try {
            ServiceRequest serviceRequest = sparkJobExecutor.setupRequest(chunkContext);

            StagingAppConfig stagingAppConfig = objectMapper.readValue(
                    serviceRequest.getRequestMessage(), StagingAppConfig.class);

            for (StagingDatasetConfig stagingDatasetConfig : stagingAppConfig.getItems()) {

                String datasetPath = stagingDatasetConfig.getSource().fileStreamPath();

                log.debug("Delete [{}]", datasetPath);

                if (!fileSystemTemplate.exists(datasetPath)) {
                    log.warn("[{}] was not deleted because it does not exist", datasetPath);
                    return RepeatStatus.FINISHED;
                }

                boolean deleted = fileSystemTemplate.delete(datasetPath);
                if (!deleted) {
                    log.error("[{}] was not deleted for an unknown reason", datasetPath);
                }
            }
        } catch (final Exception e) {
            log.error("Cleanup tasklet failed with message: " + e.getMessage(), e);
        }

        return RepeatStatus.FINISHED;
    }

    @Override
    public void stop() {
        //no-op
    }
}
