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
import com.lombardrisk.ignis.design.server.pipeline.test.model.ActualDataRow;
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
import static com.lombardrisk.ignis.design.server.pipeline.test.model.PipelineStepTestRow.Type.ACTUAL;
import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@RunWith(SpringRunner.class)
@DesignStudioTestConfig
public class ActualDataRowJpaRepositoryIT {

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

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    public void create_retrieve() {
        PipelineStepTest saveStepTest = pipelineStepTestJpaRepository
                .save(
                        Design.Populated.pipelineStepTest()
                                .name("test")
                                .description("description")
                                .pipelineStepId(176757L)
                                .build());

        ActualDataRow save = actualDataRowJpaRepository
                .save(
                        Design.Populated.actualDataRow()
                                .pipelineStepTestId(saveStepTest.getId())
                                .schemaId(123L)
                                .status(ActualDataRow.Status.MATCHED)
                                .isRun(false)
                                .build());

        ActualDataRow retrieved = actualDataRowJpaRepository
                .findById(save.getId())
                .get();

        soft.assertThat(retrieved.getPipelineStepTestId())
                .isEqualTo(saveStepTest.getId());
        soft.assertThat(retrieved.getSchemaId())
                .isEqualTo(123L);
        soft.assertThat(retrieved.getStatus())
                .isEqualTo(ActualDataRow.Status.MATCHED);
        soft.assertThat(retrieved.getType())
                .isEqualTo(ACTUAL);
        soft.assertThat(retrieved.isRun())
                .isEqualTo(false);
    }

    @Test
    public void deleteAllByPipelineStepTestId() {
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

        PipelineStepTest stepTest = pipelineStepTestJpaRepository
                .save(
                        Design.Populated.pipelineStepTest()
                                .name("test")
                                .description("description")
                                .pipelineStepId(pipelineMapStep.getId())
                                .build());

        ActualDataRow save = actualDataRowJpaRepository
                .save(
                        Design.Populated.actualDataRow()
                                .pipelineStepTestId(stepTest.getId())
                                .schemaId(123L)
                                .status(ActualDataRow.Status.MATCHED)
                                .isRun(false)
                                .build());

        actualDataRowJpaRepository.deleteAllByPipelineStepTestId(stepTest.getId());

        List<ActualDataRow> allRows = actualDataRowJpaRepository.findAll();

        assertThat(allRows)
                .extracting(ActualDataRow::getPipelineStepTestId)
                .doesNotContain(stepTest.getId());
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

        ActualDataRow actualDataRow1 = actualDataRowJpaRepository.save(Design.Populated.actualDataRow()
                .pipelineStepTestId(stepTest1.getId()).schemaId(schemaIn.getId()).isRun(true)
                .cells(newHashSet(
                        PipelineStepTestCell.builder().data("test1, row1, cell1").fieldId(field1.getId()).build(),
                        PipelineStepTestCell.builder().data("test1, row1, cell2").fieldId(field2.getId()).build()))
                .build());

        ActualDataRow actualDataRow2 = actualDataRowJpaRepository.save(Design.Populated.actualDataRow()
                .pipelineStepTestId(stepTest1.getId()).schemaId(schemaIn.getId()).isRun(true)
                .cells(newHashSet(
                        PipelineStepTestCell.builder().data("test1, row2, cell1").fieldId(field1.getId()).build(),
                        PipelineStepTestCell.builder().data("test1, row2, cell2").fieldId(field2.getId()).build()))
                .build());

        ActualDataRow actualDataRow3 = actualDataRowJpaRepository.save(Design.Populated.actualDataRow()
                .pipelineStepTestId(stepTest2.getId()).schemaId(schemaIn.getId()).isRun(true)
                .cells(newHashSet(
                        PipelineStepTestCell.builder().data("test2, row1, cell1").fieldId(field1.getId()).build(),
                        PipelineStepTestCell.builder().data("test2, row1, cell2").fieldId(field2.getId()).build()))
                .build());

        entityManager.flush();
        entityManager.clear();

        List<ActualDataRow> test1Rows = actualDataRowJpaRepository
                .findAllByPipelineStepTestIdOrderById(stepTest1.getId());
        List<ActualDataRow> test2Rows = actualDataRowJpaRepository
                .findAllByPipelineStepTestIdOrderById(stepTest2.getId());

        soft.assertThat(test1Rows)
                .extracting(ActualDataRow::getId, ActualDataRow::getPipelineStepTestId, ActualDataRow::getSchemaId)
                .containsExactly(
                        tuple(actualDataRow1.getId(), stepTest1.getId(), schemaIn.getId()),
                        tuple(actualDataRow2.getId(), stepTest1.getId(), schemaIn.getId()));

        soft.assertThat(test2Rows)
                .extracting(ActualDataRow::getId, ActualDataRow::getPipelineStepTestId, ActualDataRow::getSchemaId)
                .containsExactly(
                        tuple(actualDataRow3.getId(), stepTest2.getId(), schemaIn.getId()));

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

        soft.assertThat(actualDataRowJpaRepository.findAllByPipelineStepTestIdOrderById(1234567L))
                .isEmpty();
    }
}
