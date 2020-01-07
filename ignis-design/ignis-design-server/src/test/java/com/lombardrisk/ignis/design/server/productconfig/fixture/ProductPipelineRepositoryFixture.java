package com.lombardrisk.ignis.design.server.productconfig.fixture;

import com.lombardrisk.ignis.design.server.pipeline.PipelineService;
import com.lombardrisk.ignis.design.server.pipeline.model.Pipeline;
import com.lombardrisk.ignis.design.server.productconfig.api.ProductPipelineRepository;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class ProductPipelineRepositoryFixture implements ProductPipelineRepository {

    private final PipelineService pipelineService;

    @Override
    public List<Pipeline> findAllByProductId(final Long productId) {
        return pipelineService.findByProductId(productId);
    }

    @Override
    public void deleteAllByProductId(final Long productId) {
        pipelineService.deleteAllByProductId(productId);
    }
}
