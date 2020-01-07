package com.lombardrisk.ignis.server.job.pipeline.model;

import com.lombardrisk.ignis.pipeline.step.api.SelectColumn;
import com.lombardrisk.ignis.server.dataset.model.Dataset;
import com.lombardrisk.ignis.server.dataset.pipeline.invocation.model.PipelineStepInvocation;
import com.lombardrisk.ignis.server.product.pipeline.model.PipelineStep;
import com.lombardrisk.ignis.server.product.table.model.Table;
import com.lombardrisk.ignis.spark.api.DatasetTableLookup;
import com.lombardrisk.ignis.spark.api.pipeline.PipelineStepAppConfig;
import com.lombardrisk.ignis.spark.api.pipeline.PipelineStepDatasetLookup;
import com.lombardrisk.ignis.spark.api.staging.DatasetProperties;
import com.lombardrisk.ignis.spark.api.staging.StagingDatasetConfig;
import com.lombardrisk.ignis.spark.api.staging.StagingSchemaValidation;
import io.vavr.control.Either;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

@AllArgsConstructor
@Data
public abstract class PipelineStepInput<T extends PipelineStep> {

    private final T pipelineStep;
    private final Table schemaOut;
    private final Set<SelectColumn> selectColumns;
    private final boolean skipped;

    public abstract PipelineStepAppConfig toPipelineStepAppConfig(
            String entityCode,
            LocalDate referenceDate,
            Map<Long, PipelineStepInvocation> stepIdsToStepInvocations);

    protected StagingDatasetConfig stagingDatasetConfig(final String entityCode, final LocalDate referenceDate) {
        return StagingDatasetConfig.builder()
                .stagingSchemaValidation(StagingSchemaValidation.builder()
                        .physicalTableName(schemaOut.getPhysicalTableName())
                        .displayName(schemaOut.getDisplayName())
                        .schemaId(schemaOut.getId())
                        .build())
                .datasetProperties(DatasetProperties.builder()
                        .entityCode(entityCode)
                        .referenceDate(referenceDate)
                        .build())
                .build();
    }

    static PipelineStepDatasetLookup toPipelineStepDatasetLookup(
            final PipelineStepDatasetInput datasetInput,
            final Map<Long, PipelineStepInvocation> stepIdsToStepInvocations) {

        Either<Dataset, PipelineStep> datasetLookup = datasetInput.getDatasetInput();

        return datasetLookup.isLeft()
                ? toDatasetLookup(datasetLookup.getLeft())
                : toPipelineStepInvocationLookup(datasetLookup.get(), stepIdsToStepInvocations);
    }

    private static PipelineStepDatasetLookup toDatasetLookup(final Dataset dataset) {
        return PipelineStepDatasetLookup.datasetTableLookup(
                DatasetTableLookup.builder()
                        .datasetId(dataset.getId())
                        .datasetName(dataset.getSchema().getPhysicalTableName())
                        .predicate(dataset.getPredicate())
                        .rowKeySeed(dataset.getRowKeySeed())
                        .recordsCount(dataset.getRecordsCount())
                        .build());
    }

    private static PipelineStepDatasetLookup toPipelineStepInvocationLookup(
            final PipelineStep dependentStep,
            final Map<Long, PipelineStepInvocation> stepIdsToStepInvocations) {

        PipelineStepInvocation dependentStepInvocation = stepIdsToStepInvocations.get(dependentStep.getId());
        return PipelineStepDatasetLookup.pipelineStepInvocationId(dependentStepInvocation.getId());
    }
}
