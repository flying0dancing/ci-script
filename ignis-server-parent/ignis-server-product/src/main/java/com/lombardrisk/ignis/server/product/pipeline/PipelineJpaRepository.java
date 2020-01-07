package com.lombardrisk.ignis.server.product.pipeline;

import com.lombardrisk.ignis.server.product.pipeline.model.Pipeline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PipelineJpaRepository extends JpaRepository<Pipeline, Long> {

    void deleteAllByProductIdIn(final List<Long> productIds);

    boolean existsByName(final String name);

    List<Pipeline> findByProductId(long productId);

    Optional<Pipeline> findByName(String name);

    @Query("select p from Pipeline p left join fetch p.steps s left join fetch s.pipelineFilters where p.id = :id")
    Optional<Pipeline> findByIdFetchingFilters(@Param("id") long pipelineId);
}
