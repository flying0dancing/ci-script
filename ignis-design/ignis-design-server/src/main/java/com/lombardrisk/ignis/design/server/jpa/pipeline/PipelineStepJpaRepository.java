package com.lombardrisk.ignis.design.server.jpa.pipeline;

import com.lombardrisk.ignis.design.server.pipeline.model.PipelineStep;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PipelineStepJpaRepository extends JpaRepository<PipelineStep, Long> {

}
