package com.lombardrisk.ignis.client.internal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lombardrisk.ignis.api.rule.SummaryStatus;
import io.vavr.control.Option;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class RuleSummaryRequest {

    private long ruleId;
    private Long numberOfFailures;
    private Long numberOfErrors;

    @NonNull
    private SummaryStatus status;
    private String errorMessage;

    @JsonIgnore
    public Option<Long> numberOfErrors() {
        return Option.of(numberOfErrors);
    }

    @JsonIgnore
    public Option<Long> numberOfFailures() {
        return Option.of(numberOfFailures);
    }
}
