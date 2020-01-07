package com.lombardrisk.ignis.server.util.spark;

public enum FinalAppStatus {

    /** Undefined state when either the application has not yet finished */
    UNDEFINED,

    /** Application which finished successfully. */
    SUCCEEDED,

    /** Application which failed. */
    FAILED,

    /** Application which was terminated by a user or admin. */
    KILLED
}
