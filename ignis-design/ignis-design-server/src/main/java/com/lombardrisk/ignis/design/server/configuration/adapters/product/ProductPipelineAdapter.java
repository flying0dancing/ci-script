package com.lombardrisk.ignis.design.server.configuration.adapters.product;

import com.lombardrisk.ignis.design.server.pipeline.PipelineService;
import com.lombardrisk.ignis.design.server.pipeline.model.Pipeline;
import com.lombardrisk.ignis.design.server.productconfig.api.ProductPipelineRepository;

import java.util.List;

public class ProductPipelineAdapter implements ProductPipelineRepository {

    private final PipelineService pipelineService;

    public ProductPipelineAdapter(final PipelineService pipelineService) {
        this.pipelineService = pipelineService;
    }

    @Override
    public List<Pipeline> findAllByProductId(final Long productId) {
        return pipelineService.findByProductId(productId);
    }

    @Override
    public void deleteAllByProductId(final Long productId) {
        pipelineService.deleteAllByProductId(productId);
    }
}
