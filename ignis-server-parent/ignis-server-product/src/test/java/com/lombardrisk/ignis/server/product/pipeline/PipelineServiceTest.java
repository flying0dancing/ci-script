package com.lombardrisk.ignis.server.product.pipeline;

import com.google.common.collect.ImmutableList;
import com.lombardrisk.ignis.client.external.fixture.ExternalClient;
import com.lombardrisk.ignis.client.external.pipeline.export.TransformationType;
import com.lombardrisk.ignis.client.external.pipeline.view.DownstreamPipelineView;
import com.lombardrisk.ignis.client.external.pipeline.view.PipelineEdgeView;
import com.lombardrisk.ignis.client.external.pipeline.view.PipelineStepView;
import com.lombardrisk.ignis.client.external.pipeline.view.PipelineView;
import com.lombardrisk.ignis.client.external.pipeline.view.SchemaDetailsView;
import com.lombardrisk.ignis.common.assertions.VavrAssert;
import com.lombardrisk.ignis.data.common.error.ErrorResponse;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.server.product.fixture.ProductPopulated;
import com.lombardrisk.ignis.server.product.pipeline.details.SchemaDetails;
import com.lombardrisk.ignis.server.product.pipeline.model.Pipeline;
import com.lombardrisk.ignis.server.product.pipeline.model.PipelineAggregationStep;
import com.lombardrisk.ignis.server.product.pipeline.model.PipelineJoinStep;
import com.lombardrisk.ignis.server.product.pipeline.model.PipelineMapStep;
import com.lombardrisk.ignis.server.product.pipeline.model.PipelineStep;
import com.lombardrisk.ignis.server.product.pipeline.select.PipelineFilter;
import com.lombardrisk.ignis.server.product.pipeline.select.Select;
import com.lombardrisk.ignis.server.product.pipeline.transformation.JoinField;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Sets.newLinkedHashSet;
import static com.lombardrisk.ignis.client.external.pipeline.export.TransformationType.JOIN;
import static com.lombardrisk.ignis.client.external.pipeline.export.TransformationType.MAP;
import static com.lombardrisk.ignis.server.product.fixture.ProductPopulated.schemaDetails;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("ALL")
@RunWith(MockitoJUnitRunner.class)
public class PipelineServiceTest {

    @Mock
    private PipelineJpaRepository pipelineJpaRepository;

    @InjectMocks
    private PipelineService pipelineService;

    @Rule
    public JUnitSoftAssertions soft = new JUnitSoftAssertions();

    @Test
    public void findAll_PipelineWithAllSteps_ReturnsPipelineViewWithAllTypes() {
        when(pipelineJpaRepository.findAll())
                .thenReturn(singletonList(ProductPopulated.pipeline()
                        .id(1234L)
                        .name("test pipeline")
                        .steps(newHashSet(
                                ProductPopulated.mapPipelineStep().id(12L).build(),
                                ProductPopulated.aggregatePipelineStep().id(34L).build(),
                                ProductPopulated.joinPipelineStep().id(56L).build(),
                                ProductPopulated.windowPipelineStep().id(78L).build(),
                                ProductPopulated.unionPipelineStep().id(89L).build(),
                                ProductPopulated.scriptletPipelineStep().id(910L).build()))
                        .build()));

        List<PipelineView> pipelineViews = pipelineService.findAll();

        soft.assertThat(pipelineViews)
                .hasSize(1);

        soft.assertThat(pipelineViews.get(0).getSteps())
                .extracting(PipelineStepView::getId, PipelineStepView::getType)
                .containsExactlyInAnyOrder(
                        tuple(12L, TransformationType.MAP),
                        tuple(34L, TransformationType.AGGREGATION),
                        tuple(56L, TransformationType.JOIN),
                        tuple(78L, TransformationType.WINDOW),
                        tuple(89L, TransformationType.UNION),
                        tuple(910L, TransformationType.SCRIPTLET));
    }

    @Test
    public void findAll_PipelineWithUnionStep_ReturnsUnionStepViewWithNameAndDescription() {
        when(pipelineJpaRepository.findAll())
                .thenReturn(singletonList(ProductPopulated.pipeline()
                        .steps(newHashSet(ProductPopulated.unionPipelineStep()
                                .name("Union 1")
                                .description("Union step that does union stuff")
                                .build()))
                        .build()));

        List<PipelineView> pipelineViews = pipelineService.findAll();
        soft.assertThat(pipelineViews)
                .hasSize(1);

        PipelineStepView pipelineStepView = pipelineViews.get(0).getSteps().get(0);
        soft.assertThat(pipelineStepView.getName())
                .isEqualTo("Union 1");
        soft.assertThat(pipelineStepView.getDescription())
                .isEqualTo("Union step that does union stuff");
    }

    @Test
    public void findAll_PipelineWithUnionStep_ReturnsUnionStepViewWithUnionSchemas() {
        when(pipelineJpaRepository.findAll())
                .thenReturn(singletonList(ProductPopulated.pipeline()
                        .steps(newHashSet(ProductPopulated.unionPipelineStep()
                                .schemasIn(newHashSet(
                                        schemaDetails("A", 1).id(101L).build(),
                                        schemaDetails("B", 1).id(102L).build()))
                                .build()))
                        .build()));

        List<PipelineView> pipelineViews = pipelineService.findAll();
        soft.assertThat(pipelineViews)
                .hasSize(1);

        PipelineStepView pipelineStepView = pipelineViews.get(0).getSteps().get(0);
        soft.assertThat(pipelineStepView.getUnionInputSchemas())
                .containsExactlyInAnyOrder(
                        ExternalClient.Populated.schemaDetailsView("A", 1).id(101L).build(),
                        ExternalClient.Populated.schemaDetailsView("B", 1).id(102L).build());
    }

    @Test
    public void findAll_PipelineWithUnionStep_ReturnsUnionStepViewWithSchemaOut() {
        when(pipelineJpaRepository.findAll())
                .thenReturn(singletonList(ProductPopulated.pipeline()
                        .steps(newHashSet(ProductPopulated.unionPipelineStep()
                                .schemaOut(schemaDetails("A", 1).id(101L).build())
                                .build()))
                        .build()));

        List<PipelineView> pipelineViews = pipelineService.findAll();
        soft.assertThat(pipelineViews)
                .hasSize(1);

        PipelineStepView pipelineStepView = pipelineViews.get(0).getSteps().get(0);
        soft.assertThat(pipelineStepView.getSchemaOut())
                .isEqualTo(
                        ExternalClient.Populated.schemaDetailsView("A", 1).id(101L).build());
    }

    @Test
    public void findAll_PipelineWithScriptletStep_ReturnsScriptletView() {
        when(pipelineJpaRepository.findAll())
                .thenReturn(singletonList(ProductPopulated.pipeline()
                        .steps(newHashSet(ProductPopulated.scriptletPipelineStep()
                                .id(123L)
                                .name("Scriptlet step")
                                .description("Custom scriptlet step")
                                .jarFile("custom-step.jar")
                                .className("com.vermeg.MyCustomStep")
                                .schemaOutId(10L)
                                .schemaOut(schemaDetails()
                                        .id(10L)
                                        .displayName("input schema")
                                        .physicalTableName("MY_INPUT")
                                        .version(33)
                                        .build())
                                .build()))
                        .build()));

        List<PipelineView> pipelineViews = pipelineService.findAll();

        soft.assertThat(pipelineViews)
                .hasSize(1);
        soft.assertThat(pipelineViews.get(0).getSteps())
                .hasSize(1);
        soft.assertThat(pipelineViews.get(0).getSteps().get(0))
                .isEqualToComparingFieldByFieldRecursively(
                        PipelineStepView.builder()
                                .id(123L)
                                .type(TransformationType.SCRIPTLET)
                                .name("Scriptlet step")
                                .description("Custom scriptlet step")
                                .jarFile("custom-step.jar")
                                .className("com.vermeg.MyCustomStep")
                                .schemaOut(SchemaDetailsView.builder()
                                        .id(10L)
                                        .displayName("input schema")
                                        .physicalTableName("MY_INPUT")
                                        .version(33)
                                        .build())
                                .build());
    }

    @Test
    public void findStepById_PipelineNotFound_ReturnsError() {
        when(pipelineJpaRepository.findById(any()))
                .thenReturn(Optional.empty());

        VavrAssert.assertFailed(pipelineService.findStepById(1L, 2L))
                .withFailure(CRUDFailure.notFoundIds("Pipeline", 1L));
    }

    @Test
    public void findStepById_PipelineStepNotFound_ReturnsError() {
        when(pipelineJpaRepository.findById(any()))
                .thenReturn(Optional.of(ProductPopulated.pipeline()
                        .steps(newHashSet(ProductPopulated.mapPipelineStep().id(1200L).build()))
                        .build()));

        VavrAssert.assertFailed(pipelineService.findStepById(1L, 2L))
                .withFailure(CRUDFailure.notFoundIds("PipelineStep", 2L));
    }

    @Test
    public void findStepById_PipelineStepFound_ReturnsStep() {
        PipelineMapStep pipelineMapStep = ProductPopulated.mapPipelineStep().id(2L).build();
        when(pipelineJpaRepository.findById(any()))
                .thenReturn(Optional.of(ProductPopulated.pipeline()
                        .steps(newHashSet(pipelineMapStep))
                        .build()));

        VavrAssert.assertValid(pipelineService.findStepById(1L, 2L))
                .withResult(pipelineMapStep);
    }

    @Test
    public void getRequiredSchemas_PipelineNotFound_ReturnsError() {
        when(pipelineJpaRepository.findById(any()))
                .thenReturn(Optional.empty());

        VavrAssert.assertFailed(pipelineService.getRequiredSchemas(123456L))
                .withFailure(singletonList(CRUDFailure.notFoundIds("Pipeline", 123456L).toErrorResponse()));
    }

    @Test
    public void getRequiredSchemas_PipelinePlanNotValid_ReturnsError() {
        when(pipelineJpaRepository.findById(any()))
                .thenReturn(Optional.of(ProductPopulated.pipeline()
                        .steps(singleton(ProductPopulated.mapPipelineStep()
                                .schemaInId(123L).schemaIn(schemaDetails().id(123L).build())
                                .schemaOutId(123L).schemaOut(schemaDetails().id(123L).build())
                                .build()))
                        .build()));

        List<ErrorResponse> errors =
                VavrAssert.assertFailed(pipelineService.getRequiredSchemas(45325L)).getValidation();

        assertThat(errors)
                .extracting(ErrorResponse::getErrorCode)
                .containsExactly("PIPELINE_STEP_SELF_JOIN");
    }

    @Test
    public void getRequiredSchemas_ValidPipeline_ReturnsSchemaDetails() {
        SchemaDetails schema1 = schemaDetails()
                .id(1L).physicalTableName("SCHEMA_1").displayName("schema one").version(1).build();

        SchemaDetails schema2 = schemaDetails()
                .id(2L).physicalTableName("SCHEMA_2").displayName("schema two").version(2).build();

        SchemaDetails schema3 = schemaDetails()
                .id(3L).physicalTableName("SCHEMA_3").displayName("schema three").version(3).build();

        SchemaDetails schema4 = schemaDetails()
                .id(4L).physicalTableName("SCHEMA_4").displayName("schema four").version(4).build();

        SchemaDetails schema5 = schemaDetails()
                .id(5L).physicalTableName("SCHEMA_5").displayName("schema five").version(5).build();

        SchemaDetails schema6 = schemaDetails()
                .id(6L).physicalTableName("SCHEMA_6").displayName("schema six").version(6).build();

        when(pipelineJpaRepository.findById(any()))
                .thenReturn(Optional.of(ProductPopulated.pipeline()
                        .steps(newHashSet(
                                ProductPopulated.mapPipelineStep()
                                        .schemaInId(1L).schemaIn(schema1)
                                        .schemaOutId(2L).schemaOut(schema2)
                                        .build(),
                                ProductPopulated.aggregatePipelineStep()
                                        .schemaInId(2L).schemaIn(schema2)
                                        .schemaOutId(3L).schemaOut(schema3)
                                        .build(),
                                ProductPopulated.joinPipelineStep()
                                        .joins(newHashSet(
                                                ProductPopulated.join()
                                                        .leftSchemaId(4L).leftSchema(schema4)
                                                        .rightSchemaId(3L).rightSchema(schema3)
                                                        .build(),
                                                ProductPopulated.join()
                                                        .leftSchemaId(3L).leftSchema(schema3)
                                                        .rightSchemaId(5L).rightSchema(schema5)
                                                        .build()))
                                        .schemaOutId(6L).schemaOut(schema6)
                                        .build()))
                        .build()));

        VavrAssert.assertValid(pipelineService.getRequiredSchemas(364656L))
                .withResult(newHashSet(
                        SchemaDetailsView.builder()
                                .id(1L).physicalTableName("SCHEMA_1").displayName("schema one").version(1).build(),
                        SchemaDetailsView.builder()
                                .id(4L).physicalTableName("SCHEMA_4").displayName("schema four").version(4).build(),
                        SchemaDetailsView.builder()
                                .id(5L).physicalTableName("SCHEMA_5").displayName("schema five").version(5).build()
                ));
    }

    @Test
    public void findPipelineEdgeData_PipelineDoesNotExist_ReturnsError() {
        when(pipelineJpaRepository.findById(any()))
                .thenReturn(Optional.empty());

        VavrAssert.assertFailed(pipelineService.findPipelineEdgeData(-1L))
                .withFailure(CRUDFailure.notFoundIds("Pipeline", -1L));
    }

    @Test
    public void findPipelineEdgeData_PipelineHasManyEdges_ReturnsAllEdges() {
        PipelineJoinStep joinStep = ProductPopulated.joinPipelineStep()
                .id(1001L)
                .joins(newHashSet(ProductPopulated.join()
                        .leftSchema(schemaDetails()
                                .id(1L)
                                .physicalTableName("A")
                                .build())
                        .rightSchema(schemaDetails()
                                .id(2L)
                                .physicalTableName("B")
                                .build())
                        .joinFields(newHashSet(JoinField.builder()
                                .leftJoinField(ProductPopulated.longField().build())
                                .rightJoinField(ProductPopulated.longField().build())
                                .build()))
                        .build()))
                .schemaOut(schemaDetails()
                        .id(3L)
                        .physicalTableName("C")
                        .build())
                .build();
        PipelineMapStep mapStep = ProductPopulated.mapPipelineStep()
                .id(1002L)
                .schemaIn(schemaDetails()
                        .id(3L)
                        .physicalTableName("C")
                        .build())
                .schemaOut(schemaDetails()
                        .id(4L)
                        .physicalTableName("D")
                        .build())
                .build();

        when(pipelineJpaRepository.findById(any()))
                .thenReturn(Optional.of(ProductPopulated.pipeline()
                        .steps(newHashSet(joinStep, mapStep))
                        .build()));

        List<PipelineEdgeView> edges = VavrAssert.assertValid(pipelineService.findPipelineEdgeData(1L))
                .getResult();

        soft.assertThat(edges)
                .extracting(step -> tuple(step.getSource(), step.getTarget()))
                .containsOnly(
                        tuple(1L, 3L),
                        tuple(2L, 3L),
                        tuple(3L, 4L));

        soft.assertThat(edges)
                .extracting(PipelineEdgeView::getPipelineStep)
                .extracting(step -> tuple(step.getId(), step.getType()))
                .containsOnly(
                        tuple(1001L, JOIN),
                        tuple(1002L, MAP));
    }

    @Test
    public void findDownstreamPipelines_ReturnsDownstreamPipelineViews() {
        SchemaDetails schemaA = schemaDetails().id(1L).physicalTableName("A").displayName("schema A").build();
        SchemaDetails schemaB = schemaDetails().id(2L).physicalTableName("B").displayName("schema B").build();
        SchemaDetails schemaC = schemaDetails().id(3L).physicalTableName("C").displayName("schema C").build();

        when(pipelineJpaRepository.findAll())
                .thenReturn(asList(
                        ProductPopulated.pipeline().id(123L)
                                .name("Pipeline Number 1")
                                .steps(newHashSet(ProductPopulated.mapPipelineStep()
                                        .schemaInId(schemaA.getId()).schemaIn(schemaA)
                                        .schemaOutId(schemaB.getId()).schemaOut(schemaB)
                                        .build()))
                                .build(),
                        ProductPopulated.pipeline().id(456L)
                                .name("Pipeline Number 2")
                                .steps(newHashSet(ProductPopulated.aggregatePipelineStep()
                                        .schemaInId(schemaB.getId()).schemaIn(schemaB)
                                        .schemaOutId(schemaC.getId()).schemaOut(schemaC)
                                        .build()))
                                .build()));

        Set<DownstreamPipelineView> downStreamPipelines = VavrAssert
                .assertValid(pipelineService.findDownstreamPipelines())
                .getResult();

        soft.assertThat(downStreamPipelines)
                .hasSize(2);

        soft.assertThat(downStreamPipelines)
                .containsExactlyInAnyOrder(
                        DownstreamPipelineView.builder()
                                .pipelineId(123L)
                                .pipelineName("Pipeline Number 1")
                                .requiredSchemas(newHashSet(
                                        SchemaDetailsView.builder()
                                                .id(schemaA.getId())
                                                .displayName(schemaA.getDisplayName())
                                                .physicalTableName(schemaA.getPhysicalTableName())
                                                .version(1)
                                                .build()))
                                .build(),
                        DownstreamPipelineView.builder()
                                .pipelineId(456L)
                                .pipelineName("Pipeline Number 2")
                                .requiredSchemas(newHashSet(
                                        SchemaDetailsView.builder()
                                                .id(schemaB.getId())
                                                .displayName(schemaB.getDisplayName())
                                                .physicalTableName(schemaB.getPhysicalTableName())
                                                .version(1)
                                                .build()))
                                .build());
    }

    @Test
    public void findDownstreamPipelines_InvalidPipeline_DoesNotReturnPipelineView() {
        SchemaDetails schemaA = schemaDetails().id(1L).physicalTableName("A").displayName("schema A").build();
        SchemaDetails schemaB = schemaDetails().id(2L).physicalTableName("B").displayName("schema B").build();

        when(pipelineJpaRepository.findAll())
                .thenReturn(asList(
                        ProductPopulated.pipeline().id(123L)
                                .name("Pipeline Number 1")
                                .steps(newHashSet(ProductPopulated.mapPipelineStep()
                                        .schemaInId(schemaA.getId()).schemaIn(schemaA)
                                        .schemaOutId(schemaB.getId()).schemaOut(schemaB)
                                        .build()))
                                .build(),
                        ProductPopulated.pipeline().id(456L)
                                .name("Pipeline Number 2")
                                .steps(newHashSet(ProductPopulated.aggregatePipelineStep()
                                        .schemaInId(schemaA.getId()).schemaIn(schemaA)
                                        .schemaOutId(schemaA.getId()).schemaOut(schemaA)
                                        .build()))
                                .build()));

        Set<DownstreamPipelineView> downStreamPipelines = VavrAssert
                .assertValid(pipelineService.findDownstreamPipelines())
                .getResult();

        soft.assertThat(downStreamPipelines)
                .hasSize(1);

        soft.assertThat(downStreamPipelines)
                .containsExactly(
                        DownstreamPipelineView.builder()
                                .pipelineId(123L)
                                .pipelineName("Pipeline Number 1")
                                .requiredSchemas(newHashSet(
                                        SchemaDetailsView.builder()
                                                .id(schemaA.getId())
                                                .displayName(schemaA.getDisplayName())
                                                .physicalTableName(schemaA.getPhysicalTableName())
                                                .version(1)
                                                .build()))
                                .build());
    }

    @Test
    public void findByName_PipelineExists_ReturnsPipeline() {
        Pipeline pipeline = ProductPopulated.pipeline().id(123L).name("pipeline").build();

        when(pipelineJpaRepository.findByName(any()))
                .thenReturn(Optional.of(pipeline));

        VavrAssert.assertValid(pipelineService.findByName("pipeline"))
                .withResult(pipeline);
    }

    @Test
    public void findByName_PipelineDoesNotExist_ReturnsError() {
        when(pipelineJpaRepository.findByName(any()))
                .thenReturn(Optional.empty());

        VavrAssert.assertFailed(pipelineService.findByName("invalid pipeline name"))
                .withFailure(CRUDFailure.cannotFind("Pipeline").with("name", "invalid pipeline name").asFailure());
    }

    @Test
    public void savePipeline_PipelineWithSteps_SetsPipelineOnSteps() {
        PipelineStep step1 = ProductPopulated.mapPipelineStep().name("Step 1")
                .pipeline(null).selects(null).filters(null)
                .build();
        PipelineStep step2 = ProductPopulated.aggregatePipelineStep().name("Step 2")
                .pipeline(null).selects(null).filters(null)
                .build();
        PipelineStep step3 = ProductPopulated.unionPipelineStep().name("Step 3")
                .pipeline(null).selects(null).filters(null)
                .build();

        Pipeline pipeline = ProductPopulated.pipeline()
                .name("My New Pipeline")
                .productId(1234567L)
                .steps(newHashSet(step1, step2, step3))
                .build();

        pipelineService.savePipeline(pipeline);

        ArgumentCaptor<Pipeline> pipelineArgumentCaptor = ArgumentCaptor.forClass(Pipeline.class);
        verify(pipelineJpaRepository).save(pipelineArgumentCaptor.capture());

        Pipeline savedPipeline = pipelineArgumentCaptor.getValue();

        soft.assertThat(savedPipeline.getName())
                .isEqualTo("My New Pipeline");

        soft.assertThat(savedPipeline.getProductId())
                .isEqualTo(1234567L);

        soft.assertThat(savedPipeline.getSteps())
                .hasSize(3);

        soft.assertThat(savedPipeline.getSteps())
                .extracting(PipelineStep::getPipeline)
                .containsOnly(savedPipeline);
    }

    @Test
    public void savePipeline_StepsWithSelectsAndFilters_SetsStepOnSelectsAndFilters() {
        PipelineMapStep step1 = ProductPopulated.mapPipelineStep()
                .name("Step 1")
                .selects(newHashSet(
                        ProductPopulated.select().select("INPUT_FIELD").build()))
                .filters(newHashSet("INPUT_FIELD > 0"))
                .build();

        PipelineAggregationStep step2 = ProductPopulated.aggregatePipelineStep()
                .name("Step 2")
                .selects(newHashSet(
                        ProductPopulated.select().select("FIELD_A").build(),
                        ProductPopulated.select().select("FIELD_B").build()))
                .filters(newHashSet("FIELD_A > 0", "FIELD_B < 100"))
                .build();

        Pipeline pipeline = ProductPopulated.pipeline()
                .name("My New Pipeline")
                .productId(1234567L)
                .steps(newLinkedHashSet(asList(step1, step2)))
                .build();

        pipelineService.savePipeline(pipeline);

        ArgumentCaptor<Pipeline> pipelineArgumentCaptor = ArgumentCaptor.forClass(Pipeline.class);
        verify(pipelineJpaRepository).save(pipelineArgumentCaptor.capture());

        Pipeline savedPipeline = pipelineArgumentCaptor.getValue();

        soft.assertThat(savedPipeline.getSteps())
                .hasSize(2);

        Iterator<PipelineStep> stepIterator = savedPipeline.getSteps().iterator();
        PipelineStep savedStep1 = stepIterator.next();
        PipelineStep savedStep2 = stepIterator.next();

        soft.assertThat(savedStep1.getSelects())
                .extracting(Select::getPipelineStep)
                .hasSize(1)
                .containsOnly(savedStep1);

        soft.assertThat(savedStep1.getPipelineFilters())
                .extracting(PipelineFilter::getPipelineStep)
                .hasSize(1)
                .containsOnly(savedStep1);

        soft.assertThat(savedStep2.getSelects())
                .extracting(Select::getPipelineStep)
                .hasSize(2)
                .containsOnly(savedStep2);

        soft.assertThat(savedStep2.getPipelineFilters())
                .extracting(PipelineFilter::getPipelineStep)
                .hasSize(2)
                .containsOnly(savedStep2);
    }

    @Test
    public void savePipelines_MultiplePipelines_SavesAllPipelines() {
        Pipeline pipeline1 = ProductPopulated.pipeline().build();
        Pipeline pipeline2 = ProductPopulated.pipeline().build();
        Pipeline pipeline3 = ProductPopulated.pipeline().build();

        pipelineService.savePipelines(ImmutableList.of(pipeline1, pipeline2, pipeline3));

        ArgumentCaptor<Pipeline> pipelineArgumentCaptor = ArgumentCaptor.forClass(Pipeline.class);
        verify(pipelineJpaRepository, times(3)).save(pipelineArgumentCaptor.capture());

        assertThat(pipelineArgumentCaptor.getAllValues())
                .containsExactly(pipeline1, pipeline2, pipeline3);
    }
}
