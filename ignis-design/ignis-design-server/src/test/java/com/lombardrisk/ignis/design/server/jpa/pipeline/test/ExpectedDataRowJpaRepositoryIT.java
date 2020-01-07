package com.lombardrisk.ignis.design.server.jpa.pipeline.test;

import com.lombardrisk.ignis.design.field.model.StringField;
import com.lombardrisk.ignis.design.server.DesignStudioTestConfig;
import com.lombardrisk.ignis.design.server.fixtures.Design;
import com.lombardrisk.ignis.design.server.jpa.FieldJpaRepository;
import com.lombardrisk.ignis.design.server.jpa.ProductConfigJpaRepository;
import com.lombardrisk.ignis.design.server.jpa.SchemaJpaRepository;
import com.lombardrisk.ignis.design.server.jpa.pipeline.PipelineJpaRepository;
import com.lombardrisk.ignis.design.server.jpa.pipeline.PipelineStepJpaRepository;
import com.lombardrisk.ignis.design.server.pipeline.model.Pipeline;
import com.lombardrisk.ignis.design.server.pipeline.model.PipelineMapStep;
import com.lombardrisk.ignis.design.server.pipeline.test.model.ExpectedDataRow;
import com.lombardrisk.ignis.design.server.pipeline.test.model.PipelineStepTest;
import com.lombardrisk.ignis.design.server.pipeline.test.model.PipelineStepTestCell;
import com.lombardrisk.ignis.design.server.productconfig.ProductConfig;
import com.lombardrisk.ignis.design.server.productconfig.schema.Schema;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.EntityManager;
import java.util.List;

import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Sets.newLinkedHashSet;
import static com.lombardrisk.ignis.design.server.pipeline.test.model.ExpectedDataRow.Status.MATCHED;
import static com.lombardrisk.ignis.design.server.pipeline.test.model.ExpectedDataRow.Status.NOT_FOUND;
import static com.lombardrisk.ignis.design.server.pipeline.test.model.PipelineStepTestRow.Type.EXPECTED;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@RunWith(SpringRunner.class)
@DesignStudioTestConfig
public class ExpectedDataRowJpaRepositoryIT {

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
    private FieldJpaRepository fieldJpaRepository;

    @Autowired
    private ProductConfigJpaRepository productConfigJpaRepository;

    @Autowired
    private EntityManager entityManager;
    @Rule
    public JUnitSoftAssertions soft = new JUnitSoftAssertions();

    @Test
    public void create_retrieve() {
        PipelineStepTest saveStepTest = pipelineStepTestJpaRepository
                .save(
                        Design.Populated.pipelineStepTest()
                                .name("test")
                                .description("description")
                                .pipelineStepId(176757L)
                                .build());

        ExpectedDataRow save = expectedDataRowJpaRepository
                .save(
                        Design.Populated.expectedDataRow()
                                .pipelineStepTestId(saveStepTest.getId())
                                .schemaId(123L)
                                .status(ExpectedDataRow.Status.MATCHED)
                                .isRun(false)
                                .build());

        ExpectedDataRow retrieved = expectedDataRowJpaRepository.getOne(save.getId());

        soft.assertThat(retrieved.getPipelineStepTestId())
                .isEqualTo(saveStepTest.getId());
        soft.assertThat(retrieved.getSchemaId())
                .isEqualTo(123L);
        soft.assertThat(retrieved.getStatus())
                .isEqualTo(ExpectedDataRow.Status.MATCHED);
        soft.assertThat(retrieved.getType())
                .isEqualTo(EXPECTED);
        soft.assertThat(retrieved.isRun())
                .isEqualTo(false);
    }

    @Test
    public void updateRun_UpdatesAllByIds() {
        ProductConfig product = productConfigJpaRepository.save(Design.Populated.productConfig().build());

        Schema schemaIn = schemaJpaRepository.save(Design.Populated.schema()
                .productId(product.getId())
                .displayName("table1")
                .physicalTableName("tb1")
                .validationRules(emptySet())
                .fields(emptySet())
                .build());
        Schema schemaOut = schemaJpaRepository.save(Design.Populated.schema()
                .productId(product.getId())
                .displayName("table2")
                .physicalTableName("tb2")
                .validationRules(emptySet())
                .fields(emptySet())
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

        PipelineStepTest stepTest = pipelineStepTestJpaRepository.save(
                Design.Populated.pipelineStepTest()
                        .pipelineStepId(pipelineMapStep.getId())
                        .build());

        ExpectedDataRow expectedDataRow = expectedDataRowJpaRepository.save(
                Design.Populated.expectedDataRow()
                        .pipelineStepTestId(stepTest.getId())
                        .schemaId(schemaOut.getId())
                        .status(NOT_FOUND)
                        .isRun(false)
                        .build());

        assertThat(expectedDataRow.isRun())
                .isFalse();

        assertThat(expectedDataRow.getStatus())
                .isEqualTo(NOT_FOUND);

        int updateCount = expectedDataRowJpaRepository.updateExpectedRowsStatusAndRun(
                singletonList(expectedDataRow.getId()),
                ExpectedDataRow.Status.MATCHED,
                true);

        assertThat(updateCount)
                .isEqualTo(1);

        clearEntityManager();

        ExpectedDataRow retrieved = expectedDataRowJpaRepository.getOne(expectedDataRow.getId());

        assertThat(retrieved.getStatus())
                .isEqualTo(MATCHED);
        assertThat(retrieved.isRun())
                .isTrue();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void findAllByPipelineStepTestId() {
        ProductConfig product = productConfigJpaRepository.save(Design.Populated.productConfig().build());

        Schema schemaIn = schemaJpaRepository.save(Design.Populated.schema()
                .productId(product.getId())
                .displayName("table1")
                .physicalTableName("tb1")
                .validationRules(emptySet())
                .fields(newHashSet(StringField.builder().build()))
                .build());
        Schema schemaOut = schemaJpaRepository.save(Design.Populated.schema()
                .productId(product.getId())
                .displayName("table2")
                .physicalTableName("tb2")
                .validationRules(emptySet())
                .fields(emptySet())
                .build());

        StringField field1 = fieldJpaRepository
                .save(StringField.builder().schemaId(schemaIn.getId()).name("A").build());
        StringField field2 = fieldJpaRepository
                .save(StringField.builder().schemaId(schemaIn.getId()).name("B").build());

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

        PipelineStepTest stepTest1 = pipelineStepTestJpaRepository.save(
                Design.Populated.pipelineStepTest()
                        .pipelineStepId(pipelineMapStep.getId())
                        .build());

        PipelineStepTest stepTest2 = pipelineStepTestJpaRepository.save(
                Design.Populated.pipelineStepTest()
                        .pipelineStepId(pipelineMapStep.getId())
                        .build());

        ExpectedDataRow expectedDataRow1 = expectedDataRowJpaRepository.save(Design.Populated.expectedDataRow()
                .pipelineStepTestId(stepTest1.getId()).schemaId(schemaIn.getId()).isRun(true)
                .cells(newHashSet(
                        PipelineStepTestCell.builder().data("test1, row1, cell1").fieldId(field1.getId()).build(),
                        PipelineStepTestCell.builder().data("test1, row1, cell2").fieldId(field2.getId()).build()))
                .build());

        ExpectedDataRow expectedDataRow2 = expectedDataRowJpaRepository.save(Design.Populated.expectedDataRow()
                .pipelineStepTestId(stepTest1.getId()).schemaId(schemaIn.getId()).isRun(true)
                .cells(newHashSet(
                        PipelineStepTestCell.builder().data("test1, row2, cell1").fieldId(field1.getId()).build(),
                        PipelineStepTestCell.builder().data("test1, row2, cell2").fieldId(field2.getId()).build()))
                .build());

        ExpectedDataRow expectedDataRow3 = expectedDataRowJpaRepository.save(Design.Populated.expectedDataRow()
                .pipelineStepTestId(stepTest2.getId()).schemaId(schemaIn.getId()).isRun(true)
                .cells(newHashSet(
                        PipelineStepTestCell.builder().data("test2, row1, cell1").fieldId(field1.getId()).build(),
                        PipelineStepTestCell.builder().data("test2, row1, cell2").fieldId(field2.getId()).build()))
                .build());

        clearEntityManager();

        List<ExpectedDataRow> test1Rows = expectedDataRowJpaRepository
                .findAllByPipelineStepTestIdOrderById(stepTest1.getId());
        List<ExpectedDataRow> test2Rows = expectedDataRowJpaRepository
                .findAllByPipelineStepTestIdOrderById(stepTest2.getId());

        soft.assertThat(test1Rows)
                .extracting(ExpectedDataRow::getId, ExpectedDataRow::getPipelineStepTestId, ExpectedDataRow::getSchemaId)
                .containsExactly(
                        tuple(expectedDataRow1.getId(), stepTest1.getId(), schemaIn.getId()),
                        tuple(expectedDataRow2.getId(), stepTest1.getId(), schemaIn.getId()));

        soft.assertThat(test2Rows)
                .extracting(ExpectedDataRow::getId, ExpectedDataRow::getPipelineStepTestId, ExpectedDataRow::getSchemaId)
                .containsExactly(
                        tuple(expectedDataRow3.getId(), stepTest2.getId(), schemaIn.getId()));

        soft.assertThat(test1Rows.get(0).getCells())
                .extracting(PipelineStepTestCell::getFieldId, PipelineStepTestCell::getData)
                .containsExactly(
                        tuple(field1.getId(), "test1, row1, cell1"),
                        tuple(field2.getId(), "test1, row1, cell2"));

        soft.assertThat(test1Rows.get(1).getCells())
                .extracting(PipelineStepTestCell::getFieldId, PipelineStepTestCell::getData)
                .containsExactly(
                        tuple(field1.getId(), "test1, row2, cell1"),
                        tuple(field2.getId(), "test1, row2, cell2"));

        soft.assertThat(test2Rows.get(0).getCells())
                .extracting(PipelineStepTestCell::getFieldId, PipelineStepTestCell::getData)
                .containsExactly(
                        tuple(field1.getId(), "test2, row1, cell1"),
                        tuple(field2.getId(), "test2, row1, cell2"));

        soft.assertThat(expectedDataRowJpaRepository.findAllByPipelineStepTestIdOrderById(1234567L))
                .isEmpty();
    }

    private void clearEntityManager() {
        entityManager.flush();
        entityManager.clear();
    }
}
