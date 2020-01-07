package com.lombardrisk.ignis.client.external.rule;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.lombardrisk.ignis.client.core.page.response.Page;
import com.lombardrisk.ignis.client.external.productconfig.export.FieldExport;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ValidationResultsDetailView {

    private List<FieldExport> schema;
    private List<Map<String, Object>> data;
    private Page page;
}
