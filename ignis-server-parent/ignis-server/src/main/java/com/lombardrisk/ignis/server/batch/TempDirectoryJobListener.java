package com.lombardrisk.ignis.server.batch;

import com.lombardrisk.ignis.server.job.servicerequest.ServiceRequest;
import com.lombardrisk.ignis.server.util.TempDirectory;
import io.vavr.control.Option;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;

@Slf4j
@AllArgsConstructor
public class TempDirectoryJobListener implements JobExecutionListener {

    private final TempDirectory tempDirectory;

    @Override
    public void beforeJob(final JobExecution jobExecution) {
        Option<String> serviceRequestId =
                Option.of(jobExecution.getJobParameters().getString(ServiceRequest.SERVICE_REQUEST_ID));

        if (serviceRequestId.isDefined()) {
            tempDirectory.createDirectory(serviceRequestId.get())
                    .onSuccess(path -> log.debug("Created directory [{}]", path.toAbsolutePath()))
                    .onFailure(throwable -> handleException(jobExecution, throwable));
        } else {
            jobExecution.stop();
        }
    }

    @Override
    public void afterJob(final JobExecution jobExecution) {
        Option<String> serviceRequestId =
                Option.of(jobExecution.getJobParameters().getString(ServiceRequest.SERVICE_REQUEST_ID));

        if (serviceRequestId.isDefined()) {
            tempDirectory.delete(serviceRequestId.get())
                    .onSuccess(deleted -> log.debug("Deleted directory [{}]", serviceRequestId.get()))
                    .onFailure(throwable -> handleException(jobExecution, throwable));
        } else {
            jobExecution.stop();
        }
    }

    private void handleException(final JobExecution jobExecution, final Throwable throwable) {
        log.error("Stopping job due to exception", throwable);
        jobExecution.stop();
    }
}
