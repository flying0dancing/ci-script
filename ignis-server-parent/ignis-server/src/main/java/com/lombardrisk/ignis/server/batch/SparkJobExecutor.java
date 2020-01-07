package com.lombardrisk.ignis.server.batch;

import com.lombardrisk.ignis.common.log.CorrelationId;
import com.lombardrisk.ignis.server.job.servicerequest.ServiceRequest;
import com.lombardrisk.ignis.server.job.servicerequest.ServiceRequestRepository;
import com.lombardrisk.ignis.server.util.spark.AppSession;
import com.lombardrisk.ignis.server.util.spark.AppStatus;
import com.lombardrisk.ignis.server.util.spark.AppSubmitter;
import com.lombardrisk.ignis.server.util.spark.FinalAppStatus;
import com.lombardrisk.ignis.server.util.spark.SparkSubmitOption;
import io.vavr.Tuple2;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.apache.hadoop.yarn.exceptions.YarnException;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;

import java.io.IOException;
import java.util.Map;

import static com.lombardrisk.ignis.server.job.servicerequest.ServiceRequest.CORRELATION_ID;
import static com.lombardrisk.ignis.server.job.servicerequest.ServiceRequest.SERVICE_REQUEST_ID;

@Slf4j
public class SparkJobExecutor {

    private final YarnClient yarnClient;
    private final ServiceRequestRepository serviceRequestRepository;
    private final AppSubmitter appSubmitter;
    private final String appTrackingResourceUrl;

    public SparkJobExecutor(
            final YarnClient yarnClient,
            final ServiceRequestRepository serviceRequestRepository,
            final String appTrackingResourceUrl,
            final AppSubmitter appSubmitter) {
        this.yarnClient = yarnClient;
        this.serviceRequestRepository = serviceRequestRepository;
        this.appTrackingResourceUrl = appTrackingResourceUrl;
        this.appSubmitter = appSubmitter;
    }

    public ServiceRequest setupRequest(final ChunkContext chunkContext) throws JobExecutionException {
        Map<String, Object> jobParameters = chunkContext.getStepContext().getJobParameters();
        String correlationId = (String) jobParameters.get(CORRELATION_ID);
        CorrelationId.setCorrelationId(correlationId);

        Long serviceRequestId = Long.parseLong((String) jobParameters.get(SERVICE_REQUEST_ID));
        return serviceRequestRepository.findById(serviceRequestId)
                .orElseThrow(() -> new JobExecutionException(
                        "Service request not found with id [" + serviceRequestId + "] on step context"));
    }

    public void executeSparkJob(
            final StepContribution stepContribution,
            final ChunkContext chunkContext,
            final AppId taskletScopedAppId,
            final SparkSubmitOption sparkSubmitOption) throws JobExecutionException {
        ServiceRequest serviceRequest = setupRequest(chunkContext);

        log.debug("Running job [{}] with {}", serviceRequest.getId(), taskletScopedAppId);

        try (AppSession appSession = appSubmitter.submit(sparkSubmitOption)) {
            updateTaskletAppId(taskletScopedAppId, appSession);

            saveTrackingUrl(serviceRequest, taskletScopedAppId);

            Tuple2<AppStatus, FinalAppStatus> monitorStatus = appSession.monitor();

            stepContribution.setExitStatus(
                    convertJobExitStatus(monitorStatus._1, monitorStatus._2));

            log.debug("Job [{}] finished with status [{}]",
                    serviceRequest.getId(), stepContribution.getExitStatus());
        } catch (Exception e) {
            log.warn("An error occurred while executing Spark App with id [" + taskletScopedAppId + "]", e);

            stepContribution.setExitStatus(ExitStatus.FAILED);
        }
    }

    private static void updateTaskletAppId(final AppId taskletScopedAppId, final AppSession appSession) {
        AppId submittedAppId = appSession.getAppId();

        taskletScopedAppId.setClusterTimestamp(submittedAppId.getClusterTimestamp());
        taskletScopedAppId.setId(submittedAppId.getId());
    }

    @SuppressWarnings("WeakerAccess")
    public void saveTrackingUrl(final ServiceRequest serviceRequest, final AppId taskletAppId) {
        serviceRequest.setTrackingUrl(appTrackingResourceUrl + "/" + toApplicationId(taskletAppId));

        serviceRequestRepository.save(serviceRequest);
    }

    public void stop(final AppId taskletScopedAppId) {
        ApplicationId applicationId = toApplicationId(taskletScopedAppId);
        try {
            log.debug("Kill job with application ID [{}]", applicationId);

            yarnClient.killApplication(applicationId);
        } catch (YarnException | IOException e) {
            String errorMessage = "An error occurred while stopping Spark with application id [" + applicationId + "]";
            log.error(errorMessage, e);
        }
    }

    private static ApplicationId toApplicationId(final AppId taskletScopedAppId) {
        return ApplicationId.newInstance(
                taskletScopedAppId.getClusterTimestamp(),
                taskletScopedAppId.getId());
    }

    private static ExitStatus convertJobExitStatus(final AppStatus appStatus, final FinalAppStatus finalAppStatus) {
        switch (appStatus) {
            case FAILED:
                return ExitStatus.FAILED;
            case FINISHED:
                return convertFinalAppStatus(finalAppStatus);
            case KILLED:
                return ExitStatus.STOPPED;
            default:
                return ExitStatus.UNKNOWN;
        }
    }

    private static ExitStatus convertFinalAppStatus(final FinalAppStatus finalAppStatus) {
        switch (finalAppStatus) {
            case FAILED:
                return ExitStatus.FAILED;
            /* Application which was terminated by a user or admin. */
            case KILLED:
                return ExitStatus.STOPPED;
            case SUCCEEDED:
                return ExitStatus.COMPLETED;
            case UNDEFINED:
                return ExitStatus.UNKNOWN;
            default:
                return ExitStatus.UNKNOWN;
        }
    }
}
