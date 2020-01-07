package com.lombardrisk.ignis.data.common.search;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum FilterType {
    @JsonProperty("text")
    TEXT,
    @JsonProperty("date")
    DATE,
    @JsonProperty("number")
    NUMBER
}
