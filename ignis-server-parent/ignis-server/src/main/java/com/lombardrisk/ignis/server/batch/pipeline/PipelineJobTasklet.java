package com.lombardrisk.ignis.server.batch.pipeline;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lombardrisk.ignis.server.batch.AppId;
import com.lombardrisk.ignis.server.batch.SparkJobExecutor;
import com.lombardrisk.ignis.server.job.servicerequest.ServiceRequest;
import com.lombardrisk.ignis.server.util.spark.SparkConfFactory;
import com.lombardrisk.ignis.server.util.spark.SparkSubmitOption;
import com.lombardrisk.ignis.spark.api.JobType;
import com.lombardrisk.ignis.spark.api.pipeline.PipelineAppConfig;
import com.lombardrisk.ignis.spark.api.pipeline.ScriptletStepAppConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.StoppableTasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.core.env.Environment;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class PipelineJobTasklet implements StoppableTasklet {

    private final SparkJobExecutor sparkJobExecutor;
    private final SparkConfFactory sparkConfFactory;
    private final String driverJar;
    private final String mainClass;
    private final String hdfsUser;
    private final AppId taskletScopedAppId;
    private final ObjectMapper objectMapper;

    public PipelineJobTasklet(
            final Environment env,
            final SparkConfFactory sparkConfFactory,
            final SparkJobExecutor sparkJobExecutor,
            final AppId taskletScopedAppId,
            final ObjectMapper objectMapper) {
        this.sparkConfFactory = sparkConfFactory;

        driverJar = env.getRequiredProperty("spark.drivers.pipeline.resource");
        mainClass = env.getRequiredProperty("spark.drivers.pipeline.mainClass");
        hdfsUser = env.getRequiredProperty("hadoop.user");

        this.sparkJobExecutor = sparkJobExecutor;
        this.taskletScopedAppId = taskletScopedAppId;
        this.objectMapper = objectMapper;
    }

    @Override
    public void stop() {
        if (taskletScopedAppId.isNotEmpty()) {
            sparkJobExecutor.stop(taskletScopedAppId);
        } else {
            log.warn("Tasklet app id was not updated: {}", taskletScopedAppId);
        }
    }

    @Override
    public RepeatStatus execute(
            final StepContribution contribution, final ChunkContext chunkContext) throws Exception {

        ServiceRequest serviceRequest = sparkJobExecutor.setupRequest(chunkContext);
        SparkSubmitOption sparkSubmitOption = createSparkSubmitOption(serviceRequest);

        sparkJobExecutor.executeSparkJob(contribution, chunkContext, taskletScopedAppId, sparkSubmitOption);
        return RepeatStatus.FINISHED;
    }

    private SparkSubmitOption createSparkSubmitOption(final ServiceRequest serviceRequest) throws IOException {
        Long requestId = serviceRequest.getId();
        PipelineAppConfig jobRequest = toJobRequest(serviceRequest);

        List<String> extraJars = getExtraJarsForPipeline(jobRequest);

        return SparkSubmitOption.builder()
                .clazz(mainClass)
                .job(jobRequest)
                .jobExecutionId(requestId)
                .jobType(JobType.PIPELINE)
                .hdfsUser(hdfsUser)
                .jobName(JobType.PIPELINE + " Job " + requestId)
                .driverJar(driverJar)
                .extraJars(extraJars)
                .sparkConf(sparkConfFactory.create())
                .build();
    }

    private PipelineAppConfig toJobRequest(final ServiceRequest serviceRequest) throws IOException {
        return objectMapper.readValue(serviceRequest.getRequestMessage(), PipelineAppConfig.class);
    }

    private List<String> getExtraJarsForPipeline(final PipelineAppConfig pipelineAppConfig) {
        return pipelineAppConfig.getPipelineSteps().stream()
                .filter(step -> ScriptletStepAppConfig.class.isAssignableFrom(step.getClass()))
                .map(ScriptletStepAppConfig.class::cast)
                .map(ScriptletStepAppConfig::getJarFile)
                .distinct()
                .collect(Collectors.toList());
    }
}
