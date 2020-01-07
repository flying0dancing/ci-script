package com.lombardrisk.ignis.server.controller.staging;

import com.lombardrisk.ignis.data.common.error.ErrorCoded;

public enum StagingErrorCodes implements ErrorCoded {

    DATASET_ERRORS_FILE_NOT_FOUND;

    @Override
    public String getErrorCode() {
        return name();
    }
}
