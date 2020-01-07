package com.lombardrisk.ignis.util;

import com.lombardrisk.ignis.server.product.pipeline.model.PipelineStep;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Collection;

public class PipelineStepUtil {
    private final JdbcTemplate jdbcTemplate;
    private final PipelineStepSelectUtil pipelineStepSelectUtil;

    public PipelineStepUtil(
            final JdbcTemplate jdbcTemplate,
            final PipelineStepSelectUtil pipelineStepSelectUtil) {
        this.jdbcTemplate = jdbcTemplate;
        this.pipelineStepSelectUtil = pipelineStepSelectUtil;
    }

    public void insert(final PipelineStep pipelineStep, final Long pipelineId) {
        jdbcTemplate.update("INSERT INTO PIPELINE_STEP(ID, NAME, DESCRIPTION, TYPE, PIPELINE_ID) VALUES(?, ?, ?, ?, ?)",
                pipelineStep.getId(),
                pipelineStep.getName(),
                pipelineStep.getDescription(),
                pipelineStep.getType().toString(),
                pipelineId);

        pipelineStepSelectUtil.insertAll(pipelineStep.getSelects(), pipelineStep.getId());
    }

    public void insertAll(final Collection<PipelineStep> pipelineSteps, final Long pipelineId) {
        for (PipelineStep pipelineStep : pipelineSteps) {
            insert(pipelineStep, pipelineId);
        }
    }
}
