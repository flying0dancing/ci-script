package com.lombardrisk.ignis.design.server.pipeline;

import com.lombardrisk.ignis.client.core.response.ApiErrorCode;
import com.lombardrisk.ignis.client.design.pipeline.CreatePipelineRequest;
import com.lombardrisk.ignis.client.design.pipeline.PipelineConnectedGraphs;
import com.lombardrisk.ignis.client.design.pipeline.PipelineDisplayErrorView;
import com.lombardrisk.ignis.client.design.pipeline.PipelineEdgeView;
import com.lombardrisk.ignis.client.design.pipeline.PipelinePlanErrorView;
import com.lombardrisk.ignis.client.design.pipeline.PipelinePlanErrorView.MultipleStepsToSameOutputSchemaView;
import com.lombardrisk.ignis.client.design.pipeline.PipelineStepView;
import com.lombardrisk.ignis.client.design.pipeline.PipelineView;
import com.lombardrisk.ignis.client.design.pipeline.join.JoinFieldRequest;
import com.lombardrisk.ignis.client.design.schema.field.FieldDto;
import com.lombardrisk.ignis.client.external.pipeline.export.step.JoinType;
import com.lombardrisk.ignis.common.assertions.VavrAssert;
import com.lombardrisk.ignis.data.common.Identifiable;
import com.lombardrisk.ignis.data.common.error.ErrorResponse;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.design.field.DesignField;
import com.lombardrisk.ignis.design.field.FieldService;
import com.lombardrisk.ignis.design.server.fixtures.Design.Populated;
import com.lombardrisk.ignis.design.server.pipeline.fixture.PipelineServiceFixtureFactory;
import com.lombardrisk.ignis.design.server.pipeline.model.Pipeline;
import com.lombardrisk.ignis.design.server.productconfig.ProductConfig;
import com.lombardrisk.ignis.design.server.productconfig.ProductConfigService;
import com.lombardrisk.ignis.design.server.productconfig.fixture.ProductServiceFixtureFactory;
import com.lombardrisk.ignis.design.server.productconfig.fixture.SchemaServiceFixtureFactory;
import com.lombardrisk.ignis.design.server.productconfig.schema.Schema;
import com.lombardrisk.ignis.design.server.productconfig.schema.SchemaService;
import io.vavr.control.Option;
import io.vavr.control.Validation;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

public class PipelineServiceTest {

    @Rule
    public JUnitSoftAssertions soft = new JUnitSoftAssertions();

    private PipelineService pipelineService;
    private PipelineStepService pipelineStepService;
    private SchemaService schemaService;
    private FieldService fieldService;
    private ProductConfigService productConfigService;

    private ProductConfig product;

    @Before
    public void setUp() {
        Integer csvImportMaxLines = 100;

        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("user", ""));

        SchemaServiceFixtureFactory schemaServiceFixtureFactory = SchemaServiceFixtureFactory.create();
        productConfigService =
                ProductServiceFixtureFactory.create(schemaServiceFixtureFactory, csvImportMaxLines).getProductService();
        schemaService = schemaServiceFixtureFactory.getSchemaService();
        fieldService = schemaServiceFixtureFactory.getFieldService();

        PipelineServiceFixtureFactory pipelineServiceFixtureFactory = PipelineServiceFixtureFactory
                .create(schemaServiceFixtureFactory, csvImportMaxLines);
        PipelineServiceFixtureFactory.Exports exports = pipelineServiceFixtureFactory.getExports();

        pipelineService = exports.getPipelineService();
        pipelineStepService = exports.getPipelineStepService();

        product = VavrAssert.assertValid(
                productConfigService.createProductConfig(Populated.newProductRequest("my product").build()))
                .getResult();
    }

    @Test
    public void findAllPipelines_ReturnsPipelineViews() {
        pipelineService.saveNewPipeline(
                CreatePipelineRequest.builder()
                        .name("my new pipeline")
                        .productId(232L)
                        .build());

        pipelineService.saveNewPipeline(
                CreatePipelineRequest.builder()
                        .name("my other new pipeline")
                        .productId(555L)
                        .build());

        List<PipelineView> pipelines = pipelineService.findAllPipelines();

        soft.assertThat(pipelines)
                .extracting(PipelineView::getName)
                .containsExactly("my new pipeline", "my other new pipeline");

        soft.assertThat(pipelines)
                .extracting(PipelineView::getProductId)
                .containsExactly(232L, 555L);

        soft.assertThat(pipelines)
                .extracting(PipelineView::getError)
                .containsOnly(null, null);
    }

    @Test
    public void findAllPipelines_SelfJoiningStep_ReturnsPipelineViewsWithErrors() {
        Long schemaId = VavrAssert.assertValid(schemaService.validateAndCreateNew(
                Populated.schema("A").build()))
                .getResult().getId();

        PipelineView myNewPipeline = VavrAssert.assertValid(
                pipelineService.saveNewPipeline(
                        CreatePipelineRequest.builder()
                                .name("my new pipeline")
                                .productId(product.getId())
                                .build()))
                .getResult();

        VavrAssert.assertValid(pipelineStepService.savePipelineStep(
                myNewPipeline.getId(),
                Populated.pipelineMapStepRequest()
                        .name("map")
                        .schemaInId(schemaId)
                        .schemaOutId(schemaId)
                        .build()));

        List<PipelineView> pipelines = pipelineService.findAllPipelines();
        assertThat(pipelines).hasSize(1);

        PipelinePlanErrorView errorView = pipelines.get(0).getError();

        soft.assertThat(errorView.getErrors())
                .extracting(ApiErrorCode::getErrorCode, ApiErrorCode::getErrorMessage)
                .containsExactly(tuple("PIPELINE_STEP_SELF_JOIN", "Self-join on pipeline steps not allowed [map]"));

        soft.assertThat(errorView.getSelfJoiningSteps())
                .containsOnly(schemaId);
    }

    @Test
    public void findAllPipelines_PipelineHasCycles_ReturnsPipelineViewsWithErrors() {
        Schema schemaA = VavrAssert.assertValid(schemaService.validateAndCreateNew(
                Populated.schema("A").build()))
                .getResult();
        Schema schemaB = VavrAssert.assertValid(schemaService.validateAndCreateNew(
                Populated.schema("B").build()))
                .getResult();
        Schema schemaC = VavrAssert.assertValid(schemaService.validateAndCreateNew(
                Populated.schema("C").build()))
                .getResult();

        PipelineView myNewPipeline = VavrAssert.assertValid(
                pipelineService.saveNewPipeline(
                        CreatePipelineRequest.builder().productId(product.getId()).build()))
                .getResult();

        PipelineStepView aToB = VavrAssert.assertValid(pipelineStepService.savePipelineStep(
                myNewPipeline.getId(),
                Populated.pipelineMapStepRequest()
                        .schemaInId(schemaA.getId())
                        .schemaOutId(schemaB.getId())
                        .build()))
                .getResult();
        PipelineStepView bToC = VavrAssert.assertValid(pipelineStepService.savePipelineStep(
                myNewPipeline.getId(),
                Populated.pipelineMapStepRequest()
                        .schemaInId(schemaB.getId())
                        .schemaOutId(schemaC.getId())
                        .build()))
                .getResult();
        PipelineStepView cToA = VavrAssert.assertValid(pipelineStepService.savePipelineStep(
                myNewPipeline.getId(),
                Populated.pipelineMapStepRequest()
                        .schemaInId(schemaC.getId())
                        .schemaOutId(schemaA.getId())
                        .build()))
                .getResult();

        List<PipelineView> pipelines = pipelineService.findAllPipelines();
        assertThat(pipelines).hasSize(1);

        PipelinePlanErrorView errorView = pipelines.get(0).getError();

        soft.assertThat(errorView.isHasCycles())
                .isTrue();
    }

    @Test
    public void findAllPipelines_PipelineNotConnected_ReturnsPipelineViewsWithError() {
        Schema schemaA = VavrAssert.assertValid(
                schemaService.validateAndCreateNew(Populated.schema("A").build()))
                .getResult();

        Schema schemaB = VavrAssert.assertValid(
                schemaService.validateAndCreateNew(Populated.schema("B").build()))
                .getResult();

        Schema schemaY = VavrAssert.assertValid(
                schemaService.validateAndCreateNew(Populated.schema("Y").build()))
                .getResult();

        Schema schemaZ = VavrAssert.assertValid(
                schemaService.validateAndCreateNew(Populated.schema("Z").build()))
                .getResult();

        PipelineView pipeline = VavrAssert.assertValid(pipelineService.saveNewPipeline(
                Populated.createPipelineRequest().productId(product.getId()).build()))
                .getResult();

        VavrAssert.assertValid(pipelineStepService.savePipelineStep(
                pipeline.getId(),
                Populated.pipelineMapStepRequest()
                        .schemaInId(schemaA.getId())
                        .schemaOutId(schemaB.getId())
                        .build()));

        VavrAssert.assertValid(pipelineStepService.savePipelineStep(
                pipeline.getId(),
                Populated.pipelineMapStepRequest()
                        .schemaInId(schemaY.getId())
                        .schemaOutId(schemaZ.getId())
                        .build()));

        List<PipelineView> pipelines = pipelineService.findAllPipelines();

        assertThat(pipelines).hasSize(1);

        assertThat(pipelines.get(0).getError().getGraphNotConnected().getConnectedSets())
                .containsExactly(
                        newHashSet(schemaA.getId(), schemaB.getId()),
                        newHashSet(schemaY.getId(), schemaZ.getId()));
    }

    @Test
    public void findAllPipelines_PipelineStepOutputsToSameSchema_ReturnsPipelineViewsWithError() {
        Schema schemaA = VavrAssert.assertValid(
                schemaService.validateAndCreateNew(Populated.schema("A").build()))
                .getResult();

        Schema schemaB = VavrAssert.assertValid(
                schemaService.validateAndCreateNew(Populated.schema("B").build()))
                .getResult();

        Schema schemaY = VavrAssert.assertValid(
                schemaService.validateAndCreateNew(Populated.schema("Y").build()))
                .getResult();

        PipelineView pipeline = VavrAssert.assertValid(pipelineService.saveNewPipeline(
                Populated.createPipelineRequest().productId(product.getId()).build()))
                .getResult();

        PipelineStepView aToB = VavrAssert.assertValid(pipelineStepService.savePipelineStep(
                pipeline.getId(),
                Populated.pipelineMapStepRequest()
                        .schemaInId(schemaA.getId())
                        .schemaOutId(schemaB.getId())
                        .build()))
                .getResult();

        PipelineStepView yToB = VavrAssert.assertValid(pipelineStepService.savePipelineStep(
                pipeline.getId(),
                Populated.pipelineMapStepRequest()
                        .schemaInId(schemaY.getId())
                        .schemaOutId(schemaB.getId())
                        .build()))
                .getResult();

        List<PipelineView> pipelines = pipelineService.findAllPipelines();

        assertThat(pipelines).hasSize(1);

        soft.assertThat(pipelines.get(0).getError().getStepsToSameOutputSchemas())
                .extracting(MultipleStepsToSameOutputSchemaView::getStepsIds)
                .containsExactly(Arrays.asList(aToB.getId(), yToB.getId()));

        soft.assertThat(pipelines.get(0).getError().getStepsToSameOutputSchemas())
                .extracting(MultipleStepsToSameOutputSchemaView::getOutputSchemaId)
                .containsOnly(schemaB.getId());
    }

    @Test
    public void findOne_PipelineExists_ReturnsPipelineView() {
        Long existingId = VavrAssert.assertValid(
                pipelineService.saveNewPipeline(
                        CreatePipelineRequest.builder()
                                .name("newPipeline")
                                .productId(232L)
                                .build()))
                .getResult().getId();

        VavrAssert.assertValid(pipelineService.findOne(existingId))
                .withResult(PipelineView.builder()
                        .id(existingId)
                        .name("newPipeline")
                        .productId(232L)
                        .steps(Collections.emptySet())
                        .build());
    }

    @Test
    public void findOne_PipelineDoesNotExists_ReturnsNotFound() {
        VavrAssert.assertFailed(pipelineService.findOne(-1))
                .withFailure(CRUDFailure.notFoundIds("Pipeline", -1L));
    }

    @Test
    public void saveNewPipeline_ValidPipeline_ReturnsSavedPipelineView() {
        Validation<ErrorResponse, PipelineView> savedPipelineValidation = pipelineService.saveNewPipeline(
                Populated.createPipelineRequest()
                        .name("my new pipeline")
                        .productId(32L)
                        .build());

        assertThat(savedPipelineValidation.isValid()).isTrue();

        PipelineView savedPipeline = savedPipelineValidation.get();

        soft.assertThat(savedPipeline.getId()).isNotNull();
        soft.assertThat(savedPipeline.getName()).isEqualTo("my new pipeline");
        soft.assertThat(savedPipeline.getProductId()).isEqualTo(32L);
        soft.assertThat(savedPipeline.getSteps()).isEmpty();
        soft.assertThat(savedPipeline.getError()).isNull();
    }

    @Test
    public void saveNewPipeline_ValidPipeline_SavesPipeline() {
        Validation<ErrorResponse, PipelineView> savedPipelineValidation = pipelineService.saveNewPipeline(
                Populated.createPipelineRequest()
                        .name("my new pipeline")
                        .productId(34L)
                        .build());

        assertThat(savedPipelineValidation.isValid()).isTrue();

        PipelineView savedPipeline = savedPipelineValidation.get();

        Option<Pipeline> foundPipelineOption = pipelineService.findById(savedPipeline.getId());

        assertThat(foundPipelineOption.isDefined()).isTrue();

        Pipeline foundPipeline = foundPipelineOption.get();

        soft.assertThat(foundPipeline.getId()).isEqualTo(savedPipeline.getId());
        soft.assertThat(foundPipeline.getName()).isEqualTo("my new pipeline");
        soft.assertThat(foundPipeline.getProductId()).isEqualTo(34L);
    }

    @Test
    public void saveNewPipeline_PipelineNameAlreadyExists_ReturnsFailure() {
        pipelineService.saveNewPipeline(Populated.createPipelineRequest()
                .name("my new pipeline")
                .build());

        Validation<ErrorResponse, PipelineView> savedPipelineValidation = pipelineService.saveNewPipeline(
                Populated.createPipelineRequest()
                        .name("my new pipeline")
                        .build());

        assertThat(savedPipelineValidation.isInvalid()).isTrue();

        ErrorResponse failure = savedPipelineValidation.getError();
        soft.assertThat(failure.getErrorCode())
                .isEqualTo("name");
        soft.assertThat(failure.getErrorMessage())
                .contains("my new pipeline", "already exists");
    }

    @Test
    public void updatePipeline_ValidPipeline_ReturnsSavedPipelineView() {
        PipelineView savedPipeline = VavrAssert.assertValid(pipelineService.saveNewPipeline(
                Populated.createPipelineRequest()
                        .name("my new pipeline")
                        .productId(2342L)
                        .build())).getResult();

        PipelineView updatedPipeline = VavrAssert.assertValid(pipelineService.updatePipeline(
                savedPipeline.getId(),
                Populated.updatePipelineRequest()
                        .name("my updated pipeline")
                        .build())).getResult();

        soft.assertThat(updatedPipeline.getId()).isEqualTo(savedPipeline.getId());
        soft.assertThat(updatedPipeline.getName()).isEqualTo("my updated pipeline");
    }

    @Test
    public void updatePipeline_ValidPipeline_UpdatesPipeline() {
        PipelineView savedPipeline = VavrAssert.assertValid(pipelineService.saveNewPipeline(
                Populated.createPipelineRequest()
                        .name("my new pipeline")
                        .build())).getResult();

        PipelineView updatedPipeline = VavrAssert.assertValid(pipelineService.updatePipeline(
                savedPipeline.getId(),
                Populated.updatePipelineRequest()
                        .name("my updated pipeline")
                        .build())).getResult();

        Option<Pipeline> foundPipelineOption = pipelineService.findById(savedPipeline.getId());

        assertThat(foundPipelineOption.isDefined()).isTrue();

        Pipeline foundPipeline = foundPipelineOption.get();

        soft.assertThat(foundPipeline.getId()).isEqualTo(savedPipeline.getId());
        soft.assertThat(foundPipeline.getName()).isEqualTo("my updated pipeline");
    }

    @Test
    public void updatePipeline_PipelineDoesNotExist_ReturnsFailure() {
        CRUDFailure failure = VavrAssert.assertFailed(pipelineService.updatePipeline(
                2342L,
                Populated.updatePipelineRequest().build())).getValidation();

        soft.assertThat(failure)
                .isEqualTo(CRUDFailure.notFoundIds(Pipeline.class.getSimpleName(), 2342L));
    }

    @Test
    public void updatePipeline_PipelineWithErrors_ReturnsPipelineViewsWithErrors() {
        Long schemaId =
                VavrAssert.assertValid(schemaService.validateAndCreateNew(Populated.schema()
                        .productId(product.getId())
                        .displayName("THE_ONLY_SCHEMA")
                        .physicalTableName("PHYS")
                        .fields(newHashSet(DesignField.Populated.stringField("NAME").build()))
                        .build())).getResult().getId();

        PipelineView savedPipeline = VavrAssert.assertValid(pipelineService.saveNewPipeline(
                Populated.createPipelineRequest()
                        .name("my new pipeline")
                        .productId(product.getId())
                        .build())).getResult();

        VavrAssert.assertValid(pipelineStepService.savePipelineStep(
                savedPipeline.getId(),
                Populated.pipelineMapStepRequest()
                        .name("map")
                        .schemaInId(schemaId)
                        .schemaOutId(schemaId)
                        .build()));

        PipelineView updatedPipeline = VavrAssert.assertValid(pipelineService.updatePipeline(
                savedPipeline.getId(),
                Populated.updatePipelineRequest()
                        .name("my updated pipeline")
                        .build())).getResult();

        soft.assertThat(updatedPipeline.getError().getErrors())
                .containsExactly(ApiErrorCode.builder()
                        .errorCode("PIPELINE_STEP_SELF_JOIN")
                        .errorMessage("Self-join on pipeline steps not allowed [map]")
                        .build());
    }

    @Test
    public void deleteById_PipelineIdNotFound_ReturnsCrudFailure() {
        Validation<CRUDFailure, Identifiable> pipelineIdValidation = pipelineService.deleteById(23L);

        assertThat(pipelineIdValidation.isInvalid()).isTrue();
        assertThat(pipelineIdValidation.getError())
                .isEqualTo(CRUDFailure.notFoundIds(Pipeline.class.getSimpleName(), 23L));
    }

    @Test
    public void deleteById_PipelineExists_DeletesPipelineAndReturnsDeletedPipelineId() {
        PipelineView savedPipeline =
                pipelineService.saveNewPipeline(Populated.createPipelineRequest().build()).get();

        assertThat(pipelineService.findById(savedPipeline.getId()).isDefined())
                .isTrue();

        Validation<CRUDFailure, Identifiable> idValidation = pipelineService.deleteById(savedPipeline.getId());

        assertThat(idValidation.isValid()).isTrue();
        assertThat(idValidation.get().getId()).isEqualTo(savedPipeline.getId());

        assertThat(pipelineService.findById(savedPipeline.getId()).isDefined()).isFalse();
    }

    @Test
    public void getPipelineEdgeData_PipelineNotFound_ReturnsError() {
        VavrAssert.assertFailed(pipelineService.calculatePipelineEdgeData(-1L))
                .withFailure(PipelineDisplayErrorView.notFound("NOT_FOUND", "Could not find Pipeline for ids [-1]"));
    }

    @Test
    public void getPipelineEdgeData_PipelineHasManySteps_ReturnsEdges() {
        ProductConfig productConfig = VavrAssert.assertValid(
                productConfigService.createProductConfig(Populated.newProductRequest().build())
        ).getResult();

        PipelineView savedPipeline = VavrAssert.assertValid(pipelineService.saveNewPipeline(
                Populated.createPipelineRequest()
                        .productId(productConfig.getId())
                        .build()))
                .getResult();

        Schema schemaA = VavrAssert.assertValid(productConfigService.createNewSchemaOnProduct(
                productConfig.getId(), Populated.randomisedCreateSchemaRequest().build()))
                .getResult();

        FieldDto schemaAFieldId =
                fieldService.save(schemaA.getId(), DesignField.Populated.longFieldRequest("ID").build()).get();
        FieldDto schemaAFieldName =
                fieldService.save(schemaA.getId(), DesignField.Populated.stringFieldRequest("NAME").build()).get();

        Schema schemaB = VavrAssert.assertValid(
                productConfigService.createNewSchemaOnProduct(
                        productConfig.getId(), Populated.randomisedCreateSchemaRequest().build()))
                .getResult();

        FieldDto schemaBFieldId =
                fieldService.save(schemaB.getId(), DesignField.Populated.longFieldRequest("ID").build()).get();
        FieldDto schemaBFieldName =
                fieldService.save(schemaB.getId(), DesignField.Populated.stringFieldRequest("NAME").build()).get();

        Schema schemaC = VavrAssert.assertValid(productConfigService.createNewSchemaOnProduct(
                productConfig.getId(), Populated.randomisedCreateSchemaRequest().build()))
                .getResult();

        Schema schemaD = VavrAssert.assertValid(
                productConfigService.createNewSchemaOnProduct(
                        productConfig.getId(), Populated.randomisedCreateSchemaRequest().build()))
                .getResult();

        PipelineStepView joinStep = VavrAssert.assertValid(pipelineStepService.savePipelineStep(
                savedPipeline.getId(),
                Populated.pipelineJoinStepRequest()
                        .schemaOutId(schemaC.getId())
                        .joins(newHashSet(Populated.joinRequest()
                                .joinType(JoinType.FULL_OUTER)
                                .rightSchemaId(schemaA.getId())
                                .joinFields(Arrays.asList(
                                        JoinFieldRequest.builder()
                                                .leftFieldId(schemaB.getFields().iterator().next().getId())
                                                .rightFieldId(schemaA.getFields().iterator().next().getId())
                                                .build(),
                                        JoinFieldRequest.builder()
                                                .leftFieldId(schemaB.getFields().iterator().next().getId())
                                                .rightFieldId(schemaA.getFields().iterator().next().getId())
                                                .build()))
                                .leftSchemaId(schemaB.getId())
                                .build()))
                        .build()))
                .getResult();

        PipelineStepView mapStep = VavrAssert.assertValid(
                pipelineStepService.savePipelineStep(
                        savedPipeline.getId(),
                        Populated.pipelineMapStepRequest()
                                .schemaInId(schemaC.getId())
                                .schemaOutId(schemaD.getId())
                                .build()))
                .getResult();

        PipelineConnectedGraphs connectedGraphs =
                VavrAssert.assertValid(pipelineService.calculatePipelineEdgeData(savedPipeline.getId())).getResult();

        Set<PipelineEdgeView> connectedSet = connectedGraphs.getConnectedSets().iterator().next();
        assertThat(connectedSet).containsOnly(
                new PipelineEdgeView(schemaA.getId(), schemaC.getId(), joinStep),
                new PipelineEdgeView(schemaB.getId(), schemaC.getId(), joinStep),
                new PipelineEdgeView(schemaC.getId(), schemaD.getId(), mapStep)
        );
    }

    @Test
    public void getPipelineEdgeData_PipelineHasCycles_ReturnsCycleError() {
        ProductConfig productConfig = VavrAssert.assertValid(
                productConfigService.createProductConfig(Populated.newProductRequest().build())
        ).getResult();

        PipelineView savedPipeline = VavrAssert.assertValid(pipelineService.saveNewPipeline(
                Populated.createPipelineRequest()
                        .productId(productConfig.getId())
                        .build()))
                .getResult();

        Schema schemaA = VavrAssert.assertValid(
                productConfigService.createNewSchemaOnProduct(
                        productConfig.getId(), Populated.randomisedCreateSchemaRequest().build()))
                .getResult();

        FieldDto schemaAField =
                fieldService.save(schemaA.getId(), DesignField.Populated.longFieldRequest("ID").build()).get();

        Schema schemaB = VavrAssert.assertValid(
                productConfigService.createNewSchemaOnProduct(
                        productConfig.getId(), Populated.randomisedCreateSchemaRequest().build()))
                .getResult();

        FieldDto schemaBField =
                fieldService.save(schemaB.getId(), DesignField.Populated.longFieldRequest("ID").build()).get();

        Schema schemaC = VavrAssert.assertValid(
                productConfigService.createNewSchemaOnProduct(
                        productConfig.getId(), Populated.randomisedCreateSchemaRequest().build()))
                .getResult();

        PipelineStepView aToB = VavrAssert.assertValid(
                pipelineStepService.savePipelineStep(
                        savedPipeline.getId(),
                        Populated.pipelineMapStepRequest()
                                .schemaInId(schemaA.getId())
                                .schemaOutId(schemaB.getId())
                                .build()))
                .getResult();
        PipelineStepView bToC = VavrAssert.assertValid(
                pipelineStepService.savePipelineStep(
                        savedPipeline.getId(),
                        Populated.pipelineMapStepRequest()
                                .schemaInId(schemaB.getId())
                                .schemaOutId(schemaC.getId())
                                .build()))
                .getResult();
        PipelineStepView cToA = VavrAssert.assertValid(
                pipelineStepService.savePipelineStep(
                        savedPipeline.getId(),
                        Populated.pipelineMapStepRequest()
                                .schemaInId(schemaC.getId())
                                .schemaOutId(schemaA.getId())
                                .build()))
                .getResult();

        VavrAssert.assertFailed(pipelineService.calculatePipelineEdgeData(savedPipeline.getId()))
                .withFailure(
                        PipelineDisplayErrorView.hasCyclesError(
                                newHashSet(schemaA.getId(), schemaB.getId()),
                                cToA.getId()));
    }
}
