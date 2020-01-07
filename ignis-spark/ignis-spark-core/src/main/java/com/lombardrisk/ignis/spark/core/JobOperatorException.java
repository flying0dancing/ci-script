package com.lombardrisk.ignis.spark.core;

public class JobOperatorException extends RuntimeException {

    private static final long serialVersionUID = -6995408551104988525L;

    public JobOperatorException(String message) {
        super(message);
    }

    public JobOperatorException(String message, Throwable cause) {
        super(message, cause);
    }

    public JobOperatorException(Throwable cause) {
        super(cause);
    }

    public JobOperatorException(
            String message,
            Throwable cause,
            boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
