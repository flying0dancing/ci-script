package com.lombardrisk.ignis.server.batch.staging;

import com.lombardrisk.ignis.server.batch.AppId;
import com.lombardrisk.ignis.server.batch.SparkJobExecutor;
import com.lombardrisk.ignis.server.job.servicerequest.ServiceRequest;
import com.lombardrisk.ignis.server.util.spark.SparkConfFactory;
import com.lombardrisk.ignis.server.util.spark.SparkSubmitOption;
import com.lombardrisk.ignis.spark.api.JobType;
import com.lombardrisk.ignis.spark.api.staging.StagingAppConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.StoppableTasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.core.env.Environment;

import java.io.IOException;

import static com.lombardrisk.ignis.common.json.MapperWrapper.MAPPER;

/**
 * dataset validate will submit a spark driver into yarn cluster
 */
@Slf4j
public class DatasetStagingTasklet implements StoppableTasklet {

    private final SparkConfFactory sparkConfFactory;
    private final String driverJar;
    private final String mainClass;
    private final String hdfsUser;
    private final SparkJobExecutor sparkJobExecutor;
    private final AppId taskletScopedAppId;

    public DatasetStagingTasklet(
            final Environment env,
            final SparkConfFactory sparkConfFactory,
            final SparkJobExecutor sparkJobExecutor,
            final AppId taskletScopedAppId) {
        this.sparkConfFactory = sparkConfFactory;

        driverJar = env.getRequiredProperty("spark.drivers.staging.resource");
        mainClass = env.getRequiredProperty("spark.drivers.staging.mainClass");
        hdfsUser = env.getRequiredProperty("hadoop.user");

        this.sparkJobExecutor = sparkJobExecutor;
        this.taskletScopedAppId = taskletScopedAppId;
    }

    @Override
    public RepeatStatus execute(final StepContribution contribution, final ChunkContext chunkContext) throws Exception {
        ServiceRequest serviceRequest = sparkJobExecutor.setupRequest(chunkContext);

        SparkSubmitOption submitOption = createSparkSubmitOption(serviceRequest);

        sparkJobExecutor.executeSparkJob(
                contribution,
                chunkContext,
                taskletScopedAppId,
                submitOption);

        return RepeatStatus.FINISHED;
    }

    @Override
    public void stop() {
        if (taskletScopedAppId.isNotEmpty()) {
            sparkJobExecutor.stop(taskletScopedAppId);
        } else {
            log.warn("Tasklet app id was not updated: {}", taskletScopedAppId);
        }
    }

    private SparkSubmitOption createSparkSubmitOption(final ServiceRequest serviceRequest) throws IOException {
        Long requestId = serviceRequest.getId();
        StagingAppConfig jobRequest = toJobRequest(serviceRequest, requestId);

        return SparkSubmitOption.builder()
                .clazz(mainClass)
                .job(jobRequest)
                .jobExecutionId(requestId)
                .jobType(JobType.STAGING_DATASET)
                .hdfsUser(hdfsUser)
                .jobName(JobType.STAGING_DATASET + " Job " + requestId)
                .driverJar(driverJar)
                .sparkConf(sparkConfFactory.create())
                .build();
    }

    private StagingAppConfig toJobRequest(
            final ServiceRequest serviceRequest,
            final long serviceRequestId) throws IOException {
        StagingAppConfig requestMessageJobRequest =
                MAPPER.readValue(serviceRequest.getRequestMessage(), StagingAppConfig.class);

        return StagingAppConfig.builder()
                .name(requestMessageJobRequest.getName())
                .items(requestMessageJobRequest.getItems())
                .serviceRequestId(serviceRequestId)
                .build();
    }
}
