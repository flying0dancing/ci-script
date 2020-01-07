package com.lombardrisk.ignis.functional.test.steps.service;

import com.lombardrisk.ignis.client.external.dataset.DatasetQueryParams;

import java.util.Set;

public interface StagingService<T> {

    Long startJob(T request);

    Set<DatasetQueryParams> toDatasetQueryParams(T request);
}
