package com.lombardrisk.ignis.server.product.pipeline;

import com.lombardrisk.ignis.server.product.pipeline.model.PipelineStep;
import com.lombardrisk.ignis.server.product.pipeline.select.Select;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SelectJpaRepository extends JpaRepository<Select, Long> {

    List<Select> findAllByPipelineStepOrderByOrder(PipelineStep step);

    @Query("select s from Select s join fetch s.window where s.isWindow = true and s.pipelineStep = :pipelineStep")
    List<Select> findAllWithWindowByPipelineStep(@Param("pipelineStep") PipelineStep step);
}
