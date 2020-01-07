package com.lombardrisk.ignis.client.external.productconfig.view;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.lombardrisk.ignis.client.external.pipeline.view.PipelineView;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@AllArgsConstructor
@Builder
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductConfigView {

    private final Long id;
    private final String name;
    private final String version;
    private final String importStatus;
    private List<SchemaView> schemas;
    private List<PipelineView> pipelines;
}
