package com.lombardrisk.ignis.data.common.search;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class Filter implements FilterExpression {
    private String columnName;
    private FilterOption type;
    private String filter;
    private String filterTo;
    private FilterType filterType;
    private String dateFrom;
    private String dateTo;
    private String dateFormat;

    @Override
    @JsonIgnore
    public ExpressionType getExpressionType() {
        return ExpressionType.SIMPLE;
    }
}
