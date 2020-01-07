package com.lombardrisk.ignis.design.server.pipeline.test;

import com.lombardrisk.ignis.client.design.pipeline.PipelineStepView;
import com.lombardrisk.ignis.client.design.pipeline.select.SelectRequest;
import com.lombardrisk.ignis.client.design.pipeline.test.request.CreateStepTestRequest;
import com.lombardrisk.ignis.client.design.pipeline.test.view.StepRowInputDataView;
import com.lombardrisk.ignis.client.design.pipeline.test.view.StepTestCellView;
import com.lombardrisk.ignis.client.design.pipeline.test.view.StepTestRowView;
import com.lombardrisk.ignis.client.design.schema.field.FieldDto;
import com.lombardrisk.ignis.common.assertions.VavrAssert;
import com.lombardrisk.ignis.data.common.Identifiable;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.data.common.utils.CsvUtils;
import com.lombardrisk.ignis.design.field.DesignField;
import com.lombardrisk.ignis.design.field.FieldService;
import com.lombardrisk.ignis.design.server.fixtures.Design;
import com.lombardrisk.ignis.design.server.fixtures.Design.Populated;
import com.lombardrisk.ignis.design.server.pipeline.PipelineService;
import com.lombardrisk.ignis.design.server.pipeline.PipelineStepExecutor;
import com.lombardrisk.ignis.design.server.pipeline.PipelineStepService;
import com.lombardrisk.ignis.design.server.pipeline.api.PipelineStepTestRowRepository;
import com.lombardrisk.ignis.design.server.pipeline.fixture.PipelineServiceFixtureFactory;
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
import org.apache.spark.sql.types.StructType;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static com.lombardrisk.ignis.design.field.DesignField.Populated.longFieldRequest;
import static com.lombardrisk.ignis.design.field.DesignField.Populated.stringFieldRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PipelineStepTestRowServiceTest {

    @Rule
    public JUnitSoftAssertions soft = new JUnitSoftAssertions();

    private SchemaService schemaService;
    private FieldService fieldService;
    private PipelineService pipelineService;
    private PipelineStepService pipelineStepService;
    private PipelineStepTestService pipelineStepTestService;
    private PipelineStepTestRowService pipelineStepTestRowService;
    private PipelineStepExecutor mockSparkSqlExecutor;
    private Integer csvImportMaxLines = 100;

    @Mock
    PipelineStepTestRowRepository mockPipelineStepTestRowRepository;

    @Before
    public void setUp() {

        SchemaServiceFixtureFactory schemaServiceFixtureFactory = SchemaServiceFixtureFactory.create();
        ProductConfigService productConfigService =
                ProductServiceFixtureFactory.create(schemaServiceFixtureFactory, csvImportMaxLines)
                        .getProductService();
        schemaService = schemaServiceFixtureFactory.getSchemaService();
        fieldService = schemaServiceFixtureFactory.getFieldService();

        PipelineServiceFixtureFactory pipelineServiceFixtureFactory =
                PipelineServiceFixtureFactory.create(schemaServiceFixtureFactory, csvImportMaxLines);
        PipelineServiceFixtureFactory.TestHelpers testHelpers = pipelineServiceFixtureFactory.getTestHelpers();

        PipelineServiceFixtureFactory.Exports exports = pipelineServiceFixtureFactory.getExports();
        pipelineService = exports.getPipelineService();
        pipelineStepService = exports.getPipelineStepService();
        pipelineStepTestService = exports.getPipelineStepTestService();
        pipelineStepTestRowService = exports.getPipelineStepTestRowService();
        mockSparkSqlExecutor = testHelpers.getPipelineStepExecutor();

        mockPipelineStepTestRowRepository = mock(PipelineStepTestRowRepository.class);
    }

    @Test
    public void createInputDataRow_PipelineStepTestFound_ReturnsView() {
        Long pipelineOne = VavrAssert.assertValid(
                pipelineService.saveNewPipeline(Populated.createPipelineRequest()
                        .name("PipelineOne")
                        .build()))
                .getResult()
                .getId();

        Schema inputSchema = schemaService.createNew(Populated.schema("a").build());

        fieldService.save(inputSchema.getId(), longFieldRequest().name("MENU_ID").build()).get();
        fieldService.save(inputSchema.getId(), stringFieldRequest().name("MENU_NAME").build()).get();

        Schema outputSchema = schemaService.createNew(Populated.schema("b").build());

        fieldService.save(outputSchema.getId(), longFieldRequest().name("DISH_ID").build()).get();
        fieldService.save(outputSchema.getId(), stringFieldRequest().name("DISH_NAME").build()).get();

        PipelineStepView pipelineStepView = VavrAssert.assertValid(
                pipelineStepService.savePipelineStep(pipelineOne, Populated.pipelineMapStepRequest()
                        .schemaInId(inputSchema.getId())
                        .schemaOutId(outputSchema.getId())
                        .build()))
                .getResult();

        Identifiable stepTestView = VavrAssert.assertValid(
                pipelineStepTestService.create(CreateStepTestRequest.builder()
                        .name("hello")
                        .description("Just testing")
                        .pipelineStepId(pipelineStepView.getId())
                        .build()))
                .getResult();

        StepTestRowView stepTestRowView = VavrAssert.assertValid(
                pipelineStepTestRowService.createInputDataRow(stepTestView.getId(), inputSchema.getId()))
                .getResult();

        soft.assertThat(stepTestRowView.getId())
                .isNotNull();
        soft.assertThat(stepTestRowView.getCells())
                .extracting(StepTestRowView.CellIdAndField::getFieldId)
                .containsOnly(1L, 2L);
    }

    @Test
    public void createExpectedDataRow_PipelineStepTestFound_ReturnsView() {
        Long pipelineOne = VavrAssert.assertValid(pipelineService.saveNewPipeline(
                Populated.createPipelineRequest().name("PipelineOne").build()))
                .getResult().getId();

        Schema input = schemaService.createNew(Populated.schema("a").build());

        fieldService.save(input.getId(), longFieldRequest().name("MENU_ID").build()).get();
        fieldService.save(input.getId(), stringFieldRequest().name("MENU_NAME").build()).get();

        Schema output = schemaService.createNew(Populated.schema("b").build());

        fieldService.save(output.getId(), longFieldRequest().name("DISH_ID").build()).get();
        fieldService.save(output.getId(), stringFieldRequest().name("DISH_NAME").build()).get();

        PipelineStepView pipelineStepView = VavrAssert.assertValid(
                pipelineStepService.savePipelineStep(pipelineOne, Populated.pipelineMapStepRequest()
                        .schemaInId(input.getId())
                        .schemaOutId(output.getId())
                        .build()))
                .getResult();

        Identifiable stepTestView = VavrAssert.assertValid(
                pipelineStepTestService.create(CreateStepTestRequest.builder()
                        .name("hello")
                        .description("Just testing")
                        .pipelineStepId(pipelineStepView.getId())
                        .build()))
                .getResult();

        StepTestRowView stepTestRowView = VavrAssert.assertValid(
                pipelineStepTestRowService.createExpectedDataRow(stepTestView.getId(), output.getId()))
                .getResult();

        soft.assertThat(stepTestRowView.getId())
                .isNotNull();
        soft.assertThat(stepTestRowView.getCells())
                .extracting(StepTestRowView.CellIdAndField::getFieldId)
                .containsOnly(3L, 4L);
    }

    @Test
    public void updateCellValue_RowAndCellExist_UpdatesValue() {
        Long pipelineId = VavrAssert.assertValid(
                pipelineService.saveNewPipeline(Populated.createPipelineRequest().build()))
                .getResult()
                .getId();

        Schema a = schemaService.createNew(Populated.schema("a").build());
        fieldService.save(a.getId(), stringFieldRequest("NAME").build()).get();

        Schema b = schemaService.createNew(Populated.schema("b").build());

        PipelineStepView pipelineStepView = VavrAssert.assertValid(pipelineStepService.savePipelineStep(
                pipelineId, Populated.pipelineMapStepRequest()
                        .schemaInId(a.getId())
                        .schemaOutId(b.getId())
                        .build()))
                .getResult();

        Identifiable stepTestView = VavrAssert.assertValid(pipelineStepTestService.create(
                CreateStepTestRequest.builder()
                        .name("hello")
                        .description("Just testing")
                        .pipelineStepId(pipelineStepView.getId())
                        .build()))
                .getResult();

        StepTestRowView createdRow = VavrAssert.assertValid(
                pipelineStepTestRowService.createInputDataRow(stepTestView.getId(), a.getId()))
                .getResult();

        VavrAssert.assertValid(pipelineStepTestRowService.updateCellValue(
                stepTestView.getId(), createdRow.getId(), createdRow.getCells().get(0).getId(),
                "HOMER"));

        List<StepRowInputDataView> stepRowInputDataViews = VavrAssert
                .assertValid(pipelineStepTestService.getTestInputRows(stepTestView.getId()))
                .getResult()
                .get(a.getId());

        assertThat(stepRowInputDataViews.get(0).getCells().values())
                .extracting(StepTestCellView::getData)
                .containsExactly("HOMER");
    }

    @Test
    public void updateCellValue_InputRowAndCellExist_ReturnsUpdatedTestRowView() {
        Long pipelineId = VavrAssert.assertValid(
                pipelineService.saveNewPipeline(Populated.createPipelineRequest().build()))
                .getResult()
                .getId();

        Schema a = schemaService.createNew(Populated.schema("a")
                .fields(newHashSet(DesignField.Populated.stringField("NAME").build()))
                .build());

        FieldDto schemaAField = fieldService.save(a.getId(), stringFieldRequest("NAME").build()).get();

        Schema b = schemaService.createNew(Populated.schema("b").build());

        PipelineStepView pipelineStepView = VavrAssert.assertValid(pipelineStepService.savePipelineStep(
                pipelineId, Populated.pipelineMapStepRequest()
                        .schemaInId(a.getId())
                        .schemaOutId(b.getId())
                        .build()))
                .getResult();

        Identifiable stepTestView = VavrAssert.assertValid(pipelineStepTestService.create(
                CreateStepTestRequest.builder()
                        .name("hello")
                        .description("Just testing")
                        .pipelineStepId(pipelineStepView.getId())
                        .build()))
                .getResult();

        StepTestRowView createdRow = VavrAssert.assertValid(
                pipelineStepTestRowService.createInputDataRow(stepTestView.getId(), a.getId()))
                .getResult();

        StepTestRowView updatedRow = VavrAssert.assertValid(pipelineStepTestRowService
                .updateCellValue(
                        stepTestView.getId(), createdRow.getId(), createdRow.getCells().get(0).getId(), "HOMER"))
                .getResult();

        soft.assertThat(updatedRow.getId())
                .isEqualTo(createdRow.getId());

        soft.assertThat(updatedRow.getCells())
                .extracting(StepTestRowView.CellIdAndField::getId)
                .containsExactly(createdRow.getCells().get(0).getId());

        soft.assertThat(updatedRow.getCells())
                .extracting(StepTestRowView.CellIdAndField::getFieldId)
                .containsExactly(schemaAField.getId());

        soft.assertThat(updatedRow.getCells())
                .extracting(StepTestRowView.CellIdAndField::getCellData)
                .containsExactly("HOMER");
    }

    @Test
    public void updateCellValue_ExpectedRowAndCellExist_ReturnsUpdatedTestView() {
        Long pipelineId = VavrAssert.assertValid(
                pipelineService.saveNewPipeline(Populated.createPipelineRequest().build()))
                .getResult()
                .getId();

        Schema a = schemaService.createNew(Populated.schema("a").build());
        FieldDto schemaAField = fieldService.save(a.getId(), stringFieldRequest("NAME").build()).get();

        Schema b = schemaService.createNew(Populated.schema("b")
                .fields(newHashSet(DesignField.Populated.stringField("NAME").build()))
                .build());
        FieldDto schemaBField = fieldService.save(b.getId(), stringFieldRequest("NAME").build()).get();

        PipelineStepView pipelineStepView = VavrAssert.assertValid(pipelineStepService.savePipelineStep(
                pipelineId, Populated.pipelineMapStepRequest()
                        .schemaInId(a.getId())
                        .schemaOutId(b.getId())
                        .build()))
                .getResult();

        Identifiable stepTestView = VavrAssert.assertValid(pipelineStepTestService.create(
                CreateStepTestRequest.builder()
                        .name("hello")
                        .description("Just testing")
                        .pipelineStepId(pipelineStepView.getId())
                        .build()))
                .getResult();

        StepTestRowView createdRow = VavrAssert.assertValid(
                pipelineStepTestRowService.createExpectedDataRow(stepTestView.getId(), b.getId()))
                .getResult();

        StepTestRowView updatedRow = VavrAssert
                .assertValid(pipelineStepTestRowService.updateCellValue(
                        stepTestView.getId(), createdRow.getId(), createdRow.getCells().get(0).getId(), "HOMER"))
                .getResult();

        soft.assertThat(updatedRow.getId())
                .isEqualTo(createdRow.getId());

        soft.assertThat(updatedRow.getCells())
                .extracting(StepTestRowView.CellIdAndField::getId)
                .containsExactly(createdRow.getCells().get(0).getId());

        soft.assertThat(updatedRow.getCells())
                .extracting(StepTestRowView.CellIdAndField::getFieldId)
                .containsExactly(schemaBField.getId());

        soft.assertThat(updatedRow.getCells())
                .extracting(StepTestRowView.CellIdAndField::getCellData)
                .containsExactly("HOMER");
    }

    @Test
    public void updateCellValue_RowHasBeenRunThroughTest_UpdatesRowToNotRun() {
        Long pipelineId = VavrAssert.assertValid(
                pipelineService.saveNewPipeline(Populated.createPipelineRequest().build()))
                .getResult()
                .getId();

        Schema a = schemaService.createNew(Populated.schema("a").build());
        FieldDto schemaAField = fieldService.save(a.getId(), stringFieldRequest("NAME").build()).get();

        Schema b = schemaService.createNew(Populated.schema("b").build());

        PipelineStepView pipelineStepView = VavrAssert.assertValid(pipelineStepService.savePipelineStep(
                pipelineId, Populated.pipelineMapStepRequest()
                        .schemaInId(a.getId())
                        .schemaOutId(b.getId())
                        .build()))
                .getResult();

        Identifiable stepTestView = VavrAssert.assertValid(pipelineStepTestService.create(
                CreateStepTestRequest.builder()
                        .name("hello")
                        .description("Just testing")
                        .pipelineStepId(pipelineStepView.getId())
                        .build()))
                .getResult();

        StepTestRowView createdRow = VavrAssert.assertValid(
                pipelineStepTestRowService.createInputDataRow(stepTestView.getId(), a.getId()))
                .getResult();

        Dataset dataset = mock(Dataset.class);
        when(dataset.collectAsList()).thenReturn(Collections.emptyList());

        when(mockSparkSqlExecutor.executeFullTransformation(any(), any()))
                .thenReturn(Validation.valid(dataset));

        VavrAssert.assertValid(pipelineStepTestService.runTest(stepTestView.getId()));

        StepRowInputDataView rowAfterTestRun =
                VavrAssert.assertValid(pipelineStepTestService.getTestInputRows(stepTestView.getId()))
                        .getResult()
                        .get(a.getId())
                        .get(0);

        assertThat(rowAfterTestRun.isRun())
                .isTrue();

        VavrAssert.assertValid(pipelineStepTestRowService.updateCellValue(
                stepTestView.getId(), createdRow.getId(), createdRow.getCells().get(0).getId(), "HOMER"));

        StepRowInputDataView rowAfterCellUpdate =
                VavrAssert.assertValid(pipelineStepTestService.getTestInputRows(stepTestView.getId()))
                        .getResult()
                        .get(a.getId())
                        .get(0);

        assertThat(rowAfterCellUpdate.isRun())
                .isFalse();
    }

    @Test
    public void updateCellValue_RowExistsButCellDoesNot_ReturnsError() {
        Long pipelineId = VavrAssert.assertValid(
                pipelineService.saveNewPipeline(Populated.createPipelineRequest().build()))
                .getResult()
                .getId();

        Schema inputSchema = schemaService.createNew(Populated.schema("a").build());
        fieldService.save(inputSchema.getId(), stringFieldRequest("NAME").build()).get();

        Schema outputSchema = schemaService.createNew(Populated.schema("b").build());

        PipelineStepView pipelineStepView = VavrAssert.assertValid(pipelineStepService.savePipelineStep(
                pipelineId, Populated.pipelineMapStepRequest()
                        .schemaInId(inputSchema.getId())
                        .schemaOutId(outputSchema.getId())
                        .build()))
                .getResult();

        Identifiable stepTestView = VavrAssert.assertValid(pipelineStepTestService.create(
                CreateStepTestRequest.builder()
                        .name("hello")
                        .description("Just testing")
                        .pipelineStepId(pipelineStepView.getId())
                        .build()))
                .getResult();

        Long inputRowId = VavrAssert.assertValid(
                pipelineStepTestRowService.createInputDataRow(stepTestView.getId(), inputSchema.getId()))
                .getResult().getId();

        VavrAssert.assertFailed(
                pipelineStepTestRowService.updateCellValue(stepTestView.getId(), inputRowId, -100L, "HOMER"))
                .withFailure(CRUDFailure.notFoundIds("PipelineStepTestCell", -100L));
    }

    @Test
    public void updateCellValue_RowDoesNotExist_ReturnsError() {
        VavrAssert.assertFailed(
                pipelineStepTestRowService.updateCellValue(-1L, -10L, -100L, "HOMER"))
                .withFailure(CRUDFailure.notFoundIds("PipelineStepTestRow", -10L));
    }

    @Test
    public void updateCellValue_IncorrectFormatInCell_ReturnsError() {
        Long pipelineId = VavrAssert.assertValid(
                pipelineService.saveNewPipeline(Design.Populated.createPipelineRequest().build()))
                .getResult()
                .getId();

        Schema a = schemaService.createNew(Design.Populated.schema("a").build());
        Schema b = schemaService.createNew(Design.Populated.schema("b").build());

        FieldDto schemaAField = fieldService.save(
                a.getId(),
                DesignField.Populated.dateFieldRequest("ADATE").build()).get();

        FieldDto schemaBField = fieldService.save(
                b.getId(),
                DesignField.Populated.dateFieldRequest("BDATE")
                        .format("dd/MM/yyyy HH:MM:SS")
                        .build()).get();

        PipelineStepView pipelineStepView = VavrAssert.assertValid(pipelineStepService.savePipelineStep(
                pipelineId, Design.Populated.pipelineMapStepRequest()
                        .schemaInId(a.getId())
                        .schemaOutId(b.getId())
                        .build()))
                .getResult();

        Identifiable stepTestView = VavrAssert.assertValid(pipelineStepTestService.create(
                CreateStepTestRequest.builder()
                        .name("hello")
                        .description("Just testing")
                        .pipelineStepId(pipelineStepView.getId())
                        .build()))
                .getResult();

        StepTestRowView expectedRow = VavrAssert.assertValid(
                pipelineStepTestRowService.createExpectedDataRow(stepTestView.getId(), b.getId()))
                .getResult();

        VavrAssert.assertFailed(pipelineStepTestRowService.updateCellValue(
                stepTestView.getId(), expectedRow.getId(), expectedRow.getCells().get(0).getId(), "2019/10/18"))
                .withFailure(CRUDFailure.invalidParameters()
                        .paramError(
                                "BDATE : 2019/10/18",
                                "Failed to parse date: 2019/10/18 using format: dd/MM/yyyy HH:MM:SS").asFailure());
    }

    @Test
    public void updateCellValue_CorrectFormatInCell_ReturnsUpdatedTestView() {
        Long pipelineId = VavrAssert.assertValid(
                pipelineService.saveNewPipeline(Design.Populated.createPipelineRequest().build()))
                .getResult()
                .getId();

        Schema a = schemaService.createNew(Design.Populated.schema("a")
                .build());
        Schema b = schemaService.createNew(Design.Populated.schema("b")
                .build());

        FieldDto schemaAField = fieldService.save(
                a.getId(),
                DesignField.Populated.dateFieldRequest("ADATE").build()).get();

        FieldDto schemaBField = fieldService.save(
                b.getId(),
                DesignField.Populated.dateFieldRequest("BDATE")
                        .format("dd/MM/yyyy HH:MM:SS")
                        .build()).get();

        PipelineStepView pipelineStepView = VavrAssert.assertValid(pipelineStepService.savePipelineStep(
                pipelineId, Design.Populated.pipelineMapStepRequest()
                        .schemaInId(a.getId())
                        .schemaOutId(b.getId())
                        .build()))
                .getResult();

        Identifiable stepTestView = VavrAssert.assertValid(pipelineStepTestService.create(
                CreateStepTestRequest.builder()
                        .name("hello")
                        .description("Just testing")
                        .pipelineStepId(pipelineStepView.getId())
                        .build()))
                .getResult();

        StepTestRowView createdRow = VavrAssert.assertValid(
                pipelineStepTestRowService.createExpectedDataRow(stepTestView.getId(), b.getId()))
                .getResult();

        StepTestRowView updatedRow = VavrAssert
                .assertValid(pipelineStepTestRowService.updateCellValue(
                        stepTestView.getId(), createdRow.getId(), createdRow.getCells().get(0).getId(),
                        "01/01/2019 12:00:00"))
                .getResult();

        soft.assertThat(updatedRow.getId())
                .isEqualTo(createdRow.getId());

        soft.assertThat(updatedRow.getCells())
                .extracting(StepTestRowView.CellIdAndField::getId)
                .containsExactly(createdRow.getCells().get(0).getId());

        soft.assertThat(updatedRow.getCells())
                .extracting(StepTestRowView.CellIdAndField::getFieldId)
                .containsExactly(schemaBField.getId());

        soft.assertThat(updatedRow.getCells())
                .extracting(StepTestRowView.CellIdAndField::getCellData)
                .containsExactly("01/01/2019 12:00:00");
    }

    @Test
    public void deleteRow_RowAndCellExist_DeletesRowAndCell() {
        Long pipelineId = VavrAssert.assertValid(
                pipelineService.saveNewPipeline(Populated.createPipelineRequest().build()))
                .getResult()
                .getId();

        Schema a = schemaService.createNew(Populated.schema("a").build());
        FieldDto schemaAField = fieldService.save(a.getId(), stringFieldRequest("NAME").build()).get();
        Schema b = schemaService.createNew(Populated.schema("b").build());

        PipelineStepView pipelineStepView = VavrAssert.assertValid(pipelineStepService.savePipelineStep(
                pipelineId, Populated.pipelineMapStepRequest()
                        .schemaInId(a.getId())
                        .schemaOutId(b.getId())
                        .build()))
                .getResult();

        Identifiable stepTestView = VavrAssert.assertValid(pipelineStepTestService.create(
                CreateStepTestRequest.builder()
                        .name("hello")
                        .description("Just testing")
                        .pipelineStepId(pipelineStepView.getId())
                        .build()))
                .getResult();

        StepTestRowView createdRow = VavrAssert.assertValid(
                pipelineStepTestRowService.createInputDataRow(stepTestView.getId(), a.getId()))
                .getResult();

        VavrAssert.assertValid(
                pipelineStepTestRowService.updateCellValue(
                        stepTestView.getId(), createdRow.getId(),
                        createdRow.getCells().get(0).getId(),
                        "HOMER"));

        Map<Long, List<StepRowInputDataView>> inputRowsBeforeDeleting = VavrAssert
                .assertValid(pipelineStepTestService.getTestInputRows(stepTestView.getId()))
                .getResult();

        assertThat(inputRowsBeforeDeleting.get(a.getId()))
                .isNotEmpty();

        StepRowInputDataView retrievedRow = inputRowsBeforeDeleting.get(a.getId()).get(0);

        VavrAssert.assertValid(
                pipelineStepTestRowService.deleteInput(retrievedRow.getId()));

        Map<Long, List<StepRowInputDataView>> inputRowsAfterDeleting = VavrAssert
                .assertValid(pipelineStepTestService.getTestInputRows(stepTestView.getId()))
                .getResult();

        assertThat(inputRowsAfterDeleting.get(a.getId()))
                .isEmpty();
    }

    @Test
    public void importCsvInputDataRow_withValidCsvContent_savesCsvRows() {
        Long pipelineOne = VavrAssert.assertValid(
                pipelineService.saveNewPipeline(Populated.createPipelineRequest()
                        .name("PipelineOne")
                        .build()))
                .getResult()
                .getId();

        Schema inputSchema = schemaService.createNew(Populated.schema("a").build());

        fieldService.save(inputSchema.getId(), longFieldRequest().name("MENU_ID").build()).get();
        fieldService.save(inputSchema.getId(), stringFieldRequest().name("MENU_NAME").build()).get();

        Schema outputSchema = schemaService.createNew(Populated.schema("b").build());

        fieldService.save(outputSchema.getId(), longFieldRequest().name("DISH_ID").build()).get();
        fieldService.save(outputSchema.getId(), stringFieldRequest().name("DISH_NAME").build()).get();

        PipelineStepView pipelineStepView = VavrAssert.assertValid(
                pipelineStepService.savePipelineStep(pipelineOne, Populated.pipelineMapStepRequest()
                        .schemaInId(inputSchema.getId())
                        .schemaOutId(outputSchema.getId())
                        .build()))
                .getResult();

        Identifiable stepTestView = VavrAssert.assertValid(
                pipelineStepTestService.create(CreateStepTestRequest.builder()
                        .name("hello")
                        .description("Just testing")
                        .pipelineStepId(pipelineStepView.getId())
                        .build()))
                .getResult();

        final ByteArrayInputStream byteArrayInputStream =
                new ByteArrayInputStream("MENU_ID,MENU_NAME\n1352,PASTA".getBytes());

        List<StepTestRowView> result = VavrAssert.assertValid(
                pipelineStepTestRowService.importCsvInputDataRow(
                        stepTestView.getId(),
                        inputSchema.getId(),
                        byteArrayInputStream)).getResult();

        soft.assertThat(result)
                .isNotNull()
                .hasSize(1);

        soft.assertThat(result.get(0).getCells())
                .extracting(StepTestRowView.CellIdAndField::getCellData)
                .containsOnly("1352", "PASTA");
    }

    @Test
    public void importCvsInputDataRow_csvContentContainsLineNumberMoreThantTheAllowedNumber_returnsCrudFailer() {
        Long pipelineOne = VavrAssert.assertValid(
                pipelineService.saveNewPipeline(Populated.createPipelineRequest()
                        .name("PipelineOne")
                        .build()))
                .getResult()
                .getId();

        Schema inputSchema = schemaService.createNew(Populated.schema("a").build());

        fieldService.save(inputSchema.getId(), longFieldRequest().name("MENU_ID").build()).get();
        fieldService.save(inputSchema.getId(), stringFieldRequest().name("MENU_NAME").build()).get();

        Schema outputSchema = schemaService.createNew(Populated.schema("b").build());

        fieldService.save(outputSchema.getId(), longFieldRequest().name("DISH_ID").build()).get();
        fieldService.save(outputSchema.getId(), stringFieldRequest().name("DISH_NAME").build()).get();

        PipelineStepView pipelineStepView = VavrAssert.assertValid(
                pipelineStepService.savePipelineStep(pipelineOne, Populated.pipelineMapStepRequest()
                        .schemaInId(inputSchema.getId())
                        .schemaOutId(outputSchema.getId())
                        .build()))
                .getResult();

        Identifiable stepTestView = VavrAssert.assertValid(
                pipelineStepTestService.create(CreateStepTestRequest.builder()
                        .name("hello")
                        .description("Just testing")
                        .pipelineStepId(pipelineStepView.getId())
                        .build()))
                .getResult();

        final ByteArrayInputStream byteArrayInputStream =
                new ByteArrayInputStream(String.format(
                        "MENU_ID,MENU_NAME\n%s",
                        buildCsvContent(csvImportMaxLines + 1))
                        .getBytes());

        Validation<CRUDFailure, List<StepTestRowView>> result = pipelineStepTestRowService.importCsvInputDataRow(
                stepTestView.getId(),
                inputSchema.getId(),
                byteArrayInputStream);

        assertThat(result.getError())
                .isEqualTo(CRUDFailure.constraintFailure(
                        "Csv file would contain more lines [101] than max import lines allows [100]"));
    }

    @Test
    public void importCvsExpectedDataRow_csvContentContainsLineNumberMoreThantTheAllowedNumber_returnsCrudFailer() {
        Long pipelineOne = VavrAssert.assertValid(
                pipelineService.saveNewPipeline(Populated.createPipelineRequest()
                        .name("PipelineOne")
                        .build()))
                .getResult()
                .getId();

        Schema inputSchema = schemaService.createNew(Populated.schema("a").build());

        fieldService.save(inputSchema.getId(), longFieldRequest().name("MENU_ID").build()).get();
        fieldService.save(inputSchema.getId(), stringFieldRequest().name("MENU_NAME").build()).get();

        Schema outputSchema = schemaService.createNew(Populated.schema("b").build());

        fieldService.save(outputSchema.getId(), longFieldRequest().name("DISH_ID").build()).get();
        fieldService.save(outputSchema.getId(), stringFieldRequest().name("DISH_NAME").build()).get();

        PipelineStepView pipelineStepView = VavrAssert.assertValid(
                pipelineStepService.savePipelineStep(pipelineOne, Populated.pipelineMapStepRequest()
                        .schemaInId(inputSchema.getId())
                        .schemaOutId(outputSchema.getId())
                        .build()))
                .getResult();

        Identifiable stepTestView = VavrAssert.assertValid(
                pipelineStepTestService.create(CreateStepTestRequest.builder()
                        .name("hello")
                        .description("Just testing")
                        .pipelineStepId(pipelineStepView.getId())
                        .build()))
                .getResult();

        final ByteArrayInputStream byteArrayInputStream =
                new ByteArrayInputStream(String.format(
                        "DISH_ID,DISH_NAME\n%s",
                        buildCsvContent(csvImportMaxLines + 1))
                        .getBytes());

        Validation<CRUDFailure, List<StepTestRowView>> result = pipelineStepTestRowService.importCsvExpectedDataRow(
                stepTestView.getId(),
                outputSchema.getId(),
                byteArrayInputStream);

        assertThat(result.getError())
                .isEqualTo(CRUDFailure.constraintFailure(
                        "Csv file would contain more lines [101] than max import lines allows [100]"));
    }

    @Test
    public void importCsvExpectedDataRow_withValidCsvContent_savesCsvRows() {
        Long pipelineOne = VavrAssert.assertValid(
                pipelineService.saveNewPipeline(Populated.createPipelineRequest()
                        .name("PipelineOne")
                        .build()))
                .getResult()
                .getId();

        Schema inputSchema = schemaService.createNew(Populated.schema("a").build());

        fieldService.save(inputSchema.getId(), longFieldRequest().name("MENU_ID").build()).get();
        fieldService.save(inputSchema.getId(), stringFieldRequest().name("MENU_NAME").build()).get();

        Schema outputSchema = schemaService.createNew(Populated.schema("b").build());

        fieldService.save(outputSchema.getId(), longFieldRequest().name("DISH_ID").build()).get();
        fieldService.save(outputSchema.getId(), stringFieldRequest().name("DISH_NAME").build()).get();

        PipelineStepView pipelineStepView = VavrAssert.assertValid(
                pipelineStepService.savePipelineStep(pipelineOne, Populated.pipelineMapStepRequest()
                        .schemaInId(inputSchema.getId())
                        .schemaOutId(outputSchema.getId())
                        .build()))
                .getResult();

        Identifiable stepTestView = VavrAssert.assertValid(
                pipelineStepTestService.create(CreateStepTestRequest.builder()
                        .name("hello")
                        .description("Just testing")
                        .pipelineStepId(pipelineStepView.getId())
                        .build()))
                .getResult();

        final ByteArrayInputStream byteArrayInputStream =
                new ByteArrayInputStream("DISH_ID,DISH_NAME\n1352,PASTA".getBytes());

        List<StepTestRowView> result = VavrAssert.assertValid(
                pipelineStepTestRowService.importCsvExpectedDataRow(
                        stepTestView.getId(),
                        outputSchema.getId(),
                        byteArrayInputStream)).getResult();

        soft.assertThat(result)
                .isNotNull()
                .hasSize(1);

        soft.assertThat(result.get(0).getCells())
                .extracting(StepTestRowView.CellIdAndField::getCellData)
                .containsOnly("1352", "PASTA");
    }

    @Test
    public void exportCsvInputDataRow_exportsCsvContent() {
        Long pipelineOne = VavrAssert.assertValid(
                pipelineService.saveNewPipeline(Populated.createPipelineRequest()
                        .name("PipelineOne")
                        .build()))
                .getResult()
                .getId();

        Schema inputSchema = schemaService.createNew(Populated.schema("a").build());

        fieldService.save(inputSchema.getId(), longFieldRequest().name("MENU_ID").build()).get();
        fieldService.save(inputSchema.getId(), stringFieldRequest().name("MENU_NAME").build()).get();

        Schema outputSchema = schemaService.createNew(Populated.schema("b").build());

        fieldService.save(outputSchema.getId(), longFieldRequest().name("DISH_ID").build()).get();
        fieldService.save(outputSchema.getId(), stringFieldRequest().name("DISH_NAME").build()).get();

        PipelineStepView pipelineStepView = VavrAssert.assertValid(
                pipelineStepService.savePipelineStep(pipelineOne, Populated.pipelineMapStepRequest()
                        .schemaInId(inputSchema.getId())
                        .schemaOutId(outputSchema.getId())
                        .build()))
                .getResult();

        Identifiable stepTestView = VavrAssert.assertValid(
                pipelineStepTestService.create(CreateStepTestRequest.builder()
                        .name("hello")
                        .description("Just testing")
                        .pipelineStepId(pipelineStepView.getId())
                        .build()))
                .getResult();

        final ByteArrayInputStream byteArrayInputStream =
                new ByteArrayInputStream("MENU_ID,MENU_NAME\n1352,PASTA".getBytes());

        pipelineStepTestRowService.importCsvInputDataRow(
                stepTestView.getId(),
                inputSchema.getId(),
                byteArrayInputStream);

        CsvUtils.CsvOutputStream csvOutputStream = VavrAssert.assertValid(
                pipelineStepTestRowService.exportCsvInputDataRow(
                        stepTestView.getId(),
                        inputSchema.getId(),
                        new ByteArrayOutputStream())).getResult();

        soft.assertThat(csvOutputStream.getFilename()).isEqualTo("input_a.csv");
        soft.assertThat(new String(csvOutputStream.getOutputStream().toString()))
                .isEqualTo("MENU_ID,MENU_NAME\n1352,PASTA");
    }

    @Test
    public void exportCsvExpectedDataRow_exportsCsvContent() {
        Long pipelineOne = VavrAssert.assertValid(
                pipelineService.saveNewPipeline(Populated.createPipelineRequest()
                        .name("PipelineOne")
                        .build()))
                .getResult()
                .getId();

        Schema inputSchema = schemaService.createNew(Populated.schema("a").build());

        fieldService.save(inputSchema.getId(), longFieldRequest().name("MENU_ID").build()).get();
        fieldService.save(inputSchema.getId(), stringFieldRequest().name("MENU_NAME").build()).get();

        Schema outputSchema = schemaService.createNew(Populated.schema("b").build());

        fieldService.save(outputSchema.getId(), longFieldRequest().name("DISH_ID").build()).get();
        fieldService.save(outputSchema.getId(), stringFieldRequest().name("DISH_NAME").build()).get();

        PipelineStepView pipelineStepView = VavrAssert.assertValid(
                pipelineStepService.savePipelineStep(pipelineOne, Populated.pipelineMapStepRequest()
                        .schemaInId(inputSchema.getId())
                        .schemaOutId(outputSchema.getId())
                        .build()))
                .getResult();

        Identifiable stepTestView = VavrAssert.assertValid(
                pipelineStepTestService.create(CreateStepTestRequest.builder()
                        .name("hello")
                        .description("Just testing")
                        .pipelineStepId(pipelineStepView.getId())
                        .build()))
                .getResult();

        final ByteArrayInputStream byteArrayInputStream =
                new ByteArrayInputStream("DISH_ID,DISH_NAME\n1352,PASTA".getBytes());

        pipelineStepTestRowService.importCsvExpectedDataRow(
                stepTestView.getId(),
                outputSchema.getId(),
                byteArrayInputStream);

        CsvUtils.CsvOutputStream csvOutputStream = VavrAssert.assertValid(
                pipelineStepTestRowService.exportCsvExpectedDataRow(
                        stepTestView.getId(),
                        outputSchema.getId(),
                        new ByteArrayOutputStream())).getResult();

        soft.assertThat(csvOutputStream.getFilename()).isEqualTo("expected_b.csv");
        soft.assertThat(new String(csvOutputStream.getOutputStream().toString()))
                .isEqualTo("DISH_ID,DISH_NAME\n1352,PASTA");
    }

    @Test
    public void exportCsvActualDataRow_exportsCsvContent() {
        Long pipelineOne = VavrAssert.assertValid(
                pipelineService.saveNewPipeline(Populated.createPipelineRequest()
                        .name("PipelineOne")
                        .build()))
                .getResult()
                .getId();

        Schema inputSchema = schemaService.createNew(Populated.schema("a").build());

        fieldService.save(inputSchema.getId(), longFieldRequest().id(1L).name("MENU_ID").build()).get();
        fieldService.save(inputSchema.getId(), stringFieldRequest().id(2L).name("MENU_NAME").build()).get();

        Schema outputSchema = schemaService.createNew(Populated.schema("b").build());

        fieldService.save(outputSchema.getId(), longFieldRequest().id(3L).name("DISH_ID").build()).get();
        fieldService.save(outputSchema.getId(), stringFieldRequest().id(4L).name("DISH_NAME").build()).get();

        Set<SelectRequest> selects = new HashSet<>();
        selects.add(SelectRequest.builder()
                .select("MENU_ID")
                .outputFieldId(3L)
                .build());

        selects.add(SelectRequest.builder()
                .select("MENU_NAME")
                .outputFieldId(4L)
                .build());

        PipelineStepView pipelineStepView = VavrAssert.assertValid(
                pipelineStepService.savePipelineStep(pipelineOne, Populated.pipelineMapStepRequest()
                        .schemaInId(inputSchema.getId())
                        .selects(selects)
                        .schemaOutId(outputSchema.getId())
                        .build()))
                .getResult();

        Identifiable stepTestView = VavrAssert.assertValid(
                pipelineStepTestService.create(CreateStepTestRequest.builder()
                        .name("hello")
                        .description("Just testing")
                        .pipelineStepId(pipelineStepView.getId())
                        .build()))
                .getResult();

        final ByteArrayInputStream byteArrayInputStream =
                new ByteArrayInputStream("MENU_ID,MENU_NAME\n1352,PASTA".getBytes());

        pipelineStepTestRowService.importCsvInputDataRow(
                stepTestView.getId(),
                inputSchema.getId(),
                byteArrayInputStream);

        Dataset dataset = mock(Dataset.class);
        StructType nameType = DataTypes.createStructType(com.google.common.collect.ImmutableList.of(
                DataTypes.createStructField("DISH_ID", DataTypes.LongType, false),
                DataTypes.createStructField("DISH_NAME", DataTypes.StringType, true)));

        com.google.common.collect.ImmutableList<Row> data = com.google.common.collect.ImmutableList.of(
                new GenericRowWithSchema(new Object[]{ 1352L, "PASTA" }, nameType));

        when(dataset.collectAsList()).thenReturn(data);

        when(mockSparkSqlExecutor.executeFullTransformation(any(), any()))
                .thenReturn(Validation.valid(dataset));

        pipelineStepTestService.runTest(stepTestView.getId());

        CsvUtils.CsvOutputStream csvOutputStream = VavrAssert.assertValid(
                pipelineStepTestRowService.exportCsvActualDataRow(
                        stepTestView.getId(),
                        outputSchema.getId(),
                        new ByteArrayOutputStream())).getResult();

        soft.assertThat(csvOutputStream.getFilename()).isEqualTo("actual_b.csv");
        soft.assertThat(new String(csvOutputStream.getOutputStream().toString()))
                .isEqualTo("DISH_ID,DISH_NAME\n1352,PASTA");
    }

    @Test
    public void export_cellsDataWithNullValue_exportsEmptyString() {
        Long pipelineOne = VavrAssert.assertValid(
                pipelineService.saveNewPipeline(Populated.createPipelineRequest()
                        .name("PipelineOne")
                        .build()))
                .getResult()
                .getId();

        Schema inputSchema = schemaService.createNew(Populated.schema("a").build());

        fieldService.save(inputSchema.getId(), longFieldRequest().name("MENU_ID").build()).get();
        fieldService.save(inputSchema.getId(), stringFieldRequest().name("MENU_NAME").build()).get();

        Schema outputSchema = schemaService.createNew(Populated.schema("b").build());

        fieldService.save(outputSchema.getId(), longFieldRequest().name("DISH_ID").build()).get();
        fieldService.save(outputSchema.getId(), stringFieldRequest().name("DISH_NAME").build()).get();

        PipelineStepView pipelineStepView = VavrAssert.assertValid(
                pipelineStepService.savePipelineStep(pipelineOne, Populated.pipelineMapStepRequest()
                        .schemaInId(inputSchema.getId())
                        .schemaOutId(outputSchema.getId())
                        .build()))
                .getResult();

        Identifiable stepTestView = VavrAssert.assertValid(
                pipelineStepTestService.create(CreateStepTestRequest.builder()
                        .name("hello")
                        .description("Just testing")
                        .pipelineStepId(pipelineStepView.getId())
                        .build()))
                .getResult();

        StepTestRowView stepTestRowView = VavrAssert.assertValid(
                pipelineStepTestRowService.createInputDataRow(stepTestView.getId(), inputSchema.getId()))
                .getResult();

        StepTestRowView updatedRow1 = VavrAssert.assertValid(pipelineStepTestRowService
                .updateCellValue(
                        stepTestView.getId(), stepTestRowView.getId(), stepTestRowView.getCells().get(0).getId(), null))
                .getResult();

        StepTestRowView updatedRow2 = VavrAssert.assertValid(pipelineStepTestRowService
                .updateCellValue(
                        stepTestView.getId(),
                        stepTestRowView.getId(),
                        stepTestRowView.getCells().get(1).getId(),
                        null))
                .getResult();

        CsvUtils.CsvOutputStream csvOutputStream = VavrAssert.assertValid(
                pipelineStepTestRowService.exportCsvInputDataRow(
                        stepTestView.getId(),
                        inputSchema.getId(),
                        new ByteArrayOutputStream())).getResult();

        soft.assertThat(csvOutputStream.getFilename()).isEqualTo("input_a.csv");
        soft.assertThat(new String(csvOutputStream.getOutputStream().toString()))
                .isEqualTo("MENU_ID,MENU_NAME\n,");
    }

    private String buildCsvContent(final int linesNumber) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < linesNumber; i++) {
            sb.append(String.format("%s,PASTA %s", i, i));
            sb.append("\n");
        }
        return sb.toString();
    }
}
