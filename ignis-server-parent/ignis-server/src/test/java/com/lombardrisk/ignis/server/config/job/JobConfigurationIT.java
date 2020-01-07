package com.lombardrisk.ignis.server.config.job;

import com.lombardrisk.ignis.server.config.IntegrationTestConfig;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.job.AbstractJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static com.lombardrisk.ignis.server.job.servicerequest.ServiceRequestType.IMPORT_PRODUCT;
import static com.lombardrisk.ignis.server.job.servicerequest.ServiceRequestType.ROLLBACK_PRODUCT;
import static com.lombardrisk.ignis.server.job.servicerequest.ServiceRequestType.STAGING;
import static com.lombardrisk.ignis.server.job.servicerequest.ServiceRequestType.VALIDATION;

@RunWith(SpringRunner.class)
@IntegrationTestConfig
public class JobConfigurationIT {

    @Autowired
    private List<Job> jobs;

    @Rule
    public JUnitSoftAssertions soft = new JUnitSoftAssertions();

    @Test
    public void datasetValidationJob() {
        AbstractJob validationJob =
                jobs.stream()
                        .filter(job -> VALIDATION.name().equals(job.getName()))
                        .map(AbstractJob.class::cast)
                        .findFirst()
                        .orElseThrow(() -> new AssertionError("Validation job not configured"));

        soft.assertThat(validationJob.getName())
                .isEqualTo(VALIDATION.name());

        soft.assertThat(validationJob.getStepNames())
                .containsSequence("validationStep");
    }

    @Test
    public void stagingJob() {
        AbstractJob stagingJob =
                jobs.stream()
                        .filter(job -> STAGING.name().equals(job.getName()))
                        .map(AbstractJob.class::cast)
                        .findFirst()
                        .orElseThrow(() -> new AssertionError("staging job not configured"));

        soft.assertThat(stagingJob.getName())
                .isEqualTo(STAGING.name());

        soft.assertThat(stagingJob.getStepNames())
                .containsExactlyInAnyOrder(
                        "upload",
                        "staging",
                        "autoValidateDataset",
                        "downstreamPipeline",
                        "cleanup",
                        "cleanup after step stopped",
                        "cleanup after step failed");
    }

    @Test
    public void importProductJob() {
        AbstractJob importProductJob =
                jobs.stream()
                        .filter(job -> IMPORT_PRODUCT.name().equals(job.getName()))
                        .map(AbstractJob.class::cast)
                        .findFirst()
                        .orElseThrow(() -> new AssertionError("import product job not configured"));

        soft.assertThat(importProductJob.getName())
                .isEqualTo(IMPORT_PRODUCT.name());

        soft.assertThat(importProductJob.getStepNames())
                .containsExactlyInAnyOrder(
                        "import product",
                        "automatically rollback product");
    }

    @Test
    public void manuallyTriggeredRollbackProductJob() {
        AbstractJob rollbackJob =
                jobs.stream()
                        .filter(job -> ROLLBACK_PRODUCT.name().equals(job.getName()))
                        .map(AbstractJob.class::cast)
                        .findFirst()
                        .orElseThrow(() -> new AssertionError("rollback product job not configured"));

        soft.assertThat(rollbackJob.getName())
                .isEqualTo(ROLLBACK_PRODUCT.name());

        soft.assertThat(rollbackJob.getStepNames())
                .containsExactly("manually rollback product");
    }
}
