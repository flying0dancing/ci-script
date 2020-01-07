package com.lombardrisk.ignis.design.server.pipeline.api;

import com.lombardrisk.ignis.design.server.pipeline.model.Pipeline;

import java.util.List;
import java.util.Optional;

public interface PipelineRepository {

    Optional<Pipeline> findByName(String name);

    List<Pipeline> findAll();

    Optional<Pipeline> findById(long id);

    List<Pipeline> findAllById(Iterable<Long> ids);

    void delete(Pipeline pipeline);

    Pipeline save(Pipeline pipeline);

    List<Pipeline> findAllByProductId(Long productId);

    void deleteAllByProductId(Long productId);
}
