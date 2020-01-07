package com.lombardrisk.ignis.client.external.rule;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.lombardrisk.ignis.client.external.productconfig.export.ValidationRuleExport;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ValidationRuleSummaryView {

    private final Long id;
    private final ValidationRuleExport validationRule;
    private final Long datasetId;
    private final Long totalRecords;
    private final Long numberOfFailures;
    private final Long numberOfErrors;
    private final SummaryStatus status;
    private final String errorMessage;

    public enum SummaryStatus {
        SUCCESS,
        FAIL,
        ERROR
    }

    @JsonCreator
    public ValidationRuleSummaryView(
            @JsonProperty("id") final Long id,
            @JsonProperty("validationRule") final ValidationRuleExport validationRule,
            @JsonProperty("datasetId") final Long datasetId,
            @JsonProperty("totalRecords") final Long totalRecords,
            @JsonProperty("numberOfFailures") final Long numberOfFailures,
            @JsonProperty("numberOfErrors") final Long numberOfErrors,
            @JsonProperty("status") final SummaryStatus status,
            @JsonProperty("errorMessage") final String errorMessage) {
        this.id = id;
        this.validationRule = validationRule;
        this.datasetId = datasetId;
        this.totalRecords = totalRecords;
        this.numberOfFailures = numberOfFailures;
        this.numberOfErrors = numberOfErrors;
        this.status = status;
        this.errorMessage = errorMessage;
    }
}
