package com.lombardrisk.ignis.spark.pipeline.job;

import com.lombardrisk.ignis.spark.core.JobOperatorException;

public class PipelineJobOperatorException extends JobOperatorException {

    public PipelineJobOperatorException(final Throwable cause) {
        super(cause);
    }
}
