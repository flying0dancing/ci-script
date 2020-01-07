package com.lombardrisk.ignis.design.server.configuration.adapters.pipeline;

import com.lombardrisk.ignis.design.server.jpa.pipeline.PipelineStepJpaRepository;
import com.lombardrisk.ignis.design.server.pipeline.api.PipelineStepRepository;
import com.lombardrisk.ignis.design.server.pipeline.model.PipelineStep;
import io.vavr.control.Option;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public class PipelineStepAdapter implements PipelineStepRepository {

    private final PipelineStepJpaRepository pipelineStepRepository;

    public PipelineStepAdapter(final PipelineStepJpaRepository pipelineStepRepository) {
        this.pipelineStepRepository = pipelineStepRepository;
    }

    @Override
    public List<PipelineStep> findAll() {
        return pipelineStepRepository.findAll();
    }

    @Override
    public PipelineStep savePipelineStep(final PipelineStep step) {
        return pipelineStepRepository.save(step);
    }

    @Override
    public Option<PipelineStep> findById(final long id) {
        return Option.ofOptional(pipelineStepRepository.findById(id));
    }

    @Override
    public List<PipelineStep> findAllById(final Iterable<Long> ids) {
        return pipelineStepRepository.findAllById(ids);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void delete(final PipelineStep schema) {
        pipelineStepRepository.delete(schema);
    }
}
