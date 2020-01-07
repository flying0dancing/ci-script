package com.lombardrisk.ignis.design.server.pipeline.converter;

import com.lombardrisk.ignis.client.core.response.ApiErrorCode;
import com.lombardrisk.ignis.client.design.pipeline.PipelinePlanErrorView;
import com.lombardrisk.ignis.client.design.pipeline.PipelinePlanErrorView.GraphNotConnectedView;
import com.lombardrisk.ignis.client.design.pipeline.PipelinePlanErrorView.MultipleStepsToSameOutputSchemaView;
import com.lombardrisk.ignis.client.design.pipeline.PipelineView;
import com.lombardrisk.ignis.common.MapperUtils;
import com.lombardrisk.ignis.design.server.pipeline.model.Pipeline;
import com.lombardrisk.ignis.design.server.pipeline.model.PipelineStep;
import com.lombardrisk.ignis.pipeline.PipelinePlan;
import com.lombardrisk.ignis.pipeline.PipelinePlanError;
import com.lombardrisk.ignis.pipeline.PipelinePlanError.MultipleStepsToSameOutput;
import com.lombardrisk.ignis.pipeline.PipelinePlanGenerator;
import io.vavr.Function1;
import io.vavr.control.Validation;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;

public class PipelineConverter implements Function1<Pipeline, PipelineView> {

    private static final long serialVersionUID = -5185043632396200630L;
    private final PipelineStepConverter stepConverter;
    private final PipelinePlanGenerator<Long, PipelineStep> pipelinePlanGenerator;

    @Autowired
    public PipelineConverter(
            final PipelineStepConverter stepConverter,
            final PipelinePlanGenerator<Long, PipelineStep> pipelinePlanGenerator) {
        this.stepConverter = stepConverter;
        this.pipelinePlanGenerator = pipelinePlanGenerator;
    }

    @Override
    public PipelineView apply(final Pipeline pipeline) {
        return PipelineView.builder()
                .id(pipeline.getId())
                .name(pipeline.getName())
                .productId(pipeline.getProductId())
                .steps(MapperUtils.mapOrEmptySet(pipeline.getSteps(), stepConverter))
                .error(extractPipelineError(pipeline))
                .build();
    }

    private PipelinePlanErrorView extractPipelineError(final Pipeline pipeline) {
        Validation<PipelinePlanError<Long, PipelineStep>, PipelinePlan<Long, PipelineStep>> pipelinePlans =
                pipelinePlanGenerator.generateExecutionPlan(pipeline);

        if (pipelinePlans.isValid()) {
            return null;
        }

        PipelinePlanError<Long, PipelineStep> planError = pipelinePlans.getError();

        PipelinePlanErrorView.PipelinePlanErrorViewBuilder errorBuilder = PipelinePlanErrorView.builder()
                .selfJoiningSteps(MapperUtils.map(planError.getSelfJoiningSteps(), PipelineStep::getId))
                .hasCycles(planError.isHasCycles());

        if (planError.getGraphNotConnected() != null) {
            List<Set<Long>> connectedSets = planError.getGraphNotConnected()
                    .getConnectedSets();

            errorBuilder.graphNotConnected(new GraphNotConnectedView(connectedSets));
        }

        for (MultipleStepsToSameOutput<Long, PipelineStep> stepsToSameOutputSchema
                : planError.getStepsToSameOutputSchemas()) {

            List<Long> stepIds = MapperUtils.map(stepsToSameOutputSchema.getSteps(), PipelineStep::getId);
            errorBuilder.stepsToSameOutputSchema(new MultipleStepsToSameOutputSchemaView(
                    stepIds, stepsToSameOutputSchema.getOutput()));
        }

        return errorBuilder.errors(MapperUtils.map(
                planError.getErrors(),
                error -> new ApiErrorCode(error.getErrorCode(), error.getErrorMessage())))
                .build();
    }
}
