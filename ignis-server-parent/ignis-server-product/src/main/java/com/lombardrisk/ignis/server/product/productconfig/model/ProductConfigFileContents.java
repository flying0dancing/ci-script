package com.lombardrisk.ignis.server.product.productconfig.model;

import com.lombardrisk.ignis.client.external.pipeline.export.PipelineExport;
import com.lombardrisk.ignis.client.external.productconfig.export.SchemaExport;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class ProductConfigFileContents {

    private final ProductManifest productMetadata;
    private final List<SchemaExport> schemas;
    private final List<PipelineExport> pipelineExports;
}
