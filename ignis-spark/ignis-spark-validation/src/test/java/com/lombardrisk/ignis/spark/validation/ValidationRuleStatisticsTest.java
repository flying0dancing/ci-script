package com.lombardrisk.ignis.spark.validation;

import com.lombardrisk.ignis.api.rule.SummaryStatus;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ValidationRuleStatisticsTest {

    @Test
    public void toStatus_ManyErrors_ReturnsError() {
        ValidationRuleStatistics ruleStatistics = ValidationRuleStatistics.builder()
                .numberOfErrors(100)
                .numberOfFailures(1)
                .build();

        assertThat(ruleStatistics.toStatus())
                .isEqualTo(SummaryStatus.ERROR);
    }

    @Test
    public void toStatus_FailuresNoErrors_ReturnsFailures() {
        ValidationRuleStatistics ruleStatistics = ValidationRuleStatistics.builder()
                .numberOfFailures(1)
                .build();

        assertThat(ruleStatistics.toStatus())
                .isEqualTo(SummaryStatus.FAIL);
    }

    @Test
    public void toStatus_NoFailuresOrErrors_ReturnsSuccess() {
        ValidationRuleStatistics ruleStatistics = ValidationRuleStatistics.builder()
                .build();

        assertThat(ruleStatistics.toStatus())
                .isEqualTo(SummaryStatus.SUCCESS);
    }
}