package com.lombardrisk.ignis.spark.validation;

import com.lombardrisk.ignis.api.rule.SummaryStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ValidationRuleStatistics {

    private final long numberOfFailures;
    private final long numberOfErrors;

    public SummaryStatus toStatus() {
        if (numberOfErrors > 0) {
            return SummaryStatus.ERROR;
        }

        if( numberOfFailures > 0) {
            return SummaryStatus.FAIL;
        }

        return SummaryStatus.SUCCESS;
    }
}
