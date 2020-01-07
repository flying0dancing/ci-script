package com.lombardrisk.ignis.design.server.jpa;

import com.lombardrisk.ignis.design.server.jpa.pipeline.PipelineJpaRepository;
import com.lombardrisk.ignis.design.server.jpa.pipeline.PipelineStepJpaRepository;
import com.lombardrisk.ignis.design.server.jpa.pipeline.test.ActualDataRowJpaRepository;
import com.lombardrisk.ignis.design.server.jpa.pipeline.test.ExpectedDataRowJpaRepository;
import com.lombardrisk.ignis.design.server.jpa.pipeline.test.InputDataRowJpaRepository;
import com.lombardrisk.ignis.design.server.jpa.pipeline.test.PipelineStepTestCellJpaRepository;
import com.lombardrisk.ignis.design.server.jpa.pipeline.test.PipelineStepTestJpaRepository;
import com.lombardrisk.ignis.design.server.jpa.pipeline.test.PipelineStepTestRowJpaRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
@AllArgsConstructor(onConstructor = @__({ @Autowired }))
public class JpaRepositoryConfiguration {

    private final SchemaJpaRepository schemaRepository;
    private final ValidationRuleJpaRepository ruleRepository;
    private final ProductConfigJpaRepository productConfigRepository;
    private final DatasetSchemaFieldJpaRepository datasetSchemaFieldJpaRepository;
    private final FieldJpaRepository fieldJpaRepository;
    private final PipelineJpaRepository pipelineRepository;
    private final PipelineStepJpaRepository pipelineStepRepository;
    private final PipelineStepTestJpaRepository pipelineStepTestJpaRepository;
    private final InputDataRowJpaRepository inputDataRowJpaRepository;
    private final ExpectedDataRowJpaRepository expectedDataRowJpaRepository;
    private final ActualDataRowJpaRepository actualDataRowJpaRepository;
    private final PipelineStepTestRowJpaRepository pipelineStepTestRowJpaRepository;
    private final PipelineSelectJpaRepository pipelineSelectJpaRepository;
    private final PipelineJoinJpaRepository pipelineJoinJpaRepository;
    private final PipelineStepTestCellJpaRepository pipelineStepTestCellJpaRepository;
}
