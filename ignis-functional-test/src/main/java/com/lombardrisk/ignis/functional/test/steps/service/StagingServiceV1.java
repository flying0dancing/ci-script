package com.lombardrisk.ignis.functional.test.steps.service;

import com.lombardrisk.ignis.client.external.dataset.DatasetQueryParams;
import com.lombardrisk.ignis.client.external.job.JobsClient;
import com.lombardrisk.ignis.client.external.job.staging.request.v1.StagingRequest;

import java.util.Set;

import static com.lombardrisk.ignis.functional.test.steps.CallAssertion.callAndExpectSuccess;
import static com.lombardrisk.ignis.functional.test.utils.ReferenceDateParser.convertToLocalDate;
import static java.util.stream.Collectors.toSet;

public class StagingServiceV1 implements StagingService<StagingRequest> {
    private final JobsClient jobsClient;

    public StagingServiceV1(final JobsClient jobsClient) {
        this.jobsClient = jobsClient;
    }

    @Override
    public Long startJob(final StagingRequest stagingRequest) {
        return callAndExpectSuccess(jobsClient.startJob(stagingRequest)).getId();
    }

    @Override
    public Set<DatasetQueryParams> toDatasetQueryParams(final StagingRequest stagingRequest) {
        return stagingRequest.getItems().stream()
                .map(item -> DatasetQueryParams.builder()
                        .schema(item.getSchema())
                        .entityCode(item.getDataset().getEntityCode())
                        .referenceDate(convertToLocalDate(item.getDataset().getReferenceDate()))
                        .build())
                .collect(toSet());
    }
}
