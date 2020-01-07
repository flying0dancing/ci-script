package com.lombardrisk.ignis.spark.validation.exception;

import com.lombardrisk.ignis.spark.core.JobOperatorException;

public class ValidationJobOperatorException extends JobOperatorException {

    private static final long serialVersionUID = -3916025964771268130L;

    public ValidationJobOperatorException(String message) {
        super(message);
    }

    public ValidationJobOperatorException(Throwable cause) {
        super(cause);
    }
}
