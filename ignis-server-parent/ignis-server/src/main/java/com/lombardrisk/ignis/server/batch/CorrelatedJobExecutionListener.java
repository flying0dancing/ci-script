package com.lombardrisk.ignis.server.batch;

import com.lombardrisk.ignis.common.log.CorrelationId;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.JobParameters;

import static com.lombardrisk.ignis.server.job.servicerequest.ServiceRequest.CORRELATION_ID;

public interface CorrelatedJobExecutionListener extends JobExecutionListener {

    default void setCorrelationId(final JobExecution jobExecution) {
        JobParameters jobParameters = jobExecution.getJobParameters();
        String correlationId = jobParameters.getString(CORRELATION_ID);
        CorrelationId.setCorrelationId(correlationId);
    }
}
