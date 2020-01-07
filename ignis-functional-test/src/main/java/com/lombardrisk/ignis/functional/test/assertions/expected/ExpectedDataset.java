package com.lombardrisk.ignis.functional.test.assertions.expected;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.assertj.core.groups.Tuple;

import java.time.LocalDate;

import static org.assertj.core.groups.Tuple.tuple;

@Builder(builderMethodName = "expected")
@Getter
@ToString
@EqualsAndHashCode
public class ExpectedDataset {

    private final String schema;
    private final String entityCode;
    private final LocalDate referenceDate;
    private final long numberOfRows;

    public ExpectedDatasetBuilder copy() {
        return ExpectedDataset.expected()
                .schema(schema)
                .entityCode(entityCode)
                .referenceDate(referenceDate)
                .numberOfRows(numberOfRows);
    }

    public Tuple toTuple() {
        return tuple(schema, entityCode, referenceDate, numberOfRows);
    }
}
