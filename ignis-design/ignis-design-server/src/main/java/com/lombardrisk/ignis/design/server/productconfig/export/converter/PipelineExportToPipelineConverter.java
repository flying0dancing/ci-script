package com.lombardrisk.ignis.design.server.productconfig.export.converter;

import com.lombardrisk.ignis.client.design.pipeline.CreatePipelineRequest;
import com.lombardrisk.ignis.client.external.pipeline.export.PipelineExport;
import com.lombardrisk.ignis.design.server.productconfig.ProductConfig;

import java.util.function.BiFunction;

public class PipelineExportToPipelineConverter
        implements BiFunction<PipelineExport, ProductConfig, CreatePipelineRequest> {

    @Override
    public CreatePipelineRequest apply(final PipelineExport pipelineExport, final ProductConfig productConfig) {
        return CreatePipelineRequest.builder()
                .name(pipelineExport.getName())
                .productId(productConfig.getId())
                .build();
    }
}
