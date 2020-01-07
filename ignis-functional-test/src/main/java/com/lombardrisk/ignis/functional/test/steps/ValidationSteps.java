package com.lombardrisk.ignis.functional.test.steps;

import com.lombardrisk.ignis.client.core.page.request.PageRequest;
import com.lombardrisk.ignis.client.external.dataset.DatasetClient;
import com.lombardrisk.ignis.client.external.dataset.DatasetService;
import com.lombardrisk.ignis.client.external.job.JobExecutionView;
import com.lombardrisk.ignis.client.external.job.JobsClient;
import com.lombardrisk.ignis.client.external.job.validation.ValidationJobRequest;
import com.lombardrisk.ignis.client.external.rule.ValidationResultsDetailView;
import com.lombardrisk.ignis.client.external.rule.ValidationRuleSummaryView;
import com.lombardrisk.ignis.functional.test.config.properties.Timeouts;
import com.lombardrisk.ignis.functional.test.dsl.ValidationJobContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

import static com.lombardrisk.ignis.functional.test.steps.CallAssertion.callAndExpectSuccess;
import static com.lombardrisk.ignis.functional.test.steps.CallAssertion.callAndWaitForResult;

public class ValidationSteps {

    private static final Logger LOGGER = LoggerFactory.getLogger(ValidationSteps.class);

    private final JobsClient jobsClient;
    private final DatasetClient externalDatasetClient;
    private final DatasetService datasetService;
    private final Timeouts timeouts;

    public ValidationSteps(
            final JobsClient jobsClient,
            final DatasetClient externalDatasetClient,
            final DatasetService datasetService,
            final Timeouts timeouts) {
        this.jobsClient = jobsClient;
        this.externalDatasetClient = externalDatasetClient;
        this.datasetService = datasetService;
        this.timeouts = timeouts;
    }

    public ValidationJobContext validateDataset(final Long datasetId) {
        ValidationJobRequest validationJobRequest = ValidationJobRequest.builder()
                .datasetId(datasetId)
                .name("Validation job for " + datasetId)
                .build();

        LOGGER.info("Start {}", validationJobRequest.getName());

        Long jobId = callAndExpectSuccess(jobsClient.startJob(validationJobRequest)).getId();
        callAndWaitForResult(
                timeouts.validationJob(),
                timeouts.polling(),
                jobsClient.getJob(jobId),
                ValidationSteps::isFinished);

        LOGGER.info("Validated dataset [{}]", datasetId);

        return ValidationJobContext.builder()
                .jobId(jobId)
                .datasetId(datasetId)
                .build();
    }

    public List<ValidationRuleSummaryView> findResultSummaries(final Long datasetId) {
        List<ValidationRuleSummaryView> validationResultsSummaries =
                callAndExpectSuccess(externalDatasetClient.getValidationResultsSummaries(datasetId));

        LOGGER.info("Found [{}] validation result summaries for dataset id [{}]",
                validationResultsSummaries.size(), datasetId);

        return validationResultsSummaries;
    }

    public ValidationResultsDetailView findResultDetails(
            final Long datasetId, final PageRequest pageRequest) {

        ValidationResultsDetailView validationResultsSummaries =
                callAndExpectSuccess(() -> datasetService.findValidationResultDetails(datasetId, pageRequest));

        int ids = validationResultsSummaries.getData().size();
        LOGGER.info("Found [{}] validation results details for dataset id [{}]", ids, datasetId);

        return validationResultsSummaries;
    }

    private static Boolean isFinished(final JobExecutionView job) {
        List<String> finishedJobStatuses = Arrays.asList("completed", "failed", "stopped");
        return finishedJobStatuses.contains(job.getStatus().name().toLowerCase());
    }
}
