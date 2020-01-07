package com.lombardrisk.ignis.client.core.page.request;

import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Builder
@Data
public class Sort {

    private static final String SORT_REQUEST_PARAM = "sort";

    private final String field;
    private final Direction direction;

    public enum Direction {
        ASC("asc"),
        DESC("desc");

        private final String urlParamValue;

        Direction(final String urlParamValue) {
            this.urlParamValue = urlParamValue;
        }
    }

    public Map<String, Object> toParameterMap() {
        Map<String, Object> parameterMap = new HashMap<>();
        parameterMap.put(SORT_REQUEST_PARAM, field + "," + direction.urlParamValue);
        return parameterMap;
    }
}
