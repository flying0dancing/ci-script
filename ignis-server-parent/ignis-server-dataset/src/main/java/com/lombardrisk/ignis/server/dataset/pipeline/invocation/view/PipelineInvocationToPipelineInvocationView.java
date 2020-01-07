package com.lombardrisk.ignis.server.dataset.pipeline.invocation.view;

import com.lombardrisk.ignis.client.external.pipeline.view.PipelineInvocationView;
import com.lombardrisk.ignis.client.external.pipeline.view.PipelineStepInvocationDatasetView;
import com.lombardrisk.ignis.client.external.pipeline.view.PipelineStepInvocationView;
import com.lombardrisk.ignis.client.external.pipeline.view.PipelineStepStatus;
import com.lombardrisk.ignis.common.MapperUtils;
import com.lombardrisk.ignis.common.time.TimeSource;
import com.lombardrisk.ignis.server.dataset.pipeline.invocation.model.PipelineInvocation;
import com.lombardrisk.ignis.server.dataset.pipeline.invocation.model.PipelineStepInvocation;
import com.lombardrisk.ignis.server.dataset.pipeline.invocation.model.PipelineStepInvocationDataset;
import com.lombardrisk.ignis.server.product.pipeline.view.PipelineStepToPipelineStepView;
import lombok.AllArgsConstructor;

import java.util.function.Function;

@AllArgsConstructor
public class PipelineInvocationToPipelineInvocationView implements Function<PipelineInvocation, PipelineInvocationView> {

    private final TimeSource timeSource;

    private final PipelineStepToPipelineStepView pipelineStepDetailsToPipelineStepView =
            new PipelineStepToPipelineStepView();

    @Override
    public PipelineInvocationView apply(final PipelineInvocation pipelineInvocation) {
        return PipelineInvocationView.builder()
                .id(pipelineInvocation.getId())
                .name(pipelineInvocation.getName())
                .createdTime(timeSource.toZonedDateTime(pipelineInvocation.getCreatedTime()))
                .serviceRequestId(pipelineInvocation.getServiceRequestId())
                .pipelineId(pipelineInvocation.getPipelineId())
                .entityCode(pipelineInvocation.getEntityCode())
                .referenceDate(pipelineInvocation.getReferenceDate())
                .invocationSteps(MapperUtils.map(pipelineInvocation.getSteps(), this::apply))
                .build();
    }

    private PipelineStepInvocationView apply(final PipelineStepInvocation pipelineStepInvocation) {
        return PipelineStepInvocationView.builder()
                .id(pipelineStepInvocation.getId())
                .datasetsIn(MapperUtils.mapSet(pipelineStepInvocation.getInputDatasets(), this::apply))
                .inputPipelineStepIds(pipelineStepInvocation.getInputPipelineStepIds())
                .datasetOutId(pipelineStepInvocation.getOutputDatasetIds().stream()
                        .findFirst()
                        .orElse(null))
                .pipelineStep(pipelineStepDetailsToPipelineStepView.apply(pipelineStepInvocation.getPipelineStep()))
                .status(PipelineStepStatus.valueOf(pipelineStepInvocation.getStatus().toString()))
                .build();
    }

    private PipelineStepInvocationDatasetView apply(final PipelineStepInvocationDataset inputDataset) {
        return PipelineStepInvocationDatasetView.builder()
                .datasetId(inputDataset.getDatasetId())
                .datasetRunKey(inputDataset.getDatasetRunKey())
                .build();
    }
}
