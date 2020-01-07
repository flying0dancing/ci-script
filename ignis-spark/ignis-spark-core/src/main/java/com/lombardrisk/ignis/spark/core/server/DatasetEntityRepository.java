package com.lombardrisk.ignis.spark.core.server;

import com.lombardrisk.ignis.client.core.view.IdView;
import com.lombardrisk.ignis.client.internal.CreateDatasetCall;
import com.lombardrisk.ignis.client.internal.InternalDatasetClient;
import com.lombardrisk.ignis.client.internal.UpdateDatasetRunCall;
import com.lombardrisk.ignis.spark.api.JobRequest;
import com.lombardrisk.ignis.spark.api.pipeline.PipelineAppConfig;
import com.lombardrisk.ignis.spark.api.pipeline.PipelineStepAppConfig;
import com.lombardrisk.ignis.spark.api.staging.DatasetProperties;
import com.lombardrisk.ignis.spark.api.staging.StagingDatasetConfig;
import com.lombardrisk.ignis.spark.core.JobOperatorException;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;

import static org.apache.commons.lang3.StringUtils.EMPTY;

@Slf4j
public class DatasetEntityRepository {

    private final JobRequest jobRequest;
    private final InternalDatasetClient datasetClient;

    public DatasetEntityRepository(
            final JobRequest jobRequest,
            final InternalDatasetClient datasetClient) {
        this.jobRequest = jobRequest;
        this.datasetClient = datasetClient;
    }

    public Long createStagingDataset(
            final long recordsCount,
            final long rowKeySeed,
            final String rowKeyPredicate,
            final StagingDatasetConfig stagingDatasetConfig) {

        final DatasetProperties datasetProperties = stagingDatasetConfig.getDatasetProperties();

        Long datasetId = createDataset(
                CreateDatasetCall.builder()
                        .stagingDatasetId(stagingDatasetConfig.getId())
                        .stagingJobId(jobRequest.getServiceRequestId())
                        .recordsCount(recordsCount)
                        .entityCode(datasetProperties.getEntityCode())
                        .referenceDate(datasetProperties.getReferenceDate())
                        .schemaId(stagingDatasetConfig.getStagingSchemaValidation().getSchemaId())
                        .autoValidate(stagingDatasetConfig.isAutoValidate())
                        .rowKeySeed(rowKeySeed)
                        .predicate(rowKeyPredicate)
                        .build())
                .getId();

        log.info("Created dataset ID [{}]", datasetId);

        return datasetId;
    }

    public Long createPipelineDataset(
            final long recordsCount,
            final long rowKeySeed,
            final String rowKeyPredicate,
            final PipelineAppConfig pipelineAppConfig,
            final PipelineStepAppConfig pipelineStepAppConfig) {

        StagingDatasetConfig outputDataset = pipelineStepAppConfig.getOutputDataset();

        final DatasetProperties datasetProperties = outputDataset.getDatasetProperties();

        Long datasetId = createDataset(
                CreateDatasetCall.builder()
                        .pipelineJobId(jobRequest.getServiceRequestId())
                        .pipelineInvocationId(pipelineAppConfig.getPipelineInvocationId())
                        .pipelineStepInvocationId(pipelineStepAppConfig.getPipelineStepInvocationId())
                        .recordsCount(recordsCount)
                        .entityCode(datasetProperties.getEntityCode())
                        .referenceDate(datasetProperties.getReferenceDate())
                        .schemaId(outputDataset.getStagingSchemaValidation().getSchemaId())
                        .rowKeySeed(rowKeySeed)
                        .predicate(rowKeyPredicate)
                        .build())
                .getId();

        log.info("Created dataset ID [{}]", datasetId);

        return datasetId;
    }

    public Long updateStagingDataset(final long datasetId, final long stagingDatasetId, final long recordsCount) {
        Long updatedDatasetId = updateDataset(
                datasetId,
                UpdateDatasetRunCall.builder()
                        .stagingDatasetId(stagingDatasetId)
                        .stagingJobId(jobRequest.getServiceRequestId())
                        .recordsCount(recordsCount)
                        .build())
                .getId();

        log.info("Updated dataset ID [{}]", updatedDatasetId);

        return updatedDatasetId;
    }

    private IdView createDataset(final CreateDatasetCall datasetCall) {
        log.info("Creating new dataset with schema ID [{}] for job [{}]",
                datasetCall.getSchemaId(), jobRequest.getServiceRequestId());

        return executeDatasetCall(datasetClient.createDataset(datasetCall));
    }

    private IdView updateDataset(final long datasetId, final UpdateDatasetRunCall datasetCall) {
        log.info("Updating dataset ID [{}] with record count [{}]", datasetId, datasetCall.getRecordsCount());

        return executeDatasetCall(datasetClient.updateDatasetRun(datasetId, datasetCall));
    }

    private <T> T executeDatasetCall(final Call<T> call) {
        return Try.of(call::execute)
                .onSuccess(response -> log.info("Dataset response code [{}] for job [{}]",
                        response.code(), jobRequest.getServiceRequestId()))
                .mapTry(this::handleResponse)
                .getOrElseThrow(throwable -> new JobOperatorException(
                        "Request failed for job " + jobRequest.getServiceRequestId(), throwable));
    }

    private <T> T handleResponse(final Response<T> response) throws IOException {
        if (!response.isSuccessful()) {
            ResponseBody responseBody = response.errorBody();
            String body = responseBody != null ? responseBody.string() : EMPTY;
            log.warn("Error code [{}], body [{}]", response.code(), body);
        }

        return response.body();
    }
}
