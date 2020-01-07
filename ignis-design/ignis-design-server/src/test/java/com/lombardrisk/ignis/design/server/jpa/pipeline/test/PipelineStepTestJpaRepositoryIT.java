package com.lombardrisk.ignis.design.server.jpa.pipeline.test;

import com.lombardrisk.ignis.client.design.pipeline.test.StepTestStatus;
import com.lombardrisk.ignis.client.design.pipeline.test.view.StepTestView;
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
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Sets.newLinkedHashSet;
import static com.lombardrisk.ignis.design.field.DesignField.Populated.stringField;
import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@RunWith(SpringRunner.class)
@DesignStudioTestConfig
public class PipelineStepTestJpaRepositoryIT {

    @Autowired
    private FieldJpaRepository fieldJpaRepository;

    @Autowired
    private PipelineStepTestJpaRepository pipelineStepTestJpaRepository;

    @Autowired
    private ActualDataRowJpaRepository actualDataRowJpaRepository;

    @Autowired
    private InputDataRowJpaRepository inputDataRowJpaRepository;

    @Autowired
    private ExpectedDataRowJpaRepository expectedDataRowJpaRepository;

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

    @Rule
    public JUnitSoftAssertions soft = new JUnitSoftAssertions();

    @Test
    public void create_retrieve() {
        PipelineStepTest save = pipelineStepTestJpaRepository
                .save(
                        Design.Populated.pipelineStepTest()
                                .name("test")
                                .description("description")
                                .pipelineStepId(176757L)
                                .build());

        PipelineStepTest retrieved = pipelineStepTestJpaRepository
                .findById(save.getId())
                .get();

        soft.assertThat(retrieved.getName())
                .isEqualTo("test");
        soft.assertThat(retrieved.getDescription())
                .isEqualTo("description");
        soft.assertThat(retrieved.getPipelineStepId())
                .isEqualTo(176757L);
    }

    @Test
    public void saveDataRows_InputExpectedActual_ReturnsAllTestData() {
        ProductConfig product = productConfigJpaRepository.save(Design.Populated.productConfig().build());

        Schema schemaIn = schemaJpaRepository.save(Design.Populated.schema()
                .productId(product.getId())
                .displayName("table1")
                .physicalTableName("tb1")
                .fields(newHashSet())
                .validationRules(emptySet())
                .build());

        StringField inField =
                fieldJpaRepository.save(stringField("NAME").schemaId(schemaIn.getId()).build());

        Schema schemaOut = schemaJpaRepository.save(Design.Populated.schema()
                .productId(product.getId())
                .displayName("table2")
                .physicalTableName("tb2")
                .validationRules(emptySet())
                .fields(emptySet())
                .build());

        StringField outField =
                fieldJpaRepository.save(stringField("OTHER_NAME").schemaId(schemaOut.getId()).build());

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
                .save(Design.Populated.pipelineStepTest()
                        .pipelineStepId(pipelineMapStep.getId())
                        .build());

        InputDataRow inputDataRow = inputDataRowJpaRepository.save(
                Design.Populated.inputDataRow()
                        .pipelineStepTestId(saveStepTest.getId())
                        .schemaId(schemaIn.getId())
                        .cells(newHashSet(
                                PipelineStepTestCell.builder()
                                        .fieldId(inField.getId())
                                        .data("HOMER")
                                        .build()))
                        .build());

        ExpectedDataRow expected = expectedDataRowJpaRepository.save(
                Design.Populated.expectedDataRow()
                        .pipelineStepTestId(saveStepTest.getId())
                        .schemaId(schemaOut.getId())
                        .cells(newHashSet(
                                PipelineStepTestCell.builder()
                                        .fieldId(outField.getId())
                                        .data("HOMER SIMPSON")
                                        .build()))
                        .build());

        ActualDataRow actualDataRow = actualDataRowJpaRepository.save(
                Design.Populated.actualDataRow()
                        .pipelineStepTestId(saveStepTest.getId())
                        .schemaId(schemaOut.getId())
                        .cells(newHashSet(
                                PipelineStepTestCell.builder()
                                        .fieldId(outField.getId())
                                        .data("HOMER JAY")
                                        .build()))
                        .build());

        entityManager.flush();
        entityManager.clear();

        PipelineStepTest retrieved = pipelineStepTestJpaRepository
                .findById(saveStepTest.getId())
                .get();

        assertThat(retrieved.getInputData())
                .extracting(PipelineStepTestRow::getId)
                .containsOnly(inputDataRow.getId());
        assertThat(retrieved.getActualData())
                .extracting(PipelineStepTestRow::getId)
                .containsOnly(actualDataRow.getId());
        assertThat(retrieved.getExpectedData())
                .extracting(PipelineStepTestRow::getId)
                .containsOnly(expected.getId());

        soft.assertThat(retrieved.getInputData().iterator().next().getCells())
                .extracting(PipelineStepTestCell::getData)
                .containsOnly("HOMER");
        soft.assertThat(retrieved.getExpectedData().iterator().next().getCells())
                .extracting(PipelineStepTestCell::getData)
                .containsOnly("HOMER SIMPSON");
        soft.assertThat(retrieved.getActualData().iterator().next().getCells())
                .extracting(PipelineStepTestCell::getData)
                .containsOnly("HOMER JAY");
    }

    @Test
    public void findByPipelineId_ReturnsAllTestsForAllStepsInPipeline() {
        ProductConfig product = productConfigJpaRepository.save(Design.Populated.productConfig().build());

        Schema schemaIn = schemaJpaRepository.save(Design.Populated.schema("IN")
                .productId(product.getId())
                .validationRules(emptySet())
                .fields(emptySet())
                .build());

        Schema schemaOut = schemaJpaRepository.save(Design.Populated.schema("OUT")
                .productId(product.getId())
                .validationRules(emptySet())
                .fields(emptySet())
                .build());

        Pipeline createdPipeline = pipelineJpaRepository.save(Design.Populated.pipeline()
                .productId(product.getId())
                .build());

        PipelineMapStep stepOne = pipelineStepJpaRepository.save(
                Design.Populated.pipelineMapStep()
                        .name("StepOne")
                        .pipelineId(createdPipeline.getId())
                        .schemaInId(schemaIn.getId())
                        .schemaOutId(schemaOut.getId())
                        .selects(newLinkedHashSet())
                        .build());

        PipelineMapStep stepTwo = pipelineStepJpaRepository.save(
                Design.Populated.pipelineMapStep()
                        .name("StepTwo")
                        .pipelineId(createdPipeline.getId())
                        .schemaInId(schemaIn.getId())
                        .schemaOutId(schemaOut.getId())
                        .selects(newLinkedHashSet())
                        .build());

        PipelineStepTest stepOneTest = pipelineStepTestJpaRepository
                .save(Design.Populated.pipelineStepTest()
                        .pipelineStepId(stepOne.getId())
                        .build());

        PipelineStepTest stepTwoTest = pipelineStepTestJpaRepository
                .save(Design.Populated.pipelineStepTest()
                        .pipelineStepId(stepTwo.getId())
                        .build());

        List<PipelineStepTest> stepsForPipeline =
                pipelineStepTestJpaRepository.findAllByPipelineId(createdPipeline.getId());

        assertThat(stepsForPipeline)
                .containsOnly(stepOneTest, stepTwoTest);
    }

    @Test
    public void findByPipelineStepId_ReturnsAllTestsForStep() {
        ProductConfig product = productConfigJpaRepository.save(Design.Populated.productConfig().build());

        Schema schemaIn = schemaJpaRepository.save(Design.Populated.schema("IN")
                .productId(product.getId())
                .validationRules(emptySet())
                .fields(emptySet())
                .build());

        Schema schemaOut = schemaJpaRepository.save(Design.Populated.schema("OUT")
                .productId(product.getId())
                .validationRules(emptySet())
                .fields(emptySet())
                .build());

        Pipeline createdPipeline = pipelineJpaRepository.save(Design.Populated.pipeline()
                .productId(product.getId())
                .build());

        PipelineMapStep stepOne = pipelineStepJpaRepository.save(
                Design.Populated.pipelineMapStep()
                        .name("StepOne")
                        .pipelineId(createdPipeline.getId())
                        .schemaInId(schemaIn.getId())
                        .schemaOutId(schemaOut.getId())
                        .selects(newLinkedHashSet())
                        .build());

        PipelineMapStep stepTwo = pipelineStepJpaRepository.save(
                Design.Populated.pipelineMapStep()
                        .name("StepTwo")
                        .pipelineId(createdPipeline.getId())
                        .schemaInId(schemaIn.getId())
                        .schemaOutId(schemaOut.getId())
                        .selects(newLinkedHashSet())
                        .build());

        PipelineStepTest stepOneTest = pipelineStepTestJpaRepository
                .save(Design.Populated.pipelineStepTest()
                        .pipelineStepId(stepOne.getId())
                        .build());

        PipelineStepTest stepTwoTest = pipelineStepTestJpaRepository
                .save(Design.Populated.pipelineStepTest()
                        .pipelineStepId(stepTwo.getId())
                        .build());

        List<PipelineStepTest> stepsForPipelineStep =
                pipelineStepTestJpaRepository.findAllByPipelineStepId(stepOne.getId());

        assertThat(stepsForPipelineStep)
                .containsOnly(stepOneTest);
    }

    @Test
    public void findStepTestViewById() {
        ProductConfig product = productConfigJpaRepository.save(Design.Populated.productConfig().build());

        Schema schemaIn = schemaJpaRepository.save(Design.Populated.schema("IN")
                .productId(product.getId())
                .validationRules(emptySet())
                .fields(emptySet())
                .build());

        Schema schemaOut = schemaJpaRepository.save(Design.Populated.schema("OUT")
                .productId(product.getId())
                .validationRules(emptySet())
                .fields(emptySet())
                .build());

        Pipeline createdPipeline = pipelineJpaRepository.save(Design.Populated.pipeline()
                .productId(product.getId())
                .build());

        PipelineMapStep stepOne = pipelineStepJpaRepository.save(
                Design.Populated.pipelineMapStep()
                        .name("StepOne")
                        .pipelineId(createdPipeline.getId())
                        .schemaInId(schemaIn.getId())
                        .schemaOutId(schemaOut.getId())
                        .selects(newLinkedHashSet())
                        .build());

        PipelineMapStep stepTwo = pipelineStepJpaRepository.save(
                Design.Populated.pipelineMapStep()
                        .name("StepTwo")
                        .pipelineId(createdPipeline.getId())
                        .schemaInId(schemaIn.getId())
                        .schemaOutId(schemaOut.getId())
                        .selects(newLinkedHashSet())
                        .build());

        PipelineStepTest stepOneTest = pipelineStepTestJpaRepository
                .save(Design.Populated.pipelineStepTest()
                        .name("test one name")
                        .description("test one description")
                        .testReferenceDate(LocalDate.of(2019, 1, 31))
                        .pipelineStepId(stepOne.getId())
                        .pipelineStepStatus(StepTestStatus.PASS)
                        .build());

        PipelineStepTest stepTwoTest = pipelineStepTestJpaRepository
                .save(Design.Populated.pipelineStepTest()
                        .name("test two name")
                        .description("test two description")
                        .testReferenceDate(LocalDate.of(2019, 12, 31))
                        .pipelineStepId(stepTwo.getId())
                        .pipelineStepStatus(StepTestStatus.FAIL)
                        .build());

        Optional<StepTestView> testDto1 =
                pipelineStepTestJpaRepository.findStepTestViewById(stepOneTest.getId());

        soft.assertThat(testDto1)
                .isPresent();
        soft.assertThat(testDto1.get().getId())
                .isEqualTo(stepOneTest.getId());
        soft.assertThat(testDto1.get().getName())
                .isEqualTo("test one name");
        soft.assertThat(testDto1.get().getDescription())
                .isEqualTo("test one description");
        soft.assertThat(testDto1.get().getTestReferenceDate())
                .isEqualTo(LocalDate.of(2019, 1, 31));
        soft.assertThat(testDto1.get().getPipelineId())
                .isEqualTo(createdPipeline.getId());
        soft.assertThat(testDto1.get().getPipelineStepId())
                .isEqualTo(stepOne.getId());
        soft.assertThat(testDto1.get().getStatus())
                .isEqualTo(StepTestStatus.PASS);

        Optional<StepTestView> testDto2 =
                pipelineStepTestJpaRepository.findStepTestViewById(stepTwoTest.getId());

        soft.assertThat(testDto2)
                .isPresent();
        soft.assertThat(testDto2.get().getId())
                .isEqualTo(stepTwoTest.getId());
        soft.assertThat(testDto2.get().getName())
                .isEqualTo("test two name");
        soft.assertThat(testDto2.get().getDescription())
                .isEqualTo("test two description");
        soft.assertThat(testDto2.get().getTestReferenceDate())
                .isEqualTo(LocalDate.of(2019, 12, 31));
        soft.assertThat(testDto2.get().getPipelineId())
                .isEqualTo(createdPipeline.getId());
        soft.assertThat(testDto2.get().getPipelineStepId())
                .isEqualTo(stepTwo.getId());
        soft.assertThat(testDto2.get().getStatus())
                .isEqualTo(StepTestStatus.FAIL);

        soft.assertThat(pipelineStepTestJpaRepository.findStepTestViewById(1234L))
                .isEmpty();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void findStepTestViewsByPipelineStepId() {
        ProductConfig product = productConfigJpaRepository.save(Design.Populated.productConfig().build());

        Schema schemaIn = schemaJpaRepository.save(Design.Populated.schema("IN")
                .productId(product.getId())
                .validationRules(emptySet())
                .fields(emptySet())
                .build());

        Schema schemaOut = schemaJpaRepository.save(Design.Populated.schema("OUT")
                .productId(product.getId())
                .validationRules(emptySet())
                .fields(emptySet())
                .build());

        Pipeline createdPipeline = pipelineJpaRepository.save(Design.Populated.pipeline()
                .productId(product.getId())
                .build());

        PipelineMapStep stepOne = pipelineStepJpaRepository.save(
                Design.Populated.pipelineMapStep()
                        .name("StepOne")
                        .pipelineId(createdPipeline.getId())
                        .schemaInId(schemaIn.getId())
                        .schemaOutId(schemaOut.getId())
                        .selects(newLinkedHashSet())
                        .build());

        PipelineMapStep stepTwo = pipelineStepJpaRepository.save(
                Design.Populated.pipelineMapStep()
                        .name("StepTwo")
                        .pipelineId(createdPipeline.getId())
                        .schemaInId(schemaIn.getId())
                        .schemaOutId(schemaOut.getId())
                        .selects(newLinkedHashSet())
                        .build());

        PipelineStepTest stepOneTestOne = pipelineStepTestJpaRepository
                .save(Design.Populated.pipelineStepTest()
                        .name("pipeline step 1 test 1")
                        .description("pipeline step 1 test 1 description")
                        .testReferenceDate(LocalDate.of(2019, 1, 31))
                        .pipelineStepId(stepOne.getId())
                        .pipelineStepStatus(StepTestStatus.PASS)
                        .build());

        PipelineStepTest stepOneTestTwo = pipelineStepTestJpaRepository
                .save(Design.Populated.pipelineStepTest()
                        .name("pipeline step 1 test 2")
                        .description("pipeline step 1 test 2 description")
                        .testReferenceDate(LocalDate.of(2019, 2, 1))
                        .pipelineStepId(stepOne.getId())
                        .pipelineStepStatus(StepTestStatus.PENDING)
                        .build());

        PipelineStepTest stepTwoTestOne = pipelineStepTestJpaRepository
                .save(Design.Populated.pipelineStepTest()
                        .name("pipeline step 2 test 1")
                        .description("pipeline step 2 test 1 description")
                        .testReferenceDate(LocalDate.of(2019, 12, 31))
                        .pipelineStepId(stepTwo.getId())
                        .pipelineStepStatus(StepTestStatus.FAIL)
                        .build());

        List<StepTestView> stepOneTests =
                pipelineStepTestJpaRepository.findStepTestViewsByPipelineStepId(stepOne.getId());

        soft.assertThat(stepOneTests)
                .extracting(
                        StepTestView::getId,
                        StepTestView::getName,
                        StepTestView::getDescription,
                        StepTestView::getTestReferenceDate,
                        StepTestView::getPipelineId,
                        StepTestView::getPipelineStepId,
                        StepTestView::getStatus)
                .containsExactly(
                        tuple(
                                stepOneTestOne.getId(),
                                "pipeline step 1 test 1",
                                "pipeline step 1 test 1 description",
                                LocalDate.of(2019, 1, 31),
                                createdPipeline.getId(),
                                stepOne.getId(),
                                StepTestStatus.PASS),
                        tuple(
                                stepOneTestTwo.getId(),
                                "pipeline step 1 test 2",
                                "pipeline step 1 test 2 description",
                                LocalDate.of(2019, 2, 1),
                                createdPipeline.getId(),
                                stepOne.getId(),
                                StepTestStatus.PENDING));

        List<StepTestView> stepTwoTests =
                pipelineStepTestJpaRepository.findStepTestViewsByPipelineStepId(stepTwo.getId());

        soft.assertThat(stepTwoTests)
                .extracting(
                        StepTestView::getId,
                        StepTestView::getName,
                        StepTestView::getDescription,
                        StepTestView::getTestReferenceDate,
                        StepTestView::getPipelineId,
                        StepTestView::getPipelineStepId,
                        StepTestView::getStatus)
                .containsExactly(
                        tuple(
                                stepTwoTestOne.getId(),
                                "pipeline step 2 test 1",
                                "pipeline step 2 test 1 description",
                                LocalDate.of(2019, 12, 31),
                                createdPipeline.getId(),
                                stepTwo.getId(),
                                StepTestStatus.FAIL));

        soft.assertThat(pipelineStepTestJpaRepository.findStepTestViewsByPipelineStepId(1234L))
                .isEmpty();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void findStepTestViewsByPipelineId() {
        ProductConfig product = productConfigJpaRepository.save(Design.Populated.productConfig().build());

        Schema schemaIn = schemaJpaRepository.save(Design.Populated.schema("IN")
                .productId(product.getId())
                .validationRules(emptySet())
                .fields(emptySet())
                .build());

        Schema schemaOut = schemaJpaRepository.save(Design.Populated.schema("OUT")
                .productId(product.getId())
                .validationRules(emptySet())
                .fields(emptySet())
                .build());

        Pipeline pipelineOne = pipelineJpaRepository.save(Design.Populated.pipeline()
                .productId(product.getId())
                .build());

        Pipeline pipelineTwo = pipelineJpaRepository.save(Design.Populated.pipeline()
                .productId(product.getId())
                .build());

        PipelineMapStep pipelineOneStep = pipelineStepJpaRepository.save(
                Design.Populated.pipelineMapStep()
                        .name("StepOne")
                        .pipelineId(pipelineOne.getId())
                        .schemaInId(schemaIn.getId())
                        .schemaOutId(schemaOut.getId())
                        .selects(newLinkedHashSet())
                        .build());

        PipelineMapStep pipelineTwoStep = pipelineStepJpaRepository.save(
                Design.Populated.pipelineMapStep()
                        .name("StepTwo")
                        .pipelineId(pipelineTwo.getId())
                        .schemaInId(schemaIn.getId())
                        .schemaOutId(schemaOut.getId())
                        .selects(newLinkedHashSet())
                        .build());

        PipelineStepTest pipelineOneStepTest = pipelineStepTestJpaRepository
                .save(Design.Populated.pipelineStepTest()
                        .name("pipeline 1 step 1 test")
                        .description("pipeline 1 step 1 test description")
                        .testReferenceDate(LocalDate.of(2019, 1, 31))
                        .pipelineStepId(pipelineOneStep.getId())
                        .pipelineStepStatus(StepTestStatus.PASS)
                        .build());

        PipelineStepTest pipelineTwoStepTest = pipelineStepTestJpaRepository
                .save(Design.Populated.pipelineStepTest()
                        .name("pipeline 2 step 1 test")
                        .description("pipeline 2 step 1 test description")
                        .testReferenceDate(LocalDate.of(2019, 12, 31))
                        .pipelineStepId(pipelineTwoStep.getId())
                        .pipelineStepStatus(StepTestStatus.FAIL)
                        .build());

        List<StepTestView> stepOneTests =
                pipelineStepTestJpaRepository.findStepTestViewsByPipelineId(pipelineOne.getId());

        soft.assertThat(stepOneTests)
                .extracting(
                        StepTestView::getId,
                        StepTestView::getName,
                        StepTestView::getDescription,
                        StepTestView::getTestReferenceDate,
                        StepTestView::getPipelineId,
                        StepTestView::getPipelineStepId,
                        StepTestView::getStatus)
                .containsExactly(
                        tuple(
                                pipelineOneStepTest.getId(),
                                "pipeline 1 step 1 test",
                                "pipeline 1 step 1 test description",
                                LocalDate.of(2019, 1, 31),
                                pipelineOne.getId(),
                                pipelineOneStep.getId(),
                                StepTestStatus.PASS));

        List<StepTestView> stepTwoTests =
                pipelineStepTestJpaRepository.findStepTestViewsByPipelineId(pipelineTwo.getId());

        soft.assertThat(stepTwoTests)
                .extracting(
                        StepTestView::getId,
                        StepTestView::getName,
                        StepTestView::getDescription,
                        StepTestView::getTestReferenceDate,
                        StepTestView::getPipelineId,
                        StepTestView::getPipelineStepId,
                        StepTestView::getStatus)
                .containsExactly(
                        tuple(
                                pipelineTwoStepTest.getId(),
                                "pipeline 2 step 1 test",
                                "pipeline 2 step 1 test description",
                                LocalDate.of(2019, 12, 31),
                                pipelineTwo.getId(),
                                pipelineTwoStep.getId(),
                                StepTestStatus.FAIL));

        soft.assertThat(pipelineStepTestJpaRepository.findStepTestViewsByPipelineId(1234L))
                .isEmpty();
    }
}
