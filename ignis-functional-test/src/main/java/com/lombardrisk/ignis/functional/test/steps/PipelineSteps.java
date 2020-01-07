package com.lombardrisk.ignis.functional.test.steps;

import com.lombardrisk.ignis.client.core.page.request.PageRequest;
import com.lombardrisk.ignis.client.core.page.request.Sort;
import com.lombardrisk.ignis.client.external.dataset.DatasetQueryParams;
import com.lombardrisk.ignis.client.external.dataset.DatasetService;
import com.lombardrisk.ignis.client.external.dataset.model.Dataset;
import com.lombardrisk.ignis.client.external.dataset.model.PagedDataset;
import com.lombardrisk.ignis.client.external.drillback.DatasetRowDataView;
import com.lombardrisk.ignis.client.external.drillback.DrillbackClient;
import com.lombardrisk.ignis.client.external.job.JobExecutionView;
import com.lombardrisk.ignis.client.external.job.JobsClient;
import com.lombardrisk.ignis.client.external.job.pipeline.PipelineRequest;
import com.lombardrisk.ignis.client.external.pipeline.PipelineClient;
import com.lombardrisk.ignis.functional.test.config.properties.Timeouts;
import com.lombardrisk.ignis.functional.test.dsl.PipelineJobContext;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.lombardrisk.ignis.functional.test.steps.CallAssertion.callAndExpectSuccess;
import static com.lombardrisk.ignis.functional.test.steps.CallAssertion.callAndWaitForResult;

@AllArgsConstructor
public class PipelineSteps {

    private static final Logger LOGGER = LoggerFactory.getLogger(PipelineSteps.class);

    private final PipelineClient pipelineClient;
    private final DrillbackClient drillbackClient;
    private final DatasetService datasetService;
    private Timeouts timeouts;
    private final JobsClient jobClient;

    public PipelineJobContext runPipelineJob(final PipelineRequest pipelineRequest) {
        LOGGER.info("Run pipeline job [{}]", pipelineRequest.getName());

        Long jobId = callAndExpectSuccess(jobClient.startJob(pipelineRequest)).getId();

        callAndWaitForResult(
                timeouts.stagingJob(),
                timeouts.polling(),
                jobClient.getJob(jobId),
                PipelineSteps::isFinished);

        PagedDataset pagedDataset = callAndExpectSuccess(() -> datasetService.findDatasets(DatasetQueryParams.builder()
                .pageRequest(PageRequest.builder()
                        .page(0)
                        .sort(Sort.builder()
                                .field("id")
                                .direction(Sort.Direction.DESC)
                                .build())
                        .build())
                .build()));

        List<Dataset> pipelineDatasets = pagedDataset.datasets()
                .stream()
                .filter(dataset -> jobId.equals(dataset.getPipelineJobId()))
                .collect(Collectors.toList());

        return PipelineJobContext.builder()
                .jobId(jobId)
                .pipelineDatasets(pipelineDatasets)
                .build();
    }

    public DatasetRowDataView drillBackDataset(
            final Long datasetId,
            final Long pipelineId,
            final Long pipelineStepId) {
        PageRequest page = PageRequest.builder().build();
        return callAndExpectSuccess(
                drillbackClient.getOutputDrillback(datasetId, pipelineId, pipelineStepId, page));
    }

    private static Boolean isFinished(final JobExecutionView job) {
        List<String> finishedJobStatuses = Arrays.asList("completed", "failed", "stopped");
        return finishedJobStatuses.contains(job.getStatus().name().toLowerCase());
    }
}
