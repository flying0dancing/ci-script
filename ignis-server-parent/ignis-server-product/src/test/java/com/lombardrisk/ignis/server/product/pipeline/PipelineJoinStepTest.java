package com.lombardrisk.ignis.server.product.pipeline;

import com.lombardrisk.ignis.server.product.fixture.ProductPopulated;
import com.lombardrisk.ignis.server.product.pipeline.model.PipelineJoinStep;
import org.junit.Test;

import static com.google.common.collect.Sets.newHashSet;
import static org.assertj.core.api.Assertions.assertThat;

public class PipelineJoinStepTest {

    @Test
    public void findInputSchemas_ReturnsAllIds() {
        PipelineJoinStep joinStep = ProductPopulated.joinPipelineStep()
                .joins(newHashSet(
                        ProductPopulated.join()
                                .rightSchemaId(101L)
                                .leftSchemaId(102L)
                                .build(),
                        ProductPopulated.join()
                                .rightSchemaId(101L)
                                .leftSchemaId(103L)
                                .build(),
                        ProductPopulated.join()
                                .rightSchemaId(103L)
                                .leftSchemaId(104L)
                                .build()))
                .build();

        assertThat(joinStep.findInputSchemas())
                .containsOnly(101L, 102L, 103L, 104L);
    }
}
