package com.lombardrisk.ignis.client.internal;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor(onConstructor = @__({ @JsonCreator }))
public class UpdateDatasetRunCall {

    private final Long stagingDatasetId;
    private final Long stagingJobId;
    private final Long recordsCount;
}
