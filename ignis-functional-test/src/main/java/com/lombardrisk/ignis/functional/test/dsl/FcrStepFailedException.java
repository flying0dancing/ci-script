package com.lombardrisk.ignis.functional.test.dsl;

public class FcrStepFailedException extends RuntimeException {

    public FcrStepFailedException(final String message, final Throwable throwable) {
        super(message, throwable);
    }
}
