package com.lombardrisk.ignis.spark.api.pipeline;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.lombardrisk.ignis.spark.api.DatasetTableLookup;
import io.vavr.control.Either;
import io.vavr.control.Option;
import lombok.Data;

@Data
public class PipelineStepDatasetLookup {
    private final DatasetTableLookup datasetTableLookup;
    private final Long pipelineStepInvocationId;

    @JsonCreator
    public PipelineStepDatasetLookup(
            @JsonProperty("datasetTableLookup") final DatasetTableLookup datasetTableLookup,
            @JsonProperty("pipelineStepInvocationId") final Long pipelineStepInvocationId) {

        if (datasetTableLookup == null && pipelineStepInvocationId == null) {
            throw new IllegalArgumentException("Must provide either dataset or pipeline step invocation ID");
        }

        this.datasetTableLookup = datasetTableLookup;
        this.pipelineStepInvocationId = pipelineStepInvocationId;
    }

    public static PipelineStepDatasetLookup datasetTableLookup(final DatasetTableLookup datasetTableLookup) {
        return new PipelineStepDatasetLookup(datasetTableLookup, null);
    }

    public static PipelineStepDatasetLookup pipelineStepInvocationId(final Long pipelineStepInvocationId) {
        return new PipelineStepDatasetLookup(null, pipelineStepInvocationId);
    }

    public Either<DatasetTableLookup, Long> toLookup() {
        return Option.of(pipelineStepInvocationId).toEither(datasetTableLookup);
    }
}
