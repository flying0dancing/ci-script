package com.lombardrisk.ignis.server.batch.exception;

public class JobExecutionStopException extends Exception {

    public JobExecutionStopException(final String message) {
        super(message);
    }

    public JobExecutionStopException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public JobExecutionStopException(final Throwable cause) {
        super(cause);
    }
}
