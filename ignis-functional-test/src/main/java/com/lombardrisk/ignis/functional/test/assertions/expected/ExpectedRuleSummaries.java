package com.lombardrisk.ignis.functional.test.assertions.expected;

import com.lombardrisk.ignis.functional.test.assertions.ValidationRuleSummaryViewsAssertions;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Builder(builderMethodName = "expected")
@Getter
@ToString
public class ExpectedRuleSummaries {
    private long totalNumberOfRecords;
    private List<ValidationRuleSummaryViewsAssertions.ExpectedValidationRuleSummaryView> ruleSummaries;
}
