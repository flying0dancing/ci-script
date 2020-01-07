package com.lombardrisk.ignis.design.server.jpa.pipeline.test;

import com.lombardrisk.ignis.design.server.pipeline.test.model.PipelineStepTestCell;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PipelineStepTestCellJpaRepository extends JpaRepository<PipelineStepTestCell, Long> {

    void deleteByFieldId(Long fieldId);
}
