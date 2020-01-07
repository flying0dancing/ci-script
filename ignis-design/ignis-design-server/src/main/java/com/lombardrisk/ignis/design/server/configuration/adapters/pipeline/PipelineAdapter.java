package com.lombardrisk.ignis.design.server.configuration.adapters.pipeline;

import com.lombardrisk.ignis.design.server.jpa.pipeline.PipelineJpaRepository;
import com.lombardrisk.ignis.design.server.pipeline.api.PipelineRepository;
import com.lombardrisk.ignis.design.server.pipeline.model.Pipeline;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public class PipelineAdapter implements PipelineRepository {

    private final PipelineJpaRepository pipelineRepository;

    public PipelineAdapter(final PipelineJpaRepository pipelineRepository) {
        this.pipelineRepository = pipelineRepository;
    }

    @Override
    public Optional<Pipeline> findByName(final String name) {
        return pipelineRepository.findByName(name);
    }

    @Override
    public Optional<Pipeline> findById(final long id) {
        return pipelineRepository.findById(id);
    }

    @Override
    public List<Pipeline> findAll() {
        return pipelineRepository.findAll();
    }

    @Override
    public List<Pipeline> findAllById(final Iterable<Long> ids) {
        return pipelineRepository.findAllById(ids);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void delete(final Pipeline pipeline) {
        pipelineRepository.delete(pipeline);
    }

    @Override
    public Pipeline save(final Pipeline pipeline) {
        return pipelineRepository.save(pipeline);
    }

    @Override
    public List<Pipeline> findAllByProductId(final Long productId) {
        return pipelineRepository.findByProductId(productId);
    }

    @Override
    public void deleteAllByProductId(final Long productId) {
        pipelineRepository.deleteAllByProductId(productId);
    }
}
