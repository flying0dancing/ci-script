package com.lombardrisk.ignis.functional.test.steps.service;

public class StagingServiceProvider {

    private final StagingServiceV1 v1;
    private final StagingServiceV2 v2;

    public StagingServiceProvider(
            final StagingServiceV1 v1,
            final StagingServiceV2 v2) {
        this.v1 = v1;
        this.v2 = v2;
    }

    public StagingServiceV1 v1() {
        return v1;
    }

    public StagingServiceV2 v2() {
        return v2;
    }
}
