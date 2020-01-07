package com.lombardrisk.ignis.design.server.jpa.pipeline;

import com.lombardrisk.ignis.design.server.pipeline.model.Pipeline;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PipelineJpaRepository extends JpaRepository<Pipeline, Long> {

    Optional<Pipeline> findByName(String name);

    List<Pipeline> findByProductId(Long productId);

    void deleteAllByProductId(Long productId);
}
