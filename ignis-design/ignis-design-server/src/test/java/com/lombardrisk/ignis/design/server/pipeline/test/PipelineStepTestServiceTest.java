package com.lombardrisk.ignis.design.server.pipeline.test;

import com.lombardrisk.ignis.client.design.pipeline.PipelineStepView;
import com.lombardrisk.ignis.client.design.pipeline.test.StepTestStatus;
import com.lombardrisk.ignis.client.design.pipeline.test.request.CreateStepTestRequest;
import com.lombardrisk.ignis.client.design.pipeline.test.request.UpdateStepTestRequest;
import com.lombardrisk.ignis.client.design.pipeline.test.view.StepRowInputDataView;
import com.lombardrisk.ignis.client.design.pipeline.test.view.StepRowOutputDataView;
import com.lombardrisk.ignis.client.design.pipeline.test.view.StepTestCellView;
import com.lombardrisk.ignis.client.design.pipeline.test.view.StepTestRowView;
import com.lombardrisk.ignis.client.design.pipeline.test.view.StepTestRowView.CellIdAndField;
import com.lombardrisk.ignis.client.design.pipeline.test.view.StepTestView;
import com.lombardrisk.ignis.client.design.pipeline.test.view.TestRunResultView;
import com.lombardrisk.ignis.client.design.schema.field.FieldDto;
import com.lombardrisk.ignis.common.assertions.VavrAssert;
import com.lombardrisk.ignis.data.common.Identifiable;
import com.lombardrisk.ignis.data.common.error.ErrorResponse;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.design.server.fixtures.Design;
import com.lombardrisk.ignis.design.server.pipeline.PipelineService;
import com.lombardrisk.ignis.design.server.pipeline.PipelineStepExecutor;
import com.lombardrisk.ignis.design.server.pipeline.PipelineStepService;
import com.lombardrisk.ignis.design.server.pipeline.fixture.ExamplePipelineHelper;
import com.lombardrisk.ignis.design.server.pipeline.fixture.PipelineServiceFixtureFactory;
import com.lombardrisk.ignis.design.server.pipeline.fixture.PipelineStepTestRowRepositoryFixture;
import com.lombardrisk.ignis.design.server.productconfig.ProductConfigService;
import com.lombardrisk.ignis.design.server.productconfig.fixture.ProductServiceFixtureFactory;
import com.lombardrisk.ignis.design.server.productconfig.fixture.SchemaServiceFixtureFactory;
import com.lombardrisk.ignis.design.server.productconfig.schema.Schema;
import com.lombardrisk.ignis.design.server.productconfig.schema.SchemaService;
import io.vavr.control.Validation;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import scala.collection.Seq;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.lombardrisk.ignis.client.design.pipeline.test.StepTestStatus.FAIL;
import static com.lombardrisk.ignis.client.design.pipeline.test.StepTestStatus.PASS;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PipelineStepTestServiceTest {

    @Rule
    public JUnitSoftAssertions soft = new JUnitSoftAssertions();

    private SchemaService schemaService;
    private PipelineService pipelineService;
    private PipelineStepService pipelineStepService;
    private PipelineStepTestService pipelineStepTestService;
    private PipelineStepTestRowService pipelineStepTestRowService;
    private ExamplePipelineHelper examplePipelineHelper;
    private PipelineStepExecutor mockSparkSqlExecutor;

    private PipelineStepTestRowRepositoryFixture pipelineStepTestRowRepository;

    private Integer csvImportMaxLines = 100;

    @Before
    public void setUp() {
        SchemaServiceFixtureFactory schemaServiceFixtureFactory = SchemaServiceFixtureFactory.create();
        ProductConfigService productConfigService =
                ProductServiceFixtureFactory.create(schemaServiceFixtureFactory, csvImportMaxLines)
                .getProductService();
        schemaService = schemaServiceFixtureFactory.getSchemaService();

        PipelineServiceFixtureFactory pipelineServiceFixtureFactory =
                PipelineServiceFixtureFactory.create(schemaServiceFixtureFactory, csvImportMaxLines);
        PipelineServiceFixtureFactory.Exports exports = pipelineServiceFixtureFactory.getExports();
        PipelineServiceFixtureFactory.TestHelpers testHelpers = pipelineServiceFixtureFactory.getTestHelpers();

        pipelineService = exports.getPipelineService();
        pipelineStepService = exports.getPipelineStepService();
        pipelineStepTestService = exports.getPipelineStepTestService();
        pipelineStepTestRowService = exports.getPipelineStepTestRowService();

        examplePipelineHelper = testHelpers.getExamplePipelineHelper();
        mockSparkSqlExecutor = testHelpers.getPipelineStepExecutor();

        pipelineStepTestRowRepository =
                pipelineServiceFixtureFactory.getDependencies().getPipelineStepTestRowRepository();

        Dataset dataset = mock(Dataset.class);
        when(dataset.collectAsList()).thenReturn(emptyList());
        when(mockSparkSqlExecutor.executeFullTransformation(any(), any()))
                .thenReturn(Validation.valid(dataset));
    }

    @Test
    public void create_PipelineStepFound_ReturnsView() {
        Long pipelineId = VavrAssert.assertValid(
                pipelineService.saveNewPipeline(Design.Populated.createPipelineRequest()
                        .name("PipelineOne")
                        .build()))
                .getResult()
                .getId();

        Schema a = schemaService.createNew(Design.Populated.schema("a").build());
        Schema b = schemaService.createNew(Design.Populated.schema("b").build());

        PipelineStepView pipelineStepView = VavrAssert.assertValid(
                pipelineStepService.savePipelineStep(pipelineId, Design.Populated.pipelineMapStepRequest()
                        .schemaInId(a.getId())
                        .schemaOutId(b.getId())
                        .build()))
                .getResult();

        Long testId = VavrAssert.assertValid(
                pipelineStepTestService.create(CreateStepTestRequest.builder()
                        .name("hello")
                        .description("Just testing")
                        .testReferenceDate(LocalDate.of(2019, 1, 1))
                        .pipelineStepId(pipelineStepView.getId())
                        .build()))
                .getResult()
                .getId();

        StepTestView stepTestView = VavrAssert.assertValid(pipelineStepTestService.findById(testId))
                .getResult();

        soft.assertThat(stepTestView.getId())
                .isNotNull();
        soft.assertThat(stepTestView.getName())
                .isEqualTo("hello");
        soft.assertThat(stepTestView.getDescription())
                .isEqualTo("Just testing");
        soft.assertThat(stepTestView.getTestReferenceDate())
                .isEqualTo(LocalDate.of(2019, 1, 1));
        soft.assertThat(stepTestView.getStatus())
                .isEqualTo(StepTestStatus.PENDING);
        soft.assertThat(stepTestView.getPipelineStepId())
                .isEqualTo(pipelineStepView.getId());
        soft.assertThat(stepTestView.getPipelineId())
                .isEqualTo(pipelineId);
    }

    @Test
    public void findAllTestsByStepId_ReturnsStepTestViews() {
        Long pipelineId = VavrAssert.assertValid(
                pipelineService.saveNewPipeline(Design.Populated.createPipelineRequest()
                        .name("PipelineOne")
                        .build()))
                .getResult()
                .getId();

        Schema a = schemaService.createNew(Design.Populated.schema("a").build());
        Schema b = schemaService.createNew(Design.Populated.schema("b").build());

        PipelineStepView pipelineStepView = VavrAssert.assertValid(
                pipelineStepService.savePipelineStep(pipelineId, Design.Populated.pipelineMapStepRequest()
                        .schemaInId(a.getId())
                        .schemaOutId(b.getId())
                        .build()))
                .getResult();

        Identifiable stepTestViewId = VavrAssert.assertValid(
                pipelineStepTestService.create(CreateStepTestRequest.builder()
                        .name("hello")
                        .description("Just testing")
                        .testReferenceDate(LocalDate.of(2019, 1, 1))
                        .pipelineStepId(pipelineStepView.getId())
                        .build()))
                .getResult();

        List<StepTestView> allTests = pipelineStepTestService
                .findAllTestsByStepId(pipelineStepView.getId());

        assertThat(allTests).hasSize(1);

        StepTestView returnedStepTestView = allTests.get(0);
        soft.assertThat(returnedStepTestView.getId())
                .isEqualTo(stepTestViewId.getId());
        soft.assertThat(returnedStepTestView.getName())
                .isEqualTo("hello");
        soft.assertThat(returnedStepTestView.getDescription())
                .isEqualTo("Just testing");
        soft.assertThat(returnedStepTestView.getTestReferenceDate())
                .isEqualTo(LocalDate.of(2019, 1, 1));
        soft.assertThat(returnedStepTestView.getPipelineStepId())
                .isEqualTo(pipelineStepView.getId());
        soft.assertThat(returnedStepTestView.getPipelineId())
                .isEqualTo(pipelineId);
    }

    @Test
    public void findAllTestsByPipelineId_ReturnsStepTestViews() {
        Long pipelineOne = VavrAssert.assertValid(
                pipelineService.saveNewPipeline(Design.Populated.createPipelineRequest()
                        .name("PipelineOne")
                        .build()))
                .getResult()
                .getId();

        Long pipelineTwo = VavrAssert.assertValid(
                pipelineService.saveNewPipeline(Design.Populated.createPipelineRequest()
                        .name("PipelineTwo")
                        .build()))
                .getResult()
                .getId();

        Schema a = schemaService.createNew(Design.Populated.schema("a").build());
        Schema b = schemaService.createNew(Design.Populated.schema("b").build());

        PipelineStepView stepOne = VavrAssert.assertValid(
                pipelineStepService.savePipelineStep(pipelineOne, Design.Populated.pipelineMapStepRequest()
                        .name("Step one")
                        .schemaInId(a.getId())
                        .schemaOutId(b.getId())
                        .build()))
                .getResult();

        PipelineStepView stepTwo = VavrAssert.assertValid(
                pipelineStepService.savePipelineStep(pipelineOne, Design.Populated.pipelineMapStepRequest()
                        .name("Step Two")
                        .schemaInId(a.getId())
                        .schemaOutId(b.getId())
                        .build()))
                .getResult();

        PipelineStepView stepForPipelineTwo = VavrAssert.assertValid(
                pipelineStepService.savePipelineStep(pipelineTwo, Design.Populated.pipelineMapStepRequest()
                        .name("Step Two")
                        .schemaInId(a.getId())
                        .schemaOutId(b.getId())
                        .build()))
                .getResult();

        Identifiable stepTestOne = VavrAssert.assertValid(
                pipelineStepTestService.create(CreateStepTestRequest.builder()
                        .name("hello")
                        .description("Just testing")
                        .testReferenceDate(LocalDate.of(2019, 1, 1))
                        .pipelineStepId(stepOne.getId())
                        .build()))
                .getResult();

        Identifiable stepTestTwo = VavrAssert.assertValid(
                pipelineStepTestService.create(CreateStepTestRequest.builder()
                        .name("hello")
                        .description("Just testing")
                        .testReferenceDate(LocalDate.of(2019, 1, 1))
                        .pipelineStepId(stepTwo.getId())
                        .build()))
                .getResult();

        Identifiable stepTestPipelineTwo = VavrAssert.assertValid(
                pipelineStepTestService.create(CreateStepTestRequest.builder()
                        .name("hello")
                        .description("Just testing")
                        .testReferenceDate(LocalDate.of(2019, 1, 1))
                        .pipelineStepId(stepForPipelineTwo.getId())
                        .build()))
                .getResult();

        List<StepTestView> allTests = pipelineStepTestService
                .findAllTestsByPipelineId(pipelineOne);

        assertThat(allTests)
                .extracting(StepTestView::getId)
                .containsOnly(stepTestOne.getId(), stepTestTwo.getId());
    }

    @Test
    public void getTestInputRows_TestNotFound_ReturnsCrudFailure() {
        VavrAssert.assertFailed(pipelineStepTestService.getTestInputRows(1234L))
                .withFailure(CRUDFailure.notFoundIds("StepTest", 1234L));
    }

    @Test
    public void getTestInputRows_TestFound_ReturnsInputRows() {
        Long pipelineId = VavrAssert.assertValid(
                pipelineService.saveNewPipeline(Design.Populated.createPipelineRequest()
                        .name("PipelineOne")
                        .build()))
                .getResult()
                .getId();

        Schema schemaA = schemaService.createNew(Design.Populated.schema("a").build());
        Schema schemaB = schemaService.createNew(Design.Populated.schema("b").build());

        PipelineStepView pipelineStepView = VavrAssert.assertValid(
                pipelineStepService.savePipelineStep(pipelineId, Design.Populated.pipelineMapStepRequest()
                        .schemaInId(schemaA.getId())
                        .schemaOutId(schemaB.getId())
                        .build()))
                .getResult();

        Identifiable stepTestView = VavrAssert.assertValid(
                pipelineStepTestService.create(CreateStepTestRequest.builder()
                        .name("hello")
                        .description("Just testing")
                        .testReferenceDate(LocalDate.of(2019, 1, 1))
                        .pipelineStepId(pipelineStepView.getId())
                        .build()))
                .getResult();

        StepTestRowView createdInputRow = VavrAssert
                .assertValid(pipelineStepTestRowService.createInputDataRow(stepTestView.getId(), schemaA.getId()))
                .getResult();

        Map<Long, List<StepRowInputDataView>> inputRows =
                VavrAssert.assertValid(pipelineStepTestService.getTestInputRows(stepTestView.getId())).getResult();

        soft.assertThat(inputRows)
                .containsOnlyKeys(schemaA.getId());

        soft.assertThat(inputRows.get(schemaA.getId()))
                .hasSize(1);

        soft.assertThat(inputRows.get(schemaA.getId()).get(0).getId())
                .isEqualTo(createdInputRow.getId());
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    public void runTest_ActualMatchesExpected_ReturnsPassAndUpdatesExpectedAndActual() {
        ExamplePipelineHelper.FullNameMapStepConfig mapConfiguration =
                examplePipelineHelper.fullNameMapConfig();

        Schema inputSchema = mapConfiguration.getInputSchema();
        Schema outputSchema = mapConfiguration.getOutputSchema();
        FieldDto inputFirstNameField = mapConfiguration.getInputFirstNameField();
        FieldDto inputLastNameField = mapConfiguration.getInputLastNameField();
        FieldDto outputFullNameField = mapConfiguration.getOutputFullNameField();
        PipelineStepView pipelineStep = mapConfiguration.getPipelineStep();

        Identifiable stepTest = VavrAssert.assertValid(pipelineStepTestService.create(
                CreateStepTestRequest.builder()
                        .name("Test full name creation")
                        .pipelineStepId(pipelineStep.getId())
                        .build()))
                .getResult();

        StepTestRowView inputRow = VavrAssert.assertValid(
                pipelineStepTestRowService.createInputDataRow(
                        stepTest.getId(),
                        inputSchema.getId()))
                .getResult();

        CellIdAndField firstNameInputCell = inputRow.getCells().stream()
                .filter(cell -> cell.getFieldId().equals(inputFirstNameField.getId()))
                .findFirst().get();

        CellIdAndField lastNameInputCell = inputRow.getCells().stream()
                .filter(cell -> cell.getFieldId().equals(inputLastNameField.getId()))
                .findFirst().get();

        VavrAssert.assertValid(
                pipelineStepTestRowService.updateCellValue(
                        stepTest.getId(),
                        inputRow.getId(),
                        firstNameInputCell.getId(),
                        "HOMER"));
        VavrAssert.assertValid(
                pipelineStepTestRowService.updateCellValue(
                        stepTest.getId(),
                        inputRow.getId(), lastNameInputCell.getId(), "SIMPSON"));

        StepTestRowView expectedRow = VavrAssert.assertValid(
                pipelineStepTestRowService.createExpectedDataRow(
                        stepTest.getId(),
                        outputSchema.getId()))
                .getResult();

        CellIdAndField fullNameOutputCell = expectedRow.getCells().stream()
                .filter(cell -> cell.getFieldId().equals(outputFullNameField.getId()))
                .findFirst().get();

        VavrAssert.assertValid(
                pipelineStepTestRowService.updateCellValue(
                        stepTest.getId(), expectedRow.getId(), fullNameOutputCell.getId(), "HOMER SIMPSON"));

        Dataset<Row> mockDataset = mockDataset(singletonList(fullNameRow("HOMER SIMPSON")));

        when(mockSparkSqlExecutor.executeFullTransformation(any(), any()))
                .thenReturn(Validation.valid(mockDataset));

        VavrAssert.assertValid(pipelineStepTestService.runTest(stepTest.getId()))
                .extracting(TestRunResultView::getStatus)
                .withResult(PASS);

        StepTestView runTestResult = VavrAssert.assertValid(pipelineStepTestService.findById(stepTest.getId()))
                .getResult();

        List<StepRowOutputDataView> outputRows = pipelineStepTestService.getTestOutputRows(stepTest.getId());

        assertThat(runTestResult.getStatus())
                .isEqualTo(PASS);
        assertThat(outputRows)
                .hasSize(2);

        StepRowOutputDataView expected = outputRows.stream()
                .filter(row -> row.getType().equals("EXPECTED"))
                .findFirst().get();
        StepRowOutputDataView actual = outputRows.stream()
                .filter(row -> row.getType().equals("ACTUAL"))
                .findFirst().get();

        soft.assertThat(expected.getStatus())
                .isEqualTo("MATCHED");
        soft.assertThat(expected.isRun())
                .isEqualTo(true);
        soft.assertThat(actual.getStatus())
                .isEqualTo("MATCHED");
        soft.assertThat(actual.isRun())
                .isEqualTo(true);

        assertThat(expected.getCells())
                .hasSize(1);

        StepTestCellView fullExpectedNameCell = expected.getCells().values().iterator().next();
        soft.assertThat(fullExpectedNameCell.getData())
                .isEqualTo("HOMER SIMPSON");

        assertThat(actual.getCells())
                .hasSize(1);
        StepTestCellView actualFullNameCell = actual.getCells().values().iterator().next();
        soft.assertThat(actualFullNameCell.getData())
                .isEqualTo("HOMER SIMPSON");
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    public void runTest_OneNotFoundOneUnexpected_ReturnsFailAndUpdatesRows() {
        ExamplePipelineHelper.FullNameMapStepConfig mapConfiguration =
                examplePipelineHelper.fullNameMapConfig();

        Schema inputSchema = mapConfiguration.getInputSchema();
        Schema outputSchema = mapConfiguration.getOutputSchema();
        FieldDto inputFirstNameField = mapConfiguration.getInputFirstNameField();
        FieldDto inputLastNameField = mapConfiguration.getInputLastNameField();
        FieldDto outputFullNameField = mapConfiguration.getOutputFullNameField();
        PipelineStepView pipelineStep = mapConfiguration.getPipelineStep();

        Identifiable stepTest = VavrAssert.assertValid(pipelineStepTestService.create(CreateStepTestRequest.builder()
                .name("Test full name creation")
                .pipelineStepId(pipelineStep.getId())
                .build()))
                .getResult();

        StepTestRowView inputRow = VavrAssert.assertValid(pipelineStepTestRowService.createInputDataRow(
                stepTest.getId(), inputSchema.getId()))
                .getResult();

        CellIdAndField firstNameInputCell = inputRow.getCells().stream()
                .filter(cell -> cell.getFieldId().equals(inputFirstNameField.getId()))
                .findFirst().get();

        CellIdAndField lastNameInputCell = inputRow.getCells().stream()
                .filter(cell -> cell.getFieldId().equals(inputLastNameField.getId()))
                .findFirst().get();

        VavrAssert.assertValid(
                pipelineStepTestRowService.updateCellValue(
                        stepTest.getId(),
                        inputRow.getId(),
                        firstNameInputCell.getId(),
                        "NED"));
        VavrAssert.assertValid(
                pipelineStepTestRowService.updateCellValue(
                        stepTest.getId(),
                        inputRow.getId(), lastNameInputCell.getId(), "FLANDERS"));

        StepTestRowView expectedNedRow = VavrAssert.assertValid(pipelineStepTestRowService
                .createExpectedDataRow(stepTest.getId(), outputSchema.getId()))
                .getResult();

        CellIdAndField fullNameOutputCell = expectedNedRow.getCells().stream()
                .filter(cell -> cell.getFieldId().equals(outputFullNameField.getId()))
                .findFirst().get();

        VavrAssert.assertValid(
                pipelineStepTestRowService.updateCellValue(
                        stepTest.getId(), expectedNedRow.getId(), fullNameOutputCell.getId(), "NED FLANDERS"));

        Dataset<Row> mockDataset = mockDataset(singletonList(fullNameRow("NED PHILANDERER")));

        when(mockSparkSqlExecutor.executeFullTransformation(any(), any()))
                .thenReturn(Validation.valid(mockDataset));

        VavrAssert.assertValid(pipelineStepTestService.runTest(stepTest.getId()))
                .extracting(TestRunResultView::getStatus)
                .withResult(FAIL);

        StepTestView runTestResult = VavrAssert.assertValid(pipelineStepTestService.findById(stepTest.getId()))
                .getResult();

        List<StepRowOutputDataView> outputRows = pipelineStepTestService.getTestOutputRows(stepTest.getId());

        assertThat(runTestResult.getStatus())
                .isEqualTo(FAIL);
        assertThat(outputRows)
                .hasSize(2);

        StepRowOutputDataView expected = outputRows.stream()
                .filter(row -> row.getType().equals("EXPECTED"))
                .findFirst().get();
        StepRowOutputDataView actual = outputRows.stream()
                .filter(row -> row.getType().equals("ACTUAL"))
                .findFirst().get();

        soft.assertThat(expected.getStatus())
                .isEqualTo("NOT_FOUND");
        soft.assertThat(expected.isRun())
                .isEqualTo(true);
        soft.assertThat(actual.getStatus())
                .isEqualTo("UNEXPECTED");
        soft.assertThat(actual.isRun())
                .isEqualTo(true);

        assertThat(expected.getCells())
                .hasSize(1);

        StepTestCellView fullExpectedNameCell = expected.getCells().values().iterator().next();
        soft.assertThat(fullExpectedNameCell.getData())
                .isEqualTo("NED FLANDERS");

        assertThat(actual.getCells())
                .hasSize(1);
        StepTestCellView actualFullNameCell = actual.getCells().values().iterator().next();
        soft.assertThat(actualFullNameCell.getData())
                .isEqualTo("NED PHILANDERER");
    }

    @Test
    public void runTest_UpdatesInputRowsAsRun() {
        ExamplePipelineHelper.FullNameMapStepConfig mapConfiguration =
                examplePipelineHelper.fullNameMapConfig();

        Schema inputSchema = mapConfiguration.getInputSchema();
        Schema outputSchema = mapConfiguration.getOutputSchema();
        FieldDto inputFirstNameField = mapConfiguration.getInputFirstNameField();
        FieldDto inputLastNameField = mapConfiguration.getInputLastNameField();
        FieldDto outputFullNameField = mapConfiguration.getOutputFullNameField();
        PipelineStepView pipelineStep = mapConfiguration.getPipelineStep();

        Identifiable stepTest = VavrAssert.assertValid(pipelineStepTestService.create(CreateStepTestRequest.builder()
                .name("Test full name creation")
                .pipelineStepId(pipelineStep.getId())
                .build()))
                .getResult();

        StepTestRowView inputRow = VavrAssert.assertValid(pipelineStepTestRowService.createInputDataRow(
                stepTest.getId(), inputSchema.getId()))
                .getResult();

        List<StepRowInputDataView> inputRowsBeforeExecution = VavrAssert
                .assertValid(pipelineStepTestService.getTestInputRows(stepTest.getId()))
                .getResult()
                .get(inputSchema.getId());

        assertThat(inputRowsBeforeExecution)
                .extracting(StepRowInputDataView::isRun)
                .containsOnly(false);

        VavrAssert.assertValid(pipelineStepTestService.runTest(stepTest.getId()));

        List<StepRowInputDataView> inputRowsAfterExecution = VavrAssert
                .assertValid(pipelineStepTestService.getTestInputRows(stepTest.getId()))
                .getResult()
                .get(inputSchema.getId());

        assertThat(inputRowsAfterExecution)
                .extracting(StepRowInputDataView::isRun)
                .containsOnly(true);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    public void runTest_OneMatchedTwoUnexpected_ReturnsFailRows() {
        ExamplePipelineHelper.OutputMapStepConfig mapConfiguration =
                examplePipelineHelper.intOutputMapConfig();

        Schema inputSchema = mapConfiguration.getInputSchema();
        Schema outputSchema = mapConfiguration.getOutputSchema();
        FieldDto inputIntInputField = mapConfiguration.getInputField();
        FieldDto outputIntOutputField = mapConfiguration.getOutputField();
        PipelineStepView pipelineStep = mapConfiguration.getPipelineStep();

        Identifiable stepTest = VavrAssert.assertValid(pipelineStepTestService.create(CreateStepTestRequest.builder()
                .name("Test int creation")
                .pipelineStepId(pipelineStep.getId())
                .build()))
                .getResult();

        StepTestRowView inputRow1 = VavrAssert.assertValid(pipelineStepTestRowService.createInputDataRow(
                stepTest.getId(), inputSchema.getId()))
                .getResult();

        CellIdAndField intInputRow1Cell1 = inputRow1.getCells().stream()
                .filter(cell -> cell.getFieldId().equals(inputIntInputField.getId()))
                .findFirst().get();

        VavrAssert.assertValid(
                pipelineStepTestRowService.updateCellValue(
                        stepTest.getId(),
                        inputRow1.getId(),
                        intInputRow1Cell1.getId(),
                        "10"));

        StepTestRowView inputRow2 = VavrAssert.assertValid(pipelineStepTestRowService.createInputDataRow(
                stepTest.getId(), inputSchema.getId()))
                .getResult();

        CellIdAndField intInputRow2Cell1 = inputRow2.getCells().stream()
                .filter(cell -> cell.getFieldId().equals(inputIntInputField.getId()))
                .findFirst().get();

        VavrAssert.assertValid(
                pipelineStepTestRowService.updateCellValue(
                        stepTest.getId(),
                        inputRow2.getId(),
                        intInputRow2Cell1.getId(),
                        "100"));

        StepTestRowView inputRow3 = VavrAssert.assertValid(pipelineStepTestRowService.createInputDataRow(
                stepTest.getId(), inputSchema.getId()))
                .getResult();

        CellIdAndField intInputRow3Cell1 = inputRow3.getCells().stream()
                .filter(cell -> cell.getFieldId().equals(inputIntInputField.getId()))
                .findFirst().get();

        VavrAssert.assertValid(
                pipelineStepTestRowService.updateCellValue(
                        stepTest.getId(),
                        inputRow3.getId(),
                        intInputRow3Cell1.getId(),
                        "10000"));

        StepTestRowView expectedRow = VavrAssert.assertValid(pipelineStepTestRowService
                .createExpectedDataRow(stepTest.getId(), outputSchema.getId()))
                .getResult();

        CellIdAndField intOutputCell = expectedRow.getCells().stream()
                .filter(cell -> cell.getFieldId().equals(outputIntOutputField.getId()))
                .findFirst().get();

        VavrAssert.assertValid(
                pipelineStepTestRowService.updateCellValue(
                        stepTest.getId(), expectedRow.getId(), intOutputCell.getId(), "0"));

        Dataset<Row> mockDataset = mockDataset(Arrays.asList(intRow(0), intRow(0), intRow(0)));
        when(mockSparkSqlExecutor.executeFullTransformation(any(), any()))
                .thenReturn(Validation.valid(
                        mockDataset));

        VavrAssert.assertValid(pipelineStepTestService.runTest(stepTest.getId()))
                .extracting(TestRunResultView::getStatus)
                .withResult(FAIL);

        StepTestView runTestResult = VavrAssert.assertValid(pipelineStepTestService.findById(stepTest.getId()))
                .getResult();

        List<StepRowOutputDataView> outputRows = pipelineStepTestService.getTestOutputRows(stepTest.getId());

        assertThat(runTestResult.getStatus())
                .isEqualTo(FAIL);
        assertThat(outputRows)
                .hasSize(4);

        StepRowOutputDataView expected = outputRows.stream()
                .filter(row -> row.getType().equals("EXPECTED"))
                .findFirst().get();
        List<StepRowOutputDataView> actual = outputRows.stream()
                .filter(row -> row.getType().equals("ACTUAL"))
                .collect(Collectors.toList());

        assertThat(actual).hasSize(3);

        soft.assertThat(expected.getStatus())
                .isEqualTo("MATCHED");
        soft.assertThat(expected.isRun())
                .isEqualTo(true);
        assertThat(expected.getCells())
                .hasSize(1);
        StepTestCellView expectedCell = expected.getCells().values().iterator().next();
        soft.assertThat(expectedCell.getData())
                .isEqualTo("0");

        soft.assertThat(actual).extracting("status")
                .containsExactlyInAnyOrder("UNEXPECTED", "MATCHED", "UNEXPECTED");
        soft.assertThat(actual).extracting("run")
                .containsExactly(true, true, true);

        StepTestCellView actual1Cell = actual.get(0).getCells().values().iterator().next();
        soft.assertThat(actual1Cell.getData())
                .isEqualTo("0");
        StepTestCellView actual2Cell = actual.get(1).getCells().values().iterator().next();
        soft.assertThat(actual2Cell.getData())
                .isEqualTo("0");
        StepTestCellView actual3Cell = actual.get(2).getCells().values().iterator().next();
        soft.assertThat(actual3Cell.getData())
                .isEqualTo("0");
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    public void runTest_OneMatchedOneNotFound_ReturnsFailRows() {
        ExamplePipelineHelper.OutputMapStepConfig mapConfiguration =
                examplePipelineHelper.longOutputMapConfig();

        Schema inputSchema = mapConfiguration.getInputSchema();
        Schema outputSchema = mapConfiguration.getOutputSchema();
        FieldDto inputIntInputField = mapConfiguration.getInputField();
        FieldDto outputIntOutputField = mapConfiguration.getOutputField();
        PipelineStepView pipelineStep = mapConfiguration.getPipelineStep();

        Identifiable stepTest = VavrAssert.assertValid(pipelineStepTestService.create(CreateStepTestRequest.builder()
                .name("Test int creation")
                .pipelineStepId(pipelineStep.getId())
                .build()))
                .getResult();

        StepTestRowView inputRow1 = VavrAssert.assertValid(pipelineStepTestRowService.createInputDataRow(
                stepTest.getId(), inputSchema.getId()))
                .getResult();

        CellIdAndField intInputRow1Cell1 = inputRow1.getCells().stream()
                .filter(cell -> cell.getFieldId().equals(inputIntInputField.getId()))
                .findFirst().get();

        VavrAssert.assertValid(
                pipelineStepTestRowService.updateCellValue(
                        stepTest.getId(),
                        inputRow1.getId(),
                        intInputRow1Cell1.getId(),
                        "10"));

        StepTestRowView expectedRow1 = VavrAssert.assertValid(pipelineStepTestRowService
                .createExpectedDataRow(stepTest.getId(), outputSchema.getId()))
                .getResult();

        CellIdAndField expectedRow1Cell1 = expectedRow1.getCells().stream()
                .filter(cell -> cell.getFieldId().equals(outputIntOutputField.getId()))
                .findFirst().get();

        VavrAssert.assertValid(
                pipelineStepTestRowService.updateCellValue(
                        stepTest.getId(), expectedRow1.getId(), expectedRow1Cell1.getId(), "0"));

        StepTestRowView expectedRow2 = VavrAssert.assertValid(pipelineStepTestRowService
                .createExpectedDataRow(stepTest.getId(), outputSchema.getId()))
                .getResult();

        CellIdAndField expectedRow2Cell1 = expectedRow2.getCells().stream()
                .filter(cell -> cell.getFieldId().equals(outputIntOutputField.getId()))
                .findFirst().get();

        VavrAssert.assertValid(
                pipelineStepTestRowService.updateCellValue(
                        stepTest.getId(), expectedRow2.getId(), expectedRow2Cell1.getId(), "0"));

        Dataset<Row> mockDataset = mockDataset(singletonList(longRow(0L)));
        when(mockSparkSqlExecutor.executeFullTransformation(any(), any()))
                .thenReturn(Validation.valid(mockDataset));

        VavrAssert.assertValid(pipelineStepTestService.runTest(stepTest.getId()))
                .extracting(TestRunResultView::getStatus)
                .withResult(FAIL);

        StepTestView runTestResult = VavrAssert.assertValid(pipelineStepTestService.findById(stepTest.getId()))
                .getResult();

        List<StepRowOutputDataView> outputRows = pipelineStepTestService.getTestOutputRows(stepTest.getId());

        assertThat(runTestResult.getStatus())
                .isEqualTo(FAIL);
        assertThat(outputRows)
                .hasSize(3);

        List<StepRowOutputDataView> expected = outputRows.stream()
                .filter(row -> row.getType().equals("EXPECTED"))
                .collect(Collectors.toList());
        List<StepRowOutputDataView> actual = outputRows.stream()
                .filter(row -> row.getType().equals("ACTUAL"))
                .collect(Collectors.toList());

        assertThat(actual).hasSize(1);

        soft.assertThat(expected).extracting("status")
                .containsExactlyInAnyOrder("NOT_FOUND", "MATCHED");
        soft.assertThat(expected).extracting("run")
                .containsExactly(true, true);

        soft.assertThat(actual).extracting("status")
                .containsExactlyInAnyOrder("MATCHED");
        soft.assertThat(actual).extracting("run")
                .containsExactly(true);

        StepTestCellView actual1Cell = actual.get(0).getCells().values().iterator().next();
        soft.assertThat(actual1Cell.getData())
                .isEqualTo("0");
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    public void runTest_MultipleMatched_ReturnsPassAndUpdatesExpectedAndActual() {
        ExamplePipelineHelper.OutputMapStepConfig mapConfiguration =
                examplePipelineHelper.intOutputMapConfig();

        Schema inputSchema = mapConfiguration.getInputSchema();
        Schema outputSchema = mapConfiguration.getOutputSchema();
        FieldDto inputIntInputField = mapConfiguration.getInputField();
        FieldDto outputIntOutputField = mapConfiguration.getOutputField();
        PipelineStepView pipelineStep = mapConfiguration.getPipelineStep();

        Identifiable stepTest = VavrAssert.assertValid(pipelineStepTestService.create(CreateStepTestRequest.builder()
                .name("Test int creation")
                .pipelineStepId(pipelineStep.getId())
                .build()))
                .getResult();

        StepTestRowView inputRow1 = VavrAssert.assertValid(pipelineStepTestRowService.createInputDataRow(
                stepTest.getId(), inputSchema.getId()))
                .getResult();

        CellIdAndField intInputRow1Cell1 = inputRow1.getCells().stream()
                .filter(cell -> cell.getFieldId().equals(inputIntInputField.getId()))
                .findFirst().get();

        VavrAssert.assertValid(
                pipelineStepTestRowService.updateCellValue(
                        stepTest.getId(),
                        inputRow1.getId(),
                        intInputRow1Cell1.getId(),
                        "1"));

        StepTestRowView inputRow2 = VavrAssert.assertValid(pipelineStepTestRowService.createInputDataRow(
                stepTest.getId(), inputSchema.getId()))
                .getResult();

        CellIdAndField intInputRow2Cell1 = inputRow2.getCells().stream()
                .filter(cell -> cell.getFieldId().equals(inputIntInputField.getId()))
                .findFirst().get();

        VavrAssert.assertValid(
                pipelineStepTestRowService.updateCellValue(
                        stepTest.getId(),
                        inputRow2.getId(),
                        intInputRow2Cell1.getId(),
                        "3"));

        StepTestRowView inputRow3 = VavrAssert.assertValid(pipelineStepTestRowService.createInputDataRow(
                stepTest.getId(), inputSchema.getId()))
                .getResult();

        CellIdAndField intInputRow3Cell1 = inputRow3.getCells().stream()
                .filter(cell -> cell.getFieldId().equals(inputIntInputField.getId()))
                .findFirst().get();

        VavrAssert.assertValid(
                pipelineStepTestRowService.updateCellValue(
                        stepTest.getId(),
                        inputRow3.getId(),
                        intInputRow3Cell1.getId(),
                        "5"));

        StepTestRowView expectedRow1 = VavrAssert.assertValid(pipelineStepTestRowService
                .createExpectedDataRow(stepTest.getId(), outputSchema.getId()))
                .getResult();

        CellIdAndField expectedRow1Cell1 = expectedRow1.getCells().stream()
                .filter(cell -> cell.getFieldId().equals(outputIntOutputField.getId()))
                .findFirst().get();

        VavrAssert.assertValid(
                pipelineStepTestRowService.updateCellValue(
                        stepTest.getId(), expectedRow1.getId(), expectedRow1Cell1.getId(), "1"));

        StepTestRowView expectedRow2 = VavrAssert.assertValid(pipelineStepTestRowService
                .createExpectedDataRow(stepTest.getId(), outputSchema.getId()))
                .getResult();

        CellIdAndField expectedRow2Cell1 = expectedRow2.getCells().stream()
                .filter(cell -> cell.getFieldId().equals(outputIntOutputField.getId()))
                .findFirst().get();

        VavrAssert.assertValid(
                pipelineStepTestRowService.updateCellValue(
                        stepTest.getId(), expectedRow2.getId(), expectedRow2Cell1.getId(), "3"));

        StepTestRowView expectedRow3 = VavrAssert.assertValid(pipelineStepTestRowService
                .createExpectedDataRow(stepTest.getId(), outputSchema.getId()))
                .getResult();

        CellIdAndField expectedRow3Cell1 = expectedRow3.getCells().stream()
                .filter(cell -> cell.getFieldId().equals(outputIntOutputField.getId()))
                .findFirst().get();

        VavrAssert.assertValid(
                pipelineStepTestRowService.updateCellValue(
                        stepTest.getId(), expectedRow3.getId(), expectedRow3Cell1.getId(), "5"));

        Dataset<Row> mockDataset = mockDataset(Arrays.asList(intRow(1), intRow(3), intRow(5)));

        when(mockSparkSqlExecutor.executeFullTransformation(any(), any()))
                .thenReturn(Validation.valid(mockDataset));

        VavrAssert.assertValid(pipelineStepTestService.runTest(stepTest.getId()))
                .extracting(TestRunResultView::getStatus)
                .withResult(PASS);

        StepTestView runTestResult = VavrAssert.assertValid(pipelineStepTestService.findById(stepTest.getId()))
                .getResult();

        List<StepRowOutputDataView> outputRows = pipelineStepTestService.getTestOutputRows(stepTest.getId());

        assertThat(runTestResult.getStatus())
                .isEqualTo(PASS);
        assertThat(outputRows)
                .hasSize(6);

        List<StepRowOutputDataView> expected = outputRows.stream()
                .filter(row -> row.getType().equals("EXPECTED"))
                .collect(Collectors.toList());
        List<StepRowOutputDataView> actual = outputRows.stream()
                .filter(row -> row.getType().equals("ACTUAL"))
                .collect(Collectors.toList());

        assertThat(actual).hasSize(3);

        soft.assertThat(expected).extracting("status")
                .containsExactlyInAnyOrder("MATCHED", "MATCHED", "MATCHED");
        soft.assertThat(expected).extracting("run")
                .containsExactly(true, true, true);

        soft.assertThat(actual).extracting("status")
                .containsExactlyInAnyOrder("MATCHED", "MATCHED", "MATCHED");
        soft.assertThat(actual).extracting("run")
                .containsExactly(true, true, true);

        soft.assertThat(actual.stream()
                .flatMap(a -> a.getCells().values().stream())
                .collect(Collectors.toList())).extracting("data")
                .containsExactlyInAnyOrder("1", "3", "5");
    }

    @Test
    public void runTest_executeSqlInvalid_ReturnsFailure() {
        ExamplePipelineHelper.OutputMapStepConfig mapConfiguration =
                examplePipelineHelper.intOutputMapConfig();

        Schema inputSchema = mapConfiguration.getInputSchema();
        Schema outputSchema = mapConfiguration.getOutputSchema();
        FieldDto inputIntInputField = mapConfiguration.getInputField();
        FieldDto outputIntOutputField = mapConfiguration.getOutputField();
        PipelineStepView pipelineStep = mapConfiguration.getPipelineStep();

        Identifiable stepTest = VavrAssert.assertValid(pipelineStepTestService.create(CreateStepTestRequest.builder()
                .name("Test int creation")
                .pipelineStepId(pipelineStep.getId())
                .build()))
                .getResult();

        StepTestRowView inputRow1 = VavrAssert.assertValid(pipelineStepTestRowService.createInputDataRow(
                stepTest.getId(), inputSchema.getId()))
                .getResult();

        CellIdAndField intInputRow1Cell1 = inputRow1.getCells().stream()
                .filter(cell -> cell.getFieldId().equals(inputIntInputField.getId()))
                .findFirst().get();

        VavrAssert.assertValid(
                pipelineStepTestRowService.updateCellValue(
                        stepTest.getId(),
                        inputRow1.getId(),
                        intInputRow1Cell1.getId(),
                        "10"));

        StepTestRowView expectedRow = VavrAssert.assertValid(pipelineStepTestRowService
                .createExpectedDataRow(stepTest.getId(), outputSchema.getId()))
                .getResult();

        CellIdAndField intOutputCell = expectedRow.getCells().stream()
                .filter(cell -> cell.getFieldId().equals(outputIntOutputField.getId()))
                .findFirst().get();

        VavrAssert.assertValid(
                pipelineStepTestRowService.updateCellValue(
                        stepTest.getId(), expectedRow.getId(), intOutputCell.getId(), "0"));

        when(mockSparkSqlExecutor.executeFullTransformation(any(), any()))
                .thenReturn(Validation.invalid(singletonList(
                        ErrorResponse.valueOf("cannot resolve '`FIRST_NAME_WRONG`' given input columns: "
                                + "[FIRST_NAME, LAST_NAME]; line 1 pos 14", "SQL_ANALYSIS_ERROR")
                )));

        VavrAssert.assertFailed(pipelineStepTestService.runTest(stepTest.getId()))
                .withFailure(singletonList(
                        ErrorResponse.valueOf("cannot resolve '`FIRST_NAME_WRONG`' given input columns: "
                                + "[FIRST_NAME, LAST_NAME]; line 1 pos 14", "SQL_ANALYSIS_ERROR")));
    }

    @Test
    public void update_PipelineStepTestFound_UpdatesPiplneStepTest() {
        Long pipelineId = VavrAssert.assertValid(
                pipelineService.saveNewPipeline(Design.Populated.createPipelineRequest()
                        .name("PipelineOne")
                        .build()))
                .getResult()
                .getId();

        Schema a = schemaService.createNew(Design.Populated.schema("a").build());
        Schema b = schemaService.createNew(Design.Populated.schema("b").build());

        PipelineStepView pipelineStepView = VavrAssert.assertValid(
                pipelineStepService.savePipelineStep(pipelineId, Design.Populated.pipelineMapStepRequest()
                        .schemaInId(a.getId())
                        .schemaOutId(b.getId())
                        .build()))
                .getResult();

        Identifiable testId = VavrAssert.assertValid(
                pipelineStepTestService.create(CreateStepTestRequest.builder()
                        .name("hello")
                        .description("Just testing")
                        .testReferenceDate(LocalDate.of(2019, 1, 1))
                        .pipelineStepId(pipelineStepView.getId())
                        .build()))
                .getResult();

        StepTestView stepTestView = VavrAssert.assertValid(
                pipelineStepTestService.update(testId.getId(), UpdateStepTestRequest.builder()
                        .name("hello update")
                        .description("Updated testing")
                        .testReferenceDate(LocalDate.of(2020, 2, 2))
                        .build()
                ))
                .getResult();

        soft.assertThat(stepTestView.getId())
                .isNotNull();
        soft.assertThat(stepTestView.getName())
                .isEqualTo("hello update");
        soft.assertThat(stepTestView.getDescription())
                .isEqualTo("Updated testing");
        soft.assertThat(stepTestView.getTestReferenceDate())
                .isEqualTo(LocalDate.of(2020, 2, 2));
        soft.assertThat(stepTestView.getPipelineStepId())
                .isEqualTo(pipelineStepView.getId());
        soft.assertThat(stepTestView.getPipelineId())
                .isEqualTo(pipelineId);
    }

    @Test
    public void update_PipelineStepTestNotFound_ReturnsNotFound() {
        VavrAssert.assertFailed(
                pipelineStepTestService.update(92734723L, UpdateStepTestRequest.builder()
                        .name("hello update")
                        .description("Updated testing")
                        .testReferenceDate(LocalDate.of(2019, 1, 1))
                        .build()
                ))
                .withFailure(CRUDFailure.notFoundIds("PipelineStepTest", 92734723L));
    }

    @Test
    public void deleteTest_TestExists_RemovesInputRow() {
        ExamplePipelineHelper.FullNameMapStepConfig mapConfiguration =
                examplePipelineHelper.fullNameMapConfig();

        Schema inputSchema = mapConfiguration.getInputSchema();
        Schema outputSchema = mapConfiguration.getOutputSchema();
        FieldDto inputFirstNameField = mapConfiguration.getInputFirstNameField();
        FieldDto inputLastNameField = mapConfiguration.getInputLastNameField();
        FieldDto outputFullNameField = mapConfiguration.getOutputFullNameField();
        PipelineStepView pipelineStep = mapConfiguration.getPipelineStep();

        Identifiable stepTest = VavrAssert.assertValid(pipelineStepTestService.create(
                CreateStepTestRequest.builder()
                        .pipelineStepId(pipelineStep.getId())
                        .build()))
                .getResult();

        StepTestRowView inputRow = VavrAssert.assertValid(pipelineStepTestRowService.createInputDataRow(
                stepTest.getId(), inputSchema.getId()))
                .getResult();

        VavrAssert.assertValid(pipelineStepTestRowService.updateCellValue(
                stepTest.getId(), inputRow.getId(), inputRow.getCells().get(0).getId(), "MEH"));

        VavrAssert.assertValid(pipelineStepTestService.deleteById(stepTest.getId()));

        VavrAssert.assertFailed(pipelineStepTestService.findById(stepTest.getId()))
                .withFailure(CRUDFailure.notFoundIds("StepTest", stepTest.getId()));

        VavrAssert.assertFailed(pipelineStepTestRowService.deleteInput(inputRow.getId()))
                .withFailure(CRUDFailure.notFoundIds("PipelineStepTestRow", inputRow.getId()));
    }

    @Test
    public void deleteTest_TestExists_RemovesExpectedRow() {
        ExamplePipelineHelper.FullNameMapStepConfig mapConfiguration =
                examplePipelineHelper.fullNameMapConfig();

        Schema inputSchema = mapConfiguration.getInputSchema();
        Schema outputSchema = mapConfiguration.getOutputSchema();
        FieldDto inputFirstNameField = mapConfiguration.getInputFirstNameField();
        FieldDto inputLastNameField = mapConfiguration.getInputLastNameField();
        FieldDto outputFullNameField = mapConfiguration.getOutputFullNameField();
        PipelineStepView pipelineStep = mapConfiguration.getPipelineStep();

        Identifiable stepTest = VavrAssert.assertValid(pipelineStepTestService.create(
                CreateStepTestRequest.builder()
                        .pipelineStepId(pipelineStep.getId())
                        .build()))
                .getResult();

        StepTestRowView expectedRow = VavrAssert.assertValid(pipelineStepTestRowService.createExpectedDataRow(
                stepTest.getId(), outputSchema.getId()))
                .getResult();

        VavrAssert.assertValid(pipelineStepTestRowService.updateCellValue(
                stepTest.getId(), expectedRow.getId(), expectedRow.getCells().get(0).getId(), "MEH"));

        VavrAssert.assertValid(pipelineStepTestService.deleteById(stepTest.getId()));

        VavrAssert.assertFailed(pipelineStepTestService.findById(stepTest.getId()))
                .withFailure(CRUDFailure.notFoundIds("StepTest", stepTest.getId()));

        VavrAssert.assertFailed(pipelineStepTestRowService.deleteExpected(expectedRow.getId()))
                .withFailure(CRUDFailure.notFoundIds("PipelineStepTestRow", expectedRow.getId()));
    }

    @Test
    public void deleteById_TestHasBeenRun_DeletesActualRows() {
        ExamplePipelineHelper.FullNameMapStepConfig mapConfiguration =
                examplePipelineHelper.fullNameMapConfig();

        Schema inputSchema = mapConfiguration.getInputSchema();
        Schema outputSchema = mapConfiguration.getOutputSchema();
        FieldDto inputFirstNameField = mapConfiguration.getInputFirstNameField();
        FieldDto inputLastNameField = mapConfiguration.getInputLastNameField();
        FieldDto outputFullNameField = mapConfiguration.getOutputFullNameField();
        PipelineStepView pipelineStep = mapConfiguration.getPipelineStep();

        Identifiable stepTest = VavrAssert.assertValid(pipelineStepTestService.create(
                CreateStepTestRequest.builder()
                        .name("Test full name creation")
                        .pipelineStepId(pipelineStep.getId())
                        .build()))
                .getResult();

        StepTestRowView inputRow = VavrAssert.assertValid(
                pipelineStepTestRowService.createInputDataRow(
                        stepTest.getId(),
                        inputSchema.getId()))
                .getResult();

        CellIdAndField firstNameInputCell = inputRow.getCells().stream()
                .filter(cell -> cell.getFieldId().equals(inputFirstNameField.getId()))
                .findFirst().get();

        CellIdAndField lastNameInputCell = inputRow.getCells().stream()
                .filter(cell -> cell.getFieldId().equals(inputLastNameField.getId()))
                .findFirst().get();

        VavrAssert.assertValid(
                pipelineStepTestRowService.updateCellValue(
                        stepTest.getId(),
                        inputRow.getId(),
                        firstNameInputCell.getId(),
                        "HOMER"));
        VavrAssert.assertValid(
                pipelineStepTestRowService.updateCellValue(
                        stepTest.getId(),
                        inputRow.getId(), lastNameInputCell.getId(), "SIMPSON"));

        Dataset<Row> mockDataset = mockDataset(singletonList(fullNameRow("HOMER SIMPSON")));

        when(mockSparkSqlExecutor.executeFullTransformation(any(), any()))
                .thenReturn(Validation.valid(mockDataset));

        VavrAssert.assertValid(pipelineStepTestService.runTest(stepTest.getId()));

        VavrAssert.assertValid(pipelineStepTestService.deleteById(stepTest.getId()));

        assertThat(pipelineStepTestRowRepository.findAll())
                .isEmpty();
    }

    @Test
    public void runTest_OneMatchedFormatOfDate_ReturnsSuccessRowsWithCorrectActualDateFormat() {
        ExamplePipelineHelper.OutputMapStepConfig mapConfiguration =
                examplePipelineHelper.dateOutputMapConfig();

        Schema inputSchema = mapConfiguration.getInputSchema();
        Schema outputSchema = mapConfiguration.getOutputSchema();
        FieldDto inputField = mapConfiguration.getInputField();
        FieldDto outputField = mapConfiguration.getOutputField();
        PipelineStepView pipelineStep = mapConfiguration.getPipelineStep();

        Identifiable stepTest = VavrAssert.assertValid(pipelineStepTestService.create(CreateStepTestRequest.builder()
                .name("Test creation")
                .pipelineStepId(pipelineStep.getId())
                .build()))
                .getResult();

        StepTestRowView inputRow = VavrAssert.assertValid(pipelineStepTestRowService.createInputDataRow(
                stepTest.getId(), inputSchema.getId()))
                .getResult();

        CellIdAndField inputCell = inputRow.getCells().stream()
                .filter(cell -> cell.getFieldId().equals(inputField.getId()))
                .findFirst().get();

        VavrAssert.assertValid(
                pipelineStepTestRowService.updateCellValue(
                        stepTest.getId(),
                        inputRow.getId(),
                        inputCell.getId(),
                        "12/03/2019"));

        StepTestRowView expectedRow = VavrAssert.assertValid(pipelineStepTestRowService
                .createExpectedDataRow(stepTest.getId(), outputSchema.getId()))
                .getResult();

        CellIdAndField outputCell = expectedRow.getCells().stream()
                .filter(cell -> cell.getFieldId().equals(outputField.getId()))
                .findFirst().get();

        VavrAssert.assertValid(
                pipelineStepTestRowService.updateCellValue(
                        stepTest.getId(), expectedRow.getId(), outputCell.getId(), "12/03/2019"));

        Dataset<Row> mockDataset = mockDataset(
                singletonList(dateRow(new GregorianCalendar(2019, Calendar.MARCH, 12).getTime())));

        when(mockSparkSqlExecutor.executeFullTransformation(any(), any()))
                .thenReturn(Validation.valid(mockDataset));

        VavrAssert.assertValid(pipelineStepTestService.runTest(stepTest.getId()))
                .extracting(TestRunResultView::getStatus)
                .withResult(PASS);

        StepTestView runTestResult = VavrAssert.assertValid(pipelineStepTestService.findById(stepTest.getId()))
                .getResult();

        List<StepRowOutputDataView> outputRows = pipelineStepTestService.getTestOutputRows(stepTest.getId());

        assertThat(runTestResult.getStatus())
                .isEqualTo(PASS);
        assertThat(outputRows)
                .hasSize(2);

        StepRowOutputDataView expected = outputRows.stream()
                .filter(row -> row.getType().equals("EXPECTED"))
                .findFirst().get();
        List<StepRowOutputDataView> actual = outputRows.stream()
                .filter(row -> row.getType().equals("ACTUAL"))
                .collect(Collectors.toList());

        assertThat(actual).hasSize(1);

        soft.assertThat(expected.getStatus())
                .isEqualTo("MATCHED");
        soft.assertThat(expected.isRun())
                .isEqualTo(true);
        assertThat(expected.getCells())
                .hasSize(1);
        StepTestCellView expectedCell = expected.getCells().values().iterator().next();
        soft.assertThat(expectedCell.getData())
                .isEqualTo("12/03/2019");
    }

    private static Row fullNameRow(final String value) {
        StructType structType = new StructType(new StructField[]{
                new StructField("FULL_NAME", DataTypes.StringType, false, Metadata.empty())
        });

        return new GenericRowWithSchema(new String[]{ value }, structType);
    }

    private static Row intRow(final Integer value) {
        StructType structType = new StructType(new StructField[]{
                new StructField("INT_OUTPUT", DataTypes.IntegerType, false, Metadata.empty())
        });

        return new GenericRowWithSchema(new Integer[]{ value }, structType);
    }

    private static Row dateRow(final Date value) {
        StructType structType = new StructType(new StructField[]{
                new StructField("DATE_OUTPUT", DataTypes.DateType, false, Metadata.empty())
        });

        return new GenericRowWithSchema(new Date[]{ value }, structType);
    }

    private static Row longRow(final Long value) {
        StructType structType = new StructType(new StructField[]{
                new StructField("LONG_OUTPUT", DataTypes.LongType, false, Metadata.empty())
        });

        return new GenericRowWithSchema(new Long[]{ value }, structType);
    }

    private Dataset<Row> mockDataset(final List<Row> listValues) {
        Dataset dataset = mock(Dataset.class);
        when(dataset.select(any(Seq.class))).thenReturn(dataset);
        when(dataset.collectAsList()).thenReturn(listValues);
        return dataset;
    }
}
