package com.lombardrisk.ignis.server.job.pipeline.model;

import com.lombardrisk.ignis.server.dataset.pipeline.invocation.model.PipelineInvocation;
import com.lombardrisk.ignis.server.dataset.pipeline.invocation.model.PipelineStepInvocation;
import com.lombardrisk.ignis.server.product.pipeline.model.Pipeline;
import com.lombardrisk.ignis.spark.api.pipeline.PipelineStepAppConfig;
import io.vavr.Tuple;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@AllArgsConstructor
@Builder
@Data
public class PipelineInput {

    private final String jobName;
    private final String entityCode;
    private final LocalDate referenceDate;
    private final Pipeline pipeline;
    private final List<PipelineStepInput> pipelineSteps;

    public List<PipelineStepAppConfig> getPipelineStepAppConfigs(final PipelineInvocation pipelineInvocation) {
        final Map<Long, PipelineStepInvocation> stepIdsToStepInvocations = pipelineInvocation.getSteps().stream()
                .map(stepInvocation -> Tuple.of(stepInvocation.getPipelineStepId(), stepInvocation))
                .collect(toMap(tuple -> tuple._1, tuple -> tuple._2));

        return pipelineSteps.stream()
                .filter(s -> !s.isSkipped())
                .map(pipelineStepInput ->
                        pipelineStepInput.toPipelineStepAppConfig(entityCode, referenceDate, stepIdsToStepInvocations))
                .collect(toList());
    }
}
