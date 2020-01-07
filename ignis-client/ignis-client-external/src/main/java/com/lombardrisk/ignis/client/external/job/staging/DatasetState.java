package com.lombardrisk.ignis.client.external.job.staging;

public enum DatasetState {
    QUEUED,
    UPLOADING,
    UPLOADED,
    UPLOAD_FAILED,
    VALIDATING,
    VALIDATION_FAILED,
    VALIDATED,
    REGISTERING,
    REGISTERED,
    REGISTRATION_FAILED
}
