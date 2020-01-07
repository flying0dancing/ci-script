package com.lombardrisk.ignis.functional.test.steps;

import com.lombardrisk.ignis.client.external.dataset.DatasetQueryParams;
import com.lombardrisk.ignis.client.external.dataset.DatasetService;
import com.lombardrisk.ignis.client.external.dataset.model.Dataset;
import com.lombardrisk.ignis.client.external.dataset.model.PagedDataset;
import com.lombardrisk.ignis.client.external.job.JobExecutionView;
import com.lombardrisk.ignis.client.external.job.JobsClient;
import com.lombardrisk.ignis.client.external.job.StagingClient;
import com.lombardrisk.ignis.client.external.job.staging.StagingItemView;
import com.lombardrisk.ignis.client.external.job.staging.request.v1.StagingRequest;
import com.lombardrisk.ignis.client.external.job.staging.request.v2.StagingRequestV2;
import com.lombardrisk.ignis.functional.test.config.properties.Timeouts;
import com.lombardrisk.ignis.functional.test.dsl.StagingJobContext;
import com.lombardrisk.ignis.functional.test.steps.service.StagingService;
import com.lombardrisk.ignis.functional.test.steps.service.StagingServiceProvider;
import okhttp3.ResponseBody;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static com.lombardrisk.ignis.functional.test.steps.CallAssertion.callAndExpectSuccess;
import static com.lombardrisk.ignis.functional.test.steps.CallAssertion.callAndWaitForResult;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class StagingSteps {

    private static final Logger LOGGER = LoggerFactory.getLogger(StagingSteps.class);

    private final StagingClient stagingClient;
    private final DatasetService datasetService;
    private Timeouts timeouts;
    private final JobsClient jobClient;
    private final StagingServiceProvider stagingServiceProvider;

    public StagingSteps(
            final StagingClient stagingClient,
            final DatasetService datasetService,
            final Timeouts timeouts,
            final JobsClient jobClient,
            final StagingServiceProvider stagingServiceProvider) {
        this.stagingClient = stagingClient;
        this.datasetService = datasetService;
        this.timeouts = timeouts;
        this.jobClient = jobClient;
        this.stagingServiceProvider = stagingServiceProvider;
    }

    public StagingJobContext runStagingJobV1(final StagingRequest stagingRequest) {
        LOGGER.info("Run staging job V1 [{}]", stagingRequest.getName());
        return runStagingJob(stagingRequest, stagingServiceProvider.v1());
    }

    public StagingJobContext runStagingJobV2(final StagingRequestV2 stagingRequest) {
        LOGGER.info("Run staging job V2 [{}]", stagingRequest.getName());
        return runStagingJob(stagingRequest, stagingServiceProvider.v2());
    }

    private <T> StagingJobContext runStagingJob(final T stagingRequest, final StagingService<T> service) {
        Long jobId = service.startJob(stagingRequest);

        callAndWaitForResult(
                timeouts.stagingJob(),
                timeouts.polling(),
                jobClient.getJob(jobId),
                StagingSteps::isFinished);

        List<StagingItemView> stagingDatasets = callAndExpectSuccess(stagingClient.getStagingDatasets(jobId));
        Set<Long> stagedDatasetIds = stagingDatasets.stream().map(StagingItemView::getDatasetId).collect(toSet());

        List<Dataset> stagedDatasets = new ArrayList<>();
        for (DatasetQueryParams datasetQueryParams : service.toDatasetQueryParams(stagingRequest)) {
            PagedDataset pagedDatasets = callAndExpectSuccess(() -> datasetService.findDatasets(datasetQueryParams));

            List<Dataset> datasets = pagedDatasets.getEmbedded().getData();
            if (!datasets.isEmpty()) {
                Dataset stagedDataset = datasets.stream()
                        .filter(dataset -> stagedDatasetIds.contains(dataset.getId()))
                        .collect(toList())
                        .get(0);

                stagedDatasets.add(stagedDataset);
            }
        }

        return StagingJobContext.builder()
                .jobId(jobId)
                .stagingDatasets(stagingDatasets)
                .stagedDatasets(stagedDatasets)
                .success(true)
                .build();
    }

    public List<String> getValidationErrors(final long stagingDatasetId) throws IOException {
        ResponseBody responseBody = callAndExpectSuccess(stagingClient.downloadErrorFile(stagingDatasetId));
        return IOUtils.readLines(responseBody.byteStream(), "UTF-8");
    }

    private static Boolean isFinished(final JobExecutionView job) {
        List<String> finishedJobStatuses = Arrays.asList("completed", "failed", "stopped");
        return finishedJobStatuses.contains(job.getStatus().name().toLowerCase());
    }
}
