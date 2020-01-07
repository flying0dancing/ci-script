package com.lombardrisk.ignis.client.internal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.lombardrisk.ignis.common.json.LocalDateDeserializer;
import com.lombardrisk.ignis.common.json.LocalDateSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor(onConstructor = @__({ @JsonCreator }))
public class CreateDatasetCall {

    private final Long stagingDatasetId;
    private final Long stagingJobId;
    private final Long pipelineJobId;
    private final Long pipelineInvocationId;
    private final Long pipelineStepInvocationId;
    private final String entityCode;

    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private final LocalDate referenceDate;
    private final long schemaId;
    private final long rowKeySeed;
    private final String predicate;
    private final long recordsCount;
    private final boolean autoValidate;
}
