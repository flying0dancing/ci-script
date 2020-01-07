package com.lombardrisk.ignis.design.server.jpa.pipeline.test;

import com.lombardrisk.ignis.design.server.pipeline.test.model.ActualDataRow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActualDataRowJpaRepository extends JpaRepository<ActualDataRow, Long> {

    void deleteAllByPipelineStepTestId(Long testId);

    List<ActualDataRow> findAllByPipelineStepTestIdOrderById(Long testId);
}
