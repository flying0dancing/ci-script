package com.lombardrisk.ignis.design.server.jpa.pipeline.test;

import com.lombardrisk.ignis.design.server.pipeline.test.model.ExpectedDataRow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.List;

public interface ExpectedDataRowJpaRepository extends JpaRepository<ExpectedDataRow, Long> {

    @Transactional
    @Modifying
    @Query("update ExpectedDataRow edr set edr.status = :status, edr.run = :hasRun where edr.id in :ids")
    int updateExpectedRowsStatusAndRun(
            @Param("ids") List<Long> ids,
            @Param("status") ExpectedDataRow.Status status,
            @Param("hasRun") boolean run);

    List<ExpectedDataRow> findAllByPipelineStepTestIdOrderById(Long testId);
}
