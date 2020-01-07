package com.lombardrisk.ignis.functional.test.assertions;

import com.lombardrisk.ignis.client.external.rule.ValidationResultsDetailView;
import com.lombardrisk.ignis.functional.test.assertions.expected.ExpectedResultsDetails;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.groups.Tuple;

import java.util.List;
import java.util.Map;

import static org.assertj.core.groups.Tuple.tuple;

public final class ValidationResultsDetailAssertions {

    private final ValidationResultsDetailView resultsDetails;
    private final SoftAssertions soft = new SoftAssertions();

    private ValidationResultsDetailAssertions(final ValidationResultsDetailView resultsDetails) {
        this.resultsDetails = resultsDetails;
    }

    public static ValidationResultsDetailAssertions resultsDetail(final ValidationResultsDetailView resultsDetail) {
        return new ValidationResultsDetailAssertions(resultsDetail);
    }

    public void hasResultsDetails(final ExpectedResultsDetails expectedResultsDetails) {
        hasSchema(expectedResultsDetails.getSchema());
        hasData(expectedResultsDetails.getData().getData());

        soft.assertAll();
    }

    private void hasSchema(final Map<String, Class<?>> schema) {
        Tuple[] schemaTuples = schema.entrySet().stream()
                .map(entry -> tuple(entry.getKey(), entry.getValue()))
                .toArray(Tuple[]::new);

        soft.assertThat(resultsDetails.getSchema())
                .describedAs("Validation results details schema with names and type")
                .extracting(field -> tuple(field.getName(), field.getClass()))
                .containsOnly(schemaTuples);
    }

    private void hasData(final List<Map<String, Object>> data) {
        soft.assertThat(resultsDetails.getData())
                .describedAs("Validation results details data")
                .containsExactlyElementsOf(data);
    }
}
