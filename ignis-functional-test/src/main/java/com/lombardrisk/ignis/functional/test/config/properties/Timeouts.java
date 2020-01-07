package com.lombardrisk.ignis.functional.test.config.properties;

import org.awaitility.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

import static com.google.common.base.MoreObjects.toStringHelper;
import static java.util.concurrent.TimeUnit.SECONDS;

@ConfigurationProperties(prefix = "timeouts")
public class Timeouts {

    private long stagingJob;
    private long validationJob;
    private long importJob;
    private long rollbackJob;
    private long pollInterval;

    public long getStagingJob() {
        return stagingJob;
    }

    public Duration stagingJob() {
        return toSeconds(stagingJob);
    }

    public void setStagingJob(final long stagingJob) {
        this.stagingJob = stagingJob;
    }

    public long getValidationJob() {
        return validationJob;
    }

    public Duration validationJob() {
        return toSeconds(validationJob);
    }

    public void setValidationJob(final long validationJob) {
        this.validationJob = validationJob;
    }

    public long getImportJob() {
        return importJob;
    }

    public Duration importJob() {
        return toSeconds(importJob);
    }

    public void setImportJob(final long importJob) {
        this.importJob = importJob;
    }

    public long getRollbackJob() {
        return rollbackJob;
    }

    public Duration rollbackJob() {
        return toSeconds(rollbackJob);
    }

    public void setRollbackJob(final long rollbackJob) {
        this.rollbackJob = rollbackJob;
    }

    public long getPollInterval() {
        return pollInterval;
    }

    public Duration polling() {
        return toSeconds(pollInterval);
    }

    public void setPollInterval(final long pollInterval) {
        this.pollInterval = pollInterval;
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("stagingJob", stagingJob)
                .add("validationJob", validationJob)
                .add("importJob", importJob)
                .add("rollbackJob", rollbackJob)
                .add("pollInterval", pollInterval)
                .toString();
    }

    private static Duration toSeconds(final long stagingJob) {
        return new Duration(stagingJob, SECONDS);
    }
}

