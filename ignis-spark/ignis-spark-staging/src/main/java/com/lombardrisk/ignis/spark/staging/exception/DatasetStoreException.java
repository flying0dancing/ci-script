package com.lombardrisk.ignis.spark.staging.exception;

import com.lombardrisk.ignis.spark.core.JobOperatorException;

public class DatasetStoreException extends JobOperatorException {

    private static final long serialVersionUID = 30496410319081586L;

    public DatasetStoreException(String message, Throwable cause) {
        super(message, cause);
    }
}
