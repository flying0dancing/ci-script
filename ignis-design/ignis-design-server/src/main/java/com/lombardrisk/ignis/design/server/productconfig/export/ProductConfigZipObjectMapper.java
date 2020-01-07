package com.lombardrisk.ignis.design.server.productconfig.export;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lombardrisk.ignis.client.external.pipeline.export.PipelineExport;
import com.lombardrisk.ignis.client.external.productconfig.export.ProductConfigExport;
import com.lombardrisk.ignis.client.external.productconfig.export.SchemaExport;
import com.lombardrisk.ignis.client.external.util.ProductConfigMixin;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ProductConfigZipObjectMapper {

    private static final ObjectMapper ZIP_OBJECT_MAPPER = ProductConfigMixin.initializeObjectMapper();

    public byte[] writeProductConfigView(final ProductConfigExport productConfigView) throws IOException {
        return ZIP_OBJECT_MAPPER.writeValueAsBytes(productConfigView);
    }

    public byte[] writeTableExport(final SchemaExport schemaView) throws IOException {
        return ZIP_OBJECT_MAPPER.writeValueAsBytes(schemaView);
    }

    public byte[] writePipelineExport(final PipelineExport pipeline) throws IOException {
        return ZIP_OBJECT_MAPPER.writeValueAsBytes(pipeline);
    }

    public ProductConfigExport readProductConfigView(final byte[] productConfigJson) throws IOException {
        return ZIP_OBJECT_MAPPER.readValue(productConfigJson, ProductConfigExport.class);
    }

    public SchemaExport readTableView(final byte[] tableJson) throws IOException {
        return ZIP_OBJECT_MAPPER.readValue(tableJson, SchemaExport.class);
    }

    public PipelineExport readPipelineView(final byte[] pipelineJson) throws IOException {
        return ZIP_OBJECT_MAPPER.readValue(pipelineJson, PipelineExport.class);
    }
}
