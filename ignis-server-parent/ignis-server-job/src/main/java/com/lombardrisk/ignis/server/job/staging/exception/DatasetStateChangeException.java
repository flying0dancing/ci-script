package com.lombardrisk.ignis.server.job.staging.exception;

import com.lombardrisk.ignis.api.dataset.DatasetState;

public class DatasetStateChangeException extends Exception {

    public DatasetStateChangeException(final String message) {
        super(message);
    }

    public DatasetStateChangeException(
            final long jobExecutionId,
            final String dataset,
            final DatasetState fromState,
            final DatasetState toState) {
        super(String.format(
                "Can't transit dataset state from %s to %s with stagingJobId:%d,dataset:%s",
                fromState,
                toState,
                jobExecutionId,
                dataset));
    }
}
