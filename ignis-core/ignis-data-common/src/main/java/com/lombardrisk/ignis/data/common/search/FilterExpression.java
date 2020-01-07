package com.lombardrisk.ignis.data.common.search;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "expressionType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Filter.class, name = "simple"),
        @JsonSubTypes.Type(value = CombinedFilter.class, name = "combined")
})
public interface FilterExpression {

    ExpressionType getExpressionType();

    enum ExpressionType {
        SIMPLE,
        COMBINED
    }
}
