package com.lombardrisk.ignis.design.server.configuration.adapters.schema;

import com.lombardrisk.ignis.design.field.api.FieldDependencyRepository;
import com.lombardrisk.ignis.design.field.model.Field;
import com.lombardrisk.ignis.design.server.jpa.PipelineJoinJpaRepository;
import com.lombardrisk.ignis.design.server.jpa.PipelineSelectJpaRepository;
import com.lombardrisk.ignis.design.server.jpa.pipeline.test.PipelineStepTestCellJpaRepository;
import org.springframework.transaction.annotation.Transactional;

public class FieldDependencyAdapter implements FieldDependencyRepository {

    private final PipelineSelectJpaRepository pipelineSelectJpaRepository;
    private final PipelineJoinJpaRepository pipelineJoinJpaRepository;
    private final PipelineStepTestCellJpaRepository pipelineStepTestCellJpaRepository;

    public FieldDependencyAdapter(
            final PipelineSelectJpaRepository pipelineSelectJpaRepository,
            final PipelineJoinJpaRepository pipelineJoinJpaRepository,
            final PipelineStepTestCellJpaRepository pipelineStepTestCellJpaRepository) {
        this.pipelineSelectJpaRepository = pipelineSelectJpaRepository;
        this.pipelineJoinJpaRepository = pipelineJoinJpaRepository;
        this.pipelineStepTestCellJpaRepository = pipelineStepTestCellJpaRepository;
    }

    @Override
    @Transactional
    public void deleteForField(final Field field) {
        pipelineSelectJpaRepository.deleteAllByOutputFieldId(field.getId());

        pipelineJoinJpaRepository.deleteAll(
                pipelineJoinJpaRepository.findAllByLeftJoinFieldId(field.getId()));
        pipelineJoinJpaRepository.deleteAll(
                pipelineJoinJpaRepository.findAllByRightJoinFieldId(field.getId()));

        pipelineStepTestCellJpaRepository.deleteByFieldId(field.getId());
    }
}
