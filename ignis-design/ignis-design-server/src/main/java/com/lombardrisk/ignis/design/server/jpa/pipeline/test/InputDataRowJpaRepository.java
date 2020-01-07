package com.lombardrisk.ignis.design.server.jpa.pipeline.test;

import com.lombardrisk.ignis.design.server.pipeline.test.model.InputDataRow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.List;

public interface InputDataRowJpaRepository extends JpaRepository<InputDataRow, Long> {


    @Transactional
    @Modifying
    @Query("update InputDataRow idr set idr.run = :run where idr.id in :ids")
    int updateRun(@Param("ids") List<Long> ids, @Param("run") boolean run);

    List<InputDataRow> findAllByPipelineStepTestIdOrderById(Long testId);
}
