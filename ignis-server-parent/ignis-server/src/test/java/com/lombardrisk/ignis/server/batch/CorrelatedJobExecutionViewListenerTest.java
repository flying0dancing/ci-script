package com.lombardrisk.ignis.server.batch;

import com.google.common.collect.ImmutableMap;
import com.lombardrisk.ignis.common.log.CorrelationId;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;

import static com.lombardrisk.ignis.server.job.servicerequest.ServiceRequest.CORRELATION_ID;

public class CorrelatedJobExecutionViewListenerTest {

    @Rule
    public JUnitSoftAssertions soft = new JUnitSoftAssertions();

    private final CorrelatedJobExecutionListener correlatedJobExecutionListener = new CorrelatedJobExecutionListener() {
        @Override
        public void beforeJob(final JobExecution jobExecution) {
            //no-op
        }

        @Override
        public void afterJob(final JobExecution jobExecution) {
            //no-op
        }
    };

    @Test
    public void setCorrelationId() {
        soft.assertThat(CorrelationId.getCorrelationId())
                .isNull();

        JobParameters jobParameters =
                new JobParameters(ImmutableMap.of(CORRELATION_ID, new JobParameter("correlation")));
        JobExecution jobExecution = new JobExecution(1L, jobParameters, "");

        correlatedJobExecutionListener.setCorrelationId(jobExecution);

        soft.assertThat(CorrelationId.getCorrelationId())
                .isEqualTo("correlation");
    }
}
