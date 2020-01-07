package com.lombardrisk.ignis.client.external.dataset;

import com.lombardrisk.ignis.client.core.page.request.PageRequest;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static com.lombardrisk.ignis.client.external.path.api.Params;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;

@Builder
@Data
public class DatasetQueryParams {

    private final String name;
    private final String schema;
    private final String entityCode;
    private final LocalDate referenceDate;
    private final PageRequest pageRequest;

    public Map<String, Object> toParameterMap() {
        Map<String, Object> parameterMap = new HashMap<>();

        if (name != null) {
            parameterMap.put(Params.DATASET_NAME, name);
        }

        if (schema != null) {
            parameterMap.put(Params.DATASET_SCHEMA, schema);
        }

        if (entityCode != null) {
            parameterMap.put(Params.ENTITY_CODE, entityCode);
        }

        if (entityCode != null) {
            parameterMap.put(Params.REFERENCE_DATE, ISO_LOCAL_DATE.format(referenceDate));
        }

        if (pageRequest != null) {
            parameterMap.putAll(pageRequest.toParameterMap());
        }

        return parameterMap;
    }
}
