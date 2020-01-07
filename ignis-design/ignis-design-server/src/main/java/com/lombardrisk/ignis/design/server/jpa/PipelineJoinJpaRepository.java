package com.lombardrisk.ignis.design.server.jpa;

import com.lombardrisk.ignis.design.server.pipeline.model.join.Join;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PipelineJoinJpaRepository extends JpaRepository<Join, Long> {

    @Query("select j from Join j "
            + "where j in (select j from Join j join j.joinFields jf where jf.leftJoinFieldId = :fieldId)")
    List<Join> findAllByLeftJoinFieldId(@Param("fieldId") long fieldId);

    @Query("select j from Join j "
            + "where j in (select j from Join j join j.joinFields jf where jf.rightJoinFieldId = :fieldId)")
    List<Join> findAllByRightJoinFieldId(@Param("fieldId") long fieldId);

}
