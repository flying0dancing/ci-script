package com.lombardrisk.ignis.design.server.jpa.pipeline.test;

import com.lombardrisk.ignis.client.design.pipeline.test.view.StepTestView;
import com.lombardrisk.ignis.design.server.pipeline.test.model.PipelineStepTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PipelineStepTestJpaRepository extends JpaRepository<PipelineStepTest, Long> {

    String GET_STEP_TEST_VIEW_QUERY = "select "
            + "new com.lombardrisk.ignis.client.design.pipeline.test.view.StepTestView("
            + "test.id,"
            + "test.name,"
            + "test.description,"
            + "test.testReferenceDate,"
            + "pipeline.id,"
            + "test.pipelineStepId,"
            + "test.pipelineStepStatus) "
            + "from PipelineStepTest test "
            + "join test.pipelineStep step "
            + "join Pipeline pipeline on step.pipelineId = pipeline.id";

    @Query("select st from PipelineStepTest st where st.pipelineStepId in (select ps.id from PipelineStep ps where ps.pipelineId = :pipelineId)")
    List<PipelineStepTest> findAllByPipelineId(@Param("pipelineId") long pipelineId);

    List<PipelineStepTest> findAllByPipelineStepId(Long pipelineStepId);

    @Query(GET_STEP_TEST_VIEW_QUERY + " where test.id = :id")
    Optional<StepTestView> findStepTestViewById(@Param("id") long id);

    @Query(GET_STEP_TEST_VIEW_QUERY + " where test.pipelineStepId = :pipelineStepId")
    List<StepTestView> findStepTestViewsByPipelineStepId(@Param("pipelineStepId") Long pipelineStepId);

    @Query(GET_STEP_TEST_VIEW_QUERY + " where pipeline.id = :pipelineId")
    List<StepTestView> findStepTestViewsByPipelineId(@Param("pipelineId") Long pipelineId);
}
