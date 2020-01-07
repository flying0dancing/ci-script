package com.lombardrisk.ignis.server.job.servicerequest;

public enum ServiceRequestType {
    STAGING("Staging"),
    VALIDATION("Validation"),
    IMPORT_PRODUCT("Import product"),
    ROLLBACK_PRODUCT("Rollback product"),
    PIPELINE("Pipeline");

    private final String displayName;

    ServiceRequestType(final String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
