package com.lombardrisk.ignis.server.batch;

import lombok.ToString;

@ToString
public class DatasetId {

    private Long id;

    public Long get() {
        return id;
    }

    public void set(final Long stagingDatasetId) {
        id = stagingDatasetId;
    }

    public boolean isPresent() {
        return id != null;
    }
}
