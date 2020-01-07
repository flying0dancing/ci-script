package com.lombardrisk.ignis.server.job.pipeline.model;

import com.lombardrisk.ignis.common.MapperUtils;
import com.lombardrisk.ignis.pipeline.step.api.JoinAppConfig;
import com.lombardrisk.ignis.pipeline.step.api.JoinFieldConfig;
import com.lombardrisk.ignis.pipeline.step.api.SelectColumn;
import com.lombardrisk.ignis.server.dataset.pipeline.invocation.model.PipelineStepInvocation;
import com.lombardrisk.ignis.server.product.pipeline.model.PipelineJoinStep;
import com.lombardrisk.ignis.server.product.pipeline.transformation.Join;
import com.lombardrisk.ignis.server.product.pipeline.transformation.JoinField;
import com.lombardrisk.ignis.server.product.table.model.Table;
import com.lombardrisk.ignis.spark.api.pipeline.JoinStepAppConfig;
import com.lombardrisk.ignis.spark.api.pipeline.PipelineStepAppConfig;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@EqualsAndHashCode(callSuper = true)
@Data
public class PipelineJoinInput extends PipelineStepInput<PipelineJoinStep> {

    private final Set<PipelineStepDatasetInput> datasetInputs;

    @Builder
    public PipelineJoinInput(
            final PipelineJoinStep pipelineStep,
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

        return JoinStepAppConfig.builder()
                .pipelineStepInvocationId(stepIdsToStepInvocations.get(getPipelineStep().getId()).getId())
                .pipelineStepDatasetLookups(MapperUtils.map(datasetInputs, datasetInput ->
                        toPipelineStepDatasetLookup(datasetInput, stepIdsToStepInvocations)))
                .selects(getSelectColumns())
                .outputDataset(stagingDatasetConfig(entityCode, referenceDate))
                .joinAppConfigs(
                        getPipelineStep().getJoins().stream()
                                .map(this::toJoinAppConfig)
                                .collect(toSet()))
                .build();
    }

    private JoinAppConfig toJoinAppConfig(final Join join) {
        return JoinAppConfig.builder()
                .rightSchemaName(join.getRightSchema().getPhysicalTableName())
                .leftSchemaName(join.getLeftSchema().getPhysicalTableName())
                .joinType(JoinAppConfig.JoinType.valueOf(join.getJoinType().name()))
                .joinFields(toJoinFieldConfigs(join))
                .build();
    }

    private List<JoinFieldConfig> toJoinFieldConfigs(final Join join) {
        return join.getJoinFields().stream()
                .map(this::toJoinFieldConfig)
                .collect(toList());
    }

    private JoinFieldConfig toJoinFieldConfig(final JoinField joinField) {
        return JoinFieldConfig.builder()
                .leftJoinField(joinField.getLeftJoinField().getName())
                .rightJoinField(joinField.getRightJoinField().getName())
                .build();
    }
}
