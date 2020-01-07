package com.lombardrisk.ignis.design.server.productconfig.export;

import com.lombardrisk.ignis.client.design.productconfig.NewProductConfigRequest;
import com.lombardrisk.ignis.client.external.pipeline.export.PipelineExport;
import com.lombardrisk.ignis.client.external.productconfig.export.SchemaExport;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class ImportProductRequest {

    private final NewProductConfigRequest newProductConfigRequest;
    private final List<SchemaExport> schemaExports;
    private final List<PipelineExport> pipelineExports;
}
