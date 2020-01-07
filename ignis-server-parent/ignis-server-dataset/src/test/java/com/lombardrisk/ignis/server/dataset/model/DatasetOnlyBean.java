package com.lombardrisk.ignis.server.dataset.model;

import com.lombardrisk.ignis.api.dataset.ValidationStatus;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;
import java.util.Date;
import java.util.Set;

@Builder
@Value
public final class DatasetOnlyBean implements DatasetOnly {

    private Long id;
    private String name;
    private Long rowKeySeed;
    private String predicate;
    private Long recordsCount;
    private String schemaDisplayName;
    private Long schemaId;
    private Long validationJobId;
    private Long pipelineJobId;
    private Long pipelineInvocationId;
    private Long pipelineStepInvocationId;
    private Date createdTime;
    private Date lastUpdated;
    private String entityCode;
    private LocalDate referenceDate;
    private String validationStatus;
    private boolean rulesDefined;
    private Long runKey;
}
