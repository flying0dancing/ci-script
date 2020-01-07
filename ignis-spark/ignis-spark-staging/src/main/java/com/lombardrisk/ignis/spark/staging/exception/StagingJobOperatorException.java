package com.lombardrisk.ignis.spark.staging.exception;

import com.lombardrisk.ignis.spark.core.JobOperatorException;

public class StagingJobOperatorException extends JobOperatorException {

    private static final long serialVersionUID = -3708025315511704986L;

    public StagingJobOperatorException(Throwable cause) {
        super(cause);
    }

    StagingJobOperatorException(String message, Throwable cause) {
        super(message, cause);
    }

}
