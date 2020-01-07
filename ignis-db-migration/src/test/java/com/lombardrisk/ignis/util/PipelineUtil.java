package com.lombardrisk.ignis.util;

import com.lombardrisk.ignis.server.product.pipeline.model.Pipeline;
import org.springframework.jdbc.core.JdbcTemplate;

public class PipelineUtil {
    private final JdbcTemplate jdbcTemplate;
    private final PipelineStepUtil pipelineStepUtil;

    public PipelineUtil(
            final JdbcTemplate jdbcTemplate,
            final PipelineStepUtil pipelineStepUtil) {
        this.jdbcTemplate = jdbcTemplate;
        this.pipelineStepUtil = pipelineStepUtil;
    }

    public void insert(final Pipeline pipeline) {
        jdbcTemplate.update("INSERT INTO PIPELINE(ID, NAME, PRODUCT_ID) VALUES(?, ?, ?)",
                pipeline.getId(), pipeline.getName(), pipeline.getProductId());

        pipelineStepUtil.insertAll(pipeline.getSteps(), pipeline.getId());
    }
}
