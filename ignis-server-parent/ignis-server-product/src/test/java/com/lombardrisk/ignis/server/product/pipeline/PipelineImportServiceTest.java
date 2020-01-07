package com.lombardrisk.ignis.server.product.pipeline;

import com.google.common.collect.ImmutableSet;
import com.lombardrisk.ignis.client.external.fixture.ExternalClient;
import com.lombardrisk.ignis.client.external.pipeline.export.PipelineExport;
import com.lombardrisk.ignis.client.external.pipeline.export.step.JoinExport;
import com.lombardrisk.ignis.client.external.pipeline.export.step.JoinFieldExport;
import com.lombardrisk.ignis.client.external.pipeline.export.step.JoinType;
import com.lombardrisk.ignis.client.external.pipeline.export.step.ScriptletInputExport;
import com.lombardrisk.ignis.client.external.pipeline.export.step.select.OrderExport;
import com.lombardrisk.ignis.client.external.pipeline.export.step.select.WindowExport;
import com.lombardrisk.ignis.common.assertions.VavrAssert;
import com.lombardrisk.ignis.data.common.error.ErrorResponse;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.server.product.fixture.ProductPopulated;
import com.lombardrisk.ignis.server.product.pipeline.model.Pipeline;
import com.lombardrisk.ignis.server.product.pipeline.model.PipelineAggregationStep;
import com.lombardrisk.ignis.server.product.pipeline.model.PipelineJoinStep;
import com.lombardrisk.ignis.server.product.pipeline.model.PipelineScriptletStep;
import com.lombardrisk.ignis.server.product.pipeline.model.PipelineStep;
import com.lombardrisk.ignis.server.product.pipeline.model.PipelineUnionStep;
import com.lombardrisk.ignis.server.product.pipeline.model.PipelineWindowStep;
import com.lombardrisk.ignis.server.product.pipeline.select.Order;
import com.lombardrisk.ignis.server.product.pipeline.select.PipelineFilter;
import com.lombardrisk.ignis.server.product.pipeline.select.Select;
import com.lombardrisk.ignis.server.product.pipeline.select.Window;
import com.lombardrisk.ignis.server.product.pipeline.transformation.Join;
import com.lombardrisk.ignis.server.product.pipeline.transformation.JoinField;
import com.lombardrisk.ignis.server.product.pipeline.transformation.ScriptletInput;
import com.lombardrisk.ignis.server.product.pipeline.validation.PipelineImportBeanValidator;
import com.lombardrisk.ignis.server.product.productconfig.model.ProductConfig;
import com.lombardrisk.ignis.server.product.table.model.Table;
import io.vavr.control.Validation;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.validation.Validator;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static com.lombardrisk.ignis.client.external.fixture.ExternalClient.Populated.schemaReference;
import static com.lombardrisk.ignis.client.external.fixture.ExternalClient.Populated.selectExport;
import static com.lombardrisk.ignis.client.external.fixture.ExternalClient.Populated.unionExport;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PipelineImportServiceTest {

    @Mock
    private PipelineJpaRepository pipelineJpaRepository;
    @Mock
    private PipelineService pipelineService;
    @Mock
    private Validator validator;

    private PipelineImportService pipelineImportService;

    @Rule
    public JUnitSoftAssertions soft = new JUnitSoftAssertions();

    @Before
    public void setUp() {
        pipelineImportService = new PipelineImportService(
                new PipelineImportBeanValidator(
                        javax.validation.Validation.buildDefaultValidatorFactory().getValidator()),
                pipelineService);
        when(pipelineService.savePipelines(any()))
                .then(invocation -> invocation.getArgument(0));
    }

    @Test
    public void importProductPipelines_PipelineExportHasNoName_ReturnsError() {
        PipelineExport pipelineExport = ExternalClient.Populated.pipelineExport().name(null).build();
        ProductConfig productConfig = ProductPopulated.productConfig().build();

        Validation<List<ErrorResponse>, List<Pipeline>> result = pipelineImportService.importProductPipelines(
                singletonList(pipelineExport), 1L, emptySet());

        VavrAssert.assertCollectionFailure(result)
                .withFailure(ErrorResponse.valueOf("Pipeline name cannot be empty", "PIPELINE_INVALID"));
    }

    @Test
    public void importProductPipelines_PipelineStepExportHasNoName_ReturnsError() {
        PipelineExport pipelineExport = ExternalClient.Populated.pipelineExport()
                .name("Pipeline 1")
                .steps(singletonList(ExternalClient.Populated.pipelineMapStepExport().name(null).build()))
                .build();

        ProductConfig productConfig = ProductPopulated.productConfig().build();

        Validation<List<ErrorResponse>, List<Pipeline>> result = pipelineImportService.importProductPipelines(
                singletonList(pipelineExport), 1L, emptySet());

        VavrAssert.assertCollectionFailure(result)
                .withFailure(ErrorResponse.valueOf(
                        "Pipeline \"Pipeline 1\", Step \"null\" is invalid: step name cannot be empty",
                        "PIPELINE_INVALID"));
    }

    @Test
    public void importProductPipelines_PipelineWithNameAlreadyExists_ReturnsError() {
        PipelineExport pipelineExport = ExternalClient.Populated.pipelineExport().name("known").build();
        ProductConfig productConfig = ProductPopulated.productConfig().build();

        when(pipelineService.existsByName("known"))
                .thenReturn(true);

        Validation<List<ErrorResponse>, List<Pipeline>> result = pipelineImportService.importProductPipelines(
                singletonList(pipelineExport), 1L, emptySet());

        VavrAssert.assertValid(result)
                .withResult(emptyList());

        verify(pipelineService).savePipelines(emptyList());
    }

    @Test
    public void importProductPipelines_MapStepSchemaInNull_ReturnsError() {
        PipelineExport pipelineExport = ExternalClient.Populated.pipelineExport()
                .name("Pipeline1")
                .steps(singletonList(ExternalClient.Populated.pipelineMapStepExport()
                        .name("Step 1")
                        .schemaIn(null)
                        .build()))
                .build();

        ProductConfig productConfig = ProductPopulated.productConfig().build();

        Validation<List<ErrorResponse>, List<Pipeline>> result = pipelineImportService.importProductPipelines(
                singletonList(pipelineExport), 1L, emptySet());

        VavrAssert.assertCollectionFailure(result)
                .withFailure(ErrorResponse.valueOf(
                        "Pipeline \"Pipeline1\", Step \"Step 1\" is invalid: input schema cannot be null",
                        "PIPELINE_INVALID"));
    }

    @Test
    public void importProductPipelines_MapStepSchemaOutNull_ReturnsError() {
        PipelineExport pipelineExport = ExternalClient.Populated.pipelineExport()
                .name("Pipeline1")
                .steps(singletonList(ExternalClient.Populated.pipelineMapStepExport()
                        .name("Step 1")
                        .schemaOut(null)
                        .build()))
                .build();

        ProductConfig productConfig = ProductPopulated.productConfig().build();

        Validation<List<ErrorResponse>, List<Pipeline>> result = pipelineImportService.importProductPipelines(
                singletonList(pipelineExport), 1L, emptySet());

        VavrAssert.assertCollectionFailure(result)
                .withFailure(ErrorResponse.valueOf(
                        "Pipeline \"Pipeline1\", Step \"Step 1\" is invalid: output schema cannot be null",
                        "PIPELINE_INVALID"));
    }

    @Test
    public void importProductPipelines_MapStepNoSelectsDefined_ReturnsError() {
        PipelineExport pipelineExport = ExternalClient.Populated.pipelineExport()
                .name("Pipeline1")
                .steps(singletonList(ExternalClient.Populated.pipelineMapStepExport()
                        .name("Step 1")
                        .selects(null)
                        .build()))
                .build();

        ProductConfig productConfig = ProductPopulated.productConfig().build();

        Validation<List<ErrorResponse>, List<Pipeline>> result = pipelineImportService.importProductPipelines(
                singletonList(pipelineExport), 1L, emptySet());

        VavrAssert.assertCollectionFailure(result)
                .withFailure(ErrorResponse.valueOf(
                        "Pipeline \"Pipeline1\", Step \"Step 1\" is invalid: transformation is not defined",
                        "PIPELINE_INVALID"));
    }

    @Test
    public void importProductPipelines_MapStepSchemaInDoNotExistInProduct_ReturnsError() {
        PipelineExport pipelineExport = ExternalClient.Populated.pipelineExport()
                .steps(singletonList(ExternalClient.Populated.pipelineMapStepExport()
                        .schemaIn(schemaReference()
                                .displayName("schema 1")
                                .physicalTableName("sch_1")
                                .version(1)
                                .build())
                        .schemaOut(schemaReference()
                                .displayName("schema 2")
                                .physicalTableName("sch_2")
                                .version(1)
                                .build())
                        .build()))
                .build();

        Set<Table> tables = singleton(
                ProductPopulated.table()
                        .displayName("schema 2")
                        .physicalTableName("sch_2")
                        .version(1)
                        .build());

        ProductConfig productConfig = ProductPopulated.productConfig()
                .id(1L)
                .tables(newHashSet(tables))
                .build();

        Validation<List<ErrorResponse>, List<Pipeline>> result = pipelineImportService.importProductPipelines(
                singletonList(pipelineExport), 1L, tables);

        VavrAssert.assertCollectionFailure(result)
                .withFailure(CRUDFailure.cannotFind("schema")
                        .with("displayName", "schema 1")
                        .with("physicalTableName", "sch_1")
                        .with("version", 1)
                        .asFailure()
                        .toErrorResponse());
    }

    @Test
    public void importProductPipelines_MapStepSchemasOutNotExistInProduct_ReturnsError() {
        PipelineExport pipelineExport = ExternalClient.Populated.pipelineExport()
                .steps(singletonList(ExternalClient.Populated.pipelineMapStepExport()
                        .schemaIn(schemaReference()
                                .displayName("schema 1")
                                .physicalTableName("sch_1")
                                .version(1)
                                .build())
                        .schemaOut(schemaReference()
                                .displayName("schema 2")
                                .physicalTableName("sch_2")
                                .version(1)
                                .build())
                        .build()))
                .build();

        Set<Table> tables = singleton(
                ProductPopulated.table()
                        .displayName("schema 1")
                        .physicalTableName("sch_1")
                        .version(1)
                        .build());

        Validation<List<ErrorResponse>, List<Pipeline>> result = pipelineImportService.importProductPipelines(
                singletonList(pipelineExport), 1L, tables);

        VavrAssert.assertCollectionFailure(result)
                .withFailure(CRUDFailure.cannotFind("schema")
                        .with("displayName", "schema 2")
                        .with("physicalTableName", "sch_2")
                        .with("version", 1)
                        .asFailure()
                        .toErrorResponse());
    }

    @Test
    public void importProductPipelines_WindowStepSchemasAllExistInProduct_ReturnsPipeline() {
        PipelineExport pipelineExport = ExternalClient.Populated.pipelineExport()
                .name("pipeline 1")
                .steps(asList(
                        ExternalClient.Populated.pipelineWindowStepExport()
                                .name("step 1")
                                .description("window")
                                .filters(singletonList("A = 1"))
                                .selects(asList(
                                        ExternalClient.Populated.selectExport()
                                                .select("A").outputFieldName("A")
                                                .order(0L)
                                                .isWindow(false).build(),
                                        ExternalClient.Populated.selectExport().select("B")
                                                .outputFieldName("B")
                                                .order(1L)
                                                .isWindow(false).build(),
                                        ExternalClient.Populated.selectExport()
                                                .select("some_window_function()")
                                                .outputFieldName("C")
                                                .order(2L)
                                                .isWindow(true)
                                                .window(WindowExport.builder()
                                                        .partitionBy(singleton("A"))
                                                        .orderBy(singletonList(OrderExport.builder()
                                                                .fieldName("B")
                                                                .direction("ASC")
                                                                .priority(0)
                                                                .build()))
                                                        .build())
                                                .build()))
                                .schemaIn(schemaReference()
                                        .displayName("schema 1")
                                        .physicalTableName("sch_1")
                                        .version(1)
                                        .build())
                                .schemaOut(schemaReference()
                                        .displayName("schema 2")
                                        .physicalTableName("sch_2")
                                        .version(1)
                                        .build())
                                .build(),
                        ExternalClient.Populated.pipelineAggregationStepExport()
                                .name("step 2")
                                .description("aggregating")
                                .selects(asList(
                                        ExternalClient.Populated.selectExport()
                                                .select("D").outputFieldName("A").isWindow(false).build(),
                                        ExternalClient.Populated.selectExport()
                                                .select("SUM(A)").outputFieldName("B").isWindow(false).build(),
                                        ExternalClient.Populated.selectExport()
                                                .select("SUM(B)").outputFieldName("C").isWindow(false).build()))
                                .filters(singletonList("D='something to filter'"))
                                .groupings(asList("D", "C"))
                                .schemaIn(schemaReference()
                                        .displayName("schema 1")
                                        .physicalTableName("sch_1")
                                        .version(1)
                                        .build())
                                .schemaOut(schemaReference()
                                        .displayName("schema 2")
                                        .physicalTableName("sch_2")
                                        .version(1)
                                        .build())
                                .build()))
                .build();

        Set<Table> tables = newHashSet(
                ProductPopulated.table()
                        .id(102L)
                        .displayName("schema 1")
                        .physicalTableName("sch_1")
                        .version(1)
                        .build(),
                ProductPopulated.table()
                        .id(103L)
                        .displayName("schema 2")
                        .physicalTableName("sch_2")
                        .version(1)
                        .fields(newHashSet(
                                ProductPopulated.stringField("A").id(78324L).build(),
                                ProductPopulated.longField("B").id(4854385L).build(),
                                ProductPopulated.longField("C").id(989787L).build()))
                        .build());

        Validation<List<ErrorResponse>, List<Pipeline>> result = pipelineImportService.importProductPipelines(
                singletonList(pipelineExport), 1L, tables);

        List<Pipeline> pipelines = VavrAssert.assertValid(result).getResult();

        assertThat(pipelines).hasSize(1);

        Pipeline pipeline = pipelines.get(0);
        soft.assertThat(pipeline.getName())
                .isEqualTo("pipeline 1");

        assertThat(pipeline.getSteps())
                .hasSize(2);

        Iterator<PipelineStep> stepsIterator = pipeline.getSteps().iterator();

        PipelineWindowStep createdStep1 = (PipelineWindowStep) stepsIterator.next();
        soft.assertThat(createdStep1.getName())
                .isEqualTo("step 1");
        soft.assertThat(createdStep1.getDescription())
                .isEqualTo("window");
        soft.assertThat(createdStep1.getFilters())
                .containsExactly("A = 1");
        soft.assertThat(createdStep1.getSelects())
                .containsExactly(
                        ProductPopulated.select().select("A").order(0L).outputFieldId(78324L).build(),
                        ProductPopulated.select().select("B").order(1L).outputFieldId(4854385L).build(),
                        ProductPopulated.select()
                                .select("some_window_function()")
                                .outputFieldId(989787L)
                                .order(2L)
                                .isWindow(true)
                                .window(Window.builder()
                                        .partitions(singleton("A"))
                                        .orders(singleton(Order.builder()
                                                .fieldName("B").direction(Order.Direction.ASC).priority(0).build()))
                                        .build())
                                .build());
        soft.assertThat(createdStep1.getSchemaInId())
                .isEqualTo(102L);
        soft.assertThat(createdStep1.getSchemaOutId())
                .isEqualTo(103L);

        PipelineAggregationStep createdStep2 = (PipelineAggregationStep) stepsIterator.next();
        soft.assertThat(createdStep2.getName())
                .isEqualTo("step 2");
        soft.assertThat(createdStep2.getDescription())
                .isEqualTo("aggregating");
        soft.assertThat(createdStep2.getSelects())
                .containsExactly(
                        ProductPopulated.select().select("D").order(null).outputFieldId(78324L).build(),
                        ProductPopulated.select().select("SUM(A)").order(null).outputFieldId(4854385L).build(),
                        ProductPopulated.select().select("SUM(B)").order(null).outputFieldId(989787L).build());
        soft.assertThat(createdStep2.getFilters())
                .containsExactly("D='something to filter'");
        soft.assertThat(createdStep2.getGroupings())
                .containsExactly("D", "C");
        soft.assertThat(createdStep2.getSchemaInId())
                .isEqualTo(102L);
        soft.assertThat(createdStep2.getSchemaOutId())
                .isEqualTo(103L);
    }

    @Test
    public void importProductPipelines_AggregationStepGroupingsAreLowercase_MatchesFieldsInSchema() {
        PipelineExport pipelineExport = ExternalClient.Populated.pipelineExport()
                .name("pipeline 1")
                .steps(singletonList(
                        ExternalClient.Populated.pipelineAggregationStepExport()
                                .name("step 2")
                                .description("aggregating")
                                .selects(singletonList(
                                        ExternalClient.Populated.selectExport()
                                                .select("d").outputFieldName("A").isWindow(false).build()))
                                .groupings(singletonList("d"))
                                .schemaIn(schemaReference()
                                        .displayName("schema 1")
                                        .physicalTableName("sch_1")
                                        .version(1)
                                        .build())
                                .schemaOut(schemaReference()
                                        .displayName("schema 2")
                                        .physicalTableName("sch_2")
                                        .version(1)
                                        .build())
                                .build()))
                .build();

        Set<Table> tables = newHashSet(
                ProductPopulated.table()
                        .id(102L)
                        .displayName("schema 1")
                        .physicalTableName("sch_1")
                        .version(1)
                        .build(),
                ProductPopulated.table()
                        .id(103L)
                        .displayName("schema 2")
                        .physicalTableName("sch_2")
                        .version(1)
                        .fields(newHashSet(
                                ProductPopulated.stringField("A").id(78324L).build(),
                                ProductPopulated.longField("B").id(4854385L).build(),
                                ProductPopulated.longField("C").id(989787L).build()))
                        .build());

        Pipeline pipeline = VavrAssert.assertValid(pipelineImportService.importProductPipelines(
                singletonList(pipelineExport), 1L, tables))
                .getResult()
                .get(0);

        PipelineAggregationStep aggregationStep = (PipelineAggregationStep) pipeline.getSteps().iterator().next();
        assertThat(aggregationStep.getGroupings())
                .containsOnly("d");
    }

    @Test
    public void importProductPipelines_MapStepSelectOutputFieldDoesNotExistInOutputSchema_ReturnsError() {
        PipelineExport pipelineExport = ExternalClient.Populated.pipelineExport()
                .steps(singletonList(
                        ExternalClient.Populated.pipelineMapStepExport()
                                .name("map")
                                .description("a map step")
                                .selects(asList(
                                        ExternalClient.Populated.selectExport()
                                                .select("A").outputFieldName("X").build(),
                                        ExternalClient.Populated.selectExport()
                                                .select("B").outputFieldName("Y").build(),
                                        ExternalClient.Populated.selectExport()
                                                .select("C").outputFieldName("this_does_not_exist").build()))
                                .schemaIn(schemaReference()
                                        .displayName("schema 1")
                                        .physicalTableName("sch_1")
                                        .version(1)
                                        .build())
                                .schemaOut(schemaReference()
                                        .displayName("schema 2")
                                        .physicalTableName("sch_2")
                                        .version(1)
                                        .build())
                                .build()))
                .build();

        Set<Table> tables = newHashSet(
                ProductPopulated.table()
                        .id(102L)
                        .displayName("schema 1")
                        .physicalTableName("sch_1")
                        .version(1)
                        .build(),
                ProductPopulated.table()
                        .id(103L)
                        .displayName("schema 2")
                        .physicalTableName("sch_2")
                        .version(1)
                        .fields(newHashSet(
                                ProductPopulated.stringField("X").id(78324L).build(),
                                ProductPopulated.longField("Y").id(4854385L).build(),
                                ProductPopulated.longField("Z").id(989787L).build()))
                        .build());

        Validation<List<ErrorResponse>, List<Pipeline>> result = pipelineImportService.importProductPipelines(
                singletonList(pipelineExport), 1L, tables);

        ErrorResponse expectedError =
                CRUDFailure.cannotFind("field").with("schema", "schema 2").with("name", "this_does_not_exist")
                        .asFailure().toErrorResponse();

        VavrAssert.assertFailed(result)
                .withFailure(singletonList(expectedError));
    }

    @Test
    public void importProductPipelines_UnionStep_CreatesSelectsWithUnionSchemaId() {
        PipelineExport pipelineExport = ExternalClient.Populated.pipelineExport()
                .steps(singletonList(
                        ExternalClient.Populated.pipelineUnionStepExport()
                                .name("map")
                                .description("a map step")
                                .unions(Arrays.asList(
                                        unionExport()
                                                .selects(singletonList(selectExport()
                                                        .select("CONCAT(A, ' Mouse'")
                                                        .outputFieldName("OUT_FIELD")
                                                        .build()))
                                                .unionInSchema(schemaReference().displayName("A")
                                                        .physicalTableName("A")
                                                        .build()).build(),
                                        unionExport()
                                                .selects(singletonList(selectExport()
                                                        .select("B")
                                                        .outputFieldName("OUT_FIELD")
                                                        .build()))
                                                .unionInSchema(schemaReference().displayName("B")
                                                        .physicalTableName("B")
                                                        .build()).build()))
                                .schemaOut(schemaReference()
                                        .displayName("Out Schema")
                                        .physicalTableName("OUT")
                                        .version(1)
                                        .build())
                                .build()))
                .build();

        Set<Table> tables = newHashSet(
                ProductPopulated.table()
                        .id(101L)
                        .displayName("B")
                        .physicalTableName("B")
                        .version(1)
                        .build(),
                ProductPopulated.table()
                        .id(102L)
                        .displayName("A")
                        .physicalTableName("A")
                        .version(1)
                        .build(),
                ProductPopulated.table()
                        .id(103L)
                        .displayName("Out Schema")
                        .physicalTableName("OUT")
                        .version(1)
                        .fields(newHashSet(ProductPopulated.stringField("OUT_FIELD").id(78324L).build()))
                        .build());

        Pipeline pipeline = VavrAssert.assertValid(pipelineImportService.importProductPipelines(
                singletonList(pipelineExport), 1L, tables))
                .getResult()
                .get(0);

        PipelineStep convertedStep = pipeline.getSteps().iterator().next();
        assertThat(convertedStep).isInstanceOf(PipelineUnionStep.class);

        PipelineUnionStep unionStep = (PipelineUnionStep) convertedStep;
        assertThat(unionStep.getSelects())
                .extracting(
                        Select::getSelect,
                        select -> select.getSelectUnion().getUnionSchemaId(),
                        Select::getOutputFieldId)
                .containsExactlyInAnyOrder(
                        tuple("CONCAT(A, ' Mouse'", 102L, 78324L),
                        tuple("B", 101L, 78324L));
    }

    @Test
    public void importProductPipelines_UnionStep_CreatesFiltersWithUnionSchemaId() {
        PipelineExport pipelineExport = ExternalClient.Populated.pipelineExport()
                .steps(singletonList(
                        ExternalClient.Populated.pipelineUnionStepExport()
                                .name("map")
                                .description("a map step")
                                .unions(Arrays.asList(
                                        unionExport()
                                                .selects(singletonList(selectExport()
                                                        .select("CONCAT(A, ' Mouse'")
                                                        .outputFieldName("OUT_FIELD")
                                                        .build()))
                                                .filters(Arrays.asList("A > 2", "A < 5"))
                                                .unionInSchema(schemaReference().displayName("A")
                                                        .physicalTableName("A")
                                                        .build()).build(),
                                        unionExport().selects(
                                                singletonList(selectExport().select("B").outputFieldName("OUT_FIELD")
                                                        .build()))
                                                .unionInSchema(schemaReference().displayName("B")
                                                        .physicalTableName("B")
                                                        .build()).build()))
                                .schemaOut(schemaReference()
                                        .displayName("Out Schema")
                                        .physicalTableName("OUT")
                                        .version(1)
                                        .build())
                                .build()))
                .build();

        Set<Table> tables = newHashSet(
                ProductPopulated.table()
                        .id(101L)
                        .displayName("B")
                        .physicalTableName("B")
                        .version(1)
                        .build(),
                ProductPopulated.table()
                        .id(102L)
                        .displayName("A")
                        .physicalTableName("A")
                        .version(1)
                        .build(),
                ProductPopulated.table()
                        .id(103L)
                        .displayName("Out Schema")
                        .physicalTableName("OUT")
                        .version(1)
                        .fields(newHashSet(ProductPopulated.stringField("OUT_FIELD").id(78324L).build()))
                        .build());

        Pipeline pipeline = VavrAssert.assertValid(pipelineImportService.importProductPipelines(
                singletonList(pipelineExport), 1L, tables))
                .getResult()
                .get(0);

        PipelineStep convertedStep = pipeline.getSteps().iterator().next();
        assertThat(convertedStep).isInstanceOf(PipelineUnionStep.class);

        PipelineUnionStep unionStep = (PipelineUnionStep) convertedStep;
        assertThat(unionStep.getPipelineFilters())
                .extracting(
                        PipelineFilter::getFilter,
                        PipelineFilter::getUnionSchemaId)
                .containsExactlyInAnyOrder(
                        tuple("A > 2", 102L),
                        tuple("A < 5", 102L));
    }

    @Test
    public void importProductPipelines_UnionStep_CreatesSelectsWithNameDescriptionAndOutputSchema() {
        PipelineExport pipelineExport = ExternalClient.Populated.pipelineExport()
                .steps(singletonList(
                        ExternalClient.Populated.pipelineUnionStepExport()
                                .name("union")
                                .description("a union step")
                                .unions(Arrays.asList(
                                        unionExport()
                                                .selects(singletonList(selectExport()
                                                        .select("CONCAT(A, ' Mouse'")
                                                        .outputFieldName("OUT_FIELD")
                                                        .build()))
                                                .unionInSchema(schemaReference().displayName("A")
                                                        .physicalTableName("A")
                                                        .build()).build(),
                                        unionExport()
                                                .selects(singletonList(selectExport()
                                                        .select("B")
                                                        .outputFieldName("OUT_FIELD")
                                                        .build()))
                                                .unionInSchema(schemaReference().displayName("B")
                                                        .physicalTableName("B")
                                                        .build()).build()))
                                .schemaOut(schemaReference()
                                        .displayName("Out Schema")
                                        .physicalTableName("OUT")
                                        .version(1)
                                        .build())
                                .build()))
                .build();

        Set<Table> tables = newHashSet(
                ProductPopulated.table()
                        .id(101L)
                        .displayName("B")
                        .physicalTableName("B")
                        .version(1)
                        .build(),
                ProductPopulated.table()
                        .id(102L)
                        .displayName("A")
                        .physicalTableName("A")
                        .version(1)
                        .build(),
                ProductPopulated.table()
                        .id(103L)
                        .displayName("Out Schema")
                        .physicalTableName("OUT")
                        .version(1)
                        .fields(newHashSet(ProductPopulated.stringField("OUT_FIELD").id(78324L).build()))
                        .build());

        Pipeline pipeline = VavrAssert.assertValid(pipelineImportService.importProductPipelines(
                singletonList(pipelineExport), 1L, tables))
                .getResult()
                .get(0);

        PipelineStep convertedStep = pipeline.getSteps().iterator().next();
        assertThat(convertedStep).isInstanceOf(PipelineUnionStep.class);

        PipelineUnionStep unionStep = (PipelineUnionStep) convertedStep;
        soft.assertThat(unionStep.getName())
                .isEqualTo("union");
        soft.assertThat(unionStep.getDescription())
                .isEqualTo("a union step");
        soft.assertThat(unionStep.getSchemaOutId())
                .isEqualTo(103L);
        soft.assertThat(unionStep.getSchemaInIds())
                .containsExactlyInAnyOrder(101L, 102L);
    }

    @Test
    public void importProductPipelines_UnionStepInputSchemaNotFound_ReturnsError() {
        PipelineExport pipelineExport = ExternalClient.Populated.pipelineExport()
                .steps(singletonList(
                        ExternalClient.Populated.pipelineUnionStepExport()
                                .name("map")
                                .description("a map step")
                                .unions(Arrays.asList(
                                        unionExport()
                                                .selects(singletonList(selectExport()
                                                        .select("CONCAT(A, ' Mouse'")
                                                        .outputFieldName("OUT_FIELD")
                                                        .build()))
                                                .unionInSchema(schemaReference().displayName("A")
                                                        .physicalTableName("A")
                                                        .build()).build(),
                                        unionExport()
                                                .selects(singletonList(selectExport()
                                                        .select("B")
                                                        .outputFieldName("OUT_FIELD")
                                                        .build()))
                                                .unionInSchema(schemaReference().displayName("B")
                                                        .physicalTableName("B")
                                                        .build()).build()))
                                .schemaOut(schemaReference()
                                        .displayName("Out Schema")
                                        .physicalTableName("OUT")
                                        .version(1)
                                        .build())
                                .build()))
                .build();

        Set<Table> tables = newHashSet(
                ProductPopulated.table()
                        .id(101L)
                        .displayName("B")
                        .physicalTableName("B")
                        .version(1)
                        .build(),
                ProductPopulated.table()
                        .id(102L)
                        .displayName("This is not A")
                        .physicalTableName("NOT_A")
                        .version(1)
                        .build(),
                ProductPopulated.table()
                        .id(103L)
                        .displayName("Out Schema")
                        .physicalTableName("OUT")
                        .version(1)
                        .fields(newHashSet(ProductPopulated.stringField("OUT_FIELD").id(78324L).build()))
                        .build());

        VavrAssert.assertCollectionFailure(pipelineImportService.importProductPipelines(
                singletonList(pipelineExport), 1L, tables))
                .withFailure(CRUDFailure.cannotFind("schema")
                        .with("displayName", "A")
                        .with("physicalTableName", "A")
                        .with("version", 1)
                        .asFailure().toErrorResponse());
    }

    @Test
    public void importProductPipelines_UnionStepOutputFieldNotFound_ReturnsError() {
        PipelineExport pipelineExport = ExternalClient.Populated.pipelineExport()
                .steps(singletonList(
                        ExternalClient.Populated.pipelineUnionStepExport()
                                .name("map")
                                .description("a map step")
                                .unions(Arrays.asList(
                                        unionExport()
                                                .selects(singletonList(selectExport()
                                                        .select("CONCAT(A, ' Mouse'")
                                                        .outputFieldName("OUT_FIELD")
                                                        .build()))
                                                .unionInSchema(schemaReference().displayName("A")
                                                        .physicalTableName("A")
                                                        .build()).build(),
                                        unionExport()
                                                .selects(singletonList(selectExport()
                                                        .select("B")
                                                        .outputFieldName("OUT_FIELD")
                                                        .build()))
                                                .unionInSchema(schemaReference().displayName("B")
                                                        .physicalTableName("B")
                                                        .build()).build()))
                                .schemaOut(schemaReference()
                                        .displayName("Out Schema")
                                        .physicalTableName("OUT")
                                        .version(1)
                                        .build())
                                .build()))
                .build();

        Set<Table> tables = newHashSet(
                ProductPopulated.table()
                        .id(101L)
                        .displayName("B")
                        .physicalTableName("B")
                        .version(1)
                        .build(),
                ProductPopulated.table()
                        .id(102L)
                        .displayName("A")
                        .physicalTableName("A")
                        .version(1)
                        .build(),
                ProductPopulated.table()
                        .id(103L)
                        .displayName("Out Schema")
                        .physicalTableName("OUT")
                        .version(1)
                        .fields(newHashSet(ProductPopulated.stringField("NOT_OUTPUT_FIELD").build()))
                        .build());

        VavrAssert.assertCollectionFailure(
                pipelineImportService
                        .importProductPipelines(singletonList(pipelineExport), 1L, tables))
                .withFailure(CRUDFailure.cannotFind("field")
                        .with("schema", "Out Schema")
                        .with("name", "OUT_FIELD")
                        .asFailure().toErrorResponse());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void importProductPipelines_ValidJoinStep_CreatesPipelineStepWithJoins() {
        PipelineExport pipelineExport = ExternalClient.Populated.pipelineExport()
                .steps(singletonList(
                        ExternalClient.Populated.pipelineJoinStepExport()
                                .name("join")
                                .description("a join step step")
                                .schemaOut(schemaReference()
                                        .displayName("Table D")
                                        .physicalTableName("TABLE_D")
                                        .version(1)
                                        .build())
                                .selects(asList(
                                        selectExport().select("A.A_X").outputFieldName("X").build(),
                                        selectExport().select("B.B_Y").outputFieldName("Y").build(),
                                        selectExport().select("C.C_Z").outputFieldName("Z").build()))
                                .joins(asList(
                                        JoinExport.builder()
                                                .type(JoinType.LEFT)
                                                .left(schemaReference()
                                                        .physicalTableName("TABLE_A").displayName("Table A").build())
                                                .right(schemaReference()
                                                        .physicalTableName("TABLE_B").displayName("Table B").build())
                                                .joinFields(asList(
                                                        JoinFieldExport.builder().leftColumn("A_X").rightColumn("B_X")
                                                                .build(),
                                                        JoinFieldExport.builder().leftColumn("A_Y").rightColumn("B_Y")
                                                                .build()))
                                                .build(),
                                        JoinExport.builder()
                                                .type(JoinType.INNER)
                                                .left(schemaReference()
                                                        .physicalTableName("TABLE_B").displayName("Table B").build())
                                                .right(schemaReference()
                                                        .physicalTableName("TABLE_C").displayName("Table C").build())
                                                .joinFields(asList(
                                                        JoinFieldExport.builder().leftColumn("B_Y").rightColumn("C_Y")
                                                                .build(),
                                                        JoinFieldExport.builder().leftColumn("B_Z").rightColumn("C_Z")
                                                                .build()))
                                                .build()))
                                .build()))
                .build();

        long tableASchemaId = 101L;
        long axId = 111L;
        long ayId = 112L;
        long tableBSchemaId = 102L;
        long bxId = 211L;
        long byId = 212L;
        long bzId = 213L;
        long tableCSchemaId = 103L;
        long cyId = 312L;
        long czId = 313L;
        long tableDSchemaId = 104L;
        long xId = 991L;
        long yId = 992L;
        long zId = 993L;

        Set<Table> tables = ImmutableSet.of(
                ProductPopulated.table()
                        .id(tableASchemaId)
                        .displayName("Table A")
                        .physicalTableName("TABLE_A")
                        .version(1)
                        .fields(newHashSet(
                                ProductPopulated.stringField("A_X").id(axId).build(),
                                ProductPopulated.longField("A_Y").id(ayId).build()))
                        .build(),
                ProductPopulated.table()
                        .id(tableBSchemaId)
                        .displayName("Table B")
                        .physicalTableName("TABLE_B")
                        .version(1)
                        .fields(newHashSet(
                                ProductPopulated.stringField("B_X").id(bxId).build(),
                                ProductPopulated.longField("B_Y").id(byId).build(),
                                ProductPopulated.longField("B_Z").id(bzId).build()))
                        .build(),
                ProductPopulated.table()
                        .id(tableCSchemaId)
                        .displayName("Table C")
                        .physicalTableName("TABLE_C")
                        .version(1)
                        .fields(newHashSet(
                                ProductPopulated.stringField("C_Y").id(cyId).build(),
                                ProductPopulated.longField("C_Z").id(czId).build()))
                        .build(),
                ProductPopulated.table()
                        .id(tableDSchemaId)
                        .displayName("Table D")
                        .physicalTableName("TABLE_D")
                        .version(1)
                        .fields(newHashSet(
                                ProductPopulated.stringField("X").id(xId).build(),
                                ProductPopulated.longField("Y").id(yId).build(),
                                ProductPopulated.longField("Z").id(zId).build()))
                        .build()
        );

        List<Pipeline> pipelines = VavrAssert
                .assertValid(pipelineImportService.importProductPipelines(singletonList(pipelineExport), 1L, tables))
                .getResult();

        soft.assertThat(pipelines)
                .hasSize(1);

        Pipeline pipeline = pipelines.get(0);

        soft.assertThat(pipeline.getSteps())
                .hasSize(1);

        PipelineStep step = pipeline.getSteps().iterator().next();

        soft.assertThat(step)
                .isInstanceOf(PipelineJoinStep.class);

        PipelineJoinStep joinStep = (PipelineJoinStep) step;

        soft.assertThat(joinStep.getJoins())
                .hasSize(2);

        Iterator<Join> joinIterator = joinStep.getJoins().iterator();
        Join join1 = joinIterator.next();
        Join join2 = joinIterator.next();

        soft.assertThat(join1)
                .extracting(Join::getLeftSchemaId, Join::getRightSchemaId, Join::getJoinType)
                .containsExactly(tableASchemaId, tableBSchemaId, Join.JoinType.LEFT);

        soft.assertThat(join1.getJoinFields())
                .extracting(JoinField::getLeftJoinFieldId, JoinField::getRightJoinFieldId)
                .containsExactlyInAnyOrder(tuple(axId, bxId), tuple(ayId, byId));

        soft.assertThat(join2)
                .extracting(Join::getLeftSchemaId, Join::getRightSchemaId, Join::getJoinType)
                .containsExactly(tableBSchemaId, tableCSchemaId, Join.JoinType.INNER);

        soft.assertThat(join2.getJoinFields())
                .extracting(JoinField::getLeftJoinFieldId, JoinField::getRightJoinFieldId)
                .containsExactlyInAnyOrder(tuple(byId, cyId), tuple(bzId, czId));
    }

    @Test
    public void importProductPipelines_JoinSchemaNotFound_ReturnsError() {
        PipelineExport pipelineExport = ExternalClient.Populated.pipelineExport()
                .steps(singletonList(
                        ExternalClient.Populated.pipelineJoinStepExport()
                                .name("join")
                                .description("a join step step")
                                .schemaOut(schemaReference()
                                        .displayName("Table D")
                                        .physicalTableName("TABLE_D")
                                        .version(1)
                                        .build())
                                .selects(asList(
                                        selectExport().select("A.A_X").outputFieldName("X").build(),
                                        selectExport().select("B.B_Y").outputFieldName("Y").build(),
                                        selectExport().select("C.C_Z").outputFieldName("Z").build()))
                                .joins(asList(
                                        JoinExport.builder()
                                                .type(JoinType.LEFT)
                                                .left(schemaReference()
                                                        .physicalTableName("TABLE_A").displayName("Table A").build())
                                                .right(schemaReference()
                                                        .physicalTableName("TABLE_B").displayName("Table B").build())
                                                .joinFields(asList(
                                                        JoinFieldExport.builder().leftColumn("A_X").rightColumn("B_X")
                                                                .build(),
                                                        JoinFieldExport.builder().leftColumn("A_Y").rightColumn("B_Y")
                                                                .build()))
                                                .build(),
                                        JoinExport.builder()
                                                .type(JoinType.INNER)
                                                .left(schemaReference()
                                                        .physicalTableName("TABLE_B").displayName("Table B").build())
                                                .right(schemaReference()
                                                        .physicalTableName("TABLE_C").displayName("Table C").build())
                                                .joinFields(asList(
                                                        JoinFieldExport.builder().leftColumn("B_Y").rightColumn("C_Y")
                                                                .build(),
                                                        JoinFieldExport.builder().leftColumn("B_Z").rightColumn("C_Z")
                                                                .build()))
                                                .build()))
                                .build()))
                .build();

        long tableASchemaId = 101L;
        long axId = 111L;
        long ayId = 112L;
        long tableBSchemaId = 102L;
        long bxId = 211L;
        long byId = 212L;
        long bzId = 213L;
        long tableDSchemaId = 104L;
        long xId = 991L;
        long yId = 992L;
        long zId = 993L;

        Set<Table> tables = ImmutableSet.of(
                ProductPopulated.table()
                        .id(tableASchemaId)
                        .displayName("Table A")
                        .physicalTableName("TABLE_A")
                        .version(1)
                        .fields(newHashSet(
                                ProductPopulated.stringField("A_X").id(axId).build(),
                                ProductPopulated.longField("A_Y").id(ayId).build()))
                        .build(),
                ProductPopulated.table()
                        .id(tableBSchemaId)
                        .displayName("Table B")
                        .physicalTableName("TABLE_B")
                        .version(1)
                        .fields(newHashSet(
                                ProductPopulated.stringField("B_X").id(bxId).build(),
                                ProductPopulated.longField("B_Y").id(byId).build(),
                                ProductPopulated.longField("B_Z").id(bzId).build()))
                        .build(),
                ProductPopulated.table()
                        .id(tableDSchemaId)
                        .displayName("Table D")
                        .physicalTableName("TABLE_D")
                        .version(1)
                        .fields(newHashSet(
                                ProductPopulated.stringField("X").id(xId).build(),
                                ProductPopulated.longField("Y").id(yId).build(),
                                ProductPopulated.longField("Z").id(zId).build()))
                        .build()
        );

        List<ErrorResponse> errors = VavrAssert
                .assertFailed(pipelineImportService.importProductPipelines(singletonList(pipelineExport), 1L, tables))
                .getValidation();

        soft.assertThat(errors)
                .extracting(ErrorResponse::getErrorCode)
                .containsOnly("NOT_FOUND");

        soft.assertThat(errors)
                .extracting(ErrorResponse::getErrorMessage)
                .containsOnly(
                        "Cannot find schema with displayName 'Table C', with physicalTableName 'TABLE_C', with version '1'");
    }

    @Test
    public void importProductPipelines_JoinFieldNotFound_ReturnsError() {
        PipelineExport pipelineExport = ExternalClient.Populated.pipelineExport()
                .steps(singletonList(
                        ExternalClient.Populated.pipelineJoinStepExport()
                                .name("join")
                                .description("a join step step")
                                .schemaOut(schemaReference()
                                        .displayName("Table D")
                                        .physicalTableName("TABLE_D")
                                        .version(1)
                                        .build())
                                .selects(asList(
                                        selectExport().select("A.A_X").outputFieldName("X").build(),
                                        selectExport().select("B.B_Y").outputFieldName("Y").build(),
                                        selectExport().select("C.C_Z").outputFieldName("Z").build()))
                                .joins(asList(
                                        JoinExport.builder()
                                                .type(JoinType.LEFT)
                                                .left(schemaReference()
                                                        .physicalTableName("TABLE_A").displayName("Table A").build())
                                                .right(schemaReference()
                                                        .physicalTableName("TABLE_B").displayName("Table B").build())
                                                .joinFields(asList(
                                                        JoinFieldExport.builder().leftColumn("A_X").rightColumn("B_X")
                                                                .build(),
                                                        JoinFieldExport.builder().leftColumn("A_Y").rightColumn("B_Y")
                                                                .build()))
                                                .build(),
                                        JoinExport.builder()
                                                .type(JoinType.INNER)
                                                .left(schemaReference()
                                                        .physicalTableName("TABLE_B").displayName("Table B").build())
                                                .right(schemaReference()
                                                        .physicalTableName("TABLE_C").displayName("Table C").build())
                                                .joinFields(asList(
                                                        JoinFieldExport.builder().leftColumn("B_Y").rightColumn("C_Y")
                                                                .build(),
                                                        JoinFieldExport.builder().leftColumn("B_Z").rightColumn("C_Z")
                                                                .build()))
                                                .build()))
                                .build()))
                .build();

        long tableASchemaId = 101L;
        long axId = 111L;
        long ayId = 112L;
        long tableBSchemaId = 102L;
        long bxId = 211L;
        long byId = 212L;
        long bzId = 213L;
        long tableCSchemaId = 103L;
        long czId = 313L;
        long tableDSchemaId = 104L;
        long xId = 991L;
        long yId = 992L;
        long zId = 993L;

        Set<Table> tables = ImmutableSet.of(
                ProductPopulated.table()
                        .id(tableASchemaId)
                        .displayName("Table A")
                        .physicalTableName("TABLE_A")
                        .version(1)
                        .fields(newHashSet(
                                ProductPopulated.stringField("A_X").id(axId).build(),
                                ProductPopulated.longField("A_Y").id(ayId).build()))
                        .build(),
                ProductPopulated.table()
                        .id(tableBSchemaId)
                        .displayName("Table B")
                        .physicalTableName("TABLE_B")
                        .version(1)
                        .fields(newHashSet(
                                ProductPopulated.stringField("B_X").id(bxId).build(),
                                ProductPopulated.longField("B_Y").id(byId).build(),
                                ProductPopulated.longField("B_Z").id(bzId).build()))
                        .build(),
                ProductPopulated.table()
                        .id(tableCSchemaId)
                        .displayName("Table C")
                        .physicalTableName("TABLE_C")
                        .version(1)
                        .fields(newHashSet(ProductPopulated.longField("C_Z").id(czId).build()))
                        .build(),
                ProductPopulated.table()
                        .id(tableDSchemaId)
                        .displayName("Table D")
                        .physicalTableName("TABLE_D")
                        .version(1)
                        .fields(newHashSet(
                                ProductPopulated.stringField("X").id(xId).build(),
                                ProductPopulated.longField("Y").id(yId).build(),
                                ProductPopulated.longField("Z").id(zId).build()))
                        .build()
        );

        List<ErrorResponse> errors = VavrAssert
                .assertFailed(pipelineImportService.importProductPipelines(singletonList(pipelineExport), 1L, tables))
                .getValidation();

        soft.assertThat(errors)
                .extracting(ErrorResponse::getErrorCode)
                .containsOnly("NOT_FOUND");

        soft.assertThat(errors)
                .extracting(ErrorResponse::getErrorMessage)
                .containsOnly("Cannot find field with schema 'Table C', with name 'C_Y'");
    }

    @Test
    public void importProductPipelines_ScriptletStepWithInvalidInputSchema_ReturnsError() {
        PipelineExport pipelineExport = ExternalClient.Populated.pipelineExport()
                .steps(singletonList(
                        ExternalClient.Populated.pipelineScriptletStepExport()
                                .schemasIn(asList(
                                        ScriptletInputExport.builder()
                                                .inputName("InputA")
                                                .inputSchema(schemaReference()
                                                        .physicalTableName("TABLE_A")
                                                        .displayName("Table A")
                                                        .build())
                                                .build(),
                                        ScriptletInputExport.builder()
                                                .inputName("InputB")
                                                .inputSchema(schemaReference()
                                                        .physicalTableName("TABLE_B")
                                                        .displayName("Table B")
                                                        .build())
                                                .build()))
                                .schemaOut(schemaReference()
                                        .physicalTableName("TABLE_C")
                                        .displayName("Table C")
                                        .build())
                                .build()))
                .build();

        Set<Table> tables = ImmutableSet.of(
                ProductPopulated.table()
                        .id(1001L)
                        .displayName("Table A")
                        .physicalTableName("TABLE_A")
                        .version(1)
                        .build(),
                ProductPopulated.table()
                        .id(1003L)
                        .displayName("Table C")
                        .physicalTableName("TABLE_C")
                        .version(1)
                        .build());

        List<ErrorResponse> errors = VavrAssert
                .assertFailed(pipelineImportService.importProductPipelines(singletonList(pipelineExport), 1L, tables))
                .getValidation();

        soft.assertThat(errors)
                .extracting(ErrorResponse::getErrorCode)
                .containsOnly("NOT_FOUND");

        soft.assertThat(errors)
                .extracting(ErrorResponse::getErrorMessage)
                .containsOnly("Cannot find schema with displayName 'Table B', with physicalTableName 'TABLE_B', with version '1'");
    }

    @Test
    public void importProductPipelines_ScriptletStepWithInvalidOutputSchema_ReturnsError() {
        PipelineExport pipelineExport = ExternalClient.Populated.pipelineExport()
                .steps(singletonList(
                        ExternalClient.Populated.pipelineScriptletStepExport()
                                .schemasIn(asList(
                                        ScriptletInputExport.builder()
                                                .inputName("InputA")
                                                .inputSchema(schemaReference()
                                                        .physicalTableName("TABLE_A")
                                                        .displayName("Table A")
                                                        .build())
                                                .build(),
                                        ScriptletInputExport.builder()
                                                .inputName("InputB")
                                                .inputSchema(schemaReference()
                                                        .physicalTableName("TABLE_B")
                                                        .displayName("Table B")
                                                        .build())
                                                .build()))
                                .schemaOut(schemaReference()
                                        .physicalTableName("TABLE_C")
                                        .displayName("Table C")
                                        .build())
                                .build()))
                .build();

        Set<Table> tables = ImmutableSet.of(
                ProductPopulated.table()
                        .id(1001L)
                        .displayName("Table A")
                        .physicalTableName("TABLE_A")
                        .version(1)
                        .build(),
                ProductPopulated.table()
                        .id(1002L)
                        .displayName("Table B")
                        .physicalTableName("TABLE_B")
                        .version(1)
                        .build());

        List<ErrorResponse> errors = VavrAssert
                .assertFailed(pipelineImportService.importProductPipelines(singletonList(pipelineExport), 1L, tables))
                .getValidation();

        soft.assertThat(errors)
                .extracting(ErrorResponse::getErrorCode)
                .containsOnly("NOT_FOUND");

        soft.assertThat(errors)
                .extracting(ErrorResponse::getErrorMessage)
                .containsOnly("Cannot find schema with displayName 'Table C', with physicalTableName 'TABLE_C', with version '1'");
    }

    @Test
    public void importProductPipelines_ScriptletStep_CreatesPipelineWithScriptletStep() {
        PipelineExport pipelineExport = ExternalClient.Populated.pipelineExport()
                .name("pipeline with scriptlet step")
                .steps(singletonList(
                        ExternalClient.Populated.pipelineScriptletStepExport()
                                .name("scriptlet step")
                                .description("(A, B) => C")
                                .jarFile("my-custom.jar")
                                .className("com.MyCustomStep")
                                .schemasIn(asList(
                                        ScriptletInputExport.builder()
                                                .inputName("InputA")
                                                .inputSchema(schemaReference()
                                                        .physicalTableName("TABLE_A")
                                                        .displayName("Table A")
                                                        .build())
                                                .build(),
                                        ScriptletInputExport.builder()
                                                .inputName("InputB")
                                                .inputSchema(schemaReference()
                                                        .physicalTableName("TABLE_B")
                                                        .displayName("Table B")
                                                        .build())
                                                .build()))
                                .schemaOut(schemaReference()
                                        .physicalTableName("TABLE_C")
                                        .displayName("Table C")
                                        .build())
                                .build()))
                .build();

        Set<Table> tables = ImmutableSet.of(
                ProductPopulated.table()
                        .id(1001L)
                        .displayName("Table A")
                        .physicalTableName("TABLE_A")
                        .version(1)
                        .build(),
                ProductPopulated.table()
                        .id(1002L)
                        .displayName("Table B")
                        .physicalTableName("TABLE_B")
                        .version(1)
                        .build(),
                ProductPopulated.table()
                        .id(1003L)
                        .displayName("Table C")
                        .physicalTableName("TABLE_C")
                        .version(1)
                        .build());

        List<Pipeline> pipelines = VavrAssert
                .assertValid(pipelineImportService.importProductPipelines(singletonList(pipelineExport), 1L, tables))
                .getResult();

        assertThat(pipelines).hasSize(1);

        Pipeline pipeline = pipelines.get(0);
        soft.assertThat(pipeline.getName())
                .isEqualTo("pipeline with scriptlet step");

        assertThat(pipeline.getSteps())
                .hasSize(1);

        Iterator<PipelineStep> stepsIterator = pipeline.getSteps().iterator();

        PipelineScriptletStep createdStep = (PipelineScriptletStep) stepsIterator.next();

        soft.assertThat(createdStep.getName())
                .isEqualTo("scriptlet step");
        soft.assertThat(createdStep.getDescription())
                .isEqualTo("(A, B) => C");
        soft.assertThat(createdStep.getJarFile())
                .isEqualTo("my-custom.jar");
        soft.assertThat(createdStep.getClassName())
                .isEqualTo("com.MyCustomStep");
        soft.assertThat(createdStep.getSchemaIns())
                .containsOnly(
                        ScriptletInput.builder()
                                .inputName("InputA")
                                .schemaInId(1001L)
                                .build(),
                        ScriptletInput.builder()
                                .inputName("InputB")
                                .schemaInId(1002L)
                                .build());
        soft.assertThat(createdStep.getSchemaOutId())
                .isEqualTo(1003L);
        soft.assertThat(createdStep.getSelects())
                .isNull();
        soft.assertThat(createdStep.getPipelineFilters())
                .isNull();
    }
}
