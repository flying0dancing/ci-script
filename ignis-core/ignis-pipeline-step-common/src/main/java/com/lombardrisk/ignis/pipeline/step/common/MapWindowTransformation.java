package com.lombardrisk.ignis.pipeline.step.common;

import com.lombardrisk.ignis.pipeline.step.api.OrderSpec;
import com.lombardrisk.ignis.pipeline.step.api.SelectColumn;
import com.lombardrisk.ignis.pipeline.step.api.WindowSpec;
import io.vavr.control.Option;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.Collection;

import static com.google.common.collect.Iterables.isEmpty;
import static com.lombardrisk.ignis.common.stream.CollectionUtils.orEmpty;
import static java.util.stream.Collectors.joining;

@Builder
@AllArgsConstructor
public class MapWindowTransformation implements Transformation {

    private final String datasetName;
    private final Collection<SelectColumn> selects;

    @Override
    public String toSparkSql() {
        String selectString = selects.stream().map(this::toSelectString)
                .collect(joining(", "));

        return "SELECT " + selectString + " FROM " + datasetName;
    }

    private String toSelectString(final SelectColumn select) {
        if (select.getOver() != null) {
            return select.getSelect() + " " + windowSpecSql(select) + " AS " + select.getAs();
        }

        return select.getSelect() + " AS " + select.getAs();
    }

    private String windowSpecSql(final SelectColumn selectColumn) {
        Option<String> partitionByColumns = partitionByColumns(selectColumn.getOver());
        Option<String> orderByColumns = orderByColumns(selectColumn.getOver());

        if (partitionByColumns.isEmpty() && orderByColumns.isEmpty()) {
            return "OVER ()";
        }

        if (partitionByColumns.isEmpty()) {
            return String.format("OVER (ORDER BY %s)", orderByColumns.get());
        }

        if (orderByColumns.isEmpty()) {
            return String.format("OVER (PARTITION BY %s)", partitionByColumns.get());
        }

        return String.format("OVER (PARTITION BY %s ORDER BY %s)",
                partitionByColumns.get(), orderByColumns.get());
    }

    private Option<String> partitionByColumns(final WindowSpec windowSpec) {
        if (isEmpty(windowSpec.getPartitionBy())) {
            return Option.none();
        }

        return Option.of(String.join(", ", windowSpec.getPartitionBy()));
    }

    private Option<String> orderByColumns(final WindowSpec windowSpec) {
        if (isEmpty(orEmpty(windowSpec.getOrderBy()))) {
            return Option.none();
        }

        String orderByColumns = windowSpec.getOrderBy().stream()
                .map(this::toOrderBySql)
                .collect(joining(", "));

        return Option.of(orderByColumns);
    }

    private String toOrderBySql(final OrderSpec orderSpec) {
        return String.format("%s %s", orderSpec.getColumn(), orderSpec.getDirection());
    }
}
