package com.lombardrisk.ignis.pipeline.step.common;

import com.lombardrisk.ignis.pipeline.step.api.DrillbackColumnLink;
import com.lombardrisk.ignis.pipeline.step.api.SelectColumn;
import io.vavr.control.Validation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.collect.Sets.newHashSet;
import static org.apache.commons.lang3.StringUtils.EMPTY;

@Builder
@AllArgsConstructor
public class AggregateTransformation implements Transformation {

    private final String datasetName;
    private final String outputSchema;
    private final Set<SelectColumn> selects;
    private final Set<String> groupings;
    private final Set<String> filters;

    @Override
    public String toSparkSql() {
        Validation<List<String>, String> sparkSql = selectStrings(selects).map(this::toQueryString);

        if (sparkSql.isInvalid()) {
            throw new IllegalArgumentException(String.join(", ", sparkSql.getError()));
        }

        return sparkSql.get();
    }

    private String toQueryString(final List<String> selects) {
        String baseQuery = "SELECT " + String.join(", ", selects) + drillbackColumns() + " FROM " + datasetName;
        return baseQuery + filters() + groupings();
    }

    private String filters() {
        if (CollectionUtils.isEmpty(filters)) {
            return EMPTY;
        }

        Set<String> filtersCopy = newHashSet(filters);
        String firstFilter = filtersCopy.iterator().next();
        filtersCopy.remove(firstFilter);

        return " WHERE "
                + firstFilter
                + filtersCopy.stream().map(filter -> " AND " + filter).collect(Collectors.joining(" "));
    }

    private String groupings() {
        return CollectionUtils.isNotEmpty(groupings)
                ? " GROUP BY " + String.join(", ", groupings)
                : EMPTY;
    }

    private String drillbackColumns() {
        if (groupings.isEmpty()) {
            return "";
        }

        String groupingDrillBackSql = groupings.stream()
                .map(this::toDrillBackSqlColumn)
                .collect(Collectors.joining(", "));

        return ", " + groupingDrillBackSql;
    }

    private String toDrillBackSqlColumn(final String grouping) {
        DrillbackColumnLink drillbackColumnLink = DrillbackColumnLink.builder()
                .inputColumn(grouping)
                .outputSchema(outputSchema)
                .inputSchema(datasetName)
                .build();

        return "FIRST(" + grouping + ") AS " + drillbackColumnLink.toDrillbackColumn();
    }
}
