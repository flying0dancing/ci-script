package com.lombardrisk.ignis.util;

import com.lombardrisk.ignis.server.product.pipeline.select.Select;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Collection;

public class PipelineStepSelectUtil {
    private final JdbcTemplate jdbcTemplate;

    public PipelineStepSelectUtil(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(final Select select, final Long pipelineStepId) {
        jdbcTemplate.update("INSERT INTO PIPELINE_STEP_SELECT(ID, SELECTS, PIPELINE_STEP_ID) VALUES(?, ?, ?)",
                select.getId(), select.getSelect(), pipelineStepId);
    }

    public void insertAll(final Collection<Select> selects, final Long pipelineStepId) {
        for (Select select : selects) {
            insert(select, pipelineStepId);
        }
    }
}
