package com.lombardrisk.ignis.server.product.pipeline.view;

import com.lombardrisk.ignis.client.external.pipeline.view.PipelineStepView;
import com.lombardrisk.ignis.client.external.pipeline.view.PipelineView;
import com.lombardrisk.ignis.server.product.pipeline.model.Pipeline;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PipelineToPipelineView implements Function<Pipeline, PipelineView> {

    private final PipelineStepToPipelineStepView pipelineStepDetailsToPipelineStepView =
            new PipelineStepToPipelineStepView();

    @Override
    public PipelineView apply(final Pipeline pipeline) {
        List<PipelineStepView> pipelinesAndSchemaIds = pipeline
                .getSteps().stream()
                .map(pipelineStepDetailsToPipelineStepView)
                .collect(Collectors.toList());

        return PipelineView.builder()
                .id(pipeline.getId())
                .name(pipeline.getName())
                .steps(pipelinesAndSchemaIds)
                .build();
    }
}
