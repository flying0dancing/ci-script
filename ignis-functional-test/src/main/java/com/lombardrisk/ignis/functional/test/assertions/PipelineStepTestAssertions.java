package com.lombardrisk.ignis.functional.test.assertions;

import com.lombardrisk.ignis.client.design.pipeline.test.view.StepRowOutputDataView;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.lombardrisk.ignis.functional.test.assertions.PipelineStepTestAssertions.TestColumn.column;
import static org.junit.Assert.fail;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PipelineStepTestAssertions {

    private static final String MESSAGE_COULD_NOT_FIND_ROW = "Could not find row ";
    private final List<StepRowOutputDataView> testOutputRows;

    public static PipelineStepTestAssertions assertThat(final List<StepRowOutputDataView> outputRows) {
        return new PipelineStepTestAssertions(outputRows);
    }

    public PipelineStepTestAssertions hasNotFoundRow(final List<TestColumn> row) {
        List<List<TestColumn>> notFoundRows = convertToTestColumns("NOT_FOUND");

        Optional<List<TestColumn>> rowOptional = notFoundRows.stream()
                .filter(testColumns -> testColumns.containsAll(row))
                .findAny();

        if (!rowOptional.isPresent()) {
            fail(MESSAGE_COULD_NOT_FIND_ROW + row + " in " + notFoundRows);
        }
        return this;
    }

    public PipelineStepTestAssertions hasUnexpectedRow(final List<TestColumn> row) {
        List<List<TestColumn>> unexpectedRows = convertToTestColumns("UNEXPECTED");

        Optional<List<TestColumn>> rowOptional = unexpectedRows.stream()
                .filter(testColumns -> testColumns.containsAll(row))
                .findAny();

        if (!rowOptional.isPresent()) {
            fail(MESSAGE_COULD_NOT_FIND_ROW + row + " in " + unexpectedRows);
        }
        return this;
    }

    public PipelineStepTestAssertions hasMatchedRow(final List<TestColumn> row) {
        List<List<TestColumn>> matchedRows = convertToTestColumns("MATCHED");

        Optional<List<TestColumn>> rowOptional = matchedRows.stream()
                .filter(testColumns -> testColumns.containsAll(row))
                .findAny();

        if (!rowOptional.isPresent()) {
            fail(MESSAGE_COULD_NOT_FIND_ROW + row + " in " + matchedRows);
        }
        return this;
    }

    public PipelineStepTestAssertions hasNoMatches() {

        List<StepRowOutputDataView> matched = testOutputRows.stream()
                .filter(outputRow -> outputRow.getStatus().equals("MATCHED"))
                .collect(Collectors.toList());

        if (!matched.isEmpty()) {
            fail("Expecting output rows to have no matches but had the following " + matched);
        }

        return this;
    }

    public PipelineStepTestAssertions hasNoUnexpected() {

        List<StepRowOutputDataView> matched = testOutputRows.stream()
                .filter(outputRow -> outputRow.getStatus().equals("UNEXPECTED"))
                .collect(Collectors.toList());

        if (!matched.isEmpty()) {
            fail("Expecting output rows to have no matches but had the following " + matched);
        }

        return this;
    }

    public PipelineStepTestAssertions hasNoNotFound() {

        List<StepRowOutputDataView> matched = testOutputRows.stream()
                .filter(outputRow -> outputRow.getStatus().equals("NOT_FOUND"))
                .collect(Collectors.toList());

        if (!matched.isEmpty()) {
            fail("Expecting output rows to have no not founds but had the following " + matched);
        }

        return this;
    }

    private List<List<TestColumn>> convertToTestColumns(final String status) {

        return testOutputRows.stream()
                .filter(outputRow -> outputRow.getStatus().equals(status))
                .map(outputRow ->
                        outputRow.getCells().values().stream()
                                .map(cell -> column(cell.getFieldId(), cell.getData()))
                                .collect(Collectors.toList()))
                .collect(Collectors.toList());
    }

    @ToString
    @EqualsAndHashCode
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class TestColumn {

        private final Long fieldId;
        private final String value;

        public static TestColumn column(final Long fieldId, final String value) {
            return new TestColumn(fieldId, value);
        }
    }
}
