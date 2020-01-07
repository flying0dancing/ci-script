package com.lombardrisk.ignis.design.server.pipeline.test;

import com.lombardrisk.ignis.client.design.pipeline.test.StepTestStatus;
import com.lombardrisk.ignis.common.assertions.VavrAssert;
import com.lombardrisk.ignis.data.common.error.ErrorResponse;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.design.field.DesignField;
import com.lombardrisk.ignis.design.field.model.Field;
import com.lombardrisk.ignis.design.server.DesignStudioTestConfig;
import com.lombardrisk.ignis.design.server.fixtures.Design;
import com.lombardrisk.ignis.design.server.jpa.FieldJpaRepository;
import com.lombardrisk.ignis.design.server.jpa.ProductConfigJpaRepository;
import com.lombardrisk.ignis.design.server.jpa.SchemaJpaRepository;
import com.lombardrisk.ignis.design.server.pipeline.PipelineStepExecutor;
import com.lombardrisk.ignis.design.server.pipeline.PipelineStepSparkSqlExecutor;
import com.lombardrisk.ignis.design.server.pipeline.SparkUDFService;
import com.lombardrisk.ignis.design.server.pipeline.model.PipelineMapStep;
import com.lombardrisk.ignis.design.server.pipeline.model.select.Select;
import com.lombardrisk.ignis.design.server.pipeline.test.model.ExpectedDataRow;
import com.lombardrisk.ignis.design.server.pipeline.test.model.InputDataRow;
import com.lombardrisk.ignis.design.server.pipeline.test.model.PipelineStepTest;
import com.lombardrisk.ignis.design.server.pipeline.test.model.PipelineStepTestCell;
import com.lombardrisk.ignis.design.server.pipeline.test.model.PipelineStepTestResult;
import com.lombardrisk.ignis.design.server.productconfig.ProductConfig;
import com.lombardrisk.ignis.design.server.productconfig.schema.Schema;
import com.lombardrisk.ignis.design.server.productconfig.schema.SchemaService;
import com.lombardrisk.ignis.pipeline.step.common.MapStepExecutor;
import com.lombardrisk.ignis.pipeline.step.common.UnionStepExecutor;
import com.lombardrisk.ignis.pipeline.step.common.WindowStepExecutor;
import io.vavr.control.Validation;
import org.apache.spark.sql.SparkSession;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Sets.newLinkedHashSet;
import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@DesignStudioTestConfig
public class PipelineStepTestExecuteServiceIT {

    @Rule
    public JUnitSoftAssertions soft = new JUnitSoftAssertions();

    @Autowired
    private SparkSession sparkSession;
    @Autowired
    private SparkUDFService sparkUDFService;
    @Autowired
    private SchemaService schemaService;
    @Autowired
    private ProductConfigJpaRepository productConfigJpaRepository;
    @Autowired
    private SchemaJpaRepository schemaJpaRepository;
    @Autowired
    private FieldJpaRepository fieldJpaRepository;
    @Autowired
    private EntityManager entityManager;

    private Schema inputSchema;
    private Schema outputSchema;

    private PipelineStepTestExecuteService pipelineStepTestExecuteService;
    private Field firstName;
    private Field lastName;
    private Field fullName;
    private ProductConfig product;

    @Before
    public void setUp() {
        product = productConfigJpaRepository.save(Design.Populated.productConfig().build());

        inputSchema = schemaJpaRepository.save(Design.Populated.schema()
                .productId(product.getId()).displayName("customers in").physicalTableName("CUSTOMERS_IN").build());

        outputSchema = schemaJpaRepository.save(Design.Populated.schema()
                .productId(product.getId()).displayName("customers out").physicalTableName("CUSTOMERS_OUT").build());

        firstName = fieldJpaRepository.saveAndFlush(
                DesignField.Populated.stringField("FIRST_NAME").schemaId(inputSchema.getId()).build());
        lastName = fieldJpaRepository.saveAndFlush(
                DesignField.Populated.stringField("LAST_NAME").schemaId(inputSchema.getId()).build());
        fullName = fieldJpaRepository.saveAndFlush(
                DesignField.Populated.stringField("FULL_NAME").schemaId(outputSchema.getId()).build());

        entityManager.flush();
        entityManager.clear();

        PipelineStepSparkSqlExecutor pipelineStepSelectsExecutor = new PipelineStepSparkSqlExecutor(sparkSession);
        PipelineStepExecutor pipelineStepExecutor = new PipelineStepExecutor(
                sparkSession,
                new MapStepExecutor(),
                new WindowStepExecutor(),
                new UnionStepExecutor(),
                new PipelineStepSparkSqlExecutor(sparkSession));

        pipelineStepTestExecuteService =
                new PipelineStepTestExecuteService(
                        pipelineStepExecutor,
                        sparkUDFService,
                        schemaService);
    }

    @Test
    public void runTest_MapStep_ReturnsSuccessAndMatchingIds() {

        PipelineMapStep mapTransformation = PipelineMapStep.builder()
                .schemaInId(inputSchema.getId())
                .schemaOutId(outputSchema.getId())
                .selects(newHashSet(Select.builder()
                        .select("CONCAT(FIRST_NAME, \" \", LAST_NAME, \" \", get_reference_date())")
                        .outputFieldId(fullName.getId())
                        .build()))
                .build();

        PipelineStepTest pipelineStepTest = PipelineStepTest.builder()
                .name("Full name")
                .testReferenceDate(LocalDate.of(2019, 1, 1))
                .inputData(newHashSet(
                        InputDataRow.builder()
                                .schemaId(inputSchema.getId())
                                .cells(newLinkedHashSet(asList(
                                        PipelineStepTestCell.builder()
                                                .fieldId(firstName.getId()).data("HOMER").build(),
                                        PipelineStepTestCell.builder()
                                                .fieldId(lastName.getId()).data("SIMPSON").build())))
                                .build()))
                .expectedData(newHashSet(
                        ExpectedDataRow.builder()
                                .id(100L)
                                .schemaId(outputSchema.getId())
                                .cells(newHashSet(
                                        PipelineStepTestCell.builder()
                                                .fieldId(fullName.getId()).data("HOMER SIMPSON 2019-01-01").build()))
                                .build()))
                .build();

        PipelineStepTestResult pipelineStepTestResult =
                pipelineStepTestExecuteService.runTest(mapTransformation, pipelineStepTest).get();

        soft.assertThat(pipelineStepTestResult.getStatus())
                .isEqualTo(StepTestStatus.PASS);
        soft.assertThat(pipelineStepTestResult.getMatching())
                .containsOnly(100L);
    }

    @Test
    public void runTest_MapStepSelectsInDifferentOrder_ReturnsSuccessAndMatchingIds() {

        Schema salariesIn = schemaJpaRepository.save(Design.Populated.schema()
                .productId(product.getId())
                .displayName("Salaries")
                .physicalTableName("SALARIES_IN")
                .fields(emptySet())
                .build());

        Field firstName = fieldJpaRepository.saveAndFlush(
                DesignField.Populated.stringField("FIRST_NAME").schemaId(salariesIn.getId()).build());
        Field salary = fieldJpaRepository.saveAndFlush(
                DesignField.Populated.decimalField("Salary")
                        .schemaId(salariesIn.getId())
                        .scale(2)
                        .precision(11)
                        .build());

        Schema salariesOut = schemaJpaRepository.save(Design.Populated.schema()
                .productId(product.getId())
                .displayName("Salary Info")
                .physicalTableName("SALARY_INFO")
                .fields(emptySet())
                .build());

        Field firstNameOut = fieldJpaRepository.saveAndFlush(
                DesignField.Populated.stringField("FIRST_NAME").schemaId(salariesOut.getId()).build());
        Field salaryOut = fieldJpaRepository.saveAndFlush(
                DesignField.Populated.decimalField("Salary")
                        .schemaId(salariesOut.getId())
                        .scale(2)
                        .precision(11)
                        .build());
        Field salaryInK = fieldJpaRepository.saveAndFlush(
                DesignField.Populated.decimalField("SalaryInK")
                        .scale(2)
                        .precision(22)
                        .schemaId(salariesOut.getId()).build());

        entityManager.flush();
        entityManager.clear();

        PipelineMapStep mapTransformation = PipelineMapStep.builder()
                .schemaInId(salariesIn.getId())
                .schemaOutId(salariesOut.getId())
                .selects(newHashSet(
                        Select.builder()
                                .select("Salary")
                                .outputFieldId(salaryOut.getId())
                                .order(0L)
                                .build(),
                        Select.builder()
                                .select("round(Salary / 1000, 2)")
                                .outputFieldId(salaryInK.getId())
                                .order(1L)
                                .build(),
                        Select.builder()
                                .select("FIRST_NAME")
                                .outputFieldId(firstNameOut.getId())
                                .order(2L)
                                .build()))
                .build();

        PipelineStepTest pipelineStepTest = PipelineStepTest.builder()
                .name("Full name")
                .testReferenceDate(LocalDate.of(2019, 1, 1))
                .inputData(newHashSet(
                        InputDataRow.builder()
                                .schemaId(salariesIn.getId())
                                .cells(newLinkedHashSet(asList(
                                        PipelineStepTestCell.builder()
                                                .fieldId(firstName.getId()).data("HOMER").build(),
                                        PipelineStepTestCell.builder()
                                                .fieldId(salary.getId()).data("123456890.12").build())))
                                .build()))
                .expectedData(newHashSet(
                        ExpectedDataRow.builder()
                                .id(100L)
                                .schemaId(salariesOut.getId())
                                .cells(newHashSet(
                                        PipelineStepTestCell.builder()
                                                .fieldId(firstNameOut.getId()).data("HOMER").build(),
                                        PipelineStepTestCell.builder()
                                                .fieldId(salaryOut.getId()).data("123456890.12").build(),
                                        PipelineStepTestCell.builder()
                                                .fieldId(salaryInK.getId()).data("123456.89").build()))
                                .build()))
                .build();

        PipelineStepTestResult pipelineStepTestResult = VavrAssert.assertValid(
                pipelineStepTestExecuteService.runTest(mapTransformation, pipelineStepTest))
                .getResult();

        soft.assertThat(pipelineStepTestResult.getStatus())
                .isEqualTo(StepTestStatus.PASS);
        soft.assertThat(pipelineStepTestResult.getMatching())
                .containsOnly(100L);
        soft.assertThat(pipelineStepTestResult.getUnexpected())
                .isEmpty();
    }

    @Test
    public void runTest_MapStepWithNullValues_ReturnsSuccessAndMatchingIds() {

        PipelineMapStep mapTransformation = PipelineMapStep.builder()
                .schemaInId(inputSchema.getId())
                .schemaOutId(outputSchema.getId())
                .selects(newHashSet(Select.builder()
                        .select("CONCAT(FIRST_NAME, \" \", LAST_NAME)")
                        .outputFieldId(fullName.getId())
                        .build()))
                .build();

        PipelineStepTest pipelineStepTest = PipelineStepTest.builder()
                .name("Full name")
                .inputData(newHashSet(
                        InputDataRow.builder()
                                .schemaId(inputSchema.getId())
                                .cells(newLinkedHashSet(asList(
                                        PipelineStepTestCell.builder()
                                                .fieldId(firstName.getId()).data("").build(),
                                        PipelineStepTestCell.builder()
                                                .fieldId(lastName.getId()).data("Homer").build())))
                                .build()))
                .expectedData(newHashSet(
                        ExpectedDataRow.builder()
                                .id(100L)
                                .schemaId(outputSchema.getId())
                                .cells(newHashSet(
                                        PipelineStepTestCell.builder()
                                                .fieldId(fullName.getId()).data("").build()))
                                .build()))
                .build();

        PipelineStepTestResult pipelineStepTestResult =
                pipelineStepTestExecuteService.runTest(mapTransformation, pipelineStepTest).get();

        soft.assertThat(pipelineStepTestResult.getStatus())
                .isEqualTo(StepTestStatus.PASS);
        soft.assertThat(pipelineStepTestResult.getMatching())
                .containsOnly(100L);
    }

    @Test
    public void runTest_MapStepNotFound_ReturnsFailAndNotFoundIds() {
        PipelineMapStep mapTransformation = PipelineMapStep.builder()
                .schemaInId(inputSchema.getId())
                .schemaOutId(outputSchema.getId())
                .selects(newHashSet(Select.builder()
                        .select("CONCAT(FIRST_NAME, \" \", LAST_NAME)")
                        .outputFieldId(fullName.getId())
                        .build()))
                .build();

        PipelineStepTest pipelineStepTest = PipelineStepTest.builder()
                .name("Full name")
                .inputData(newHashSet(
                        InputDataRow.builder()
                                .schemaId(inputSchema.getId())
                                .cells(newLinkedHashSet(asList(
                                        PipelineStepTestCell.builder()
                                                .fieldId(firstName.getId()).data("HOMER").build(),
                                        PipelineStepTestCell.builder()
                                                .fieldId(lastName.getId()).data("SIMPSON").build())))
                                .build()))
                .expectedData(newHashSet(
                        ExpectedDataRow.builder()
                                .id(100L)
                                .schemaId(outputSchema.getId())
                                .cells(newHashSet(
                                        PipelineStepTestCell.builder()
                                                .fieldId(fullName.getId()).data("NED FLANDERS").build()))
                                .build(),
                        ExpectedDataRow.builder()
                                .id(101L)
                                .schemaId(outputSchema.getId())
                                .cells(newHashSet(
                                        PipelineStepTestCell.builder()
                                                .fieldId(fullName.getId()).data("HOMER SIMPSON").build()))
                                .build()))
                .build();

        PipelineStepTestResult pipelineStepTestResult =
                pipelineStepTestExecuteService.runTest(mapTransformation, pipelineStepTest).get();

        assertThat(pipelineStepTestResult.getStatus())
                .isEqualTo(StepTestStatus.FAIL);
        assertThat(pipelineStepTestResult.getMatching())
                .containsOnly(101L);
        assertThat(pipelineStepTestResult.getNotFound())
                .containsOnly(100L);
    }

    @Test
    public void runTest_MapStepUnexpected_ReturnsFailAndNotFoundIds() {
        PipelineMapStep mapTransformation = PipelineMapStep.builder()
                .id(100L)
                .schemaInId(inputSchema.getId())
                .schemaOutId(outputSchema.getId())
                .selects(newHashSet(Select.builder()
                        .select("CONCAT(FIRST_NAME, \" \", LAST_NAME)")
                        .outputFieldId(fullName.getId())
                        .build()))
                .build();

        PipelineStepTest pipelineStepTest = PipelineStepTest.builder()
                .name("Full name")
                .pipelineStepId(mapTransformation.getId())
                .inputData(newHashSet(
                        InputDataRow.builder()
                                .schemaId(inputSchema.getId())
                                .cells(newLinkedHashSet(asList(
                                        PipelineStepTestCell.builder()
                                                .fieldId(firstName.getId()).data("HOMER").build(),
                                        PipelineStepTestCell.builder()
                                                .fieldId(lastName.getId()).data("SIMPSON").build())))
                                .build()))
                .expectedData(Collections.emptySet())
                .build();

        PipelineStepTestResult pipelineStepTestResult =
                pipelineStepTestExecuteService.runTest(mapTransformation, pipelineStepTest).get();

        assertThat(pipelineStepTestResult.getStatus())
                .isEqualTo(StepTestStatus.FAIL);
        assertThat(pipelineStepTestResult.getMatching())
                .isEmpty();
        assertThat(pipelineStepTestResult.getNotFound())
                .isEmpty();
        assertThat(pipelineStepTestResult.getUnexpected())
                .containsExactlyInAnyOrder(
                        Collections.singletonMap("FULL_NAME", "HOMER SIMPSON"));
    }

    @Test
    public void runTest_MapStepNoInputAndExpectedValues_ReturnsSuccessAndMatchingIds() {

        PipelineMapStep mapTransformation = PipelineMapStep.builder()
                .schemaInId(inputSchema.getId())
                .schemaOutId(outputSchema.getId())
                .selects(newHashSet(Select.builder()
                        .select("CONCAT(FIRST_NAME, \" \", LAST_NAME)")
                        .outputFieldId(fullName.getId())
                        .build()))
                .build();

        PipelineStepTest pipelineStepTest = PipelineStepTest.builder()
                .name("Full name")
                .inputData(Collections.emptySet())
                .expectedData(Collections.emptySet())
                .build();

        PipelineStepTestResult pipelineStepTestResult =
                pipelineStepTestExecuteService.runTest(mapTransformation, pipelineStepTest).get();

        assertThat(pipelineStepTestResult.getStatus())
                .isEqualTo(StepTestStatus.PASS);
        assertThat(pipelineStepTestResult.getMatching())
                .isEmpty();
    }

    @Test
    public void runTest_MapStepIncorrectFieldNameInSelect_ReturnsSQLAnalysisError() {
        PipelineMapStep mapTransformation = PipelineMapStep.builder()
                .schemaInId(inputSchema.getId())
                .schemaOutId(outputSchema.getId())
                .selects(newHashSet(Select.builder()
                        .select("CONCAT(FIRST_NAME_WRONG, \" \", LAST_NAME, \" \", get_reference_date())")
                        .outputFieldId(fullName.getId())
                        .build()))
                .build();

        PipelineStepTest pipelineStepTest = PipelineStepTest.builder()
                .name("Full name")
                .testReferenceDate(LocalDate.of(2019, 1, 1))
                .inputData(newHashSet(
                        InputDataRow.builder()
                                .schemaId(inputSchema.getId())
                                .cells(newLinkedHashSet(asList(
                                        PipelineStepTestCell.builder()
                                                .fieldId(firstName.getId()).data("HOMER").build(),
                                        PipelineStepTestCell.builder()
                                                .fieldId(lastName.getId()).data("SIMPSON").build())))
                                .build()))
                .expectedData(newHashSet(
                        ExpectedDataRow.builder()
                                .id(100L)
                                .schemaId(outputSchema.getId())
                                .cells(newHashSet(
                                        PipelineStepTestCell.builder()
                                                .fieldId(fullName.getId()).data("HOMER SIMPSON 2019-01-01").build()))
                                .build()))
                .build();

        VavrAssert.assertCollectionFailure(pipelineStepTestExecuteService.runTest(mapTransformation, pipelineStepTest))
                .withOnlyFailures(ErrorResponse.valueOf(
                        "cannot resolve '`FIRST_NAME_WRONG`' given input columns: [ROW_KEY, FIRST_NAME, LAST_NAME]; line 1 pos 14",
                        "SQL_ANALYSIS_ERROR"));
    }

    @Test
    public void runTest_TransformationSchemasNotFound_ReturnsError() {
        PipelineMapStep mapTransformation = PipelineMapStep.builder()
                .schemaInId(inputSchema.getId())
                .schemaOutId(outputSchema.getId())
                .selects(newHashSet(Select.builder()
                        .select("CONCAT(FIRST_NAME, \" \", LAST_NAME, \" \", get_reference_date())")
                        .outputFieldId(fullName.getId())
                        .build()))
                .build();

        PipelineStepTest pipelineStepTest = PipelineStepTest.builder()
                .name("Full name")
                .testReferenceDate(LocalDate.of(2019, 1, 1))
                .inputData(newHashSet(
                        InputDataRow.builder()
                                .schemaId(123L)
                                .cells(emptySet()).build()))
                .expectedData(newHashSet(
                        ExpectedDataRow.builder()
                                .schemaId(456L)
                                .cells(emptySet())
                                .build()))
                .build();

        Validation<List<ErrorResponse>, PipelineStepTestResult> pipelineStepTestResult =
                pipelineStepTestExecuteService.runTest(mapTransformation, pipelineStepTest);

        VavrAssert.assertFailed(pipelineStepTestResult)
                .withFailure(asList(
                        CRUDFailure.notFoundIds("Schema", 123L).toErrorResponse(),
                        CRUDFailure.notFoundIds("Schema", 456L).toErrorResponse()));
    }

    @Test
    public void runTest_FieldsNotFoundOnSchemas_ReturnsError() {

        PipelineMapStep mapTransformation = PipelineMapStep.builder()
                .schemaInId(inputSchema.getId())
                .schemaOutId(outputSchema.getId())
                .selects(newHashSet(Select.builder()
                        .select("CONCAT(FIRST_NAME, \" \", LAST_NAME, \" \", get_reference_date())")
                        .outputFieldId(fullName.getId())
                        .build()))
                .build();

        PipelineStepTest pipelineStepTest = PipelineStepTest.builder()
                .name("Full name")
                .testReferenceDate(LocalDate.of(2019, 1, 1))
                .inputData(newHashSet(
                        InputDataRow.builder()
                                .schemaId(inputSchema.getId())
                                .cells(newLinkedHashSet(asList(
                                        PipelineStepTestCell.builder()
                                                .fieldId(firstName.getId()).data("HOMER").build(),
                                        PipelineStepTestCell.builder()
                                                .fieldId(1234L).data("invalid field id").build())))
                                .build()))
                .expectedData(newHashSet(
                        ExpectedDataRow.builder()
                                .schemaId(outputSchema.getId())
                                .cells(singleton(
                                        PipelineStepTestCell.builder()
                                                .fieldId(5678L).data("invalid field id").build()))
                                .build()))
                .build();

        Validation<List<ErrorResponse>, PipelineStepTestResult> pipelineStepTestResult =
                pipelineStepTestExecuteService.runTest(mapTransformation, pipelineStepTest);

        VavrAssert.assertFailed(pipelineStepTestResult)
                .withFailure(asList(
                        CRUDFailure.notFoundIds("Field", 1234L).toErrorResponse(),
                        CRUDFailure.notFoundIds("Field", 5678L).toErrorResponse()));
    }
}
