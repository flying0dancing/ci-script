package com.lombardrisk.ignis.design.server.configuration.adapters.pipeline;

import com.lombardrisk.ignis.client.design.pipeline.test.view.StepTestView;
import com.lombardrisk.ignis.design.server.jpa.pipeline.test.PipelineStepTestJpaRepository;
import com.lombardrisk.ignis.design.server.pipeline.api.PipelineStepTestRepository;
import com.lombardrisk.ignis.design.server.pipeline.test.model.PipelineStepTest;
import io.vavr.control.Option;

import java.util.List;

public class PipelineStepTestAdapter implements PipelineStepTestRepository {

    private final PipelineStepTestJpaRepository pipelineStepTestJpaRepository;

    public PipelineStepTestAdapter(final PipelineStepTestJpaRepository pipelineStepTestJpaRepository) {
        this.pipelineStepTestJpaRepository = pipelineStepTestJpaRepository;
    }

    @Override
    public PipelineStepTest save(final PipelineStepTest pipelineStepTest) {
        return pipelineStepTestJpaRepository.save(pipelineStepTest);
    }

    @Override
    public List<PipelineStepTest> findAllByPipelineStepId(final Long pipelineStepId) {
        return pipelineStepTestJpaRepository.findAllByPipelineStepId(pipelineStepId);
    }

    @Override
    public List<StepTestView> findViewsByPipelineStepId(final Long pipelineStepId) {
        return pipelineStepTestJpaRepository.findStepTestViewsByPipelineStepId(pipelineStepId);
    }

    @Override
    public List<PipelineStepTest> findAllByPipelineId(final long pipelineId) {
        return pipelineStepTestJpaRepository.findAllByPipelineId(pipelineId);
    }

    @Override
    public List<StepTestView> findViewsByPipelineId(final long pipelineId) {
        return pipelineStepTestJpaRepository.findStepTestViewsByPipelineId(pipelineId);
    }

    @Override
    public Option<PipelineStepTest> findById(final long id) {
        return Option.ofOptional(pipelineStepTestJpaRepository.findById(id));
    }

    @Override
    public Option<StepTestView> findViewById(final long id) {
        return Option.ofOptional(pipelineStepTestJpaRepository.findStepTestViewById(id));
    }

    @Override
    public void saveAll(final List<PipelineStepTest> stepTests) {
        pipelineStepTestJpaRepository.saveAll(stepTests);
    }

    @Override
    public void delete(final PipelineStepTest pipelineStepTest) {
        pipelineStepTestJpaRepository.delete(pipelineStepTest);
    }
}
