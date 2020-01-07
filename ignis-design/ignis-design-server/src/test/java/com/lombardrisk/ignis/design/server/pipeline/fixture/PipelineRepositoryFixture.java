package com.lombardrisk.ignis.design.server.pipeline.fixture;

import com.lombardrisk.ignis.design.server.pipeline.api.PipelineRepository;
import com.lombardrisk.ignis.design.server.pipeline.api.PipelineStepRepository;
import com.lombardrisk.ignis.design.server.pipeline.model.Pipeline;
import com.lombardrisk.ignis.design.server.pipeline.model.PipelineStep;
import com.lombardrisk.ignis.data.common.fixtures.InMemoryRepository;
import lombok.Getter;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class PipelineRepositoryFixture extends InMemoryRepository<Pipeline> implements PipelineRepository {

    private final AtomicLong pipelineIdSequence = new AtomicLong(1);
    @Getter
    private final AtomicLong stepIdSequence = new AtomicLong(1);

    private final PipelineStepRepository pipelineStepRepository;

    public PipelineRepositoryFixture(final PipelineStepRepository pipelineStepRepository) {
        this.pipelineStepRepository = pipelineStepRepository;
    }

    @Override
    public List<Pipeline> findAll() {
        return super.findAll().stream()
                .map(this::addPipelineSteps)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Pipeline> findByName(final String name) {
        return findAll().stream()
                .filter(pipeline -> pipeline.getName().equals(name))
                .map(this::addPipelineSteps)
                .findFirst();
    }

    @Override
    public Optional<Pipeline> findById(final long id) {
        return super.findById(id)
                .map(this::addPipelineSteps);
    }

    @Override
    public List<Pipeline> findAllById(final Iterable<Long> ids) {
        return super.findAllByIds(ids)
                .stream()
                .map(this::addPipelineSteps)
                .collect(Collectors.toList());
    }

    @Override
    public Pipeline save(final Pipeline pipeline) {
        if (pipeline.getId() == null) {
            pipeline.setId(pipelineIdSequence.getAndIncrement());
        }

        if (pipeline.getSteps() != null) {
            for (PipelineStep step : pipeline.getSteps()) {
                if (step.getId() == null) {
                    step.setId(stepIdSequence.getAndIncrement());
                }
            }
        }

        return super.save(pipeline);
    }

    @Override
    public List<Pipeline> findAllByProductId(final Long productId) {
        return findAll().stream()
                .filter(pipeline -> pipeline.getProductId().equals(productId))
                .map(this::addPipelineSteps)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteAllByProductId(final Long productId) {
        findAllByProductId(productId).forEach(pipeline -> deleteById(pipeline.getId()));
    }

    private Pipeline addPipelineSteps(final Pipeline pipeline) {
        Set<PipelineStep> steps = pipelineStepRepository.findAll().stream()
                .filter(step -> step.getPipelineId().equals(pipeline.getId()))
                .sorted(Comparator.comparing(PipelineStep::getId))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        pipeline.setSteps(steps);
        return pipeline;
    }
}
