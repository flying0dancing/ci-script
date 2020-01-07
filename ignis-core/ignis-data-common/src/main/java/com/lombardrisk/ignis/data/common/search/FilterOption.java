package com.lombardrisk.ignis.data.common.search;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum FilterOption {
    @JsonProperty("equals")
    EQUALS,
    @JsonProperty("notEqual")
    NOT_EQUAL,
    @JsonProperty("contains")
    CONTAINS,
    @JsonProperty("notContains")
    NOT_CONTAINS,
    @JsonProperty("startsWith")
    STARTS_WITH,
    @JsonProperty("endsWith")
    ENDS_WITH,
    @JsonProperty("lessThan")
    LESS_THAN,
    @JsonProperty("lessThanOrEqual")
    LESS_THAN_OR_EQUAL,
    @JsonProperty("greaterThan")
    GREATER_THAN,
    @JsonProperty("greaterThanOrEqual")
    GREATER_THAN_OR_EQUAL,
    @JsonProperty("inRange")
    IN_RANGE,
}
