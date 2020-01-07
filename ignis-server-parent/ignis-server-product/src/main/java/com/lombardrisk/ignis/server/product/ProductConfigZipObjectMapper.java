package com.lombardrisk.ignis.server.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lombardrisk.ignis.client.external.pipeline.export.PipelineExport;
import com.lombardrisk.ignis.client.external.util.ProductConfigMixin;
import com.lombardrisk.ignis.client.external.productconfig.export.ProductConfigExport;
import com.lombardrisk.ignis.client.external.productconfig.export.SchemaExport;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ProductConfigZipObjectMapper {

    private static final ObjectMapper ZIP_OBJECT_MAPPER = ProductConfigMixin.initializeObjectMapper();

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
