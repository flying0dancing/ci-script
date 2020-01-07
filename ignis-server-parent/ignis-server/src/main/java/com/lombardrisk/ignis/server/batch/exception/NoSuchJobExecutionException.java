package com.lombardrisk.ignis.server.batch.exception;

public class NoSuchJobExecutionException extends Exception {

    public NoSuchJobExecutionException(long jobExecutionId) {
        super(String.format("No such job instance with id: %d", jobExecutionId));
    }
}
