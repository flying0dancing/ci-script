package com.lombardrisk.ignis.server.batch.rule;

import com.lombardrisk.ignis.server.batch.CorrelatedJobExecutionListener;
import com.lombardrisk.ignis.server.batch.staging.JobExecutionStatusService;
import com.lombardrisk.ignis.server.dataset.model.Dataset;
import io.vavr.control.Option;
import org.springframework.batch.core.JobExecution;

public class DatasetValidationJobListener implements CorrelatedJobExecutionListener {

    private final JobExecutionStatusService jobExecutionStatusService;

    public DatasetValidationJobListener(final JobExecutionStatusService jobExecutionStatusService) {
        this.jobExecutionStatusService = jobExecutionStatusService;
    }

    @Override
    public void beforeJob(final JobExecution jobExecution) {
        setCorrelationId(jobExecution);

        Option<Dataset> dataset = jobExecutionStatusService.updateDatasetStatus(jobExecution);
        if (dataset.isEmpty()) {
            jobExecution.stop();
        }
    }

    @Override
    public void afterJob(final JobExecution jobExecution) {
        setCorrelationId(jobExecution);
        jobExecutionStatusService.updateDatasetStatus(jobExecution);
    }
}
