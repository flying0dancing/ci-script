package com.lombardrisk.ignis.server.job.pipeline.model;

import com.lombardrisk.ignis.common.MapperUtils;
import com.lombardrisk.ignis.server.dataset.pipeline.invocation.model.PipelineStepInvocation;
import com.lombardrisk.ignis.server.product.pipeline.model.PipelineScriptletStep;
import com.lombardrisk.ignis.server.product.table.model.Table;
import com.lombardrisk.ignis.spark.api.pipeline.PipelineStepAppConfig;
import com.lombardrisk.ignis.spark.api.pipeline.ScriptletStepAppConfig;
import io.vavr.Tuple;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toMap;

@EqualsAndHashCode(callSuper = true)
@Data
public class PipelineScriptletInput extends PipelineStepInput<PipelineScriptletStep> {

    private final Set<PipelineStepDatasetInput> datasetInputs;

    @Builder
    public PipelineScriptletInput(
            final PipelineScriptletStep pipelineStep,
            final Table schemaOut,
            final Set<PipelineStepDatasetInput> datasetInputs,
            final boolean skipped) {

        super(pipelineStep, schemaOut, null, skipped);
        this.datasetInputs = datasetInputs;
    }

    @Override
    public PipelineStepAppConfig toPipelineStepAppConfig(
            final String entityCode,
            final LocalDate referenceDate,
            final Map<Long, PipelineStepInvocation> stepIdsToStepInvocations) {

        PipelineScriptletStep step = getPipelineStep();

        return ScriptletStepAppConfig.builder()
                .pipelineStepInvocationId(stepIdsToStepInvocations.get(step.getId()).getId())
                .pipelineStepDatasetLookups(MapperUtils.map(datasetInputs, datasetInput ->
                        toPipelineStepDatasetLookup(datasetInput, stepIdsToStepInvocations)))
                .outputDataset(stagingDatasetConfig(entityCode, referenceDate))
                .jarFile(step.getJarFile())
                .className(step.getClassName())
                .inputSchemaMappings(getInputSchemaMappings())
                .build();
    }

    private Map<String, String> getInputSchemaMappings() {
        return getPipelineStep().getSchemaIns().stream()
                .map(input -> Tuple.of(input.getInputName(), input.getSchemaIn().getPhysicalTableName()))
                .collect(toMap(t -> t._1, t -> t._2));
    }
}
