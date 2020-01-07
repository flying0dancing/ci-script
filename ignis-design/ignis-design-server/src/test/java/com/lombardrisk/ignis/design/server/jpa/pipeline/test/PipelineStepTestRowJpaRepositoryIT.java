package com.lombardrisk.ignis.design.server.jpa.pipeline.test;

import com.lombardrisk.ignis.design.server.DesignStudioTestConfig;
import com.lombardrisk.ignis.design.server.fixtures.Design;
import com.lombardrisk.ignis.design.server.jpa.ProductConfigJpaRepository;
import com.lombardrisk.ignis.design.server.jpa.SchemaJpaRepository;
import com.lombardrisk.ignis.design.server.jpa.pipeline.PipelineJpaRepository;
import com.lombardrisk.ignis.design.server.jpa.pipeline.PipelineStepJpaRepository;
import com.lombardrisk.ignis.design.server.pipeline.model.Pipeline;
import com.lombardrisk.ignis.design.server.pipeline.model.PipelineMapStep;
import com.lombardrisk.ignis.design.server.pipeline.test.model.ActualDataRow;
import com.lombardrisk.ignis.design.server.pipeline.test.model.ExpectedDataRow;
import com.lombardrisk.ignis.design.server.pipeline.test.model.InputDataRow;
import com.lombardrisk.ignis.design.server.pipeline.test.model.PipelineStepTest;
import com.lombardrisk.ignis.design.server.pipeline.test.model.PipelineStepTestRow;
import com.lombardrisk.ignis.design.server.productconfig.ProductConfig;
import com.lombardrisk.ignis.design.server.productconfig.schema.Schema;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.EntityManager;
import java.util.List;

import static com.google.common.collect.Sets.newLinkedHashSet;
import static com.lombardrisk.ignis.design.server.pipeline.test.model.ExpectedDataRow.Status.MATCHED;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@DesignStudioTestConfig
public class PipelineStepTestRowJpaRepositoryIT {

    @Autowired
    private PipelineStepTestRowJpaRepository pipelineStepTestRowJpaRepository;

    @Autowired
    private ExpectedDataRowJpaRepository expectedDataRowJpaRepository;

    @Autowired
    private InputDataRowJpaRepository inputDataRowJpaRepository;

    @Autowired
    private ActualDataRowJpaRepository actualDataRowJpaRepository;

    @Autowired
    private PipelineStepTestJpaRepository pipelineStepTestJpaRepository;

    @Autowired
    private PipelineJpaRepository pipelineJpaRepository;
    @Autowired
    private PipelineStepJpaRepository pipelineStepJpaRepository;

    @Autowired
    private SchemaJpaRepository schemaJpaRepository;

    @Autowired
    private ProductConfigJpaRepository productConfigJpaRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    public void createEachRowType_retrieveReturnsAll() {
        ProductConfig product = productConfigJpaRepository.save(Design.Populated.productConfig().build());

        Schema schemaIn = schemaJpaRepository.save(Design.Populated.schema()
                .fields(emptySet())
                .productId(product.getId())
                .displayName("table1")
                .physicalTableName("tb1")
                .validationRules(emptySet())
                .build());
        Schema schemaOut = schemaJpaRepository.save(Design.Populated.schema()
                .fields(emptySet())
                .productId(product.getId())
                .displayName("table2")
                .physicalTableName("tb2")
                .validationRules(emptySet())
                .build());

        Pipeline createdPipeline = pipelineJpaRepository.save(Design.Populated.pipeline()
                .productId(product.getId())
                .build());

        PipelineMapStep pipelineMapStep = pipelineStepJpaRepository.save(
                Design.Populated.pipelineMapStep()
                        .pipelineId(createdPipeline.getId())
                        .schemaInId(schemaIn.getId())
                        .schemaOutId(schemaOut.getId())
                        .selects(newLinkedHashSet())
                        .build());

        PipelineStepTest saveStepTest = pipelineStepTestJpaRepository
                .save(
                        Design.Populated.pipelineStepTest()
                                .name("test")
                                .description("description")
                                .pipelineStepId(pipelineMapStep.getId())
                                .build());

        ActualDataRow actualDataRow = actualDataRowJpaRepository.save(
                Design.Populated.actualDataRow()
                        .pipelineStepTestId(saveStepTest.getId())
                        .schemaId(123L)
                        .status(ActualDataRow.Status.MATCHED)
                        .isRun(false)
                        .build());

        ExpectedDataRow expectedDataRow = expectedDataRowJpaRepository.save(
                Design.Populated.expectedDataRow()
                        .pipelineStepTestId(saveStepTest.getId())
                        .schemaId(123L)
                        .status(MATCHED)
                        .isRun(false)
                        .build());

        InputDataRow inputDataRow = inputDataRowJpaRepository.save(
                Design.Populated.inputDataRow()
                        .pipelineStepTestId(saveStepTest.getId())
                        .schemaId(123L)
                        .isRun(false)
                        .build());

        List<PipelineStepTestRow> all = pipelineStepTestRowJpaRepository.findAll();

        assertThat(all)
                .containsOnly(actualDataRow, expectedDataRow, inputDataRow);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    public void updateExpectedRowsMatching_Updates() {
        ProductConfig product = productConfigJpaRepository.save(Design.Populated.productConfig().build());

        Schema schemaIn = schemaJpaRepository.save(Design.Populated.schema()
                .fields(emptySet())
                .productId(product.getId())
                .displayName("table1")
                .physicalTableName("tb1")
                .validationRules(emptySet())
                .build());
        Schema schemaOut = schemaJpaRepository.save(Design.Populated.schema()
                .fields(emptySet())
                .productId(product.getId())
                .displayName("table2")
                .physicalTableName("tb2")
                .validationRules(emptySet())
                .build());

        Pipeline createdPipeline = pipelineJpaRepository.save(Design.Populated.pipeline()
                .productId(product.getId())
                .build());

        PipelineMapStep pipelineMapStep = pipelineStepJpaRepository.save(
                Design.Populated.pipelineMapStep()
                        .pipelineId(createdPipeline.getId())
                        .schemaInId(schemaIn.getId())
                        .schemaOutId(schemaOut.getId())
                        .selects(newLinkedHashSet())
                        .build());

        PipelineStepTest saveStepTest = pipelineStepTestJpaRepository
                .save(
                        Design.Populated.pipelineStepTest()
                                .name("test")
                                .description("description")
                                .pipelineStepId(pipelineMapStep.getId())
                                .build());

        ExpectedDataRow expectedDataRow = expectedDataRowJpaRepository.save(
                Design.Populated.expectedDataRow()
                        .pipelineStepTestId(saveStepTest.getId())
                        .schemaId(schemaOut.getId())
                        .status(null)
                        .isRun(false)
                        .build());

        expectedDataRowJpaRepository.updateExpectedRowsStatusAndRun(
                singletonList(expectedDataRow.getId()),
                MATCHED,
                true);

        entityManager.flush();
        entityManager.clear();


        ExpectedDataRow retrievedExpectedRow = expectedDataRowJpaRepository.findById(expectedDataRow.getId())
                        .get();

        assertThat(retrievedExpectedRow.getStatus())
                .isEqualTo(MATCHED);
        assertThat(retrievedExpectedRow.isRun())
                .isEqualTo(true);
    }
}
