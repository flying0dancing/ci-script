package com.lombardrisk.ignis.design.server.productconfig.api;

import com.lombardrisk.ignis.design.server.pipeline.model.Pipeline;

import java.util.List;

public interface ProductPipelineRepository {

    List<Pipeline> findAllByProductId(Long productId);

    void deleteAllByProductId(Long productId);

}
