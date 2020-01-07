package com.lombardrisk.ignis.spark.pipeline.transformation;

import com.lombardrisk.ignis.pipeline.step.api.SelectColumn;
import com.lombardrisk.ignis.pipeline.step.common.AggregateTransformation;
import org.junit.Test;

import static com.google.common.collect.Sets.newLinkedHashSet;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class AggregateTransformationTest {

    @Test
    public void toSparkSql_TwoGroupings_AddsDrillBackColumns() {
        AggregateTransformation aggregateTransformation = AggregateTransformation.builder()
                .selects(newLinkedHashSet(asList(
                        SelectColumn.builder().select("FIRST(A)").build(),
                        SelectColumn.builder().select("FIRST(B)").build(),
                        SelectColumn.builder().select("SUM(C)").build(),
                        SelectColumn.builder().select("COUNT(D)").build())))
                .datasetName("INPUT")
                .groupings(newLinkedHashSet(asList("A", "B")))
                .outputSchema("AGGED")
                .build();

        assertThat(aggregateTransformation.toSparkSql())
                .isEqualTo(
                        "SELECT FIRST(A), FIRST(B), SUM(C), COUNT(D), FIRST(A) AS FCR_SYS__INPUT__A, FIRST(B) AS FCR_SYS__INPUT__B "
                                + "FROM INPUT GROUP BY A, B");
    }

    @Test
    public void toSparkSql_NoGroupings_DoesNotAddDrillBackColumns() {
        AggregateTransformation aggregateTransformation = AggregateTransformation.builder()
                .selects(newLinkedHashSet(asList(
                        SelectColumn.builder().select("FIRST(A)").build(),
                        SelectColumn.builder().select("FIRST(B)").build(),
                        SelectColumn.builder().select("SUM(C)").build(),
                        SelectColumn.builder().select("COUNT(D)").build())))
                .datasetName("INPUT")
                .groupings(newLinkedHashSet())
                .outputSchema("AGGED")
                .build();

        assertThat(aggregateTransformation.toSparkSql())
                .isEqualTo(
                        "SELECT FIRST(A), FIRST(B), SUM(C), COUNT(D) FROM INPUT");
    }
}
