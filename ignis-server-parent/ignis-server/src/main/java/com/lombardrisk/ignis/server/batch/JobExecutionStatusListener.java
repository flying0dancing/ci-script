package com.lombardrisk.ignis.server.batch;

import com.lombardrisk.ignis.server.batch.staging.JobExecutionStatusService;
import com.lombardrisk.ignis.server.job.servicerequest.ServiceRequest;
import io.vavr.control.Option;
import lombok.AllArgsConstructor;
import org.springframework.batch.core.JobExecution;

@AllArgsConstructor
public class JobExecutionStatusListener implements CorrelatedJobExecutionListener {

    private final JobExecutionStatusService jobStatusService;

    @Override
    public void beforeJob(final JobExecution jobExecution) {
        setCorrelationId(jobExecution);

        Option<ServiceRequest> serviceRequest = jobStatusService.updateServiceRequest(jobExecution);

        if (serviceRequest.isEmpty()) {
            jobExecution.stop();
        }
    }

    @Override
    public void afterJob(final JobExecution jobExecution) {
        setCorrelationId(jobExecution);
        jobStatusService.updateServiceRequest(jobExecution);
    }
}
