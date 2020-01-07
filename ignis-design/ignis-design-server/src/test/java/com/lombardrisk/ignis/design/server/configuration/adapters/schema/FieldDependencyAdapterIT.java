package com.lombardrisk.ignis.design.server.configuration.adapters.schema;

import com.lombardrisk.ignis.design.field.DesignField;
import com.lombardrisk.ignis.design.field.api.FieldDependencyRepository;
import com.lombardrisk.ignis.design.field.model.StringField;
import com.lombardrisk.ignis.design.server.DesignStudioTestConfig;
import com.lombardrisk.ignis.design.server.fixtures.Design;
import com.lombardrisk.ignis.design.server.jpa.FieldJpaRepository;
import com.lombardrisk.ignis.design.server.jpa.PipelineSelectJpaRepository;
import com.lombardrisk.ignis.design.server.jpa.ProductConfigJpaRepository;
import com.lombardrisk.ignis.design.server.jpa.SchemaJpaRepository;
import com.lombardrisk.ignis.design.server.jpa.pipeline.PipelineStepJpaRepository;
import com.lombardrisk.ignis.design.server.jpa.pipeline.test.PipelineStepTestJpaRepository;
import com.lombardrisk.ignis.design.server.jpa.pipeline.test.PipelineStepTestRowJpaRepository;
import com.lombardrisk.ignis.design.server.pipeline.api.PipelineRepository;
import com.lombardrisk.ignis.design.server.pipeline.model.Pipeline;
import com.lombardrisk.ignis.design.server.pipeline.model.PipelineJoinStep;
import com.lombardrisk.ignis.design.server.pipeline.model.PipelineStep;
import com.lombardrisk.ignis.design.server.pipeline.model.join.JoinField;
import com.lombardrisk.ignis.design.server.pipeline.test.model.ExpectedDataRow;
import com.lombardrisk.ignis.design.server.pipeline.test.model.InputDataRow;
import com.lombardrisk.ignis.design.server.pipeline.test.model.PipelineStepTest;
import com.lombardrisk.ignis.design.server.pipeline.test.model.PipelineStepTestCell;
import com.lombardrisk.ignis.design.server.pipeline.test.model.PipelineStepTestRow;
import com.lombardrisk.ignis.design.server.productconfig.ProductConfig;
import com.lombardrisk.ignis.design.server.productconfig.schema.Schema;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.EntityManager;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static com.google.common.collect.Sets.newHashSet;

@RunWith(SpringRunner.class)
@DesignStudioTestConfig
public class FieldDependencyAdapterIT {

    @Rule
    public JUnitSoftAssertions soft = new JUnitSoftAssertions();

    @Autowired
    private ProductConfigJpaRepository productConfigJpaRepository;

    @Autowired
    private FieldJpaRepository fieldRepository;

    @Autowired
    private PipelineSelectJpaRepository fieldDependencyJpaRepository;

    @Autowired
    private PipelineRepository pipelineRepository;

    @Autowired
    private PipelineStepJpaRepository pipelineStepRepository;

    @Autowired
    private PipelineStepTestJpaRepository pipelineStepTestJpaRepository;

    @Autowired
    private PipelineStepTestRowJpaRepository pipelineStepTestRowJpaRepository;

    @Autowired
    private SchemaJpaRepository schemaRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private FieldDependencyRepository fieldDependencyAdapter;

    @Test
    public void deleteForField_FieldHasPipelineMapDependencies_DeletesSelectsAndTestCellData() {

        ProductConfig productConfig = productConfigJpaRepository.save(Design.Populated.productConfig().build());

        Schema a = schemaRepository.save(Design.Populated.emptySchema("A").productId(productConfig.getId()).build());
        StringField aField = fieldRepository.save(DesignField.Populated.stringField("A").schemaId(a.getId()).build());

        Schema b = schemaRepository.save(Design.Populated.emptySchema("B").productId(productConfig.getId()).build());
        StringField bField = fieldRepository.save(DesignField.Populated.stringField("B").schemaId(b.getId()).build());

        Pipeline pipeline = pipelineRepository.save(Design.Populated.pipeline()
                .productId(productConfig.getId())
                .build());

        PipelineStep pipelineStep = pipelineStepRepository.save(Design.Populated.pipelineMapStep()
                .schemaInId(a.getId())
                .schemaOutId(b.getId())
                .pipelineId(pipeline.getId())
                .selects(singleton(Design.Populated.select()
                        .outputFieldId(bField.getId())
                        .select("A")
                        .build()))
                .build());

        PipelineStepTest pipelineStepTest = pipelineStepTestJpaRepository.save(Design.Populated.pipelineStepTest()
                .pipelineStepId(pipelineStep.getId())
                .expectedData(emptySet())
                .inputData(emptySet())
                .actualData(emptySet())
                .build());

        ExpectedDataRow expectedDataRow = pipelineStepTestRowJpaRepository.save(Design.Populated.expectedDataRow()
                .pipelineStepTestId(pipelineStepTest.getId())
                .schemaId(a.getId())
                .cells(singleton(PipelineStepTestCell.builder()
                        .fieldId(bField.getId())
                        .data("HELLO")
                        .build()))
                .build());

        entityManager.flush();
        entityManager.clear();

        fieldDependencyAdapter.deleteForField(bField);

        entityManager.flush();
        entityManager.clear();

        PipelineStep retrievedStep = pipelineStepRepository.getOne(pipelineStep.getId());
        assertThat(retrievedStep.getSelects())
                .isEmpty();

        PipelineStepTestRow retrievedRow = pipelineStepTestRowJpaRepository.getOne(expectedDataRow.getId());
        assertThat(retrievedRow.getCells())
                .isEmpty();
    }

    @Test
    public void deleteForField_FieldHasPipelineJoinRightField_DeletesJoinsAndTestCellData() {

        ProductConfig productConfig = productConfigJpaRepository.save(Design.Populated.productConfig().build());

        Schema a = schemaRepository.save(Design.Populated.emptySchema("A").productId(productConfig.getId()).build());
        StringField aField = fieldRepository.save(DesignField.Populated.stringField("A").schemaId(a.getId()).build());
        StringField a1Field = fieldRepository.save(DesignField.Populated.stringField("A1").schemaId(a.getId()).build());

        Schema b = schemaRepository.save(Design.Populated.emptySchema("B").productId(productConfig.getId()).build());
        StringField bField = fieldRepository.save(DesignField.Populated.stringField("B").schemaId(b.getId()).build());
        StringField b1Field = fieldRepository.save(DesignField.Populated.stringField("B1").schemaId(b.getId()).build());

        Schema c = schemaRepository.save(Design.Populated.emptySchema("C").productId(productConfig.getId()).build());
        StringField cField = fieldRepository.save(DesignField.Populated.stringField("C").schemaId(c.getId()).build());

        Pipeline pipeline = pipelineRepository.save(Design.Populated.pipeline()
                .productId(productConfig.getId())
                .build());

        PipelineJoinStep pipelineStep = pipelineStepRepository.save(Design.Populated.pipelineJoinStep()
                .schemaOutId(c.getId())
                .pipelineId(pipeline.getId())
                .joins(singleton(Design.Populated.join()
                        .leftSchemaId(a.getId())
                        .rightSchemaId(b.getId())
                        .joinFields(newHashSet(
                                JoinField.builder()
                                        .leftJoinFieldId(aField.getId())
                                        .rightJoinFieldId(bField.getId())
                                        .build(),
                                JoinField.builder()
                                        .leftJoinFieldId(a1Field.getId())
                                        .rightJoinFieldId(b1Field.getId())
                                        .build()))
                        .build()))
                .selects(singleton(Design.Populated.select()
                        .outputFieldId(cField.getId())
                        .select("A")
                        .build()))
                .build());

        PipelineStepTest pipelineStepTest = pipelineStepTestJpaRepository.save(Design.Populated.pipelineStepTest()
                .pipelineStepId(pipelineStep.getId())
                .expectedData(emptySet())
                .inputData(emptySet())
                .actualData(emptySet())
                .build());

        InputDataRow inputDataRow = pipelineStepTestRowJpaRepository.save(Design.Populated.inputDataRow()
                .pipelineStepTestId(pipelineStepTest.getId())
                .schemaId(a.getId())
                .cells(singleton(PipelineStepTestCell.builder()
                        .fieldId(bField.getId())
                        .data("HELLO")
                        .build()))
                .build());

        entityManager.flush();
        entityManager.clear();

        fieldDependencyAdapter.deleteForField(bField);

        entityManager.flush();
        entityManager.clear();

        PipelineStep retrievedStep = pipelineStepRepository.findById(pipelineStep.getId()).get();

        assertThat(((PipelineJoinStep) retrievedStep).getJoins())
                .isEmpty();

        PipelineStepTestRow retrievedRow = pipelineStepTestRowJpaRepository.getOne(inputDataRow.getId());
        assertThat(retrievedRow.getCells())
                .isEmpty();
    }

    @Test
    public void deleteForField_FieldHasPipelineJoinLeftField_DeletesJoinsAndTestCellData() {

        ProductConfig productConfig = productConfigJpaRepository.save(Design.Populated.productConfig().build());

        Schema a =
                schemaRepository.save(Design.Populated.emptySchema("A").productId(productConfig.getId()).build());
        StringField aField =
                fieldRepository.save(DesignField.Populated.stringField("A").schemaId(a.getId()).build());
        StringField a1Field =
                fieldRepository.save(DesignField.Populated.stringField("A1").schemaId(a.getId()).build());

        Schema b =
                schemaRepository.save(Design.Populated.emptySchema("B").productId(productConfig.getId()).build());
        StringField bField =
                fieldRepository.save(DesignField.Populated.stringField("B").schemaId(b.getId()).build());
        StringField b1Field =
                fieldRepository.save(DesignField.Populated.stringField("B1").schemaId(b.getId()).build());

        Schema c = schemaRepository.save(Design.Populated.emptySchema("C").productId(productConfig.getId()).build());
        StringField cField = fieldRepository.save(DesignField.Populated.stringField("C").schemaId(c.getId()).build());

        Pipeline pipeline = pipelineRepository.save(Design.Populated.pipeline()
                .productId(productConfig.getId())
                .build());

        PipelineJoinStep pipelineStep = pipelineStepRepository.save(Design.Populated.pipelineJoinStep()
                .schemaOutId(c.getId())
                .pipelineId(pipeline.getId())
                .joins(singleton(Design.Populated.join()
                        .leftSchemaId(a.getId())
                        .rightSchemaId(b.getId())
                        .joinFields(newHashSet(
                                JoinField.builder()
                                        .leftJoinFieldId(aField.getId())
                                        .rightJoinFieldId(bField.getId())
                                        .build(),
                                JoinField.builder()
                                        .leftJoinFieldId(a1Field.getId())
                                        .rightJoinFieldId(b1Field.getId())
                                        .build()))
                        .build()))
                .selects(singleton(Design.Populated.select()
                        .outputFieldId(cField.getId())
                        .select("A")
                        .build()))
                .build());

        PipelineStepTest pipelineStepTest = pipelineStepTestJpaRepository.save(Design.Populated.pipelineStepTest()
                .pipelineStepId(pipelineStep.getId())
                .expectedData(emptySet())
                .inputData(emptySet())
                .actualData(emptySet())
                .build());

        InputDataRow inputDataRow = pipelineStepTestRowJpaRepository.save(Design.Populated.inputDataRow()
                .pipelineStepTestId(pipelineStepTest.getId())
                .schemaId(a.getId())
                .cells(singleton(PipelineStepTestCell.builder()
                        .fieldId(aField.getId())
                        .data("HELLO")
                        .build()))
                .build());

        entityManager.flush();
        entityManager.clear();

        fieldDependencyAdapter.deleteForField(aField);

        entityManager.flush();
        entityManager.clear();

        PipelineStep retrievedStep = pipelineStepRepository.findById(pipelineStep.getId()).get();

        assertThat(((PipelineJoinStep) retrievedStep).getJoins())
                .isEmpty();

        PipelineStepTestRow retrievedRow = pipelineStepTestRowJpaRepository.getOne(inputDataRow.getId());
        assertThat(retrievedRow.getCells())
                .isEmpty();
    }
}
