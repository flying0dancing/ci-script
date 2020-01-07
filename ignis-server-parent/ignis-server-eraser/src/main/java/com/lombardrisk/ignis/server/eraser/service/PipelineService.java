package com.lombardrisk.ignis.server.eraser.service;

import com.lombardrisk.ignis.server.dataset.pipeline.invocation.PipelineInvocationJpaRepository;
import com.lombardrisk.ignis.server.product.pipeline.PipelineJpaRepository;
import com.lombardrisk.ignis.server.product.pipeline.model.Pipeline;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@AllArgsConstructor
public class PipelineService {

    private final PipelineJpaRepository pipelineJpaRepository;
    private final PipelineInvocationJpaRepository pipelineInvocationJpaRepository;

    @Transactional
    public void deletePipeline(final Long pipelineId) {
        Pipeline pipeline = pipelineJpaRepository.findById(pipelineId)
                .orElseThrow(() -> new IllegalArgumentException("Could not find Pipeline by id " + pipelineId));

        deletePipeline(pipeline);
    }

    @Transactional
    public void deletePipeline(final Pipeline pipeline) {
        log.info("Deleting pipeline invocations for pipeline {} - {}", pipeline.getName(), pipeline.getId());
        pipelineInvocationJpaRepository.deleteAllByPipelineId(pipeline.getId());
        log.info("Deleting pipeline {} - {}", pipeline.getName(), pipeline.getId());
        pipelineJpaRepository.delete(pipeline);
    }
}
