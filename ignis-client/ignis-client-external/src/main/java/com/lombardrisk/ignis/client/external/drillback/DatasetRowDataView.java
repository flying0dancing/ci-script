package com.lombardrisk.ignis.client.external.drillback;

import com.lombardrisk.ignis.client.core.page.response.Page;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@Builder
public class DatasetRowDataView {

    private final Long datasetId;
    private final List<Map<String, Object>> data;
    private final Page page;
}
