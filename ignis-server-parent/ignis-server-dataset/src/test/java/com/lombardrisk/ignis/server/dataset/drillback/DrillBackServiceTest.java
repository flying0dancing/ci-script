package com.lombardrisk.ignis.server.dataset.drillback;

import com.google.common.collect.ImmutableMap;
import com.lombardrisk.ignis.client.core.page.response.Page;
import com.lombardrisk.ignis.client.external.drillback.DatasetRowDataView;
import com.lombardrisk.ignis.client.external.drillback.DrillBackStepDetails;
import com.lombardrisk.ignis.client.external.productconfig.view.FieldView;
import com.lombardrisk.ignis.common.assertions.VavrAssert;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.pipeline.step.api.DrillbackColumnLink;
import com.lombardrisk.ignis.server.dataset.DatasetService;
import com.lombardrisk.ignis.server.dataset.fixture.DatasetPopulated;
import com.lombardrisk.ignis.server.dataset.pipeline.invocation.PipelineInvocationService;
import com.lombardrisk.ignis.server.dataset.result.DatasetResultRepository;
import com.lombardrisk.ignis.server.dataset.result.DatasetRowData;
import com.lombardrisk.ignis.server.product.fixture.ProductPopulated;
import com.lombardrisk.ignis.server.product.pipeline.PipelineService;
import com.lombardrisk.ignis.server.product.pipeline.PipelineStepService;
import com.lombardrisk.ignis.server.product.pipeline.model.PipelineMapStep;
import com.lombardrisk.ignis.server.product.table.TableService;
import io.vavr.control.Validation;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.api.Java6Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DrillBackServiceTest {

    @Rule
    public JUnitSoftAssertions soft = new JUnitSoftAssertions();

    @Mock
    private DatasetService datasetService;

    @Mock
    private DatasetResultRepository datasetResultRepository;

    @Mock
    private TableService tableService;

    @Mock
    private PipelineService pipelineService;

    @Mock
    private PipelineStepService pipelineStepService;

    @Mock
    private PipelineInvocationService pipelineInvocationService;

    @InjectMocks
    private DrillBackService drillBackService;

    @Before
    public void setUp() {
        when(pipelineService.findById(anyLong()))
                .thenReturn(Validation.valid(ProductPopulated.pipeline()
                        .steps(newHashSet(ProductPopulated.mapPipelineStep().id(123L).build()))
                        .build()));

        when(tableService.findWithValidation(anyLong()))
                .thenReturn(Validation.valid(ProductPopulated.table().build()));

        when(pipelineInvocationService.findByInvocationIdAndStepInvocationId(anyLong(), anyLong()))
                .thenReturn(Validation.valid(DatasetPopulated.pipelineStepInvocation()
                        .pipelineStep(ProductPopulated.mapPipelineStep().build())
                        .build()));

        when(pipelineService.findStepById(any(), any()))
                .thenReturn(Validation.valid(ProductPopulated.mapPipelineStep().build()));

        PageRequest pageRequest = PageRequest.of(2, 100);
        when(datasetResultRepository.findDrillbackInputDatasetRowData(any(), any(), any(), any(), any(), any()))
                .thenReturn(DatasetRowData.builder()
                        .resultData(new PageImpl<>(emptyList(), pageRequest, 6000))
                        .build());
    }

    @Test
    public void findDrillBackStepDetails_PipelineNotFound_ReturnsError() {
        when(pipelineService.findById(anyLong()))
                .thenReturn(Validation.invalid(CRUDFailure.notFoundIds("Pipeline", 1L)));

        VavrAssert.assertFailed(
                drillBackService.findDrillBackStepDetails(1L, 123L))
                .withFailure(CRUDFailure.notFoundIds("Pipeline", 1L));
    }

    @Test
    public void findDrillBackStepDetails_PipelineStepNotFound_ReturnsError() {

        when(pipelineService.findById(anyLong()))
                .thenReturn(Validation.valid(ProductPopulated.pipeline()
                        .steps(newHashSet(ProductPopulated.mapPipelineStep()
                                .id(848L)
                                .build()))
                        .build()));

        VavrAssert.assertFailed(
                drillBackService.findDrillBackStepDetails(1L, 700L))
                .withFailure(CRUDFailure.notFoundIds("PipelineStep", 700L));
    }

    @Test
    public void findDrillBackStepDetails_TablesNotFound_ThrowsIllegalStateException() {

        when(pipelineService.findById(anyLong()))
                .thenReturn(Validation.valid(ProductPopulated.pipeline()
                        .steps(newHashSet(ProductPopulated.mapPipelineStep().id(123L).build()))
                        .build()));

        when(tableService.findWithValidation(anyLong()))
                .thenReturn(Validation.invalid(CRUDFailure.notFoundIds("Table", 9828L)));

        assertThatThrownBy(() -> drillBackService.findDrillBackStepDetails(1L, 123L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot find schemas for pipeline step 123");
    }

    @Test
    public void findDrillBackStepDetails_TablesFound_ConvertsToFieldView() {

        when(pipelineService.findById(anyLong()))
                .thenReturn(Validation.valid(ProductPopulated.pipeline()
                        .steps(newHashSet(ProductPopulated.mapPipelineStep()
                                .id(123L)
                                .schemaInId(88L)
                                .schemaIn(ProductPopulated.schemaDetails().physicalTableName("INPUT").build())
                                .schemaOutId(99L)
                                .build()))
                        .build()));

        when(tableService.findWithValidation(88L))
                .thenReturn(Validation.valid(ProductPopulated.table()
                        .fields(newHashSet(ProductPopulated.decimalField("in").id(53L).build()))
                        .build()));
        when(tableService.findWithValidation(99L))
                .thenReturn(Validation.valid(ProductPopulated.table()
                        .fields(newHashSet(ProductPopulated.stringField("out").id(73L).build()))
                        .build()));

        DrillBackStepDetails stepDetails = VavrAssert.assertValid(
                drillBackService.findDrillBackStepDetails(1L, 123L))
                .getResult();

        List<FieldView> datasetInFields = stepDetails.getSchemasIn().get(88L);
        List<FieldView> datasetOutFields = stepDetails.getSchemasOut().get(99L);

        soft.assertThat(datasetInFields)
                .containsOnly(
                        FieldView.builder()
                                .id(53L)
                                .name("in")
                                .fieldType(FieldView.Type.DECIMAL)
                                .build());

        soft.assertThat(datasetOutFields)
                .containsOnly(
                        FieldView.builder()
                                .id(73L)
                                .name("out")
                                .fieldType(FieldView.Type.STRING)
                                .build());
    }

    @Test
    public void findDrillBackStepDetails_MultipleDrillbackColumnLinks_ReturnsDrillbackRowKeyMappings() {

        when(pipelineService.findById(anyLong()))
                .thenReturn(Validation.valid(ProductPopulated.pipeline()
                        .steps(newHashSet(ProductPopulated.aggregatePipelineStep()
                                .id(123L)
                                .schemaInId(88L)
                                .schemaIn(ProductPopulated.schemaDetails().physicalTableName("INPUT").build())
                                .schemaOut(ProductPopulated.schemaDetails().physicalTableName("OUTPUT").build())
                                .groupings(newHashSet("A", "B", "C", "D"))
                                .schemaOutId(99L)
                                .build()))
                        .build()));

        when(tableService.findWithValidation(88L))
                .thenReturn(Validation.valid(ProductPopulated.table()
                        .id(88L)
                        .fields(newHashSet(
                                ProductPopulated.stringField("A").build(),
                                ProductPopulated.stringField("B").build(),
                                ProductPopulated.stringField("C").build(),
                                ProductPopulated.stringField("D").build()))
                        .build()));
        when(tableService.findWithValidation(99L))
                .thenReturn(Validation.valid(ProductPopulated.table()
                        .id(99L)
                        .fields(newHashSet(ProductPopulated.stringField("out").id(73L).build()))
                        .build()));

        when(pipelineStepService.getDrillbackColumnLinks(any()))
                .thenReturn(newHashSet(
                        DrillbackColumnLink.builder()
                                .inputSchema("INPUT").outputSchema("OUTPUT").inputColumn("A").build(),
                        DrillbackColumnLink.builder()
                                .inputSchema("INPUT").outputSchema("OUTPUT").inputColumn("B").build(),
                        DrillbackColumnLink.builder()
                                .inputSchema("INPUT").outputSchema("OUTPUT").inputColumn("C").build(),
                        DrillbackColumnLink.builder()
                                .inputSchema("INPUT").outputSchema("OUTPUT").inputColumn("D").build()));

        DrillBackStepDetails stepDetails = VavrAssert.assertValid(
                drillBackService.findDrillBackStepDetails(1L, 123L))
                .getResult();

        assertThat(stepDetails.getSchemaToInputFieldToOutputFieldMapping())
                .isEqualTo(
                        ImmutableMap.of(
                                "INPUT", ImmutableMap.of(
                                        "A", "FCR_SYS__INPUT__A",
                                        "B", "FCR_SYS__INPUT__B",
                                        "C", "FCR_SYS__INPUT__C",
                                        "D", "FCR_SYS__INPUT__D")));
    }

    @Test
    public void findDrillBackStepDetails_JoinTablesFound_ConvertsToFieldView() {

        when(pipelineService.findById(anyLong()))
                .thenReturn(Validation.valid(ProductPopulated.pipeline()
                        .steps(newHashSet(ProductPopulated.joinPipelineStep()
                                .id(123L)
                                .joins(newHashSet(
                                        ProductPopulated.join()
                                                .leftSchemaId(101L)
                                                .leftSchema(ProductPopulated.schemaDetails()
                                                        .physicalTableName("A")
                                                        .build())
                                                .rightSchemaId(102L)
                                                .rightSchema(ProductPopulated.schemaDetails()
                                                        .physicalTableName("B")
                                                        .build())
                                                .build(),
                                        ProductPopulated.join()
                                                .leftSchemaId(102L)
                                                .leftSchema(ProductPopulated.schemaDetails()
                                                        .physicalTableName("B")
                                                        .build())
                                                .rightSchemaId(103L)
                                                .rightSchema(ProductPopulated.schemaDetails()
                                                        .physicalTableName("C")
                                                        .build())
                                                .build()))
                                .schemaOutId(99L)
                                .schemaOut(ProductPopulated.schemaDetails().physicalTableName("OUTPUT").build())
                                .build()))
                        .build()));

        when(tableService.findOrIdsNotFound(newHashSet(101L, 102L, 103L)))
                .thenReturn(Validation.valid(Arrays.asList(
                        ProductPopulated.table().id(101L)
                                .fields(newHashSet(ProductPopulated.decimalField("a_id").id(53L).build()))
                                .build(),
                        ProductPopulated.table().id(102L)
                                .fields(newHashSet(ProductPopulated.stringField("b_id").id(73L).build()))
                                .build(),
                        ProductPopulated.table().id(103L)
                                .fields(newHashSet(ProductPopulated.dateField("c_id").id(93L).build()))
                                .build())));

        when(tableService.findWithValidation(99L))
                .thenReturn(Validation.valid(ProductPopulated.table()
                        .fields(newHashSet(ProductPopulated.timestampField("out_id").id(103L).build()))
                        .build()));

        DrillBackStepDetails stepDetails = VavrAssert.assertValid(
                drillBackService.findDrillBackStepDetails(1L, 123L))
                .getResult();

        List<FieldView> joinASchemaFields = stepDetails.getSchemasIn().get(101L);
        List<FieldView> joinBSchemaFields = stepDetails.getSchemasIn().get(102L);
        List<FieldView> joinCSchemaFields = stepDetails.getSchemasIn().get(103L);
        List<FieldView> datasetOutFields = stepDetails.getSchemasOut().get(99L);

        soft.assertThat(joinASchemaFields)
                .containsOnly(
                        FieldView.builder().id(53L).name("a_id")
                                .fieldType(FieldView.Type.DECIMAL)
                                .build());

        soft.assertThat(joinBSchemaFields)
                .containsOnly(
                        FieldView.builder().id(73L).name("b_id")
                                .fieldType(FieldView.Type.STRING)
                                .build());

        soft.assertThat(joinCSchemaFields)
                .containsOnly(
                        FieldView.builder().id(93L).name("c_id")
                                .fieldType(FieldView.Type.DATE)
                                .build());

        soft.assertThat(datasetOutFields)
                .containsOnly(FieldView.builder()
                        .id(103L)
                        .name("out_id")
                        .fieldType(FieldView.Type.TIMESTAMP)
                        .build());
    }

    @Test
    public void findDrillBackStepDetails_JoinTablesNotFound_ThrowsException() {

        when(pipelineService.findById(anyLong()))
                .thenReturn(Validation.valid(ProductPopulated.pipeline()
                        .steps(newHashSet(ProductPopulated.joinPipelineStep()
                                .id(123L)
                                .joins(newHashSet(
                                        ProductPopulated.join()
                                                .leftSchemaId(101L)
                                                .rightSchemaId(102L)
                                                .build(),
                                        ProductPopulated.join()
                                                .leftSchemaId(102L)
                                                .rightSchemaId(103L)
                                                .build()))
                                .schemaOutId(99L)
                                .build()))
                        .build()));

        when(tableService.findOrIdsNotFound(newHashSet(101L, 102L, 103L)))
                .thenReturn(Validation.invalid(CRUDFailure.notFoundIds("Schema", 103L)));

        assertThatThrownBy(() ->
                drillBackService.findDrillBackStepDetails(1L, 123L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot find schemas for pipeline step 123");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void findDrillBackStepDetails_PipelineScriptletStep_ReturnsDetails() {
        when(pipelineService.findById(anyLong()))
                .thenReturn(Validation.valid(ProductPopulated.pipeline()
                        .steps(newHashSet(ProductPopulated.scriptletPipelineStep()
                                .id(123L)
                                .schemaIns(newHashSet(
                                        ProductPopulated.scriptletInput()
                                                .id(1L)
                                                .schemaInId(101L)
                                                .schemaIn(ProductPopulated.schemaDetails()
                                                        .id(101L)
                                                        .physicalTableName("INPUT_A")
                                                        .displayName("Input A")
                                                        .build())
                                                .build(),
                                        ProductPopulated.scriptletInput()
                                                .id(2L)
                                                .schemaInId(102L)
                                                .schemaIn(ProductPopulated.schemaDetails()
                                                        .id(102L)
                                                        .physicalTableName("INPUT_B")
                                                        .displayName("Input B")
                                                        .build())
                                                .build()))
                                .schemaOutId(103L)
                                .schemaOut(ProductPopulated.schemaDetails()
                                        .physicalTableName("OUTPUT")
                                        .displayName("Scriptlet Output")
                                        .build())
                                .build()))
                        .build()));

        when(tableService.findOrIdsNotFound(newHashSet(101L, 102L)))
                .thenReturn(Validation.valid(Arrays.asList(
                        ProductPopulated.table().id(101L)
                                .fields(singleton(ProductPopulated.stringField("FieldA").id(10101L).build()))
                                .build(),
                        ProductPopulated.table().id(102L)
                                .fields(singleton(ProductPopulated.stringField("FieldB").id(10201L).build()))
                                .build())));

        when(tableService.findWithValidation(103L))
                .thenReturn(Validation.valid(ProductPopulated.table().id(103L)
                        .fields(singleton(ProductPopulated.stringField("FieldC").id(10301L).build()))
                        .build()));

        DrillBackStepDetails stepDetails = VavrAssert.assertValid(
                drillBackService.findDrillBackStepDetails(1L, 123L))
                .getResult();

        soft.assertThat(stepDetails.getSchemasIn())
                .containsOnly(
                        entry(101L, singletonList(FieldView.builder()
                                .id(10101L).name("FieldA").fieldType(FieldView.Type.STRING).build())),
                        entry(102L, singletonList(FieldView.builder()
                                .id(10201L).name("FieldB").fieldType(FieldView.Type.STRING).build())));

        soft.assertThat(stepDetails.getSchemasOut())
                .containsOnly(
                        entry(103L, singletonList(FieldView.builder()
                                .id(10301L).name("FieldC").fieldType(FieldView.Type.STRING).build())));

        soft.assertThat(stepDetails.getSchemaToInputFieldToOutputFieldMapping())
                .isEmpty();
    }

    @Test
    public void findDrillBackStepDetails_PipelineScriptletStepInputSchemaNotFound_ThrowsException() {
        when(pipelineService.findById(anyLong()))
                .thenReturn(Validation.valid(ProductPopulated.pipeline()
                        .steps(newHashSet(ProductPopulated.scriptletPipelineStep()
                                .id(123L)
                                .schemaIns(newHashSet(
                                        ProductPopulated.scriptletInput()
                                                .id(1L)
                                                .schemaInId(101L)
                                                .schemaIn(ProductPopulated.schemaDetails()
                                                        .id(101L)
                                                        .physicalTableName("INPUT_A")
                                                        .displayName("Input A")
                                                        .build())
                                                .build()))
                                .schemaOutId(103L)
                                .schemaOut(ProductPopulated.schemaDetails()
                                        .physicalTableName("OUTPUT")
                                        .displayName("Scriptlet Output")
                                        .build())
                                .build()))
                        .build()));

        when(tableService.findOrIdsNotFound(any()))
                .thenReturn(Validation.invalid(CRUDFailure.notFoundIds("Schema", 101L)));

        assertThatThrownBy(() -> drillBackService.findDrillBackStepDetails(1L, 123L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot find schemas for pipeline step 123");
    }

    @Test
    public void findDrillBackStepDetails_PipelineScriptletStepOutputSchemaNotFound_ThrowsException() {
        when(pipelineService.findById(anyLong()))
                .thenReturn(Validation.valid(ProductPopulated.pipeline()
                        .steps(newHashSet(ProductPopulated.scriptletPipelineStep()
                                .id(123L)
                                .schemaIns(newHashSet(
                                        ProductPopulated.scriptletInput()
                                                .id(1L)
                                                .schemaInId(101L)
                                                .schemaIn(ProductPopulated.schemaDetails()
                                                        .id(101L)
                                                        .physicalTableName("INPUT_A")
                                                        .displayName("Input A")
                                                        .build())
                                                .build()))
                                .schemaOutId(103L)
                                .schemaOut(ProductPopulated.schemaDetails()
                                        .physicalTableName("OUTPUT")
                                        .displayName("Scriptlet Output")
                                        .build())
                                .build()))
                        .build()));

        when(tableService.findOrIdsNotFound(newHashSet(101L)))
                .thenReturn(Validation.valid(singletonList(
                        ProductPopulated.table().id(101L)
                                .fields(singleton(ProductPopulated.stringField("FieldA").id(10101L).build()))
                                .build())));

        when(tableService.findWithValidation(103L))
                .thenReturn(Validation.invalid(CRUDFailure.notFoundIds("Schema", 103L)));

        assertThatThrownBy(() -> drillBackService.findDrillBackStepDetails(1L, 123L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot find schemas for pipeline step 123");
    }

    @Test
    public void findDrillBackOutputDatasetRows_DatasetNotFound_ReturnsError() {
        when(datasetService.findWithValidation(anyLong()))
                .thenReturn(Validation.invalid(CRUDFailure.cannotFind("Dataset").asFailure()));

        VavrAssert.assertFailed(
                drillBackService
                        .findDrillBackOutputDatasetRows(1L, Pageable.unpaged(), any()))
                .withFailure(CRUDFailure.cannotFind("Dataset").asFailure());
    }

    @Test
    public void findDrillBackOutputDatasetRows_DatasetIsAPipelineDataset_ReturnsPageData() {
        when(datasetService.findWithValidation(anyLong()))
                .thenReturn(Validation.valid(DatasetPopulated.dataset()
                        .pipelineInvocationId(1238L)
                        .pipelineStepInvocationId(648346L)
                        .build()));

        PageRequest pageRequest = PageRequest.of(2, 100);
        when(datasetResultRepository.findDrillbackOutputDatasetRowData(any(), any(), any(), any()))
                .thenReturn(DatasetRowData.builder()
                        .resultData(new PageImpl<>(emptyList(), pageRequest, 6000))
                        .build());

        DatasetRowDataView resultsView = VavrAssert.assertValid(drillBackService.findDrillBackOutputDatasetRows(
                1L, pageRequest, null))
                .getResult();

        assertThat(resultsView.getPage())
                .isEqualTo(Page.builder()
                        .number(2)
                        .size(100)
                        .totalElements(6000)
                        .totalPages(60)
                        .build());
    }

    @Test
    public void findDrillBackOutputDatasetRows_DatasetNotAPipelineDataset_ThrowsError() {
        when(datasetService.findWithValidation(anyLong()))
                .thenReturn(Validation.valid(DatasetPopulated.dataset().pipelineInvocationId(null).build()));

        PageRequest pageRequest = PageRequest.of(2, 100);

        VavrAssert.assertFailed(drillBackService.findDrillBackOutputDatasetRows(1L, pageRequest, null))
                .withFailure(CRUDFailure.invalidParameters()
                        .paramError("dataset", "Dataset is not a pipeline dataset")
                        .asFailure());
    }

    @Test
    public void findDrillBackOutputDatasetRows_DatasetAPipelineDataset_ReturnsRowData() {
        when(datasetService.findWithValidation(anyLong()))
                .thenReturn(Validation.valid(
                        DatasetPopulated.dataset()
                                .pipelineInvocationId(123L)
                                .pipelineStepInvocationId(102L)
                                .build()));

        PageRequest pageRequest = PageRequest.of(2, 100);

        PageImpl<Map<String, Object>> resultData = new PageImpl<>(
                singletonList(
                        ImmutableMap.of("ROW_KEY", 1912921L, "NAME", "TOM", "OCCUPATION", "RIVER PERSON")),
                pageRequest,
                6000);

        when(datasetResultRepository.findDrillbackOutputDatasetRowData(any(), any(), any(), any()))
                .thenReturn(DatasetRowData.builder().resultData(resultData).build());

        DatasetRowDataView datasetRowDataView = VavrAssert.assertValid(drillBackService.findDrillBackOutputDatasetRows(
                1L, pageRequest, null))
                .getResult();

        assertThat(datasetRowDataView.getData())
                .containsOnly(ImmutableMap.of("ROW_KEY", 1912921L, "NAME", "TOM", "OCCUPATION", "RIVER PERSON"));
    }

    @Test
    public void findDrillBackInputDatasetRows_DatasetPipelineStepFound_CallsRepositoryWithPipelineStep() {
        when(datasetService.findWithValidation(anyLong()))
                .thenReturn(Validation.valid(DatasetPopulated.dataset().pipelineInvocationId(18298L).build()));

        PipelineMapStep pipelineMapStep = ProductPopulated.mapPipelineStep().id(102L).build();
        when(pipelineService.findStepById(anyLong(), anyLong()))
                .thenReturn(Validation.valid(pipelineMapStep));

        PageRequest pageRequest = PageRequest.of(2, 100);

        VavrAssert.assertValid(drillBackService.findDrillBackInputDatasetRows(
                1L, 2L, 3L, pageRequest, false, null, null));
        verify(datasetResultRepository).findDrillbackInputDatasetRowData(
                any(), eq(pipelineMapStep), any(), any(), any(), any());
    }

    @Test
    public void findDrillBackInputDatasetRows_DatasetPipelineStepNotFound_ReturnsError() {
        when(datasetService.findWithValidation(anyLong()))
                .thenReturn(Validation.valid(DatasetPopulated.dataset()
                        .pipelineInvocationId(18298L)
                        .pipelineStepInvocationId(4657346L)
                        .build()));

        PipelineMapStep pipelineMapStep = ProductPopulated.mapPipelineStep().build();
        when(pipelineService.findStepById(any(), any()))
                .thenReturn(Validation.invalid(CRUDFailure.notFoundIds("PipelineInvocation", 18298L)));

        PageRequest pageRequest = PageRequest.of(2, 100);

        VavrAssert.assertFailed(drillBackService.findDrillBackInputDatasetRows(1L, 2L, 3L,
                pageRequest, false, null, null))
                .withFailure(CRUDFailure.notFoundIds("PipelineInvocation", 18298L));
    }

    @Test
    public void findDrillBackInputDatasetRows_DatasetNotFound_ReturnsError() {
        when(datasetService.findWithValidation(anyLong()))
                .thenReturn(Validation.invalid(CRUDFailure.cannotFind("Dataset").asFailure()));

        VavrAssert.assertFailed(drillBackService.findDrillBackInputDatasetRows(
                1L, 2L, 3L, Pageable.unpaged(), false, null, null))
                .withFailure(CRUDFailure.cannotFind("Dataset").asFailure());
    }

    @Test
    public void findDrillBackInputDatasetRows_DatasetIsNotAPipelineDataset_ReturnsPageData() {
        when(datasetService.findWithValidation(anyLong()))
                .thenReturn(Validation.valid(DatasetPopulated.dataset().build()));

        PageRequest pageRequest = PageRequest.of(3, 200);
        when(datasetResultRepository.findDrillbackInputDatasetRowData(any(), any(), any(), any(), any(), any()))
                .thenReturn(DatasetRowData.builder()
                        .resultData(new PageImpl<>(emptyList(), pageRequest, 6000))
                        .build());

        DatasetRowDataView resultsView = VavrAssert.assertValid(drillBackService.findDrillBackInputDatasetRows(
                1L, 2L, 3L, pageRequest, false, null, null))
                .getResult();

        assertThat(resultsView.getPage())
                .isEqualTo(Page.builder()
                        .number(3)
                        .size(200)
                        .totalElements(6000)
                        .totalPages(30)
                        .build());
    }
}
