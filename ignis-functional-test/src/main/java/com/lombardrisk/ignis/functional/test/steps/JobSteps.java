package com.lombardrisk.ignis.functional.test.steps;

import com.lombardrisk.ignis.client.core.view.PagedView;
import com.lombardrisk.ignis.client.external.job.ExternalExitStatus;
import com.lombardrisk.ignis.client.external.job.JobExecutionView;
import com.lombardrisk.ignis.client.external.job.JobStatus;
import com.lombardrisk.ignis.client.external.job.JobType;
import com.lombardrisk.ignis.client.external.job.JobsClient;
import com.lombardrisk.ignis.functional.test.config.properties.Timeouts;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.Duration;

import java.util.Optional;

import static com.lombardrisk.ignis.functional.test.steps.CallAssertion.callAndWaitForResult;

@Slf4j
public class JobSteps {

    private final JobsClient jobsClient;
    private final Timeouts timeouts;

    public JobSteps(final JobsClient jobsClient, final Timeouts timeouts) {
        this.jobsClient = jobsClient;
        this.timeouts = timeouts;
    }

    public void waitForImportProductJobToPass(final String productName, final String productVersion) {
        waitForProductJob(productName, productVersion, JobType.IMPORT_PRODUCT, ExternalExitStatus.COMPLETED);

        log.info("Successful import job for product [{} v.{}] has finished", productName, productVersion);
    }

    public void waitForImportProductJobToFail(final String productName, final String productVersion) {
        waitForProductJob(productName, productVersion, JobType.IMPORT_PRODUCT, ExternalExitStatus.FAILED);

        log.info("Failing import job for product [{} v.{}] has finished", productName, productVersion);
    }

    public void waitForRollbackProductJobToPass(final String productName, final String productVersion) {
        waitForProductJob(productName, productVersion, JobType.ROLLBACK_PRODUCT, ExternalExitStatus.COMPLETED);

        log.info("Successful rollback job for product [{} v.{}] has finished", productName, productVersion);
    }

    public void waitForRollbackProductJobToFail(final String productName, final String productVersion) {
        waitForProductJob(productName, productVersion, JobType.ROLLBACK_PRODUCT, ExternalExitStatus.FAILED);

        log.info("Failing rollback job for product [{} v.{}] has finished", productName, productVersion);
    }

    private void waitForProductJob(
            final String productName,
            final String productVersion,
            final JobType jobType,
            final ExternalExitStatus exitStatus) {

        JobStatus jobStatus = exitStatus == ExternalExitStatus.COMPLETED ? JobStatus.COMPLETED : JobStatus.FAILED;
        Duration timeout = jobType == JobType.IMPORT_PRODUCT
                ? timeouts.importJob()
                : timeouts.rollbackJob();

        callAndWaitForResult(
                timeout,
                Duration.FIVE_SECONDS,
                jobsClient.getAllJobs(),
                jobs -> findJob(productName, productVersion, jobType, jobStatus, jobs)
                        .map(jobExecution -> {
                            boolean foundJobHasExpectedStatus = jobExecution.getExitCode() == exitStatus;
                            log.info("Found job has expected status \"{}\"? {}", exitStatus, foundJobHasExpectedStatus);
                            return foundJobHasExpectedStatus;
                        }).orElse(false));
    }

    private Optional<JobExecutionView> findJob(
            final String productName,
            final String productVersion,
            final JobType jobType,
            final JobStatus jobStatus,
            final PagedView<JobExecutionView> jobs) {

        return jobs.getData().stream()
                .filter(jobExecution -> jobExecution.getServiceRequestType() == jobType)
                .filter(jobExecution -> jobExecution.getName().contains(productName))
                .filter(jobExecution -> jobExecution.getName().contains("v." + productVersion))
                .peek(jobExecutionView -> log.debug(
                        "Job found with name {}: {}", productName + "v." + productVersion, jobExecutionView.getStatus()))

                .filter(jobExecution -> jobExecution.getStatus() == jobStatus)
                .findFirst();
    }
}
