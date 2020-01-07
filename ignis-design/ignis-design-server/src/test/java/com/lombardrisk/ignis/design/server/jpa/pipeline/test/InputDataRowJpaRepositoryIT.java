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
import com.lombardrisk.ignis.design.server.pipeline.test.model.InputDataRow;
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
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@RunWith(SpringRunner.class)
@DesignStudioTestConfig
public class InputDataRowJpaRepositoryIT {

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

        InputDataRow save = inputDataRowJpaRepository
                .save(
                        Design.Populated.inputDataRow()
                                .pipelineStepTestId(saveStepTest.getId())
                                .schemaId(123L)
                                .isRun(false)
                                .build());

        InputDataRow retrieved = inputDataRowJpaRepository.getOne(save.getId());

        soft.assertThat(retrieved.getPipelineStepTestId())
                .isEqualTo(saveStepTest.getId());
        soft.assertThat(retrieved.getSchemaId())
                .isEqualTo(123L);
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

        InputDataRow inputDataRow = inputDataRowJpaRepository.save(
                Design.Populated.inputDataRow()
                        .pipelineStepTestId(stepTest.getId())
                        .schemaId(schemaIn.getId())
                        .isRun(false)
                        .build());

        assertThat(inputDataRow.isRun())
                .isFalse();

        int updated = inputDataRowJpaRepository.updateRun(singletonList(inputDataRow.getId()), true);

        assertThat(updated)
                .isEqualTo(1);

        entityManager.flush();
        entityManager.clear();

        InputDataRow retrieved = inputDataRowJpaRepository.getOne(inputDataRow.getId());

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

        InputDataRow inputDataRow1 = inputDataRowJpaRepository.save(Design.Populated.inputDataRow()
                .pipelineStepTestId(stepTest1.getId()).schemaId(schemaIn.getId()).isRun(true)
                .cells(newHashSet(
                        PipelineStepTestCell.builder().data("test1, row1, cell1").fieldId(field1.getId()).build(),
                        PipelineStepTestCell.builder().data("test1, row1, cell2").fieldId(field2.getId()).build()))
                .build());

        InputDataRow inputDataRow2 = inputDataRowJpaRepository.save(Design.Populated.inputDataRow()
                .pipelineStepTestId(stepTest1.getId()).schemaId(schemaIn.getId()).isRun(true)
                .cells(newHashSet(
                        PipelineStepTestCell.builder().data("test1, row2, cell1").fieldId(field1.getId()).build(),
                        PipelineStepTestCell.builder().data("test1, row2, cell2").fieldId(field2.getId()).build()))
                .build());

        InputDataRow inputDataRow3 = inputDataRowJpaRepository.save(Design.Populated.inputDataRow()
                .pipelineStepTestId(stepTest2.getId()).schemaId(schemaIn.getId()).isRun(true)
                .cells(newHashSet(
                        PipelineStepTestCell.builder().data("test2, row1, cell1").fieldId(field1.getId()).build(),
                        PipelineStepTestCell.builder().data("test2, row1, cell2").fieldId(field2.getId()).build()))
                .build());

        entityManager.flush();
        entityManager.clear();

        List<InputDataRow> test1Rows = inputDataRowJpaRepository.findAllByPipelineStepTestIdOrderById(stepTest1.getId());
        List<InputDataRow> test2Rows = inputDataRowJpaRepository.findAllByPipelineStepTestIdOrderById(stepTest2.getId());

        soft.assertThat(test1Rows)
                .extracting(InputDataRow::getId, InputDataRow::getPipelineStepTestId, InputDataRow::getSchemaId)
                .containsExactly(
                        tuple(inputDataRow1.getId(), stepTest1.getId(), schemaIn.getId()),
                        tuple(inputDataRow2.getId(), stepTest1.getId(), schemaIn.getId()));

        soft.assertThat(test2Rows)
                .extracting(InputDataRow::getId, InputDataRow::getPipelineStepTestId, InputDataRow::getSchemaId)
                .containsExactly(
                        tuple(inputDataRow3.getId(), stepTest2.getId(), schemaIn.getId()));

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

        soft.assertThat(inputDataRowJpaRepository.findAllByPipelineStepTestIdOrderById(1234567L))
                .isEmpty();
    }
}
