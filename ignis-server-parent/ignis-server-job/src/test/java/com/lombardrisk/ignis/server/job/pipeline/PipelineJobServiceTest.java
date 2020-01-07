package com.lombardrisk.ignis.server.job.pipeline;

import com.lombardrisk.ignis.api.calendar.HolidayCalendarModule;
import com.lombardrisk.ignis.client.external.fixture.ExternalClient;
import com.lombardrisk.ignis.client.external.job.pipeline.PipelineRequest;
import com.lombardrisk.ignis.client.external.pipeline.export.TransformationType;
import com.lombardrisk.ignis.common.assertions.VavrAssert;
import com.lombardrisk.ignis.common.time.TimeSource;
import com.lombardrisk.ignis.data.common.Identifiable;
import com.lombardrisk.ignis.data.common.error.ErrorResponse;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.feature.FeatureNotActiveException;
import com.lombardrisk.ignis.feature.IgnisFeature;
import com.lombardrisk.ignis.pipeline.step.api.JoinAppConfig;
import com.lombardrisk.ignis.pipeline.step.api.JoinFieldConfig;
import com.lombardrisk.ignis.pipeline.step.api.OrderSpec;
import com.lombardrisk.ignis.pipeline.step.api.SelectColumn;
import com.lombardrisk.ignis.pipeline.step.api.UnionAppConfig;
import com.lombardrisk.ignis.pipeline.step.api.UnionSpec;
import com.lombardrisk.ignis.pipeline.step.api.WindowSpec;
import com.lombardrisk.ignis.server.config.calendar.CalendarService;
import com.lombardrisk.ignis.server.config.fixture.ServerConfig;
import com.lombardrisk.ignis.server.dataset.DatasetService;
import com.lombardrisk.ignis.server.dataset.fixture.DatasetPopulated;
import com.lombardrisk.ignis.server.dataset.pipeline.invocation.PipelineInvocationService;
import com.lombardrisk.ignis.server.dataset.pipeline.invocation.model.PipelineInvocation;
import com.lombardrisk.ignis.server.dataset.pipeline.invocation.model.PipelineStepInvocation;
import com.lombardrisk.ignis.server.dataset.pipeline.invocation.model.PipelineStepInvocationDataset;
import com.lombardrisk.ignis.server.dataset.pipeline.invocation.model.PipelineStepStatus;
import com.lombardrisk.ignis.server.job.exception.JobStartException;
import com.lombardrisk.ignis.server.job.pipeline.model.PipelineInputValidator;
import com.lombardrisk.ignis.server.job.servicerequest.ServiceRequest;
import com.lombardrisk.ignis.server.job.servicerequest.ServiceRequestRepository;
import com.lombardrisk.ignis.server.job.staging.JobStarter;
import com.lombardrisk.ignis.server.product.fixture.ProductPopulated;
import com.lombardrisk.ignis.server.product.pipeline.PipelineService;
import com.lombardrisk.ignis.server.product.pipeline.PipelineStepService;
import com.lombardrisk.ignis.server.product.pipeline.details.SchemaDetails;
import com.lombardrisk.ignis.server.product.pipeline.model.PipelineMapStep;
import com.lombardrisk.ignis.server.product.pipeline.model.PipelineScriptletStep;
import com.lombardrisk.ignis.server.product.pipeline.model.PipelineUnionStep;
import com.lombardrisk.ignis.server.product.pipeline.model.PipelineWindowStep;
import com.lombardrisk.ignis.server.product.pipeline.select.Order;
import com.lombardrisk.ignis.server.product.pipeline.select.PipelineFilter;
import com.lombardrisk.ignis.server.product.pipeline.select.Select;
import com.lombardrisk.ignis.server.product.pipeline.select.Union;
import com.lombardrisk.ignis.server.product.pipeline.select.Window;
import com.lombardrisk.ignis.server.product.pipeline.transformation.Join;
import com.lombardrisk.ignis.server.product.pipeline.transformation.JoinField;
import com.lombardrisk.ignis.server.product.table.TableService;
import com.lombardrisk.ignis.server.product.table.model.Table;
import com.lombardrisk.ignis.spark.api.DatasetTableLookup;
import com.lombardrisk.ignis.spark.api.pipeline.AggregateStepAppConfig;
import com.lombardrisk.ignis.spark.api.pipeline.JoinStepAppConfig;
import com.lombardrisk.ignis.spark.api.pipeline.MapStepAppConfig;
import com.lombardrisk.ignis.spark.api.pipeline.PipelineAppConfig;
import com.lombardrisk.ignis.spark.api.pipeline.PipelineStepAppConfig;
import com.lombardrisk.ignis.spark.api.pipeline.PipelineStepDatasetLookup;
import com.lombardrisk.ignis.spark.api.pipeline.ScriptletStepAppConfig;
import com.lombardrisk.ignis.spark.api.pipeline.UnionStepAppConfig;
import com.lombardrisk.ignis.spark.api.pipeline.WindowStepAppConfig;
import com.lombardrisk.ignis.spark.api.staging.DatasetProperties;
import com.lombardrisk.ignis.spark.api.staging.StagingSchemaValidation;
import io.vavr.control.Validation;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.togglz.core.manager.FeatureManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Sets.newLinkedHashSet;
import static com.lombardrisk.ignis.client.external.pipeline.export.TransformationType.AGGREGATION;
import static com.lombardrisk.ignis.client.external.pipeline.export.TransformationType.JOIN;
import static com.lombardrisk.ignis.client.external.pipeline.export.TransformationType.MAP;
import static com.lombardrisk.ignis.common.json.MapperWrapper.MAPPER;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PipelineJobServiceTest {

    @Rule
    public JUnitSoftAssertions soft = new JUnitSoftAssertions();

    @Mock
    private JobStarter jobStarter;

    @Mock
    private ServiceRequestRepository serviceRequestRepository;

    @Mock
    private DatasetService datasetService;

    @Mock
    private PipelineService pipelineService;

    @Mock
    private PipelineStepService pipelineStepService;

    @Mock
    private PipelineInvocationService pipelineInvocationService;

    @Mock
    private CalendarService calendarService;

    @Mock
    private TableService tableService;

    @Mock
    private TimeSource timeSource;

    @Mock
    private FeatureManager featureManager;

    @Captor
    private ArgumentCaptor<ServiceRequest> serviceRequestCaptor;

    @Captor
    private ArgumentCaptor<PipelineInvocation> pipelineInvocationCaptor;

    private PipelineJobService pipelineJobService;

    @Before
    public void setUp() throws Exception {
        MAPPER.registerModule(new HolidayCalendarModule());

        PipelineInputValidator validator =
                new PipelineInputValidator(
                        pipelineService, pipelineStepService, datasetService, tableService, featureManager);
        pipelineJobService =
                new PipelineJobService(pipelineInvocationService,
                        jobStarter, serviceRequestRepository, validator,
                        calendarService, timeSource, MAPPER);

        when(jobStarter.startJob(any(), any(), any()))
                .thenReturn(100L);

        when(serviceRequestRepository.save(any()))
                .then(invocation -> {
                    ServiceRequest serviceRequest = invocation.getArgument(0);
                    if (serviceRequest != null) {
                        serviceRequest.setId(120312L);
                    }
                    return serviceRequest;
                });

        when(pipelineService.findByIdWithFilters(anyLong()))
                .thenReturn(Validation.valid(ProductPopulated.pipeline()
                        .steps(singleton(ProductPopulated.mapPipelineStep()
                                .id(125L)
                                .selects(newHashSet(
                                        anyPipelineStepSelect("NAME", 12341234L),
                                        anyPipelineStepSelect("SALARY", 56785678L)))
                                .build()))
                        .build()));

        when(pipelineStepService.getSelects(any()))
                .thenReturn(emptyList());

        when(datasetService.findLatestDatasetForSchemaId(anyLong(), anyString(), any()))
                .thenReturn(Validation.valid(DatasetPopulated.dataset().build()));

        when(tableService.findWithValidation(anyLong()))
                .thenReturn(Validation.valid(ProductPopulated.table()
                        .id(1343214L)
                        .fields(newLinkedHashSet(asList(
                                ProductPopulated.stringField("NAME").id(12341234L).build(),
                                ProductPopulated.intField("SALARY").id(56785678L).build())))
                        .build()));

        when(pipelineInvocationService.create(any()))
                .then(invocation -> invocation.getArgument(0));

        when(timeSource.nowAsLocalDateTime())
                .thenReturn(LocalDateTime.of(1992, 4, 20, 12, 1, 19));

        when(calendarService.createHolidayCalendar(any(), any(), any()))
                .thenReturn(Validation.valid(
                        ServerConfig.Populated.holidayCalendar().build()));

        when(featureManager.isActive(IgnisFeature.RUN_PIPLINE_STEP)).thenReturn(true);
    }

    @Test
    public void startJob_SavesServiceRequest() {
        PipelineRequest pipelineRequest = ExternalClient.Populated.pipelineItemRequest()
                .name("hello")
                .build();

        VavrAssert.assertValid(
                pipelineJobService.startJob(pipelineRequest, "matt"));

        verify(serviceRequestRepository, times(2)).save(serviceRequestCaptor.capture());
        ServiceRequest createdRequest = serviceRequestCaptor.getAllValues().get(0);

        soft.assertThat(createdRequest.getName())
                .isEqualTo("hello");
        soft.assertThat(createdRequest.getCreatedBy())
                .isEqualTo("matt");
    }

    @Test
    public void startJob_SavesServiceRequestMessage_WithNameAndId() throws Exception {
        PipelineRequest pipelineRequest = ExternalClient.Populated.pipelineItemRequest()
                .name("pipeline invocation 1")
                .build();

        when(serviceRequestRepository.save(any()))
                .then(invocation -> {
                    ServiceRequest serviceRequest = invocation.getArgument(0);
                    serviceRequest.setId(92138L);
                    return serviceRequest;
                });

        VavrAssert.assertValid(
                pipelineJobService.startJob(pipelineRequest, "matt"));

        verify(serviceRequestRepository, times(2)).save(serviceRequestCaptor.capture());
        ServiceRequest createdRequest = serviceRequestCaptor.getAllValues().get(1);

        PipelineAppConfig pipelineAppConfig =
                MAPPER.readValue(createdRequest.getRequestMessage(), PipelineAppConfig.class);

        soft.assertThat(pipelineAppConfig.getName())
                .isEqualTo("pipeline invocation 1");
        soft.assertThat(pipelineAppConfig.getServiceRequestId())
                .isEqualTo(92138);
    }

    @Test
    public void startJob_SavesServiceRequestMessageWithDatasetLookup() throws Exception {
        PipelineRequest pipelineRequest = ExternalClient.Populated.pipelineItemRequest().build();

        when(datasetService.findLatestDatasetForSchemaId(anyLong(), anyString(), any()))
                .thenReturn(Validation.valid(DatasetPopulated.dataset()
                        .schema(ProductPopulated.table()
                                .physicalTableName("PHYSICAL_TABLE_NAME")
                                .build())
                        .predicate("ROW_KEY < 100 AND ROW_KEY > 0")
                        .build()));

        VavrAssert.assertValid(
                pipelineJobService.startJob(pipelineRequest, "matt"));

        verify(serviceRequestRepository, times(2)).save(serviceRequestCaptor.capture());
        ServiceRequest createdRequest = serviceRequestCaptor.getAllValues().get(1);

        PipelineAppConfig pipelineAppConfig =
                MAPPER.readValue(createdRequest.getRequestMessage(), PipelineAppConfig.class);

        assertThat(pipelineAppConfig.getPipelineSteps())
                .hasSize(1);

        PipelineStepAppConfig pipelineStepAppConfig = pipelineAppConfig.getPipelineSteps().get(0);

        List<PipelineStepDatasetLookup> pipelineStepDatasetLookups =
                pipelineStepAppConfig.getPipelineStepDatasetLookups();

        soft.assertThat(pipelineStepDatasetLookups)
                .hasSize(1);

        soft.assertThat(pipelineStepDatasetLookups.get(0).getDatasetTableLookup())
                .extracting(DatasetTableLookup::getDatasetName)
                .isEqualTo("PHYSICAL_TABLE_NAME");

        soft.assertThat(pipelineStepDatasetLookups.get(0).getDatasetTableLookup())
                .extracting(DatasetTableLookup::getPredicate)
                .isEqualTo("ROW_KEY < 100 AND ROW_KEY > 0");

        soft.assertThat(pipelineStepDatasetLookups.get(0).getPipelineStepInvocationId())
                .isNull();
    }

    @Test
    public void startJob_SavesServiceRequestMessageWithSchemaValidation() throws Exception {
        PipelineRequest pipelineRequest = ExternalClient.Populated.pipelineItemRequest().build();

        when(tableService.findWithValidation(anyLong()))
                .thenReturn(Validation.valid(ProductPopulated.table()
                        .id(8829L)
                        .physicalTableName("OUTPUT_TABLE")
                        .fields(newLinkedHashSet(asList(
                                ProductPopulated.stringField("NAME").id(12341234L).build(),
                                ProductPopulated.intField("SALARY").id(56785678L).build())))
                        .build()));

        VavrAssert.assertValid(
                pipelineJobService.startJob(pipelineRequest, "matt"));

        verify(serviceRequestRepository, times(2)).save(serviceRequestCaptor.capture());
        ServiceRequest createdRequest = serviceRequestCaptor.getAllValues().get(1);

        PipelineAppConfig pipelineAppConfig =
                MAPPER.readValue(createdRequest.getRequestMessage(), PipelineAppConfig.class);

        assertThat(pipelineAppConfig.getPipelineSteps())
                .hasSize(1);

        StagingSchemaValidation stagingSchemaValidation =
                pipelineAppConfig.getPipelineSteps().get(0).getOutputDataset().getStagingSchemaValidation();

        soft.assertThat(stagingSchemaValidation.getPhysicalTableName())
                .isEqualTo("OUTPUT_TABLE");
        soft.assertThat(stagingSchemaValidation.getSchemaId())
                .isEqualTo(8829L);
    }

    @Test
    public void startJob_SavesServiceRequestMessageWithDatasetProperties() throws Exception {
        PipelineRequest pipelineRequest = ExternalClient.Populated.pipelineItemRequest()
                .entityCode("ENTITY")
                .referenceDate(LocalDate.of(2001, 1, 1))
                .build();

        VavrAssert.assertValid(
                pipelineJobService.startJob(pipelineRequest, "matt"));

        verify(serviceRequestRepository, times(2)).save(serviceRequestCaptor.capture());
        ServiceRequest createdRequest = serviceRequestCaptor.getAllValues().get(1);

        PipelineAppConfig pipelineAppConfig =
                MAPPER.readValue(createdRequest.getRequestMessage(), PipelineAppConfig.class);

        assertThat(pipelineAppConfig.getPipelineSteps())
                .hasSize(1);

        DatasetProperties datasetProperties =
                pipelineAppConfig.getPipelineSteps().get(0).getOutputDataset().getDatasetProperties();

        soft.assertThat(datasetProperties.getEntityCode())
                .isEqualTo("ENTITY");
        soft.assertThat(datasetProperties.getReferenceDate())
                .isEqualTo(LocalDate.of(2001, 1, 1));
    }

    @Test
    public void startJob_SavesServiceRequestMessageWithMapPipelineStep() throws Exception {
        PipelineRequest pipelineRequest = ExternalClient.Populated.pipelineItemRequest().build();

        when(pipelineService.findByIdWithFilters(anyLong()))
                .thenReturn(Validation.valid(ProductPopulated.pipeline()
                        .steps(newHashSet(ProductPopulated.mapPipelineStep()
                                .id(125L)
                                .selects(null)
                                .filters(newHashSet("D > 0"))
                                .build()))
                        .build()));

        when(pipelineStepService.getSelects(any()))
                .thenReturn(asList(
                        Select.builder().select("MY_NAME").outputFieldId(12341234L).build(),
                        Select.builder().select("MY_SALARY").outputFieldId(56785678L).build()));

        VavrAssert.assertValid(
                pipelineJobService.startJob(pipelineRequest, "matt"));

        verify(serviceRequestRepository, times(2)).save(serviceRequestCaptor.capture());
        ServiceRequest createdRequest = serviceRequestCaptor.getAllValues().get(1);

        PipelineAppConfig pipelineAppConfig =
                MAPPER.readValue(createdRequest.getRequestMessage(), PipelineAppConfig.class);

        List<PipelineStepAppConfig> pipelineSteps = pipelineAppConfig.getPipelineSteps();

        assertThat(pipelineSteps)
                .hasSize(1);

        assertThat(pipelineSteps)
                .hasOnlyElementsOfTypes(MapStepAppConfig.class);

        MapStepAppConfig mapStep = (MapStepAppConfig) pipelineSteps.get(0);

        soft.assertThat(mapStep.getSelects())
                .containsExactly(
                        SelectColumn.builder().select("MY_NAME").as("NAME").build(),
                        SelectColumn.builder().select("MY_SALARY").as("SALARY").build());
        soft.assertThat(mapStep.getFilters())
                .containsExactly("D > 0");
    }

    @Test
    public void startJob_SavesServiceRequestMessageWithAggregationPipelineStep() throws Exception {
        PipelineRequest pipelineRequest = ExternalClient.Populated.pipelineItemRequest().build();

        when(pipelineService.findByIdWithFilters(anyLong()))
                .thenReturn(Validation.valid(ProductPopulated.pipeline()
                        .steps(newHashSet(ProductPopulated.aggregatePipelineStep()
                                .id(125L)
                                .schemaIn(ProductPopulated.schemaDetails().id(36346L).build())
                                .schemaOut(ProductPopulated.schemaDetails().id(6346L).build())
                                .selects(null)
                                .filters(newLinkedHashSet(asList("A > 0", "B > 0")))
                                .groupings(newHashSet("DEPARTMENT"))
                                .build()
                        ))
                        .build()));

        when(pipelineStepService.getSelects(any()))
                .thenReturn(asList(
                        Select.builder().select("FIRST(NAME)").outputFieldId(12341234L).build(),
                        Select.builder().select("SUM(SALARY)").outputFieldId(56785678L).build()));

        VavrAssert.assertValid(
                pipelineJobService.startJob(pipelineRequest, "matt"));

        verify(serviceRequestRepository, times(2)).save(serviceRequestCaptor.capture());
        ServiceRequest createdRequest = serviceRequestCaptor.getAllValues().get(1);

        PipelineAppConfig pipelineAppConfig =
                MAPPER.readValue(createdRequest.getRequestMessage(), PipelineAppConfig.class);

        List<PipelineStepAppConfig> pipelineSteps = pipelineAppConfig.getPipelineSteps();

        assertThat(pipelineSteps)
                .hasSize(1);

        assertThat(pipelineSteps)
                .hasOnlyElementsOfTypes(AggregateStepAppConfig.class);

        AggregateStepAppConfig aggregationStep = (AggregateStepAppConfig) pipelineSteps.get(0);

        soft.assertThat(aggregationStep.getSelects())
                .containsExactly(
                        SelectColumn.builder().select("FIRST(NAME)").as("NAME").build(),
                        SelectColumn.builder().select("SUM(SALARY)").as("SALARY").build());

        soft.assertThat(aggregationStep.getFilters())
                .containsExactlyInAnyOrder("A > 0", "B > 0");

        soft.assertThat(aggregationStep.getGroupings())
                .containsExactly("DEPARTMENT");
    }

    @Test
    public void startJob_SavesServiceRequestMessageWithWindowPipelineStep() throws Exception {
        PipelineRequest pipelineRequest = ExternalClient.Populated.pipelineItemRequest().build();

        when(pipelineService.findByIdWithFilters(anyLong()))
                .thenReturn(Validation.valid(ProductPopulated.pipeline()
                        .steps(newHashSet(ProductPopulated.windowPipelineStep()
                                .id(125L)
                                .schemaIn(ProductPopulated.schemaDetails().id(36346L).build())
                                .schemaOut(ProductPopulated.schemaDetails().id(6346L).build())
                                .selects(null)
                                .build()))
                        .build()));

        when(pipelineStepService.getSelects(any()))
                .thenReturn(asList(
                        Select.builder().select("MY_NAME").outputFieldId(12341234L).build(),
                        Select.builder()
                                .select("window()").outputFieldId(56785678L).isWindow(true)
                                .window(Window.builder()
                                        .partitions(newHashSet("A", "B"))
                                        .orders(singleton(Order.builder()
                                                .fieldName("B")
                                                .direction(Order.Direction.ASC)
                                                .priority(0)
                                                .build()))
                                        .build())
                                .build()));

        VavrAssert.assertValid(
                pipelineJobService.startJob(pipelineRequest, "matt"));

        verify(serviceRequestRepository, times(2)).save(serviceRequestCaptor.capture());
        ServiceRequest createdRequest = serviceRequestCaptor.getAllValues().get(1);

        PipelineAppConfig pipelineAppConfig =
                MAPPER.readValue(createdRequest.getRequestMessage(), PipelineAppConfig.class);

        List<PipelineStepAppConfig> pipelineSteps = pipelineAppConfig.getPipelineSteps();

        assertThat(pipelineSteps)
                .hasSize(1);

        assertThat(pipelineSteps)
                .hasOnlyElementsOfTypes(WindowStepAppConfig.class);

        WindowStepAppConfig stepConfig = (WindowStepAppConfig) pipelineSteps.get(0);

        soft.assertThat(stepConfig.getSelects())
                .containsExactlyInAnyOrder(
                        SelectColumn.builder().select("MY_NAME").as("NAME").build(),
                        SelectColumn.builder()
                                .select("window()")
                                .as("SALARY")
                                .over(WindowSpec.builder()
                                        .partitionBy(newHashSet("A", "B"))
                                        .orderBy(singletonList(OrderSpec.column("B", OrderSpec.Direction.ASC)))
                                        .build())
                                .build());
    }

    @Test
    public void startJob_UnionStep_SavesServiceRequestMessage() throws Exception {
        PipelineRequest pipelineRequest = ExternalClient.Populated.pipelineItemRequest().build();

        SchemaDetails schemaA = ProductPopulated.schemaDetails("A", 1).id(1L).build();
        SchemaDetails schemaB = ProductPopulated.schemaDetails("B", 1).id(2L).build();

        when(tableService.findWithValidation(anyLong()))
                .thenReturn(Validation.valid(ProductPopulated.table()
                        .fields(singleton(ProductPopulated.stringField("OUT").id(101L).build()))
                        .build()));

        PipelineUnionStep unionStep = ProductPopulated.unionPipelineStep()
                .id(125L)
                .schemasIn(newHashSet(schemaA, schemaB))
                .schemaOut(ProductPopulated.schemaDetails("OUT", 1).id(3L).build())
                .selects(null)
                .filters(newHashSet(PipelineFilter.builder()
                        .unionSchemaId(2L)
                        .filter("B > 2")
                        .build()))
                .build();

        when(pipelineService.findByIdWithFilters(anyLong()))
                .thenReturn(Validation.valid(ProductPopulated.pipeline()
                        .steps(newHashSet(unionStep))
                        .build()));

        when(pipelineStepService.getSelects(any()))
                .thenReturn(asList(
                        Select.builder().select("A")
                                .outputFieldId(101L)
                                .isUnion(true)
                                .selectUnion(Union.builder().unionSchema(schemaA).build())
                                .build(),
                        Select.builder().select("B")
                                .outputFieldId(101L)
                                .isUnion(true)
                                .selectUnion(Union.builder().unionSchema(schemaB).build())
                                .build()));

        VavrAssert.assertValid(
                pipelineJobService.startJob(pipelineRequest, "matt"));

        verify(serviceRequestRepository, times(2)).save(serviceRequestCaptor.capture());
        ServiceRequest createdRequest = serviceRequestCaptor.getAllValues().get(1);

        PipelineAppConfig pipelineAppConfig =
                MAPPER.readValue(createdRequest.getRequestMessage(), PipelineAppConfig.class);

        List<PipelineStepAppConfig> pipelineSteps = pipelineAppConfig.getPipelineSteps();

        assertThat(pipelineSteps)
                .hasSize(1);

        assertThat(pipelineSteps)
                .hasOnlyElementsOfTypes(UnionStepAppConfig.class);

        UnionStepAppConfig mapStep = (UnionStepAppConfig) pipelineSteps.get(0);

        UnionSpec schemaASpec = UnionSpec.builder().schemaInPhysicalName("A").schemaId(1L).build();
        UnionSpec schemaBSpec = UnionSpec.builder().schemaInPhysicalName("B").schemaId(2L).build();

        soft.assertThat(mapStep.getUnions()).containsExactly(
                UnionAppConfig.builder()
                        .schemaIn(schemaASpec)
                        .selects(singleton(SelectColumn.builder()
                                .select("A")
                                .as("OUT")
                                .union(schemaASpec)
                                .build()))
                        .build(),
                UnionAppConfig.builder()
                        .schemaIn(schemaBSpec)
                        .selects(singleton(SelectColumn.builder().select("B").as("OUT").union(schemaBSpec).build()))
                        .filters(singleton("B > 2"))
                        .build());
    }

    @Test
    public void startJob_UnionStepNoDatasetForInputSchema_ReturnsError() {
        PipelineRequest pipelineRequest = ExternalClient.Populated.pipelineItemRequest().build();

        SchemaDetails schemaA = ProductPopulated.schemaDetails("A", 1).id(1L).build();
        SchemaDetails schemaB = ProductPopulated.schemaDetails("B", 1).id(2L).build();

        when(tableService.findWithValidation(anyLong()))
                .thenReturn(Validation.valid(ProductPopulated.table()
                        .fields(singleton(ProductPopulated.stringField("OUT").id(101L).build()))
                        .build()));

        PipelineUnionStep unionStep = ProductPopulated.unionPipelineStep()
                .id(125L)
                .schemasIn(newHashSet(schemaA, schemaB))
                .schemaOut(ProductPopulated.schemaDetails("OUT", 1).id(3L).build())
                .selects(newHashSet(
                        Select.builder().select("A")
                                .outputFieldId(101L)
                                .isUnion(true)
                                .selectUnion(Union.builder().unionSchema(schemaA).build())
                                .build(),
                        Select.builder().select("B")
                                .outputFieldId(101L)
                                .isUnion(true)
                                .selectUnion(Union.builder().unionSchema(schemaB).build())
                                .build()))
                .build();

        when(pipelineService.findByIdWithFilters(anyLong()))
                .thenReturn(Validation.valid(ProductPopulated.pipeline()
                        .steps(newHashSet(unionStep))
                        .build()));

        when(datasetService.findLatestDatasetForSchemaId(eq(2L), any(), any()))
                .thenReturn(Validation.invalid(CRUDFailure.cannotFind("Dataset")
                        .with("SchemaId", 2L)
                        .asFailure()));

        VavrAssert.assertCollectionFailure(
                pipelineJobService.startJob(pipelineRequest, "matt"))
                .withFailure(ErrorResponse.valueOf("Cannot find Dataset with SchemaId '2'", "NOT_FOUND"));
    }

    @Test
    public void startJob_UnionStep_CreatesPipelineInvocationWithTwoInputDatasets() {
        PipelineRequest pipelineRequest = ExternalClient.Populated.pipelineItemRequest().build();

        SchemaDetails schemaA = ProductPopulated.schemaDetails("A", 1).id(1L).build();
        SchemaDetails schemaB = ProductPopulated.schemaDetails("B", 1).id(2L).build();

        when(tableService.findWithValidation(anyLong()))
                .thenReturn(Validation.valid(ProductPopulated.table()
                        .fields(singleton(ProductPopulated.stringField("OUT").id(101L).build()))
                        .build()));

        PipelineUnionStep unionStep = ProductPopulated.unionPipelineStep()
                .id(125L)
                .schemasIn(newHashSet(schemaA, schemaB))
                .schemaOut(ProductPopulated.schemaDetails("OUT", 1).id(3L).build())
                .selects(newHashSet(
                        Select.builder().select("A")
                                .outputFieldId(101L)
                                .isUnion(true)
                                .selectUnion(Union.builder().unionSchema(schemaA).build())
                                .build(),
                        Select.builder().select("B")
                                .outputFieldId(101L)
                                .isUnion(true)
                                .selectUnion(Union.builder().unionSchema(schemaB).build())
                                .build()))
                .filters(newHashSet(PipelineFilter.builder()
                        .unionSchemaId(2L)
                        .filter("B > 2")
                        .build()))
                .build();

        when(pipelineService.findByIdWithFilters(anyLong()))
                .thenReturn(Validation.valid(ProductPopulated.pipeline()
                        .steps(newHashSet(unionStep))
                        .build()));

        when(datasetService.findLatestDatasetForSchemaId(eq(1L), any(), any()))
                .thenReturn(Validation.valid(DatasetPopulated.dataset().id(101L).runKey(98L).build()));
        when(datasetService.findLatestDatasetForSchemaId(eq(2L), any(), any()))
                .thenReturn(Validation.valid(DatasetPopulated.dataset().id(102L).runKey(99L).build()));

        VavrAssert.assertValid(
                pipelineJobService.startJob(pipelineRequest, "matt"));

        verify(pipelineInvocationService).create(pipelineInvocationCaptor.capture());
        PipelineInvocation createdPipelineInvocation = pipelineInvocationCaptor.getValue();
        assertThat(createdPipelineInvocation.getSteps())
                .hasSize(1);

        PipelineStepInvocation stepInvocation = createdPipelineInvocation.getSteps().iterator().next();

        soft.assertThat(stepInvocation.getInputDatasets())
                .containsExactlyInAnyOrder(
                        DatasetPopulated.pipelineStepInvocationDataset()
                                .datasetId(101L).datasetRunKey(98L).build(),
                        DatasetPopulated.pipelineStepInvocationDataset()
                                .datasetId(102L).datasetRunKey(99L).build());

        soft.assertThat(stepInvocation.getStatus())
                .isEqualTo(PipelineStepStatus.PENDING);
    }

    @Test
    public void startJob_CreatesPipelineInvocationWithWindowPipelineStep() {
        PipelineRequest pipelineRequest = ExternalClient.Populated.pipelineItemRequest().build();

        when(datasetService.findLatestDatasetForSchemaId(eq(36346L), anyString(), any()))
                .thenReturn(Validation.valid(DatasetPopulated.dataset().id(1001L).runKey(4L).build()));

        PipelineWindowStep windowStep = ProductPopulated.windowPipelineStep()
                .id(12L)
                .schemaIn(ProductPopulated.schemaDetails().id(36346L).build())
                .schemaOut(ProductPopulated.schemaDetails().id(6346L).build())
                .selects(newHashSet(
                        Select.builder().select("MY_NAME").outputFieldId(12341234L).build(),
                        Select.builder()
                                .select("window()").outputFieldId(56785678L).isWindow(true)
                                .window(Window.builder()
                                        .partitions(newHashSet("A", "B"))
                                        .orders(singleton(Order.builder()
                                                .fieldName("B")
                                                .direction(Order.Direction.ASC)
                                                .priority(0)
                                                .build()))
                                        .build())
                                .build()))
                .build();

        when(pipelineService.findByIdWithFilters(anyLong()))
                .thenReturn(Validation.valid(ProductPopulated.pipeline().steps(newHashSet(windowStep)).build()));

        VavrAssert.assertValid(
                pipelineJobService.startJob(pipelineRequest, "matt"));

        verify(pipelineInvocationService).create(pipelineInvocationCaptor.capture());

        PipelineInvocation pipeline = pipelineInvocationCaptor.getValue();
        assertThat(pipeline.getSteps())
                .hasSize(1);

        PipelineStepInvocation stepInvocation = pipeline.getSteps().iterator().next();

        assertThat(stepInvocation.getPipelineStepId())
                .isEqualTo(12L);

        assertThat(stepInvocation.getInputDatasets())
                .containsExactly(
                        DatasetPopulated.pipelineStepInvocationDataset()
                                .datasetId(1001L).datasetRunKey(4L).build());

        assertThat(stepInvocation.getStatus())
                .isEqualTo(PipelineStepStatus.PENDING);
    }

    @Test
    public void startJob_SavesServiceRequestMessageWithPipelineInvocationId() throws Exception {
        PipelineRequest pipelineRequest = ExternalClient.Populated.pipelineItemRequest().build();

        when(pipelineService.findByIdWithFilters(anyLong()))
                .thenReturn(Validation.valid(ProductPopulated.pipeline()
                        .steps(newHashSet(ProductPopulated.mapPipelineStep()
                                .id(123L)
                                .selects(newHashSet(
                                        Select.builder().select("NAME").outputFieldId(12341234L).build(),
                                        Select.builder().select("SALARY").outputFieldId(56785678L).build()))
                                .build()))
                        .build()));

        when(pipelineInvocationService.create(any()))
                .thenReturn(DatasetPopulated.pipelineInvocation()
                        .id(192L)
                        .steps(singleton(DatasetPopulated.pipelineStepInvocation().pipelineStepId(123L).build()))
                        .build());

        VavrAssert.assertValid(
                pipelineJobService.startJob(pipelineRequest, "matt"));

        verify(serviceRequestRepository, times(2)).save(serviceRequestCaptor.capture());
        ServiceRequest createdRequest = serviceRequestCaptor.getAllValues().get(1);

        PipelineAppConfig pipelineAppConfig =
                MAPPER.readValue(createdRequest.getRequestMessage(), PipelineAppConfig.class);

        assertThat(pipelineAppConfig.getPipelineSteps())
                .hasSize(1);

        soft.assertThat(pipelineAppConfig.getPipelineInvocationId())
                .isEqualTo(192L);
    }

    @Test
    public void startJob_ReturnsJobId() throws Exception {
        PipelineRequest pipelineRequest = ExternalClient.Populated.pipelineItemRequest().build();

        when(jobStarter.startJob(anyString(), any(), any()))
                .thenReturn(1928L);

        VavrAssert.assertValid(
                pipelineJobService.startJob(pipelineRequest, "matt"))
                .extracting(Identifiable::getId)
                .withResult(1928L);
    }

    @Test
    public void startJob_JobDoesNotStart_ReturnsError() throws Exception {
        PipelineRequest pipelineRequest = ExternalClient.Populated.pipelineItemRequest().build();

        when(jobStarter.startJob(anyString(), any(), any()))
                .thenThrow(new JobStartException("oops"));

        VavrAssert.assertCollectionFailure(
                pipelineJobService.startJob(pipelineRequest, "matt"))
                .withFailure(
                        ErrorResponse.valueOf("Could not start Pipeline job", "JOB_COULD_NOT_START"));
    }

    @Test
    public void startJob_PipelineItems_CreatesPipelineInvocationWithOneStep() {
        PipelineRequest pipelineRequest = ExternalClient.Populated.pipelineItemRequest().build();

        when(datasetService.findLatestDatasetForSchemaId(eq(920L), anyString(), any()))
                .thenReturn(Validation.valid(DatasetPopulated.dataset().id(1001L).runKey(5L).build()));

        when(pipelineService.findByIdWithFilters(anyLong()))
                .thenReturn(Validation.valid(ProductPopulated.pipeline()
                        .steps(newHashSet(ProductPopulated.mapPipelineStep()
                                .id(12L)
                                .selects(newHashSet(
                                        Select.builder().select("NAME").outputFieldId(12341234L).build(),
                                        Select.builder().select("SALARY").outputFieldId(56785678L).build()))
                                .schemaInId(920L).schemaIn(ProductPopulated.schemaDetails().id(920L).build())
                                .schemaOutId(921L).schemaOut(ProductPopulated.schemaDetails().id(921L).build())
                                .build()))
                        .build()));

        VavrAssert.assertValid(
                pipelineJobService.startJob(pipelineRequest, "matt"));

        verify(pipelineInvocationService).create(pipelineInvocationCaptor.capture());

        PipelineInvocation pipeline = pipelineInvocationCaptor.getValue();
        assertThat(pipeline.getSteps())
                .hasSize(1);

        PipelineStepInvocation stepInvocation = pipeline.getSteps().iterator().next();

        assertThat(stepInvocation.getPipelineStepId())
                .isEqualTo(12L);

        assertThat(stepInvocation.getInputDatasets())
                .containsExactly(
                        DatasetPopulated.pipelineStepInvocationDataset()
                                .datasetId(1001L).datasetRunKey(5L).build());

        assertThat(stepInvocation.getStatus())
                .isEqualTo(PipelineStepStatus.PENDING);
    }

    @Test
    public void startJob_PipelineJoinStep_CreatesPipelineInvocationWithAllInputDatasetIds() {
        PipelineRequest pipelineRequest = ExternalClient.Populated.pipelineItemRequest().build();

        when(datasetService.findLatestDatasetForSchemaId(eq(90210L), anyString(), any()))
                .thenReturn(Validation.valid(DatasetPopulated.dataset().id(1000L).runKey(33L).build()));
        when(datasetService.findLatestDatasetForSchemaId(eq(90211L), anyString(), any()))
                .thenReturn(Validation.valid(DatasetPopulated.dataset().id(1001L).runKey(44L).build()));

        when(pipelineService.findByIdWithFilters(anyLong()))
                .thenReturn(Validation.valid(ProductPopulated.pipeline()
                        .steps(newHashSet(ProductPopulated.joinPipelineStep()
                                .id(12L)
                                .selects(newHashSet(
                                        Select.builder().select("NAME").outputFieldId(12341234L).build(),
                                        Select.builder().select("SALARY").outputFieldId(56785678L).build()))
                                .joins(newHashSet(
                                        ProductPopulated.join()
                                                .rightSchemaId(90210L)
                                                .rightSchema(ProductPopulated.schemaDetails().id(90210L).build())
                                                .leftSchemaId(90211L)
                                                .leftSchema(ProductPopulated.schemaDetails().id(90211L).build())
                                                .joinFields(newHashSet(JoinField.builder()
                                                        .rightJoinField(ProductPopulated.longField().build())
                                                        .leftJoinField(ProductPopulated.longField().build())
                                                        .build()))
                                                .build()))
                                .build()))
                        .build()));

        VavrAssert.assertValid(
                pipelineJobService.startJob(pipelineRequest, "matt"));

        verify(pipelineInvocationService).create(pipelineInvocationCaptor.capture());

        PipelineInvocation pipeline = pipelineInvocationCaptor.getValue();
        assertThat(pipeline.getSteps())
                .hasSize(1);

        PipelineStepInvocation stepInvocation = pipeline.getSteps().iterator().next();

        assertThat(stepInvocation.getPipelineStepId())
                .isEqualTo(12L);

        assertThat(stepInvocation.getInputDatasets())
                .containsExactlyInAnyOrder(
                        DatasetPopulated.pipelineStepInvocationDataset()
                                .datasetId(1000L).datasetRunKey(33L).build(),
                        DatasetPopulated.pipelineStepInvocationDataset()
                                .datasetId(1001L).datasetRunKey(44L).build());
    }

    @Test
    public void startJob_PipelineJoinStepDatasetsMissing_ReturnsErrors() {
        PipelineRequest pipelineRequest = ExternalClient.Populated.pipelineItemRequest().build();

        when(datasetService.findLatestDatasetForSchemaId(eq(90210L), anyString(), any()))
                .thenReturn(Validation.invalid(CRUDFailure.cannotFind("dataset")
                        .with("schemaId", 90210L)
                        .asFailure()));
        when(datasetService.findLatestDatasetForSchemaId(eq(90211L), anyString(), any()))
                .thenReturn(Validation.invalid(CRUDFailure.cannotFind("dataset")
                        .with("schemaId", 90211L)
                        .asFailure()));

        when(pipelineService.findByIdWithFilters(anyLong()))
                .thenReturn(Validation.valid(ProductPopulated.pipeline()
                        .steps(newHashSet(ProductPopulated.joinPipelineStep()
                                .id(12L)
                                .selects(newHashSet(
                                        Select.builder().select("NAME").outputFieldId(12341234L).build(),
                                        Select.builder().select("SALARY").outputFieldId(56785678L).build()))
                                .joins(newHashSet(
                                        ProductPopulated.join()
                                                .rightSchemaId(90210L)
                                                .rightSchema(ProductPopulated.schemaDetails().id(90210L).build())
                                                .leftSchemaId(90211L)
                                                .leftSchema(ProductPopulated.schemaDetails().id(90211L).build())
                                                .build()))
                                .build()))
                        .build()));

        VavrAssert.assertCollectionFailure(pipelineJobService.startJob(pipelineRequest, "matt"))
                .withOnlyFailures(
                        CRUDFailure.cannotFind("dataset")
                                .with("schemaId", 90210L)
                                .asFailure()
                                .toErrorResponse(),
                        CRUDFailure.cannotFind("dataset")
                                .with("schemaId", 90211L)
                                .asFailure()
                                .toErrorResponse()
                );
    }

    @Test
    public void startJob_PipelineJoinStepCannotFindOutputSchema_ReturnsErrors() {
        PipelineRequest pipelineRequest = ExternalClient.Populated.pipelineItemRequest().build();

        when(tableService.findWithValidation(anyLong()))
                .thenReturn(Validation.invalid(CRUDFailure.notFoundIds("Schema", 10L)));
        when(pipelineService.findByIdWithFilters(anyLong()))
                .thenReturn(Validation.valid(ProductPopulated.pipeline()
                        .steps(newHashSet(ProductPopulated.joinPipelineStep()
                                .id(12L)
                                .joins(newHashSet(
                                        ProductPopulated.join()
                                                .rightSchemaId(90210L)
                                                .rightSchema(ProductPopulated.schemaDetails().id(90210L).build())
                                                .leftSchemaId(90211L)
                                                .leftSchema(ProductPopulated.schemaDetails().id(90211L).build())
                                                .build()))
                                .build()))
                        .build()));

        VavrAssert.assertCollectionFailure(pipelineJobService.startJob(pipelineRequest, "matt"))
                .withOnlyFailures(
                        CRUDFailure.notFoundIds("Schema", 10L)
                                .toErrorResponse());
    }

    @Test
    public void startJob_PipelineJoinStepSelectOutputFieldNotOnOutputSchema_ReturnsErrors() {
        PipelineRequest pipelineRequest = ExternalClient.Populated.pipelineItemRequest().build();

        when(tableService.findWithValidation(anyLong()))
                .thenReturn(Validation.valid(ProductPopulated.table()
                        .id(1343214L)
                        .displayName("a table of employees")
                        .fields(newLinkedHashSet(asList(
                                ProductPopulated.stringField("NAME").id(1234L).build(),
                                ProductPopulated.intField("SALARY").id(5678L).build())))
                        .build()));

        when(pipelineService.findByIdWithFilters(anyLong()))
                .thenReturn(Validation.valid(ProductPopulated.pipeline()
                        .steps(newHashSet(ProductPopulated.joinPipelineStep()
                                .id(12L)
                                .selects(null)
                                .joins(newHashSet(
                                        ProductPopulated.join()
                                                .rightSchemaId(90210L)
                                                .rightSchema(ProductPopulated.schemaDetails().id(90210L).build())
                                                .leftSchemaId(90211L)
                                                .leftSchema(ProductPopulated.schemaDetails().id(90211L).build())
                                                .build()))
                                .build()))
                        .build()));

        when(pipelineStepService.getSelects(any()))
                .thenReturn(asList(
                        Select.builder().select("NAME").outputFieldId(1234L).build(),
                        Select.builder().select("SALARY").outputFieldId(7777L).build()));

        VavrAssert.assertCollectionFailure(pipelineJobService.startJob(pipelineRequest, "matt"))
                .withOnlyFailures(
                        CRUDFailure.cannotFind("Field")
                                .with("schema", "a table of employees")
                                .with("fieldId", 7777L)
                                .asFailure()
                                .toErrorResponse());
    }

    @Test
    public void startJob_PipelineJoinStep_CreatesPipelineInvocationWithEntityCodeAndReferenceDate() {
        PipelineRequest pipelineRequest = ExternalClient.Populated.pipelineItemRequest()
                .entityCode("entity")
                .referenceDate(LocalDate.of(2001, 1, 1))
                .build();

        when(pipelineService.findByIdWithFilters(anyLong()))
                .thenReturn(Validation.valid(ProductPopulated.pipeline()
                        .steps(newHashSet(ProductPopulated.joinPipelineStep()
                                .id(12L)
                                .selects(newHashSet(
                                        Select.builder().select("NAME").outputFieldId(12341234L).build(),
                                        Select.builder().select("SALARY").outputFieldId(56785678L).build()))
                                .joins(newHashSet(ProductPopulated.join()
                                        .rightSchemaId(90210L)
                                        .rightSchema(ProductPopulated.schemaDetails().id(90210L).build())
                                        .leftSchemaId(90211L)
                                        .leftSchema(ProductPopulated.schemaDetails().id(90211L).build())
                                        .joinFields(newHashSet(JoinField.builder()
                                                .rightJoinField(ProductPopulated.longField().build())
                                                .leftJoinField(ProductPopulated.longField().build())
                                                .build()))
                                        .build()))
                                .build()))
                        .build()));

        when(timeSource.nowAsLocalDateTime())
                .thenReturn(LocalDateTime.of(1995, 1, 1, 12, 1, 0));

        VavrAssert.assertValid(
                pipelineJobService.startJob(pipelineRequest, "matt"));

        verify(pipelineInvocationService).create(pipelineInvocationCaptor.capture());

        PipelineInvocation pipeline = pipelineInvocationCaptor.getValue();
        soft.assertThat(pipeline.getSteps())
                .hasSize(1);
        soft.assertThat(pipeline.getEntityCode())
                .isEqualTo("entity");
        soft.assertThat(pipeline.getReferenceDate())
                .isEqualTo(LocalDate.of(2001, 1, 1));
        soft.assertThat(pipeline.getCreatedTime())
                .isEqualTo(LocalDateTime.of(1995, 1, 1, 12, 1, 0));
    }

    @Test
    public void startJob_PipelineJoinStep_CreatesServiceRequestWithJoinItemAppConfigAndDatasetInputs() throws Exception {
        PipelineRequest pipelineRequest = ExternalClient.Populated.pipelineItemRequest().build();

        when(datasetService.findLatestDatasetForSchemaId(eq(90210L), anyString(), any()))
                .thenReturn(Validation.valid(DatasetPopulated.dataset()
                        .id(11111L)
                        .predicate("ROW_KEY < 100 AND ROW_KEY > 1")
                        .schema(ProductPopulated.table().physicalTableName("right").build())
                        .recordsCount(101L)
                        .build()));
        when(datasetService.findLatestDatasetForSchemaId(eq(90211L), anyString(), any()))
                .thenReturn(Validation.valid(DatasetPopulated.dataset()
                        .id(22222L)
                        .schema(ProductPopulated.table().physicalTableName("left").build())
                        .predicate("ROW_KEY < 500 AND ROW_KEY > 400")
                        .recordsCount(201L)
                        .build()));

        when(pipelineService.findByIdWithFilters(anyLong()))
                .thenReturn(Validation.valid(ProductPopulated.pipeline()
                        .steps(newHashSet(ProductPopulated.joinPipelineStep()
                                .id(12L)
                                .selects(newHashSet(
                                        Select.builder().select("NAME").outputFieldId(12341234L).build(),
                                        Select.builder().select("SALARY").outputFieldId(56785678L).build()))
                                .joins(newHashSet(ProductPopulated.join()
                                        .rightSchemaId(90210L)
                                        .rightSchema(ProductPopulated.schemaDetails().id(90210L).build())
                                        .leftSchemaId(90211L)
                                        .leftSchema(ProductPopulated.schemaDetails().id(90211L).build())
                                        .joinFields(newHashSet(JoinField.builder()
                                                .rightJoinField(ProductPopulated.longField().build())
                                                .leftJoinField(ProductPopulated.longField().build())
                                                .build()))
                                        .build()))
                                .build()))
                        .build()));

        VavrAssert.assertValid(
                pipelineJobService.startJob(pipelineRequest, "matt"));

        verify(serviceRequestRepository, times(2)).save(serviceRequestCaptor.capture());

        ServiceRequest serviceRequest = serviceRequestCaptor.getAllValues().get(1);
        PipelineAppConfig pipelineAppConfig =
                MAPPER.readValue(serviceRequest.getRequestMessage(), PipelineAppConfig.class);

        assertThat(pipelineAppConfig.getPipelineSteps()).hasSize(1);

        assertThat(pipelineAppConfig.getPipelineSteps()).hasSize(1);

        JoinStepAppConfig joinStepAppConfig = (JoinStepAppConfig) pipelineAppConfig.getPipelineSteps().get(0);

        assertThat(joinStepAppConfig.getPipelineStepDatasetLookups())
                .containsOnly(
                        PipelineStepDatasetLookup.datasetTableLookup(
                                DatasetTableLookup.builder()
                                        .datasetId(11111L)
                                        .datasetName("right")
                                        .predicate("ROW_KEY < 100 AND ROW_KEY > 1")
                                        .rowKeySeed(234L)
                                        .recordsCount(101L)
                                        .build()
                        ),
                        PipelineStepDatasetLookup.datasetTableLookup(
                                DatasetTableLookup.builder()
                                        .datasetId(22222L)
                                        .datasetName("left")
                                        .predicate("ROW_KEY < 500 AND ROW_KEY > 400")
                                        .rowKeySeed(234L)
                                        .recordsCount(201L)
                                        .build()
                        ));
    }

    @Test
    public void startJob_PipelineJoinStep_CreatesServiceRequestWithJoinItemAppConfigAndJoinConfig() throws Exception {
        PipelineRequest pipelineRequest = ExternalClient.Populated.pipelineItemRequest().build();

        when(pipelineService.findByIdWithFilters(anyLong()))
                .thenReturn(Validation.valid(ProductPopulated.pipeline()
                        .steps(newHashSet(ProductPopulated.joinPipelineStep()
                                .id(12L)
                                .selects(newHashSet(
                                        Select.builder().select("L.NAME").outputFieldId(12341234L).build(),
                                        Select.builder().select("R.SALARY").outputFieldId(56785678L).build()))
                                .joins(newHashSet(
                                        ProductPopulated.join()
                                                .rightSchemaId(43535L)
                                                .rightSchema(ProductPopulated.schemaDetails()
                                                        .id(43535L).physicalTableName("RIGHT").build())
                                                .leftSchemaId(16545L)
                                                .leftSchema(ProductPopulated.schemaDetails()
                                                        .id(16545L).physicalTableName("LEFT").build())
                                                .joinType(Join.JoinType.LEFT)
                                                .joinFields(newHashSet(
                                                        JoinField.builder()
                                                                .rightJoinField(ProductPopulated
                                                                        .longField("R1_ID").id(101L).build())
                                                                .leftJoinField(ProductPopulated
                                                                        .longField("L1_ID").id(100L).build())
                                                                .build(),
                                                        JoinField.builder()
                                                                .rightJoinField(ProductPopulated
                                                                        .longField("R2_ID").id(103L).build())
                                                                .leftJoinField(ProductPopulated
                                                                        .longField("L2_ID").id(104L).build())
                                                                .build()))
                                                .build()))
                                .build()))
                        .build()));

        VavrAssert.assertValid(
                pipelineJobService.startJob(pipelineRequest, "matt"));

        verify(serviceRequestRepository, times(2)).save(serviceRequestCaptor.capture());

        ServiceRequest serviceRequest = serviceRequestCaptor.getAllValues().get(1);
        PipelineAppConfig pipelineAppConfig =
                MAPPER.readValue(serviceRequest.getRequestMessage(), PipelineAppConfig.class);

        assertThat(pipelineAppConfig.getPipelineSteps()).hasSize(1);

        JoinStepAppConfig joinStepAppConfig =
                (JoinStepAppConfig) pipelineAppConfig.getPipelineSteps().get(0);

        assertThat(joinStepAppConfig.getJoinAppConfigs())
                .extracting(
                        JoinAppConfig::getLeftSchemaName,
                        JoinAppConfig::getRightSchemaName,
                        JoinAppConfig::getJoinType)
                .containsExactly(tuple("LEFT", "RIGHT", JoinAppConfig.JoinType.LEFT));

        assertThat(joinStepAppConfig.getJoinAppConfigs())
                .flatExtracting(JoinAppConfig::getJoinFields)
                .containsExactlyInAnyOrder(
                        JoinFieldConfig.builder()
                                .leftJoinField("L1_ID")
                                .rightJoinField("R1_ID")
                                .build(),
                        JoinFieldConfig.builder()
                                .leftJoinField("L2_ID")
                                .rightJoinField("R2_ID")
                                .build());
    }

    @Test
    public void startJob_PipelineJoinStep_CreatesServiceRequestWithJoinItemAppConfigAndMetadata() throws Exception {
        PipelineRequest pipelineRequest = ExternalClient.Populated.pipelineItemRequest().build();

        when(tableService.findWithValidation(anyLong()))
                .thenReturn(Validation.valid(ProductPopulated.table()
                        .id(1343214L)
                        .fields(newLinkedHashSet(asList(
                                ProductPopulated.stringField("X").id(111L).build(),
                                ProductPopulated.intField("Y").id(222L).build(),
                                ProductPopulated.intField("Z").id(333L).build())))
                        .build()));

        when(pipelineService.findByIdWithFilters(anyLong()))
                .thenReturn(Validation.valid(ProductPopulated.pipeline()
                        .steps(newHashSet(ProductPopulated.joinPipelineStep()
                                .id(125L)
                                .joins(newHashSet(ProductPopulated.join()
                                        .rightSchema(ProductPopulated.schemaDetails().id(25351L).build())
                                        .leftSchema(ProductPopulated.schemaDetails().id(25352L).build())
                                        .joinFields(newHashSet(JoinField.builder()
                                                .leftJoinField(ProductPopulated.longField().build())
                                                .rightJoinField(ProductPopulated.longField().build())
                                                .build()))
                                        .build()))
                                .build()))
                        .build()));

        when(pipelineStepService.getSelects(any()))
                .thenReturn(asList(
                        Select.builder().select("L.A").outputFieldId(111L).build(),
                        Select.builder().select("L.B").outputFieldId(222L).build(),
                        Select.builder().select("R.C").outputFieldId(333L).build()));

        when(pipelineInvocationService.create(any()))
                .then(invocation -> {
                    PipelineInvocation pipelineInvocation = invocation.getArgument(0);
                    pipelineInvocation.setId(1002L);
                    return pipelineInvocation;
                });

        VavrAssert.assertValid(
                pipelineJobService.startJob(pipelineRequest, "matt"));

        verify(serviceRequestRepository, times(2)).save(serviceRequestCaptor.capture());

        ServiceRequest serviceRequest = serviceRequestCaptor.getAllValues().get(1);
        PipelineAppConfig pipelineAppConfig =
                MAPPER.readValue(serviceRequest.getRequestMessage(), PipelineAppConfig.class);

        assertThat(pipelineAppConfig.getPipelineSteps()).hasSize(1);

        assertThat(pipelineAppConfig.getPipelineSteps())
                .hasSize(1);

        JoinStepAppConfig joinStepAppConfig = (JoinStepAppConfig) pipelineAppConfig.getPipelineSteps().get(0);

        soft.assertThat(pipelineAppConfig.getPipelineInvocationId())
                .isEqualTo(1002L);
        soft.assertThat(joinStepAppConfig.getTransformationType())
                .isEqualTo(JOIN);
        soft.assertThat(joinStepAppConfig.getSelects())
                .containsOnly(
                        SelectColumn.builder().select("L.A").as("X").build(),
                        SelectColumn.builder().select("L.B").as("Y").build(),
                        SelectColumn.builder().select("R.C").as("Z").build());
    }

    @Test
    public void startJob_PipelineMapStep_CreatesPipelineInvocationWithEntityCodeAndReferenceDate() {
        PipelineRequest pipelineRequest = ExternalClient.Populated.pipelineItemRequest()
                .entityCode("entity")
                .referenceDate(LocalDate.of(2001, 1, 1))
                .build();

        when(pipelineService.findByIdWithFilters(anyLong()))
                .thenReturn(Validation.valid(ProductPopulated.pipeline()
                        .steps(newHashSet(ProductPopulated.mapPipelineStep()
                                .id(125L)
                                .selects(newHashSet(
                                        Select.builder().select("NAME").outputFieldId(12341234L).build(),
                                        Select.builder().select("SALARY").outputFieldId(56785678L).build()))
                                .build()))
                        .build()));

        when(timeSource.nowAsLocalDateTime())
                .thenReturn(LocalDateTime.of(1995, 1, 1, 12, 1, 0));

        VavrAssert.assertValid(
                pipelineJobService.startJob(pipelineRequest, "matt"));

        verify(pipelineInvocationService).create(pipelineInvocationCaptor.capture());

        PipelineInvocation pipeline = pipelineInvocationCaptor.getValue();
        soft.assertThat(pipeline.getSteps())
                .hasSize(1);
        soft.assertThat(pipeline.getEntityCode())
                .isEqualTo("entity");
        soft.assertThat(pipeline.getReferenceDate())
                .isEqualTo(LocalDate.of(2001, 1, 1));
        soft.assertThat(pipeline.getCreatedTime())
                .isEqualTo(LocalDateTime.of(1995, 1, 1, 12, 1, 0));
    }

    @Test
    public void startJob_PipelineMapStepCannotFindOutputSchema_ReturnsErrors() {
        PipelineRequest pipelineRequest = ExternalClient.Populated.pipelineItemRequest().build();

        when(tableService.findWithValidation(anyLong()))
                .thenReturn(Validation.invalid(CRUDFailure.notFoundIds("Schema", 10L)));
        when(pipelineService.findByIdWithFilters(anyLong()))
                .thenReturn(Validation.valid(ProductPopulated.pipeline()
                        .steps(newHashSet(ProductPopulated.mapPipelineStep().id(125L).build()))
                        .build()));

        VavrAssert.assertCollectionFailure(pipelineJobService.startJob(pipelineRequest, "matt"))
                .withOnlyFailures(
                        CRUDFailure.notFoundIds("Schema", 10L)
                                .toErrorResponse());
    }

    @Test
    public void startJob_PipelineMapStepSelectOutputFieldNotOnOutputSchema_ReturnsErrors() {
        PipelineRequest pipelineRequest = ExternalClient.Populated.pipelineItemRequest().build();

        when(tableService.findWithValidation(anyLong()))
                .thenReturn(Validation.valid(ProductPopulated.table()
                        .id(1343214L)
                        .displayName("a table of employees")
                        .fields(newLinkedHashSet(asList(
                                ProductPopulated.stringField("NAME").id(1234L).build(),
                                ProductPopulated.intField("SALARY").id(5678L).build())))
                        .build()));

        when(pipelineService.findByIdWithFilters(anyLong()))
                .thenReturn(Validation.valid(ProductPopulated.pipeline()
                        .steps(newHashSet(ProductPopulated.mapPipelineStep()
                                .id(125L)
                                .selects(null)
                                .schemaIn(ProductPopulated.schemaDetails().id(136346L).build())
                                .schemaOut(ProductPopulated.schemaDetails().id(643643L).build())
                                .build()))
                        .build()));

        when(pipelineStepService.getSelects(any()))
                .thenReturn(asList(
                        Select.builder().select("NAME").outputFieldId(1234L).build(),
                        Select.builder().select("SALARY").outputFieldId(7777L).build()));

        VavrAssert.assertCollectionFailure(pipelineJobService.startJob(pipelineRequest, "matt"))
                .withOnlyFailures(
                        CRUDFailure.cannotFind("Field")
                                .with("schema", "a table of employees")
                                .with("fieldId", 7777L)
                                .asFailure()
                                .toErrorResponse());
    }

    @Test
    public void startJob_PipelineMapStepCannotFindInputDataset_ReturnsErrors() {
        PipelineRequest pipelineRequest = ExternalClient.Populated.pipelineItemRequest().build();

        when(datasetService.findLatestDatasetForSchemaId(anyLong(), anyString(), any()))
                .thenReturn(Validation.invalid(CRUDFailure.cannotFind("Dataset")
                        .with("schemaId", 100L)
                        .asFailure()));
        when(pipelineService.findByIdWithFilters(anyLong()))
                .thenReturn(Validation.valid(ProductPopulated.pipeline()
                        .steps(newHashSet(ProductPopulated.mapPipelineStep()
                                .id(125L)
                                .selects(newHashSet(
                                        Select.builder().select("NAME").outputFieldId(12341234L).build(),
                                        Select.builder().select("SALARY").outputFieldId(56785678L).build()))
                                .build()))
                        .build()));

        VavrAssert.assertCollectionFailure(pipelineJobService.startJob(pipelineRequest, "matt"))
                .withOnlyFailures(
                        CRUDFailure.cannotFind("Dataset")
                                .with("schemaId", 100L)
                                .asFailure()
                                .toErrorResponse());
    }

    @Test
    public void startJob_PipelineAggregationStep_CreatesPipelineInvocationWithEntityCodeAndReferenceDate() {
        PipelineRequest pipelineRequest = ExternalClient.Populated.pipelineItemRequest()
                .entityCode("entity")
                .referenceDate(LocalDate.of(2001, 1, 1))
                .build();

        when(pipelineService.findByIdWithFilters(anyLong()))
                .thenReturn(Validation.valid(ProductPopulated.pipeline()
                        .steps(newHashSet(ProductPopulated.aggregatePipelineStep()
                                .id(125L)
                                .selects(newHashSet(
                                        Select.builder().select("NAME").outputFieldId(12341234L).build(),
                                        Select.builder().select("SALARY").outputFieldId(56785678L).build()))
                                .schemaIn(ProductPopulated.schemaDetails().id(643634L).build())
                                .schemaOut(ProductPopulated.schemaDetails().id(435345L).build())
                                .build()))
                        .build()));

        when(timeSource.nowAsLocalDateTime())
                .thenReturn(LocalDateTime.of(1995, 1, 1, 12, 1, 0));

        VavrAssert.assertValid(
                pipelineJobService.startJob(pipelineRequest, "matt"));

        verify(pipelineInvocationService).create(pipelineInvocationCaptor.capture());

        PipelineInvocation pipeline = pipelineInvocationCaptor.getValue();
        soft.assertThat(pipeline.getSteps())
                .hasSize(1);
        soft.assertThat(pipeline.getEntityCode())
                .isEqualTo("entity");
        soft.assertThat(pipeline.getReferenceDate())
                .isEqualTo(LocalDate.of(2001, 1, 1));
        soft.assertThat(pipeline.getCreatedTime())
                .isEqualTo(LocalDateTime.of(1995, 1, 1, 12, 1, 0));
    }

    @Test
    public void startJob_PipelineAggregationStepCannotFindOutputSchema_ReturnsErrors() {
        PipelineRequest pipelineRequest = ExternalClient.Populated.pipelineItemRequest().build();

        when(tableService.findWithValidation(anyLong()))
                .thenReturn(Validation.invalid(CRUDFailure.notFoundIds("Schema", 10L)));
        when(pipelineService.findByIdWithFilters(anyLong()))
                .thenReturn(Validation.valid(ProductPopulated.pipeline()
                        .steps(newHashSet(ProductPopulated.aggregatePipelineStep()
                                .id(125L)
                                .schemaIn(ProductPopulated.schemaDetails().id(9L).build())
                                .schemaOut(ProductPopulated.schemaDetails().id(10L).build())
                                .build()))
                        .build()));

        VavrAssert.assertCollectionFailure(pipelineJobService.startJob(pipelineRequest, "matt"))
                .withOnlyFailures(
                        CRUDFailure.notFoundIds("Schema", 10L)
                                .toErrorResponse());
    }

    @Test
    public void startJob_PipelineAggregationStepCannotFindInputDataset_ReturnsErrors() {
        PipelineRequest pipelineRequest = ExternalClient.Populated.pipelineItemRequest().build();

        when(datasetService.findLatestDatasetForSchemaId(anyLong(), anyString(), any()))
                .thenReturn(Validation.invalid(CRUDFailure.cannotFind("Dataset")
                        .with("schemaId", 100L)
                        .asFailure()));
        when(pipelineService.findByIdWithFilters(anyLong()))
                .thenReturn(Validation.valid(ProductPopulated.pipeline()
                        .steps(newHashSet(ProductPopulated.aggregatePipelineStep()
                                .id(125L)
                                .selects(newHashSet(
                                        Select.builder().select("NAME").outputFieldId(12341234L).build(),
                                        Select.builder().select("SALARY").outputFieldId(56785678L).build()))
                                .schemaIn(ProductPopulated.schemaDetails().id(136346L).build())
                                .schemaOut(ProductPopulated.schemaDetails().id(643643L).build())
                                .build()))
                        .build()));

        VavrAssert.assertCollectionFailure(pipelineJobService.startJob(pipelineRequest, "matt"))
                .withOnlyFailures(
                        CRUDFailure.cannotFind("Dataset")
                                .with("schemaId", 100L)
                                .asFailure()
                                .toErrorResponse());
    }

    @Test
    public void startJob_PipelineAggregationStepSelectOutputFieldNotOnOutputSchema_ReturnsErrors() {
        PipelineRequest pipelineRequest = ExternalClient.Populated.pipelineItemRequest().build();

        when(tableService.findWithValidation(anyLong()))
                .thenReturn(Validation.valid(ProductPopulated.table()
                        .id(1343214L)
                        .displayName("a table of employees")
                        .fields(newLinkedHashSet(asList(
                                ProductPopulated.stringField("NAME").id(1234L).build(),
                                ProductPopulated.intField("SALARY").id(5678L).build())))
                        .build()));

        when(pipelineService.findByIdWithFilters(anyLong()))
                .thenReturn(Validation.valid(ProductPopulated.pipeline()
                        .steps(newHashSet(ProductPopulated.aggregatePipelineStep()
                                .id(125L)
                                .selects(null)
                                .schemaIn(ProductPopulated.schemaDetails().id(136346L).build())
                                .schemaOut(ProductPopulated.schemaDetails().id(643643L).build())
                                .build()))
                        .build()));

        when(pipelineStepService.getSelects(any()))
                .thenReturn(asList(
                        Select.builder().select("NAME").outputFieldId(1234L).build(),
                        Select.builder().select("SALARY").outputFieldId(7777L).build()));

        VavrAssert.assertCollectionFailure(pipelineJobService.startJob(pipelineRequest, "matt"))
                .withOnlyFailures(
                        CRUDFailure.cannotFind("Field")
                                .with("schema", "a table of employees")
                                .with("fieldId", 7777L)
                                .asFailure()
                                .toErrorResponse());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void startJob_PipelineScriptletStep_SavesServiceRequestMessageWithClassNameAndSchemaMappings() throws Exception {
        PipelineRequest pipelineRequest = ExternalClient.Populated.pipelineItemRequest().build();

        SchemaDetails inputSchemaA = ProductPopulated.schemaDetails("PHYSICAL_TABLE_A", 1).id(1L).build();
        SchemaDetails inputSchemaB = ProductPopulated.schemaDetails("PHYSICAL_TABLE_B", 1).id(2L).build();

        when(tableService.findWithValidation(3L))
                .thenReturn(Validation.valid(ProductPopulated.table()
                        .id(3L)
                        .physicalTableName("PHYSICAL_TABLE_OUT")
                        .version(1)
                        .build()));

        when(datasetService.findLatestDatasetForSchemaId(eq(1L), anyString(), any()))
                .thenReturn(Validation.valid(DatasetPopulated.dataset()
                        .id(111L)
                        .predicate("ROW_KEY > 0 AND ROW_KEY < 2000")
                        .schema(ProductPopulated.table().physicalTableName("PHYSICAL_TABLE_A").id(1L).build())
                        .recordsCount(1999L)
                        .rowKeySeed(2468L)
                        .build()));

        when(datasetService.findLatestDatasetForSchemaId(eq(2L), anyString(), any()))
                .thenReturn(Validation.valid(DatasetPopulated.dataset()
                        .id(222L)
                        .predicate("ROW_KEY > 0 AND ROW_KEY < 10")
                        .schema(ProductPopulated.table().physicalTableName("PHYSICAL_TABLE_B").id(2L).build())
                        .recordsCount(10L)
                        .rowKeySeed(1357L)
                        .build()));

        PipelineScriptletStep scriptletStep = ProductPopulated.scriptletPipelineStep()
                .id(123456L)
                .jarFile("my-scriptlet.jar")
                .className("com.vermeg.MyScriptlet")
                .schemaIns(newHashSet(
                        ProductPopulated.scriptletInput().inputName("InputA").schemaIn(inputSchemaA).id(1L).build(),
                        ProductPopulated.scriptletInput().inputName("InputB").schemaIn(inputSchemaB).id(2L).build()))
                .schemaOut(ProductPopulated.schemaDetails("PHYSICAL_TABLE_OUT", 1).id(3L).build())
                .schemaOutId(3L)
                .build();

        when(pipelineService.findByIdWithFilters(anyLong()))
                .thenReturn(Validation.valid(ProductPopulated.pipeline().steps(newHashSet(scriptletStep)).build()));

        VavrAssert.assertValid(pipelineJobService.startJob(pipelineRequest, "matt"));

        verify(serviceRequestRepository, times(2)).save(serviceRequestCaptor.capture());
        ServiceRequest createdRequest = serviceRequestCaptor.getAllValues().get(1);

        PipelineAppConfig pipelineAppConfig =
                MAPPER.readValue(createdRequest.getRequestMessage(), PipelineAppConfig.class);

        List<PipelineStepAppConfig> pipelineSteps = pipelineAppConfig.getPipelineSteps();

        assertThat(pipelineSteps)
                .hasSize(1);

        assertThat(pipelineSteps)
                .hasOnlyElementsOfTypes(ScriptletStepAppConfig.class);

        ScriptletStepAppConfig scriptletStepAppConfig = (ScriptletStepAppConfig) pipelineSteps.get(0);

        soft.assertThat(scriptletStepAppConfig.getTransformationType())
                .isEqualTo(TransformationType.SCRIPTLET);

        soft.assertThat(scriptletStepAppConfig.getJarFile())
                .isEqualTo("my-scriptlet.jar");

        soft.assertThat(scriptletStepAppConfig.getClassName())
                .isEqualTo("com.vermeg.MyScriptlet");

        soft.assertThat(scriptletStepAppConfig.getInputSchemaMappings())
                .containsOnly(entry("InputA", "PHYSICAL_TABLE_A"), entry("InputB", "PHYSICAL_TABLE_B"));

        soft.assertThat(scriptletStepAppConfig.getPipelineStepDatasetLookups())
                .containsOnly(
                        PipelineStepDatasetLookup.datasetTableLookup(
                                DatasetTableLookup.builder()
                                        .datasetId(111L)
                                        .datasetName("PHYSICAL_TABLE_A")
                                        .predicate("ROW_KEY > 0 AND ROW_KEY < 2000")
                                        .rowKeySeed(2468L)
                                        .recordsCount(1999L)
                                        .build()),
                        PipelineStepDatasetLookup.datasetTableLookup(
                                DatasetTableLookup.builder()
                                        .datasetId(222L)
                                        .datasetName("PHYSICAL_TABLE_B")
                                        .predicate("ROW_KEY > 0 AND ROW_KEY < 10")
                                        .rowKeySeed(1357L)
                                        .recordsCount(10L)
                                        .build()));
    }

    @Test
    public void startJob_PipelineScriptletStep_CreatesPipelineInvocation() {
        PipelineRequest pipelineRequest = ExternalClient.Populated.pipelineItemRequest()
                .entityCode("my-entity-code")
                .referenceDate(LocalDate.of(2012, 4, 5))
                .build();

        SchemaDetails inputSchemaA = ProductPopulated.schemaDetails("PHYSICAL_TABLE_A", 1).id(1L).build();
        SchemaDetails inputSchemaB = ProductPopulated.schemaDetails("PHYSICAL_TABLE_B", 1).id(2L).build();

        when(tableService.findWithValidation(3L))
                .thenReturn(Validation.valid(ProductPopulated.table()
                        .id(3L)
                        .physicalTableName("PHYSICAL_TABLE_OUT")
                        .version(1)
                        .build()));

        when(datasetService.findLatestDatasetForSchemaId(eq(1L), anyString(), any()))
                .thenReturn(Validation.valid(DatasetPopulated.dataset()
                        .id(111L)
                        .predicate("ROW_KEY > 0 AND ROW_KEY < 2000")
                        .schema(ProductPopulated.table().physicalTableName("PHYSICAL_TABLE_A").id(1L).build())
                        .recordsCount(1999L)
                        .rowKeySeed(2468L)
                        .runKey(15L)
                        .build()));

        when(datasetService.findLatestDatasetForSchemaId(eq(2L), anyString(), any()))
                .thenReturn(Validation.valid(DatasetPopulated.dataset()
                        .id(222L)
                        .predicate("ROW_KEY > 0 AND ROW_KEY < 10")
                        .schema(ProductPopulated.table().physicalTableName("PHYSICAL_TABLE_B").id(2L).build())
                        .recordsCount(10L)
                        .rowKeySeed(1357L)
                        .runKey(21L)
                        .build()));

        PipelineScriptletStep scriptletStep = ProductPopulated.scriptletPipelineStep()
                .id(123456L)
                .jarFile("my-scriptlet.jar")
                .className("com.vermeg.MyScriptlet")
                .schemaIns(newHashSet(
                        ProductPopulated.scriptletInput().inputName("InputA").schemaIn(inputSchemaA).id(1L).build(),
                        ProductPopulated.scriptletInput().inputName("InputB").schemaIn(inputSchemaB).id(2L).build()))
                .schemaOut(ProductPopulated.schemaDetails("PHYSICAL_TABLE_OUT", 1).id(3L).build())
                .schemaOutId(3L)
                .build();

        when(pipelineService.findByIdWithFilters(anyLong()))
                .thenReturn(Validation.valid(ProductPopulated.pipeline().steps(newHashSet(scriptletStep)).build()));

        when(timeSource.nowAsLocalDateTime())
                .thenReturn(LocalDateTime.of(1992, 4, 20, 12, 1, 19));

        VavrAssert.assertValid(pipelineJobService.startJob(pipelineRequest, "matt"));

        verify(pipelineInvocationService).create(pipelineInvocationCaptor.capture());

        PipelineInvocation pipelineInvocation = pipelineInvocationCaptor.getValue();
        soft.assertThat(pipelineInvocation.getEntityCode())
                .isEqualTo("my-entity-code");
        soft.assertThat(pipelineInvocation.getReferenceDate())
                .isEqualTo(LocalDate.of(2012, 4, 5));
        soft.assertThat(pipelineInvocation.getCreatedTime())
                .isEqualTo(LocalDateTime.of(1992, 4, 20, 12, 1, 19));
        soft.assertThat(pipelineInvocation.getSteps())
                .hasSize(1);

        PipelineStepInvocation stepInvocation = pipelineInvocation.getSteps().iterator().next();
        soft.assertThat(stepInvocation.getPipelineStepId())
                .isEqualTo(123456L);
        soft.assertThat(stepInvocation.getInputDatasets())
                .containsExactlyInAnyOrder(
                        PipelineStepInvocationDataset.builder().datasetId(111L).datasetRunKey(15L).build(),
                        PipelineStepInvocationDataset.builder().datasetId(222L).datasetRunKey(21L).build());
        soft.assertThat(stepInvocation.getInputPipelineStepIds())
                .isEmpty();
        soft.assertThat(stepInvocation.getStatus())
                .isEqualTo(PipelineStepStatus.PENDING);
    }

    @Test
    public void startJob_PipelineItems_CreatesPipelineInvocationWithNameAndTimestamp() {
        PipelineRequest pipelineRequest = ExternalClient.Populated.pipelineItemRequest()
                .name("Job name")
                .build();

        when(timeSource.nowAsLocalDateTime())
                .thenReturn(LocalDateTime.of(2007, 4, 12, 14, 2, 19));

        VavrAssert.assertValid(
                pipelineJobService.startJob(pipelineRequest, "matt"));

        verify(pipelineInvocationService).create(pipelineInvocationCaptor.capture());

        PipelineInvocation pipelineInvocation = pipelineInvocationCaptor.getValue();

        soft.assertThat(pipelineInvocation.getName())
                .isEqualTo("Job name");
        soft.assertThat(pipelineInvocation.getCreatedTime())
                .isEqualTo(LocalDateTime.of(2007, 4, 12, 14, 2, 19));
    }

    @Test
    public void startJob_PipelineHasMultipleSteps_SavesPipelineInvocationWithMultipleSteps() {
        PipelineRequest pipelineRequest = ExternalClient.Populated.pipelineItemRequest().build();

        when(datasetService.findLatestDatasetForSchemaId(eq(123L), any(), any()))
                .thenReturn(Validation.valid(DatasetPopulated.dataset().id(3243L).runKey(12L).build()));

        when(datasetService.findLatestDatasetForSchemaId(eq(456L), any(), any()))
                .thenReturn(Validation.valid(DatasetPopulated.dataset().id(5352L).runKey(13L).build()));

        when(datasetService.findLatestDatasetForSchemaId(eq(9876L), any(), any()))
                .thenReturn(Validation.valid(DatasetPopulated.dataset().id(1356L).runKey(14L).build()));

        when(datasetService.findLatestDatasetForSchemaId(eq(6789L), any(), any()))
                .thenReturn(Validation.valid(DatasetPopulated.dataset().id(6531L).runKey(15L).build()));

        Set<Select> stepSelects = newHashSet(
                Select.builder().select("NAME").outputFieldId(12341234L).build(),
                Select.builder().select("SALARY").outputFieldId(56785678L).build());

        when(pipelineService.findByIdWithFilters(anyLong()))
                .thenReturn(Validation.valid(ProductPopulated.pipeline()
                        .steps(newHashSet(
                                ProductPopulated.mapPipelineStep()
                                        .id(111L)
                                        .selects(stepSelects)
                                        .schemaInId(1L).schemaIn(ProductPopulated.schemaDetails().id(1L).build())
                                        .schemaOutId(2L).schemaOut(ProductPopulated.schemaDetails().id(2L).build())
                                        .build(),
                                ProductPopulated.mapPipelineStep()
                                        .id(222L)
                                        .selects(stepSelects)
                                        .schemaInId(2L).schemaIn(ProductPopulated.schemaDetails().id(2L).build())
                                        .schemaOutId(3L).schemaOut(ProductPopulated.schemaDetails().id(3L).build())
                                        .build(),
                                ProductPopulated.aggregatePipelineStep()
                                        .id(333L)
                                        .selects(stepSelects)
                                        .schemaInId(3L).schemaIn(ProductPopulated.schemaDetails().id(3L).build())
                                        .schemaOutId(4L).schemaOut(ProductPopulated.schemaDetails().id(4L).build())
                                        .build(),
                                ProductPopulated.joinPipelineStep()
                                        .id(444L)
                                        .selects(stepSelects)
                                        .joins(newHashSet(
                                                ProductPopulated.join()
                                                        .leftSchemaId(123L)
                                                        .leftSchema(ProductPopulated.schemaDetails().id(123L).build())
                                                        .rightSchemaId(456L)
                                                        .rightSchema(ProductPopulated.schemaDetails().id(456L).build())
                                                        .joinFields(newHashSet(JoinField.builder()
                                                                .rightJoinField(ProductPopulated.longField("B").build())
                                                                .leftJoinField(ProductPopulated.longField("A").build())
                                                                .build()))
                                                        .build(),
                                                ProductPopulated.join()
                                                        .leftSchemaId(123L)
                                                        .leftSchema(ProductPopulated.schemaDetails().id(123L).build())
                                                        .rightSchemaId(3L)
                                                        .rightSchema(ProductPopulated.schemaDetails().id(3L).build())
                                                        .joinFields(newHashSet(JoinField.builder()
                                                                .leftJoinField(ProductPopulated.longField("A").build())
                                                                .rightJoinField(ProductPopulated.longField("C").build())
                                                                .build()))
                                                        .build(),
                                                ProductPopulated.join()
                                                        .leftSchemaId(456L)
                                                        .leftSchema(ProductPopulated.schemaDetails().id(456L).build())
                                                        .rightSchemaId(4L)
                                                        .rightSchema(ProductPopulated.schemaDetails().id(4L).build())
                                                        .joinFields(newHashSet(JoinField.builder()
                                                                .leftJoinField(ProductPopulated.longField("A").build())
                                                                .rightJoinField(ProductPopulated.longField("D").build())
                                                                .build()))
                                                        .build()))
                                        .schemaOutId(5L).schemaOut(ProductPopulated.schemaDetails().id(5L).build())
                                        .build(),
                                ProductPopulated.joinPipelineStep()
                                        .id(999L)
                                        .selects(stepSelects)
                                        .joins(newHashSet(
                                                ProductPopulated.join()
                                                        .leftSchemaId(9876L)
                                                        .leftSchema(ProductPopulated.schemaDetails().id(9876L).build())
                                                        .rightSchemaId(6789L)
                                                        .rightSchema(ProductPopulated.schemaDetails().id(6789L).build())
                                                        .joinFields(newHashSet(JoinField.builder()
                                                                .leftJoinField(ProductPopulated.longField("X").build())
                                                                .rightJoinField(ProductPopulated.longField("Y").build())
                                                                .build()))
                                                        .build()
                                        ))
                                        .schemaOutId(1L).schemaOut(ProductPopulated.schemaDetails().id(1L).build())
                                        .build()
                        ))
                        .build()));

        when(pipelineInvocationService.create(any()))
                .thenReturn(DatasetPopulated.pipelineInvocation()
                        .id(1234567890L)
                        .steps(newHashSet(
                                DatasetPopulated.pipelineStepInvocation().id(12L).pipelineStepId(111L).build(),
                                DatasetPopulated.pipelineStepInvocation().id(34L).pipelineStepId(222L).build(),
                                DatasetPopulated.pipelineStepInvocation().id(56L).pipelineStepId(333L).build(),
                                DatasetPopulated.pipelineStepInvocation().id(78L).pipelineStepId(444L).build(),
                                DatasetPopulated.pipelineStepInvocation().id(90L).pipelineStepId(999L).build()
                        ))
                        .build());

        VavrAssert.assertValid(
                pipelineJobService.startJob(pipelineRequest, "matt"));

        verify(pipelineInvocationService, times(1)).create(pipelineInvocationCaptor.capture());

        PipelineInvocation pipelineInvocation = pipelineInvocationCaptor.getValue();

        assertThat(pipelineInvocation.getSteps())
                .hasSize(5);

        assertThat(pipelineInvocation.getSteps())
                .extracting(
                        PipelineStepInvocation::getPipelineStepId,
                        PipelineStepInvocation::getStatus,
                        PipelineStepInvocation::getInputPipelineStepIds,
                        PipelineStepInvocation::getInputDatasets
                )
                .containsOnly(
                        tuple(999L, PipelineStepStatus.PENDING, emptySet(),
                                newHashSet(
                                        DatasetPopulated.pipelineStepInvocationDataset()
                                                .datasetId(1356L).datasetRunKey(14L).build(),
                                        DatasetPopulated.pipelineStepInvocationDataset()
                                                .datasetId(6531L).datasetRunKey(15L).build())),

                        tuple(111L, PipelineStepStatus.PENDING, singleton(999L), emptySet()),

                        tuple(222L, PipelineStepStatus.PENDING, singleton(111L), emptySet()),

                        tuple(333L, PipelineStepStatus.PENDING, singleton(222L), emptySet()),

                        tuple(444L, PipelineStepStatus.PENDING, newHashSet(222L, 333L),
                                newHashSet(
                                        DatasetPopulated.pipelineStepInvocationDataset()
                                                .datasetId(3243L).datasetRunKey(12L).build(),
                                        DatasetPopulated.pipelineStepInvocationDataset()
                                                .datasetId(5352L).datasetRunKey(13L).build())));
    }

    @Test
    public void startJob_PipelineHasMultipleSteps_SavesServiceRequestWithSteps() throws Exception {
        PipelineRequest pipelineRequest = ExternalClient.Populated.pipelineItemRequest().build();

        when(datasetService.findLatestDatasetForSchemaId(eq(123L), any(), any()))
                .thenReturn(Validation.valid(
                        DatasetPopulated.dataset()
                                .id(3243L)
                                .predicate("1 = 0")
                                .schema(ProductPopulated.table()
                                        .physicalTableName("TABLE_A")
                                        .build())
                                .recordsCount(10L)
                                .build()));

        when(datasetService.findLatestDatasetForSchemaId(eq(456L), any(), any()))
                .thenReturn(Validation.valid(
                        DatasetPopulated.dataset()
                                .id(5352L)
                                .predicate("ROW_KEY <= 0 AND ROW_KEY >= 10")
                                .schema(ProductPopulated.table()
                                        .physicalTableName("TABLE_B")
                                        .build())
                                .recordsCount(20L)
                                .build()));

        when(datasetService.findLatestDatasetForSchemaId(eq(9876L), any(), any()))
                .thenReturn(Validation.valid(
                        DatasetPopulated.dataset()
                                .id(1356L)
                                .predicate("A = B")
                                .schema(ProductPopulated.table()
                                        .physicalTableName("TABLE_C")
                                        .build())
                                .recordsCount(30L)
                                .build()));

        when(datasetService.findLatestDatasetForSchemaId(eq(6789L), any(), any()))
                .thenReturn(Validation.valid(
                        DatasetPopulated.dataset()
                                .id(6531L)
                                .predicate("B = A")
                                .schema(ProductPopulated.table()
                                        .physicalTableName("TABLE_D")
                                        .build())
                                .recordsCount(40L)
                                .build()));

        Set<Select> stepSelects = newHashSet(
                Select.builder().select("NAME").outputFieldId(12341234L).build(),
                Select.builder().select("SALARY").outputFieldId(56785678L).build());

        when(pipelineService.findByIdWithFilters(anyLong()))
                .thenReturn(Validation.valid(ProductPopulated.pipeline()
                        .steps(newHashSet(
                                ProductPopulated.mapPipelineStep()
                                        .id(111L)
                                        .selects(stepSelects)
                                        .schemaInId(1L).schemaIn(ProductPopulated.schemaDetails().id(1L).build())
                                        .schemaOutId(2L).schemaOut(ProductPopulated.schemaDetails().id(2L).build())
                                        .build(),
                                ProductPopulated.mapPipelineStep()
                                        .id(222L)
                                        .selects(stepSelects)
                                        .schemaInId(2L).schemaIn(ProductPopulated.schemaDetails().id(2L).build())
                                        .schemaOutId(3L).schemaOut(ProductPopulated.schemaDetails().id(3L).build())
                                        .build(),
                                ProductPopulated.aggregatePipelineStep()
                                        .id(333L)
                                        .selects(stepSelects)
                                        .schemaInId(3L).schemaIn(ProductPopulated.schemaDetails().id(3L).build())
                                        .schemaOutId(4L).schemaOut(ProductPopulated.schemaDetails().id(4L).build())
                                        .build(),
                                ProductPopulated.joinPipelineStep()
                                        .id(444L)
                                        .selects(stepSelects)
                                        .joins(newHashSet(
                                                ProductPopulated.join()
                                                        .leftSchemaId(123L)
                                                        .leftSchema(ProductPopulated.schemaDetails().id(123L).build())
                                                        .rightSchemaId(456L)
                                                        .rightSchema(ProductPopulated.schemaDetails().id(456L).build())
                                                        .joinFields(newHashSet(JoinField.builder()
                                                                .leftJoinField(ProductPopulated.longField("A").build())
                                                                .rightJoinField(ProductPopulated.longField("B").build())
                                                                .build()))
                                                        .build(),
                                                ProductPopulated.join()
                                                        .leftSchemaId(123L)
                                                        .leftSchema(ProductPopulated.schemaDetails().id(123L).build())
                                                        .rightSchemaId(3L)
                                                        .rightSchema(ProductPopulated.schemaDetails().id(3L).build())
                                                        .joinFields(newHashSet(JoinField.builder()
                                                                .leftJoinField(ProductPopulated.longField("A").build())
                                                                .rightJoinField(ProductPopulated.longField("C").build())
                                                                .build()))
                                                        .build(),
                                                ProductPopulated.join()
                                                        .leftSchemaId(456L)
                                                        .leftSchema(ProductPopulated.schemaDetails().id(456L).build())
                                                        .rightSchemaId(4L)
                                                        .rightSchema(ProductPopulated.schemaDetails().id(4L).build())
                                                        .joinFields(newHashSet(JoinField.builder()
                                                                .leftJoinField(ProductPopulated.longField("A").build())
                                                                .rightJoinField(ProductPopulated.longField("D").build())
                                                                .build()))
                                                        .build()))
                                        .schemaOutId(5L).schemaOut(ProductPopulated.schemaDetails().id(5L).build())
                                        .build(),
                                ProductPopulated.joinPipelineStep()
                                        .id(999L)
                                        .selects(stepSelects)
                                        .joins(newHashSet(
                                                ProductPopulated.join()
                                                        .leftSchemaId(9876L)
                                                        .leftSchema(ProductPopulated.schemaDetails().id(9876L).build())
                                                        .rightSchemaId(6789L)
                                                        .rightSchema(ProductPopulated.schemaDetails().id(6789L).build())
                                                        .joinFields(newHashSet(JoinField.builder()
                                                                .leftJoinField(ProductPopulated.longField("X").build())
                                                                .rightJoinField(ProductPopulated.longField("Y").build())
                                                                .build()))
                                                        .build()
                                        ))
                                        .schemaOutId(1L).schemaOut(ProductPopulated.schemaDetails().id(1L).build())
                                        .build()
                        ))
                        .build()));

        when(pipelineInvocationService.create(any()))
                .thenReturn(DatasetPopulated.pipelineInvocation()
                        .id(1234567890L)
                        .steps(newHashSet(
                                DatasetPopulated.pipelineStepInvocation().id(12L).pipelineStepId(111L).build(),
                                DatasetPopulated.pipelineStepInvocation().id(34L).pipelineStepId(222L).build(),
                                DatasetPopulated.pipelineStepInvocation().id(56L).pipelineStepId(333L).build(),
                                DatasetPopulated.pipelineStepInvocation().id(78L).pipelineStepId(444L).build(),
                                DatasetPopulated.pipelineStepInvocation().id(90L).pipelineStepId(999L).build()
                        ))
                        .build());

        VavrAssert.assertValid(
                pipelineJobService.startJob(pipelineRequest, "matt"));

        verify(serviceRequestRepository, times(2)).save(serviceRequestCaptor.capture());
        ServiceRequest serviceRequest = serviceRequestCaptor.getAllValues().get(1);

        PipelineAppConfig pipelineAppConfig =
                MAPPER.readValue(serviceRequest.getRequestMessage(), PipelineAppConfig.class);

        assertThat(pipelineAppConfig.getPipelineSteps())
                .hasSize(5);

        assertThat(pipelineAppConfig.getPipelineSteps())
                .extracting(
                        PipelineStepAppConfig::getPipelineStepInvocationId,
                        PipelineStepAppConfig::getTransformationType)
                .containsExactly(
                        tuple(90L, JOIN),
                        tuple(12L, MAP),
                        tuple(34L, MAP),
                        tuple(56L, AGGREGATION),
                        tuple(78L, JOIN));

        soft.assertThat(pipelineAppConfig.getPipelineSteps().get(0).getPipelineStepDatasetLookups())
                .containsExactlyInAnyOrder(
                        PipelineStepDatasetLookup.datasetTableLookup(
                                DatasetTableLookup.builder()
                                        .datasetId(1356L)
                                        .datasetName("TABLE_C")
                                        .predicate("A = B")
                                        .rowKeySeed(234L)
                                        .recordsCount(30L)
                                        .build()),
                        PipelineStepDatasetLookup.datasetTableLookup(
                                DatasetTableLookup.builder()
                                        .datasetId(6531L)
                                        .datasetName("TABLE_D")
                                        .predicate("B = A")
                                        .rowKeySeed(234L)
                                        .recordsCount(40L)
                                        .build()));

        soft.assertThat(pipelineAppConfig.getPipelineSteps().get(1).getPipelineStepDatasetLookups())
                .containsExactly(PipelineStepDatasetLookup.pipelineStepInvocationId(90L));

        soft.assertThat(pipelineAppConfig.getPipelineSteps().get(2).getPipelineStepDatasetLookups())
                .containsExactly(PipelineStepDatasetLookup.pipelineStepInvocationId(12L));

        soft.assertThat(pipelineAppConfig.getPipelineSteps().get(3).getPipelineStepDatasetLookups())
                .containsExactly(PipelineStepDatasetLookup.pipelineStepInvocationId(34L));

        soft.assertThat(pipelineAppConfig.getPipelineSteps().get(4).getPipelineStepDatasetLookups())
                .containsExactlyInAnyOrder(
                        PipelineStepDatasetLookup.datasetTableLookup(
                                DatasetTableLookup.builder()
                                        .datasetId(3243L)
                                        .datasetName("TABLE_A")
                                        .predicate("1 = 0")
                                        .rowKeySeed(234L)
                                        .recordsCount(10L)
                                        .build()),
                        PipelineStepDatasetLookup.datasetTableLookup(
                                DatasetTableLookup.builder()
                                        .datasetId(5352L)
                                        .datasetName("TABLE_B")
                                        .predicate("ROW_KEY <= 0 AND ROW_KEY >= 10")
                                        .rowKeySeed(234L)
                                        .recordsCount(20L)
                                        .build()),
                        PipelineStepDatasetLookup.pipelineStepInvocationId(34L),
                        PipelineStepDatasetLookup.pipelineStepInvocationId(56L));
    }

    @Test
    public void startJob_PipelineProductWithCalendar_AddsCalendarToAppConfig() throws Exception {
        PipelineRequest pipelineRequest = ExternalClient.Populated.pipelineItemRequest().build();

        when(datasetService.findLatestDatasetForSchemaId(eq(920L), anyString(), any()))
                .thenReturn(Validation.valid(DatasetPopulated.dataset().id(1001L).runKey(5L).build()));

        when(pipelineService.findByIdWithFilters(anyLong()))
                .thenReturn(Validation.valid(ProductPopulated.pipeline()
                        .steps(newHashSet(ProductPopulated.mapPipelineStep()
                                .id(12L)
                                .selects(newHashSet(
                                        Select.builder().select("NAME").outputFieldId(12341234L).build(),
                                        Select.builder().select("SALARY").outputFieldId(56785678L).build()))
                                .schemaInId(920L).schemaIn(ProductPopulated.schemaDetails().id(920L).build())
                                .schemaOutId(921L).schemaOut(ProductPopulated.schemaDetails().id(921L).build())
                                .build()))
                        .build()));

        when(calendarService.createHolidayCalendar(any(), any(), any()))
                .thenReturn(Validation.valid(ServerConfig.Populated.holidayCalendar().build()));

        VavrAssert.assertValid(
                pipelineJobService.startJob(pipelineRequest, "matt"));

        verify(serviceRequestRepository, times(2)).save(serviceRequestCaptor.capture());
        ServiceRequest serviceRequest = serviceRequestCaptor.getAllValues().get(1);
        PipelineAppConfig pipelineAppConfig =
                MAPPER.readValue(serviceRequest.getRequestMessage(), PipelineAppConfig.class);

        assertThat(pipelineAppConfig.getSparkFunctionConfig().getHolidayCalendar())
                .isEqualTo(ServerConfig.Populated.holidayCalendar().build());
    }

    @Test
    public void startJob_PipelineStepIdNullAndPipelineItems_CreatesPipelineInvocationWithMultipleSteps() {
        PipelineRequest pipelineRequest = ExternalClient.Populated.pipelineItemRequest().build();

        when(pipelineService.findByIdWithFilters(anyLong()))
                .thenReturn(Validation.valid(ProductPopulated.pipeline()
                        .steps(newHashSet(
                                anyPipelineStep(
                                        12L,
                                        newHashSet(
                                                anyPipelineStepSelect("FULL_NAME", 2001L),
                                                anyPipelineStepSelect("MONTHLY_SALARY", 2002L)
                                        ),
                                        920L,
                                        921L),
                                anyPipelineStep(
                                        13L,
                                        newHashSet(
                                                anyPipelineStepSelect("FULL_NAME", 3001L),
                                                anyPipelineStepSelect("ANNUAL_SALARY", 3002L)
                                        ),
                                        921L,
                                        922L)))
                        .build()));

        when(tableService.findWithValidation(921L))
                .thenReturn(Validation.valid(ProductPopulated.table()
                        .id(1000014L)
                        .fields(newLinkedHashSet(asList(
                                ProductPopulated.stringField("NAME").id(2001L).build(),
                                ProductPopulated.intField("MONTHLY_SALARY").id(2002L).build())))
                        .build()));

        when(tableService.findWithValidation(922L))
                .thenReturn(Validation.valid(ProductPopulated.table()
                        .id(1000015L)
                        .fields(newLinkedHashSet(asList(
                                ProductPopulated.stringField("FULL_NAME").id(3001L).build(),
                                ProductPopulated.intField("ANNUAL_SALARY").id(3002L).build())))
                        .build()));

        when(pipelineInvocationService.create(any()))
                .thenReturn(DatasetPopulated.pipelineInvocation()
                        .id(15001L)
                        .steps(newHashSet(
                                DatasetPopulated.pipelineStepInvocation().id(501L).pipelineStepId(12L).build(),
                                DatasetPopulated.pipelineStepInvocation().id(502L).pipelineStepId(13L).build()
                        ))
                        .build());

        VavrAssert.assertValid(
                pipelineJobService.startJob(pipelineRequest, "matt"));

        verify(pipelineInvocationService).create(pipelineInvocationCaptor.capture());

        PipelineInvocation pipeline = pipelineInvocationCaptor.getValue();
        assertThat(pipeline.getSteps())
                .hasSize(2);

        assertThat(pipeline.getSteps())
                .extracting(
                        PipelineStepInvocation::getPipelineStepId,
                        PipelineStepInvocation::getStatus
                )
                .containsOnly(
                        tuple(12L, PipelineStepStatus.PENDING),

                        tuple(13L, PipelineStepStatus.PENDING));
    }

    @Test
    public void startJob_PipelineWithStepIdNotNullAndPipelineItems_CreatesPipelineInvocationWithOnePendingStatusStep() {
        Table schemaB = ProductPopulated.table().id(921L).physicalTableName("B")
                .fields(newLinkedHashSet(asList(
                        ProductPopulated.stringField("NAME").id(1005L).build(),
                        ProductPopulated.stringField("SALARY").id(1006L).build())))
                .build();
        Table schemaC = ProductPopulated.table().id(922L).physicalTableName("C")
                .fields(newLinkedHashSet(asList(
                        ProductPopulated.stringField("NAME").id(2005L).build(),
                        ProductPopulated.stringField("SALARY").id(2006L).build())))
                .build();
        Table schemaD = ProductPopulated.table().id(923L).physicalTableName("D")
                .fields(newLinkedHashSet(asList(
                        ProductPopulated.stringField("NAME").id(3005L).build(),
                        ProductPopulated.stringField("SALARY").id(3006L).build())))
                .build();

        SchemaDetails schemaDetails1 = ProductPopulated.schemaDetails().id(920L).physicalTableName("A").build();
        SchemaDetails schemaDetails2 = ProductPopulated.schemaDetails().id(921L).physicalTableName("B").build();
        SchemaDetails schemaDetails3 = ProductPopulated.schemaDetails().id(922L).physicalTableName("C").build();
        SchemaDetails schemaDetails4 = ProductPopulated.schemaDetails().id(923L).physicalTableName("D").build();

        when(pipelineService.findByIdWithFilters(anyLong()))
                .thenReturn(Validation.valid(ProductPopulated.pipeline()
                        .steps(
                                newHashSet(
                                        ProductPopulated.mapPipelineStep()
                                                .id(12L)
                                                .selects(newHashSet(
                                                        anyPipelineStepSelect("NAME", 1005L),
                                                        anyPipelineStepSelect("SALARY", 1006L)))
                                                .schemaInId(920L).schemaIn(schemaDetails1)
                                                .schemaOutId(921L).schemaOut(schemaDetails2)
                                                .build(),
                                        ProductPopulated.mapPipelineStep()
                                                .id(13L)
                                                .selects(newHashSet(
                                                        anyPipelineStepSelect("NAME", 2005L),
                                                        anyPipelineStepSelect("SALARY", 2006L)))
                                                .schemaInId(921L).schemaIn(schemaDetails2)
                                                .schemaOutId(922L).schemaOut(schemaDetails3)
                                                .build(),
                                        ProductPopulated.mapPipelineStep()
                                                .id(14L)
                                                .selects(newHashSet(
                                                        anyPipelineStepSelect("NAME", 3005L),
                                                        anyPipelineStepSelect("SALARY", 3006L)))
                                                .schemaInId(922L).schemaIn(schemaDetails3)
                                                .schemaOutId(923L).schemaOut(schemaDetails4)
                                                .build()
                                ))
                        .build()));

        when(tableService.findWithValidation(921L))
                .thenReturn(Validation.valid(schemaB));

        when(tableService.findWithValidation(922L))
                .thenReturn(Validation.valid(schemaC));

        when(tableService.findWithValidation(923L))
                .thenReturn(Validation.valid(schemaD));

        when(datasetService.findLatestDatasetForSchemaId(anyLong(), anyString(), any()))
                .thenReturn(Validation.valid(DatasetPopulated.dataset()
                        .id(567L)
                        .schema(schemaB)
                        .predicate("ROW_KEY < 100 AND ROW_KEY > 0")
                        .rowKeySeed(789L)
                        .recordsCount(30L)
                        .build()));

        PipelineRequest pipelineRequest = ExternalClient.Populated.pipelineItemRequest()
                .entityCode("EntityCode")
                .referenceDate(LocalDate.of(2019, 10, 30))
                .pipelineId(123456789L)
                .stepId(13L)
                .build();

        VavrAssert.assertValid(
                pipelineJobService.startJob(pipelineRequest, "user"));

        verify(pipelineInvocationService).create(pipelineInvocationCaptor.capture());

        PipelineInvocation pipeline = pipelineInvocationCaptor.getValue();
        assertThat(pipeline.getSteps())
                .hasSize(3);

        assertThat(pipeline.getSteps())
                .extracting(
                        PipelineStepInvocation::getPipelineStepId,
                        PipelineStepInvocation::getStatus
                )
                .containsOnly(
                        tuple(12L, PipelineStepStatus.SKIPPED),
                        tuple(13L, PipelineStepStatus.PENDING),
                        tuple(14L, PipelineStepStatus.SKIPPED));
    }

    @Test
    public void startJob_PipelineWithInvalidStepIdAndPipelineItems_ReturnPipelineStepMessageError() {
        PipelineRequest pipelineRequest = ExternalClient.Populated.pipelineItemRequest()
                .stepId(15L)
                .build();

        when(pipelineService.findByIdWithFilters(anyLong()))
                .thenReturn(Validation.valid(ProductPopulated.pipeline()
                        .steps(newHashSet(
                                anyPipelineStep(
                                        12L,
                                        newHashSet(
                                                anyPipelineStepSelect("NAME", 2001L),
                                                anyPipelineStepSelect("MONTHLY_SALARY", 2002L)
                                        ),
                                        920L,
                                        921L),
                                anyPipelineStep(
                                        13L,
                                        newHashSet(
                                                anyPipelineStepSelect("FULL_NAME", 3001L),
                                                anyPipelineStepSelect("ANNUAL_SALARY", 3002L)
                                        ),
                                        921L,
                                        922L)))
                        .build()));

        VavrAssert.assertCollectionFailure(
                pipelineJobService.startJob(pipelineRequest, "matt"))
                .withFailure(ErrorResponse.valueOf("Could not find Pipeline Step for ids [15]", "NOT_FOUND"));
    }

    @Test
    public void startJob_PipelineWithStepIdWithoutDatasetAndPipelineItems_ReturnPipelineStepMessageError() {
        SchemaDetails schemaDetails1 = ProductPopulated.schemaDetails().id(920L).physicalTableName("A").build();
        SchemaDetails schemaDetails2 = ProductPopulated.schemaDetails().id(921L).physicalTableName("B").build();
        SchemaDetails schemaDetails3 = ProductPopulated.schemaDetails().id(922L).physicalTableName("C").build();
        SchemaDetails schemaDetails4 = ProductPopulated.schemaDetails().id(923L).physicalTableName("D").build();

        when(pipelineService.findByIdWithFilters(anyLong()))
                .thenReturn(Validation.valid(ProductPopulated.pipeline()
                        .steps(
                                newHashSet(
                                        ProductPopulated.mapPipelineStep()
                                                .id(12L)
                                                .selects(newHashSet(
                                                        anyPipelineStepSelect("NAME", 1005L),
                                                        anyPipelineStepSelect("SALARY", 1006L)))
                                                .schemaInId(920L).schemaIn(schemaDetails1)
                                                .schemaOutId(921L).schemaOut(schemaDetails2)
                                                .build(),
                                        ProductPopulated.mapPipelineStep()
                                                .id(13L)
                                                .selects(newHashSet(
                                                        anyPipelineStepSelect("NAME", 2005L),
                                                        anyPipelineStepSelect("SALARY", 2006L)))
                                                .schemaInId(921L).schemaIn(schemaDetails2)
                                                .schemaOutId(922L).schemaOut(schemaDetails3)
                                                .build(),
                                        ProductPopulated.mapPipelineStep()
                                                .id(14L)
                                                .selects(newHashSet(
                                                        anyPipelineStepSelect("NAME", 3005L),
                                                        anyPipelineStepSelect("SALARY", 3006L)))
                                                .schemaInId(922L).schemaIn(schemaDetails3)
                                                .schemaOutId(923L).schemaOut(schemaDetails4)
                                                .build()
                                ))
                        .build()));

        when(datasetService.findLatestDatasetForSchemaId(anyLong(), anyString(), any()))
                .thenReturn(Validation.invalid(CRUDFailure.cannotFind("Dataset")
                        .with("SchemaId", 2L)
                        .asFailure()));

        PipelineRequest pipelineRequest = ExternalClient.Populated.pipelineItemRequest()
                .entityCode("EntityCode")
                .referenceDate(LocalDate.of(2019, 10, 30))
                .pipelineId(123456789L)
                .stepId(13L)
                .build();

        VavrAssert.assertCollectionFailure(
                pipelineJobService.startJob(pipelineRequest, "matt"))
                .withFailure(ErrorResponse.valueOf(
                        "Cannot find Dataset with SchemaId '2'",
                        "NOT_FOUND"));
    }

    @Test
    public void startJob_PipelineWithStepAndIgnisFeatureNotEnabled_ThrowsFeatureNotActiveException() {
        SchemaDetails schemaDetails1 = ProductPopulated.schemaDetails().id(920L).physicalTableName("A").build();
        SchemaDetails schemaDetails2 = ProductPopulated.schemaDetails().id(921L).physicalTableName("B").build();
        SchemaDetails schemaDetails3 = ProductPopulated.schemaDetails().id(922L).physicalTableName("C").build();

        when(pipelineService.findByIdWithFilters(anyLong()))
                .thenReturn(Validation.valid(ProductPopulated.pipeline()
                        .steps(
                                newHashSet(
                                        ProductPopulated.mapPipelineStep()
                                                .id(12L)
                                                .selects(newHashSet(
                                                        anyPipelineStepSelect("NAME", 1005L),
                                                        anyPipelineStepSelect("SALARY", 1006L)))
                                                .schemaInId(920L).schemaIn(schemaDetails1)
                                                .schemaOutId(921L).schemaOut(schemaDetails2)
                                                .build(),
                                        ProductPopulated.mapPipelineStep()
                                                .id(13L)
                                                .selects(newHashSet(
                                                        anyPipelineStepSelect("NAME", 2005L),
                                                        anyPipelineStepSelect("SALARY", 2006L)))
                                                .schemaInId(921L).schemaIn(schemaDetails2)
                                                .schemaOutId(922L).schemaOut(schemaDetails3)
                                                .build()
                                ))
                        .build()));

        when(featureManager.isActive(any())).thenReturn(false);

        PipelineRequest pipelineRequest = ExternalClient.Populated.pipelineItemRequest()
                .stepId(15L)
                .build();

        assertThatThrownBy(() -> pipelineJobService.startJob(pipelineRequest, "user"))
                .isInstanceOf(FeatureNotActiveException.class)
                .hasMessage("Feature RUN_PIPLINE_STEP is not active");
    }

    private PipelineMapStep anyPipelineStep(
            final long id,
            final HashSet<Select> stepSelects,
            final long schemaInId,
            final long schemaOutId) {
        return ProductPopulated.mapPipelineStep()
                .id(id)
                .selects(stepSelects)
                .schemaInId(schemaInId).schemaIn(ProductPopulated.schemaDetails().id(schemaInId).build())
                .schemaOutId(schemaOutId).schemaOut(ProductPopulated.schemaDetails().id(schemaOutId).build())
                .build();
    }

    private Select anyPipelineStepSelect(final String selectFieldName, final long outputFieldId) {
        return Select.builder()
                .select(selectFieldName)
                .outputFieldId(outputFieldId)
                .build();
    }
}
