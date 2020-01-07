package com.lombardrisk.ignis.server.job.pipeline.model;

import com.lombardrisk.ignis.pipeline.step.api.SelectColumn;
import com.lombardrisk.ignis.server.dataset.pipeline.invocation.model.PipelineStepInvocation;
import com.lombardrisk.ignis.server.product.pipeline.model.PipelineAggregationStep;
import com.lombardrisk.ignis.server.product.table.model.Table;
import com.lombardrisk.ignis.spark.api.pipeline.AggregateStepAppConfig;
import com.lombardrisk.ignis.spark.api.pipeline.PipelineStepAppConfig;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Data
public class PipelineAggregationInput extends PipelineStepInput<PipelineAggregationStep> {

    private final PipelineStepDatasetInput datasetInput;

    @Builder
    public PipelineAggregationInput(
            final PipelineAggregationStep pipelineStep,
            final Table schemaOut,
            final PipelineStepDatasetInput datasetInput,
            final Set<SelectColumn> selectColumns,
            final boolean skipped) {

        super(pipelineStep, schemaOut, selectColumns, skipped);
        this.datasetInput = datasetInput;
    }

    @Override
    public PipelineStepAppConfig toPipelineStepAppConfig(
            final String entityCode,
            final LocalDate referenceDate,
            final Map<Long, PipelineStepInvocation> stepIdsToStepInvocations) {

        return AggregateStepAppConfig.builder()
                .pipelineStepInvocationId(stepIdsToStepInvocations.get(getPipelineStep().getId()).getId())
                .pipelineStepDatasetLookup(toPipelineStepDatasetLookup(datasetInput, stepIdsToStepInvocations))
                .selects(getSelectColumns())
                .filters(getPipelineStep().getFilters())
                .groupings(getPipelineStep().getGroupings())
                .outputDataset(stagingDatasetConfig(entityCode, referenceDate))
                .build();
    }
}
