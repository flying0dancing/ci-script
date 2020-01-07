package com.lombardrisk.ignis.server.batch.staging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lombardrisk.ignis.client.external.job.pipeline.PipelineRequest;
import com.lombardrisk.ignis.data.common.Identifiable;
import com.lombardrisk.ignis.data.common.error.ErrorResponse;
import com.lombardrisk.ignis.server.batch.SparkJobExecutor;
import com.lombardrisk.ignis.server.job.pipeline.PipelineJobService;
import com.lombardrisk.ignis.server.job.servicerequest.ServiceRequest;
import com.lombardrisk.ignis.spark.api.staging.DownstreamPipeline;
import com.lombardrisk.ignis.spark.api.staging.StagingAppConfig;
import io.vavr.control.Validation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.StoppableTasklet;
import org.springframework.batch.repeat.RepeatStatus;

import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

@Slf4j
@AllArgsConstructor
public class DownstreamPipelineTasklet implements StoppableTasklet {

    private final SparkJobExecutor sparkJobExecutor;
    private final PipelineJobService pipelineJobService;
    private final ObjectMapper objectMapper;

    @Override
    public void stop() {
        //no-op
    }

    @Override
    public RepeatStatus execute(
            final StepContribution contribution, final ChunkContext chunkContext) throws Exception {

        ServiceRequest serviceRequest = sparkJobExecutor.setupRequest(chunkContext);

        StagingAppConfig appConfig = objectMapper.readValue(serviceRequest.getRequestMessage(), StagingAppConfig.class);

        if (isNotEmpty(appConfig.getDownstreamPipelines())) {
            for (DownstreamPipeline downstreamPipeline : appConfig.getDownstreamPipelines()) {
                log.info("Starting downstream pipeline job for pipeline [{}], entity code [{}], reference date [{}]",
                        downstreamPipeline.getPipelineId(),
                        downstreamPipeline.getEntityCode(),
                        downstreamPipeline.getReferenceDate());

                PipelineRequest downstreamRequest = PipelineRequest.builder()
                        .name(downstreamPipeline.getPipelineName())
                        .entityCode(downstreamPipeline.getEntityCode())
                        .referenceDate(downstreamPipeline.getReferenceDate())
                        .pipelineId(downstreamPipeline.getPipelineId())
                        .build();

                Validation<List<ErrorResponse>, Identifiable> startJobResponse =
                        pipelineJobService.startJob(downstreamRequest, serviceRequest.getCreatedBy());

                if (startJobResponse.isValid()) {
                    log.info("Started downstream pipeline job [{}]",
                            startJobResponse.get().getId());
                } else {
                    log.error("Failed to start downstream job, [{}]",
                            formatErrorResponse(startJobResponse.getError()));
                }
            }
        }

        return RepeatStatus.FINISHED;
    }

    private String formatErrorResponse(final List<ErrorResponse> errorResponses) {
        return errorResponses.stream()
                .map(error -> String.format("[%s: %s]", error.getErrorCode(), error.getErrorMessage()))
                .collect(Collectors.joining(", "));
    }
}

