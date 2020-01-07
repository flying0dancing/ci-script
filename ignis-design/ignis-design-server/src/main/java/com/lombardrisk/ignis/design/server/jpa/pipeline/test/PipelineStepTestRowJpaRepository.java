package com.lombardrisk.ignis.design.server.jpa.pipeline.test;

import com.lombardrisk.ignis.design.server.pipeline.test.model.PipelineStepTestRow;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PipelineStepTestRowJpaRepository extends JpaRepository<PipelineStepTestRow, Long> {

    void deleteAllByIdIn(Iterable<Long> ids);
}
