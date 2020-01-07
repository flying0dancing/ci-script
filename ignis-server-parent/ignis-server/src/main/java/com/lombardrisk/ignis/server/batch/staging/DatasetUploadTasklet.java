package com.lombardrisk.ignis.server.batch.staging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lombardrisk.ignis.api.dataset.DatasetState;
import com.lombardrisk.ignis.fs.FileSystemTemplate;
import com.lombardrisk.ignis.server.batch.SparkJobExecutor;
import com.lombardrisk.ignis.server.job.servicerequest.ServiceRequest;
import com.lombardrisk.ignis.server.job.staging.StagingDatasetRepository;
import com.lombardrisk.ignis.server.job.staging.model.StagingDataset;
import com.lombardrisk.ignis.spark.api.staging.DatasetProperties;
import com.lombardrisk.ignis.spark.api.staging.StagingAppConfig;
import com.lombardrisk.ignis.spark.api.staging.StagingDatasetConfig;
import com.lombardrisk.ignis.spark.api.staging.datasource.HdfsCsvDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.StoppableTasklet;
import org.springframework.batch.repeat.RepeatStatus;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import static com.lombardrisk.ignis.api.dataset.DatasetState.UPLOADED;
import static com.lombardrisk.ignis.api.dataset.DatasetState.UPLOADING;

/**
 * upload dataset to hdfs by given job instance id
 */
@Slf4j
public class DatasetUploadTasklet implements StoppableTasklet {

    private final HdfsDatasetConf datasetConf;
    private final FileSystemTemplate fileSystemTemplate;
    private final StagingDatasetRepository stagingDatasetRepository;
    private final SparkJobExecutor sparkJobExecutor;
    private final ObjectMapper objectMapper;

    public DatasetUploadTasklet(
            final HdfsDatasetConf datasetConf,
            final FileSystemTemplate fileSystemTemplate,
            final StagingDatasetRepository stagingDatasetRepository,
            final SparkJobExecutor sparkJobExecutor, final ObjectMapper objectMapper) {
        this.datasetConf = datasetConf;
        this.fileSystemTemplate = fileSystemTemplate;
        this.stagingDatasetRepository = stagingDatasetRepository;
        this.sparkJobExecutor = sparkJobExecutor;
        this.objectMapper = objectMapper;
    }

    @Override
    public RepeatStatus execute(final StepContribution contribution, final ChunkContext chunkContext) throws Exception {
        ServiceRequest serviceRequest = sparkJobExecutor.setupRequest(chunkContext);

        StagingAppConfig stagingJobRequest =
                objectMapper.readValue(serviceRequest.getRequestMessage(), StagingAppConfig.class);

        StepExecution stepExecution = chunkContext.getStepContext().getStepExecution();

        BatchStatus batchStatus = stepExecution.getJobExecution().getStatus();
        Set<StagingDatasetConfig> stagingDatasetConfigs = new LinkedHashSet<>();

        for (StagingDatasetConfig item : stagingJobRequest.getItems()) {
            uploadDataset(batchStatus, serviceRequest, item)
                    .ifPresent(stagingDatasetConfigs::add);
        }

        if (stagingDatasetConfigs.isEmpty()) {
            stepExecution.setExitStatus(ExitStatus.NOOP);
            stepExecution.getJobExecution().setStatus(BatchStatus.STOPPED);

            return RepeatStatus.FINISHED;
        }
        return RepeatStatus.FINISHED;
    }

    @Override
    public void stop() {
        //no-op
    }

    /**
     * copy file from local to hdfs ,and  move the file to the staged directory once uploaded
     */
    private Optional<StagingDatasetConfig> uploadDataset(
            final BatchStatus batchStatus,
            final ServiceRequest serviceRequest,
            final StagingDatasetConfig stagingItemRequest) {

        if (!batchStatus.isRunning()) {
            return Optional.empty();
        }

        DatasetProperties datasetProperties = stagingItemRequest.getDatasetProperties();

        StagingDataset stagingDataset =
                stagingDatasetRepository.findByServiceRequestIdAndDatasetNameAndEntityCodeAndReferenceDate(
                        serviceRequest.getId(),
                        stagingItemRequest.getStagingSchemaValidation().getPhysicalTableName(),
                        datasetProperties.getEntityCode(),
                        datasetProperties.getReferenceDate());

        beginUpload(stagingDataset);

        try {
            upload(stagingDataset, stagingItemRequest);
            return Optional.of(stagingItemRequest);
        } catch (final IOException e) {
            errorUpload(stagingDataset, serviceRequest.getId(), stagingItemRequest, e);
        }

        return Optional.empty();
    }

    private void errorUpload(
            final StagingDataset stagingDataset,
            final long jobExecutionId,
            final StagingDatasetConfig stagingItemRequest,
            final Exception e) {

        HdfsCsvDataSource csvSource = (HdfsCsvDataSource) stagingItemRequest.getSource();
        log.warn("Dataset file [{}] could not be uploaded for job with execution id [{}]",
                csvSource.fileStreamPath(), jobExecutionId, e);

        stagingDataset.setStatus(DatasetState.UPLOAD_FAILED);
        stagingDataset.setMessage(e.getMessage());

        stagingDatasetRepository.save(stagingDataset);
    }

    private void upload(
            final StagingDataset stagingDataset,
            final StagingDatasetConfig stagingItemRequest) throws IOException {

        HdfsCsvDataSource csvSource = (HdfsCsvDataSource) stagingItemRequest.getSource();

        File localInputFile = Paths.get(datasetConf.getLocalPath(), stagingDataset.getStagingFile())
                .toFile();

        String datasetPath = csvSource.fileStreamPath();

        log.debug("Copy [{}] to [{}]", localInputFile, datasetPath);
        fileSystemTemplate.copy(localInputFile, datasetPath);

        stagingDataset.setStatus(UPLOADED);
        stagingDatasetRepository.save(stagingDataset);
    }

    private void beginUpload(final StagingDataset stagingDataset) {
        stagingDataset.setStatus(UPLOADING);
        stagingDatasetRepository.save(stagingDataset);
    }
}
