package com.lombardrisk.ignis.server.dataset.result;

import com.lombardrisk.ignis.server.dataset.model.Dataset;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
public class DatasetRowData {

    private final Dataset dataset;
    private final Page<Map<String, Object>> resultData;
}
