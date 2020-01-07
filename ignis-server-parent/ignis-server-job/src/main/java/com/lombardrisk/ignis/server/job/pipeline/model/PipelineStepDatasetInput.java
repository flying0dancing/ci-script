package com.lombardrisk.ignis.server.job.pipeline.model;

import com.lombardrisk.ignis.server.dataset.model.Dataset;
import com.lombardrisk.ignis.server.product.pipeline.model.PipelineStep;
import io.vavr.control.Either;

public interface PipelineStepDatasetInput {

    Either<Dataset, PipelineStep> getDatasetInput();
}
