package com.lombardrisk.ignis.functional.test.assertions;

import com.lombardrisk.ignis.client.external.rule.ValidationRuleSummaryView;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.groups.Tuple;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.tuple;

public final class ValidationRuleSummaryViewsAssertions {

    private final List<ValidationRuleSummaryView> validationRuleSummaries;
    private final SoftAssertions soft = new SoftAssertions();

    private ValidationRuleSummaryViewsAssertions(final List<ValidationRuleSummaryView> validationRuleSummaries) {
        this.validationRuleSummaries = validationRuleSummaries;
    }

    public static ValidationRuleSummaryViewsAssertions assertRuleSummaries(
            final List<ValidationRuleSummaryView> validationRuleSummaries) {

        return new ValidationRuleSummaryViewsAssertions(validationRuleSummaries);
    }

    public static ExpectedValidationRuleSummaryView summary() {
        return new ExpectedValidationRuleSummaryView();
    }

    public ValidationRuleSummaryViewsAssertions hasDatasetId(final Long expectedDatasetId) {
        soft.assertThat(validationRuleSummaries)
                .extracting(ValidationRuleSummaryView::getDatasetId)
                .as("Expected all summaries to have the same dataset ID")
                .containsOnly(expectedDatasetId);

        return this;
    }

    public ValidationRuleSummaryViewsAssertions containsSummaries(
            final List<ExpectedValidationRuleSummaryView> expectedViews) {

        return containsSummaries(expectedViews.toArray(new ExpectedValidationRuleSummaryView[]{}));
    }

    public ValidationRuleSummaryViewsAssertions containsSummaries(
            final ExpectedValidationRuleSummaryView... expectedViews) {

        Tuple[] expectedViewTuples = Arrays.stream(expectedViews)
                .map(expected -> tuple(
                        expected.ruleId,
                        expected.numberOfFailures,
                        expected.numberOfErrors,
                        expected.status,
                        expected.errorMessage))
                .toArray(Tuple[]::new);

        soft.assertThat(validationRuleSummaries)
                .extracting(view -> tuple(
                        view.getValidationRule().getRuleId(),
                        view.getNumberOfFailures(),
                        view.getNumberOfErrors(),
                        view.getStatus(),
                        view.getErrorMessage()))
                .as("Expected all summaries to have Rule ID, Number of failures, Number of errors, Status, Error message")
                .containsExactlyInAnyOrder(expectedViewTuples);

        return this;
    }

    public ValidationRuleSummaryViewsAssertions hasTotalRecords(final long expectedTotalRecords) {
        soft.assertThat(validationRuleSummaries)
                .extracting(ValidationRuleSummaryView::getTotalRecords)
                .as("Expected all summaries to have the same number of total records")
                .containsOnly(expectedTotalRecords);

        return this;
    }

    public Boolean assertAll() {
        soft.assertAll();
        return soft.errorsCollected().isEmpty();
    }

    public static class ExpectedValidationRuleSummaryView {

        private String ruleId;
        private Long numberOfFailures;
        private Long numberOfErrors;
        private ValidationRuleSummaryView.SummaryStatus status;
        private String errorMessage;

        public ExpectedValidationRuleSummaryView ruleId(final String ruleId) {
            this.ruleId = ruleId;
            return this;
        }

        public ExpectedValidationRuleSummaryView numberOfFailures(final Long numberOfFailures) {
            this.numberOfFailures = numberOfFailures;
            return this;
        }

        public ExpectedValidationRuleSummaryView numberOfErrors(final Long numberOfErrors) {
            this.numberOfErrors = numberOfErrors;
            return this;
        }

        public ExpectedValidationRuleSummaryView status(final ValidationRuleSummaryView.SummaryStatus status) {
            this.status = status;
            return this;
        }

        public ExpectedValidationRuleSummaryView errorMessage(final String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }
    }
}
