package com.lombardrisk.ignis.client.core.page.request;

import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Builder
@Data
public class PageRequest {

    public static final String PAGE_PARAM = "page";
    public static final String SIZE_PARAM = "size";

    private final int page;
    private final int size;
    private final Sort sort;

    public Map<String, Object> toParameterMap() {
        Map<String, Object> parameterMap = new HashMap<>();
        parameterMap.put(PAGE_PARAM, page);
        parameterMap.put(SIZE_PARAM, size);

        if (sort != null) {
            parameterMap.putAll(sort.toParameterMap());
        }
        return parameterMap;
    }
}
