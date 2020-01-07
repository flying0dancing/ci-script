package com.lombardrisk.ignis.spark.staging.exception;

import com.lombardrisk.ignis.spark.core.JobOperatorException;

public class DatasetLoaderException extends JobOperatorException {

    private static final long serialVersionUID = -528649486227617198L;

    public DatasetLoaderException(Throwable cause) {
        super(cause);
    }
}
