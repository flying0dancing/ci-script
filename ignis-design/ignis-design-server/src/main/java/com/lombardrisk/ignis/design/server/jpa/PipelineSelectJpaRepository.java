package com.lombardrisk.ignis.design.server.jpa;

import com.lombardrisk.ignis.design.server.pipeline.model.select.Select;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PipelineSelectJpaRepository extends JpaRepository<Select, Long> {
    long deleteAllByOutputFieldId(long fieldId);
}
