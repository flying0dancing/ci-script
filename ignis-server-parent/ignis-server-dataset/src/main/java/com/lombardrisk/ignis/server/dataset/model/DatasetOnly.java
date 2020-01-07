package com.lombardrisk.ignis.server.dataset.model;

import java.time.LocalDate;
import java.util.Date;

public interface DatasetOnly {

    Long getId();

    String getName();

    Long getRowKeySeed();

    String getPredicate();

    Long getRecordsCount();

    String getSchemaDisplayName();

    Long getSchemaId();

    Long getValidationJobId();

    Long getPipelineJobId();

    Long getPipelineInvocationId();

    Long getPipelineStepInvocationId();

    Date getCreatedTime();

    Date getLastUpdated();

    String getEntityCode();

    LocalDate getReferenceDate();

    String getValidationStatus();

    boolean isRulesDefined();

    Long getRunKey();

    default boolean hasRules() {
        return isRulesDefined();
    }
}
