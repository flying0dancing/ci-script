package com.lombardrisk.ignis.server.job;

import com.lombardrisk.ignis.data.common.error.ErrorCoded;

public enum JobFailure implements ErrorCoded {
    DATASET_NOT_FOUND,
    JOB_COULD_NOT_START;

    @Override
    public String getErrorCode() {
        return name();
    }

}
