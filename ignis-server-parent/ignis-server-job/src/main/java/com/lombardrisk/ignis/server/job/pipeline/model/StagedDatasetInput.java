package com.lombardrisk.ignis.server.job.pipeline.model;

import com.lombardrisk.ignis.server.dataset.model.Dataset;
import com.lombardrisk.ignis.server.product.pipeline.model.PipelineStep;
import io.vavr.control.Either;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StagedDatasetInput implements PipelineStepDatasetInput {

    private final Dataset dataset;

    @Override
    public Either<Dataset, PipelineStep> getDatasetInput() {
        return Either.left(dataset);
    }
}
