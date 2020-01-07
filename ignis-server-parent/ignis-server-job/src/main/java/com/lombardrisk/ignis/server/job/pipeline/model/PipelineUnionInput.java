package com.lombardrisk.ignis.server.job.pipeline.model;

import com.lombardrisk.ignis.common.MapperUtils;
import com.lombardrisk.ignis.common.stream.CollectorUtils;
import com.lombardrisk.ignis.pipeline.step.api.SelectColumn;
import com.lombardrisk.ignis.pipeline.step.api.UnionAppConfig;
import com.lombardrisk.ignis.pipeline.step.api.UnionSpec;
import com.lombardrisk.ignis.server.dataset.pipeline.invocation.model.PipelineStepInvocation;
import com.lombardrisk.ignis.server.product.pipeline.model.PipelineUnionStep;
import com.lombardrisk.ignis.server.product.pipeline.select.PipelineFilter;
import com.lombardrisk.ignis.server.product.table.model.Table;
import com.lombardrisk.ignis.spark.api.pipeline.PipelineStepAppConfig;
import com.lombardrisk.ignis.spark.api.pipeline.UnionStepAppConfig;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
@Data
public class PipelineUnionInput extends PipelineStepInput<PipelineUnionStep> {

    private final Set<PipelineStepDatasetInput> datasetInputs;

    @Builder
    public PipelineUnionInput(
            final PipelineUnionStep pipelineStep,
            final Table schemaOut,
            final Set<PipelineStepDatasetInput> datasetInputs,
            final Set<SelectColumn> selectColumns,
            final boolean skipped) {

        super(pipelineStep, schemaOut, selectColumns, skipped);
        this.datasetInputs = datasetInputs;
    }

    @Override
    public PipelineStepAppConfig toPipelineStepAppConfig(
            final String entityCode,
            final LocalDate referenceDate,
            final Map<Long, PipelineStepInvocation> stepIdsToStepInvocations) {

        return UnionStepAppConfig.builder()
                .pipelineStepInvocationId(stepIdsToStepInvocations.get(getPipelineStep().getId()).getId())
                .pipelineStepDatasetLookups(
                        MapperUtils.map(
                                datasetInputs,
                                datasetInput -> toPipelineStepDatasetLookup(datasetInput, stepIdsToStepInvocations)))
                .unions(toUnionConfig(getSelectColumns()))
                .outputDataset(stagingDatasetConfig(entityCode, referenceDate))
                .build();
    }

    private Set<UnionAppConfig> toUnionConfig(final Set<SelectColumn> selectColumns) {
        Map<UnionSpec, Set<SelectColumn>> unionToSelects = selectColumns.stream()
                .collect(CollectorUtils.toMultimap(SelectColumn::getUnion, Function.identity()));

        Map<Long, Set<String>> unionToFilters = getPipelineStep().getPipelineFilters().stream()
                .collect(CollectorUtils.toMultimap(PipelineFilter::getUnionSchemaId, PipelineFilter::getFilter));

        return unionToSelects.entrySet().stream()
                .map(entry -> UnionAppConfig.builder()
                        .schemaIn(entry.getKey())
                        .selects(entry.getValue())
                        .filters(unionToFilters.get(entry.getKey().getSchemaId()))
                        .build()).collect(Collectors.toSet());
    }
}
