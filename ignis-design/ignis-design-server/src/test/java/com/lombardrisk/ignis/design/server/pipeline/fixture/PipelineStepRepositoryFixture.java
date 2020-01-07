package com.lombardrisk.ignis.design.server.pipeline.fixture;

import com.lombardrisk.ignis.design.server.pipeline.api.PipelineStepRepository;
import com.lombardrisk.ignis.design.server.pipeline.model.PipelineStep;
import com.lombardrisk.ignis.data.common.fixtures.InMemoryRepository;
import io.vavr.control.Option;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class PipelineStepRepositoryFixture extends InMemoryRepository<PipelineStep> implements PipelineStepRepository {

    private final AtomicLong stepIdSequence = new AtomicLong(1);

    @Override
    public Option<PipelineStep> findById(final long id) {
        return Option.ofOptional(super.findById(id));
    }

    @Override
    public List<PipelineStep> findAllById(final Iterable<Long> ids) {
        return findAllByIds(ids);
    }

    @Override
    public void delete(final PipelineStep step) {
        deleteById(step.getId());
    }

    @Override
    public PipelineStep savePipelineStep(final PipelineStep step) {
        if (step.getId() == null) {
            step.setId(stepIdSequence.getAndIncrement());
        }

        return save(step);
    }
}
