package com.lombardrisk.ignis.design.server.pipeline.api;

import com.lombardrisk.ignis.client.design.pipeline.test.view.StepTestView;
import com.lombardrisk.ignis.design.server.pipeline.test.model.PipelineStepTest;
import io.vavr.control.Option;

import java.util.List;

public interface PipelineStepTestRepository {

    PipelineStepTest save(PipelineStepTest pipelineStepTest);

    List<PipelineStepTest> findAllByPipelineStepId(Long pipelineStepId);

    List<StepTestView> findViewsByPipelineStepId(Long pipelineStepId);

    List<PipelineStepTest> findAllByPipelineId(long pipelineId);

    List<StepTestView> findViewsByPipelineId(long pipelineId);

    Option<PipelineStepTest> findById(long id);

    Option<StepTestView> findViewById(long id);

    void saveAll(List<PipelineStepTest> stepTests);

    void delete(PipelineStepTest pipelineStepTest);
}
