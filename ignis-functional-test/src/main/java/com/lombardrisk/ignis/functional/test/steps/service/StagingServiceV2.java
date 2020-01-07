package com.lombardrisk.ignis.functional.test.steps.service;

import com.lombardrisk.ignis.client.external.dataset.DatasetQueryParams;
import com.lombardrisk.ignis.client.external.job.JobsClient;
import com.lombardrisk.ignis.client.external.job.staging.request.v2.StagingRequestV2;

import java.util.Set;

import static com.lombardrisk.ignis.functional.test.steps.CallAssertion.callAndExpectSuccess;
import static com.lombardrisk.ignis.functional.test.utils.ReferenceDateParser.convertToLocalDate;
import static java.util.stream.Collectors.toSet;

public class StagingServiceV2 implements StagingService<StagingRequestV2> {
    private final JobsClient jobsClient;

    public StagingServiceV2(final JobsClient jobsClient) {
        this.jobsClient = jobsClient;
    }

    @Override
    public Long startJob(final StagingRequestV2 stagingRequest) {
        return callAndExpectSuccess(jobsClient.startJob(stagingRequest)).getId();
    }

    @Override
    public Set<DatasetQueryParams> toDatasetQueryParams(final StagingRequestV2 stagingRequest) {
        return stagingRequest.getItems().stream()
                .map(item -> DatasetQueryParams.builder()
                        .schema(item.getSchema())
                        .entityCode(stagingRequest.getMetadata().getEntityCode())
                        .referenceDate(convertToLocalDate(stagingRequest.getMetadata().getReferenceDate()))
                        .build())
                .collect(toSet());
    }
}
