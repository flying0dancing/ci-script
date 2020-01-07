package com.lombardrisk.ignis.server.util.spark;

public enum AppStatus {
    /** Application which was just created. */
    NEW,

    /** Application which is being saved. */
    NEW_SAVING,

    /** Application which has been submitted. */
    SUBMITTED,

    /** Application has been accepted by the scheduler */
    ACCEPTED,

    /** Application which is currently running. */
    RUNNING,

    /** Application which finished successfully. */
    FINISHED,

    /** Application which failed. */
    FAILED,

    /** Application which was terminated by a user or admin. */
    KILLED
}
