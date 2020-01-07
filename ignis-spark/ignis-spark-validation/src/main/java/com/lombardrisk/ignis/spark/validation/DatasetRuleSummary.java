package com.lombardrisk.ignis.spark.validation;

import com.lombardrisk.ignis.api.rule.SummaryStatus;
import io.vavr.control.Option;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Builder
@EqualsAndHashCode
@ToString
public class DatasetRuleSummary {

    @Getter
    private final long ruleId;
    @Getter
    @NonNull
    private final SummaryStatus status;

    private String failureMessage;

    private ValidationRuleStatistics validationRuleStatistics;


    public Option<String> getFailureMessage() {
        return Option.of(failureMessage);
    }

    public Option<ValidationRuleStatistics> getStatistics() {
        return Option.of(validationRuleStatistics);
    }
}
