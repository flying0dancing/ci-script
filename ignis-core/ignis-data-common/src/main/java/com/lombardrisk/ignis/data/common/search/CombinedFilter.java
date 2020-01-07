package com.lombardrisk.ignis.data.common.search;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableList;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CombinedFilter implements FilterExpression {
    private BooleanOperator operator;
    private List<FilterExpression> filters;

    @Override
    @JsonIgnore
    public ExpressionType getExpressionType() {
        return ExpressionType.COMBINED;
    }

    public static CombinedFilter and(final List<? extends FilterExpression> filters) {
        return new CombinedFilter(BooleanOperator.AND, ImmutableList.copyOf(filters));
    }

    public static CombinedFilter and(final FilterExpression... filters) {
        return new CombinedFilter(BooleanOperator.AND, ImmutableList.copyOf(filters));
    }

    public static CombinedFilter or(final FilterExpression... filters) {
        return new CombinedFilter(BooleanOperator.OR, ImmutableList.copyOf(filters));
    }
}
