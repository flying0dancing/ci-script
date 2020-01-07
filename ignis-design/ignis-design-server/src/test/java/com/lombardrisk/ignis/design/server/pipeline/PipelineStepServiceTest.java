package com.lombardrisk.ignis.design.server.pipeline;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.lombardrisk.ignis.client.design.pipeline.PipelineStepView;
import com.lombardrisk.ignis.client.design.pipeline.PipelineView;
import com.lombardrisk.ignis.client.design.pipeline.aggregation.PipelineAggregationStepView;
import com.lombardrisk.ignis.client.design.pipeline.error.SelectResult;
import com.lombardrisk.ignis.client.design.pipeline.error.StepExecutionResult;
import com.lombardrisk.ignis.client.design.pipeline.error.UpdatePipelineError;
import com.lombardrisk.ignis.client.design.pipeline.join.JoinFieldRequest;
import com.lombardrisk.ignis.client.design.pipeline.join.PipelineJoinStepView;
import com.lombardrisk.ignis.client.design.pipeline.map.PipelineMapStepRequest;
import com.lombardrisk.ignis.client.design.pipeline.map.PipelineMapStepView;
import com.lombardrisk.ignis.client.design.pipeline.scriptlet.PipelineScriptletStepView;
import com.lombardrisk.ignis.client.design.pipeline.scriptlet.ScriptletInputView;
import com.lombardrisk.ignis.client.design.pipeline.select.OrderDirection;
import com.lombardrisk.ignis.client.design.pipeline.select.OrderRequest;
import com.lombardrisk.ignis.client.design.pipeline.select.OrderView;
import com.lombardrisk.ignis.client.design.pipeline.select.SelectRequest;
import com.lombardrisk.ignis.client.design.pipeline.select.SelectView;
import com.lombardrisk.ignis.client.design.pipeline.select.UnionRequest;
import com.lombardrisk.ignis.client.design.pipeline.select.WindowRequest;
import com.lombardrisk.ignis.client.design.pipeline.select.WindowView;
import com.lombardrisk.ignis.client.design.pipeline.test.request.CreateStepTestRequest;
import com.lombardrisk.ignis.client.design.pipeline.test.view.StepTestView;
import com.lombardrisk.ignis.client.design.pipeline.union.PipelineUnionStepView;
import com.lombardrisk.ignis.client.design.pipeline.union.UnionView;
import com.lombardrisk.ignis.client.design.pipeline.window.PipelineWindowStepView;
import com.lombardrisk.ignis.client.design.schema.field.FieldDto;
import com.lombardrisk.ignis.client.external.pipeline.export.TransformationType;
import com.lombardrisk.ignis.client.external.pipeline.export.step.JoinType;
import com.lombardrisk.ignis.common.assertions.VavrAssert;
import com.lombardrisk.ignis.data.common.Identifiable;
import com.lombardrisk.ignis.data.common.error.ErrorResponse;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.design.field.DesignField;
import com.lombardrisk.ignis.design.field.FieldService;
import com.lombardrisk.ignis.design.field.model.Field;
import com.lombardrisk.ignis.design.server.fixtures.Design.Populated;
import com.lombardrisk.ignis.design.server.pipeline.fixture.PipelineServiceFixtureFactory;
import com.lombardrisk.ignis.design.server.pipeline.model.Pipeline;
import com.lombardrisk.ignis.design.server.pipeline.model.PipelineAggregationStep;
import com.lombardrisk.ignis.design.server.pipeline.model.PipelineMapStep;
import com.lombardrisk.ignis.design.server.pipeline.model.PipelineStep;
import com.lombardrisk.ignis.design.server.pipeline.model.select.Select;
import com.lombardrisk.ignis.design.server.pipeline.model.select.Window;
import com.lombardrisk.ignis.design.server.pipeline.test.PipelineStepTestService;
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
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;

import static com.google.common.collect.Sets.newHashSet;
import static com.lombardrisk.ignis.design.field.DesignField.Populated.intField;
import static com.lombardrisk.ignis.design.field.DesignField.Populated.intFieldRequest;
import static com.lombardrisk.ignis.design.field.DesignField.Populated.longFieldRequest;
import static com.lombardrisk.ignis.design.field.DesignField.Populated.stringFieldRequest;
import static com.lombardrisk.ignis.design.server.fixtures.Design.Populated.emptySchema;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(Enclosed.class)
public class PipelineStepServiceTest {

    public static class SimpleCreateDeleteUpdate {

        @Rule
        public JUnitSoftAssertions soft = new JUnitSoftAssertions();

        private SchemaService schemaService;
        private FieldService fieldService;
        private PipelineService pipelineService;
        private PipelineStepService pipelineStepService;
        private PipelineStepTestService pipelineStepTestService;

        private PipelineView pipeline;

        private ProductConfig product;
        private Schema schemaIn;
        private Schema schemaOut;
        private FieldDto outField;

        @Before
        public void setUp() {

            SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("user", ""));
            SchemaServiceFixtureFactory schemaServiceFixtureFactory = SchemaServiceFixtureFactory.create();
            schemaService = schemaServiceFixtureFactory.getSchemaService();
            fieldService = schemaServiceFixtureFactory.getFieldService();

            PipelineServiceFixtureFactory pipelineServiceFixtureFactory =
                    PipelineServiceFixtureFactory.create(schemaServiceFixtureFactory, 100);
            PipelineServiceFixtureFactory.Exports exports = pipelineServiceFixtureFactory.getExports();

            pipelineService = exports.getPipelineService();
            pipelineStepService = exports.getPipelineStepService();
            pipelineStepTestService = exports.getPipelineStepTestService();
            ProductConfigService productConfigService = ProductServiceFixtureFactory.create().getProductService();

            pipeline = VavrAssert.assertValid(
                    pipelineService.saveNewPipeline(Populated.createPipelineRequest().name("my pipeline").build()))
                    .getResult();

            product = VavrAssert.assertValid(
                    productConfigService.createProductConfig(Populated.newProductRequest("my product").build()))
                    .getResult();

            schemaIn = VavrAssert.assertValid(schemaService.validateAndCreateNew(Populated.schema()
                    .productId(product.getId())
                    .displayName("MY_FIRST_SCHEMA")
                    .physicalTableName("MYFS")
                    .build()))
                    .getResult();

            FieldDto inField =
                    fieldService.save(schemaIn.getId(), DesignField.Populated.stringFieldRequest("NAME").build()).get();

            schemaOut = VavrAssert.assertValid(schemaService.validateAndCreateNew(Populated.schema()
                    .productId(product.getId())
                    .displayName("MY_SECOND_SCHEMA")
                    .physicalTableName("MYSS")
                    .fields(newHashSet(DesignField.Populated.stringField("NAME").build()))
                    .build()))
                    .getResult();

            outField = fieldService.save(schemaOut.getId(), DesignField.Populated.stringFieldRequest("NAME").build())
                    .get();
        }

        @Test
        public void deletePipelineStep_PipelineStepIdNotFound_ReturnsCrudFailure() {
            Validation<CRUDFailure, Identifiable> pipelineStepIdValidation =
                    pipelineStepService.deletePipelineStep(23L);

            assertThat(pipelineStepIdValidation.isInvalid()).isTrue();
            assertThat(pipelineStepIdValidation.getError())
                    .isEqualTo(CRUDFailure.notFoundIds(PipelineStep.class.getSimpleName(), 23L));
        }

        @Test
        public void deletePipelineStep_PipelineStepExists_DeletesPipelineStepAndReturnsDeletedPipelineStepId() {
            PipelineStepView savedPipelineStep =
                    pipelineStepService.savePipelineStep(pipeline.getId(), Populated.pipelineMapStepRequest()
                            .schemaInId(schemaIn.getId())
                            .schemaOutId(schemaOut.getId())
                            .build()).get();

            assertThat(pipelineStepService.findById(savedPipelineStep.getId()).isDefined())
                    .isTrue();

            Validation<CRUDFailure, Identifiable> idValidation =
                    pipelineStepService.deletePipelineStep(savedPipelineStep.getId());

            assertThat(idValidation.isValid()).isTrue();
            assertThat(idValidation.get().getId()).isEqualTo(savedPipelineStep.getId());

            assertThat(pipelineStepService.findById(savedPipelineStep.getId()).isDefined()).isFalse();
        }

        @Test
        public void deletePipelineStep_PipelineStepExists_PipelineStepShouldNotExistInPipeline() {
            PipelineStepView savedPipelineStep =
                    pipelineStepService.savePipelineStep(pipeline.getId(), Populated.pipelineMapStepRequest()
                            .schemaInId(schemaIn.getId())
                            .schemaOutId(schemaOut.getId())
                            .build()).get();

            Option<Pipeline> pipelineOption = pipelineService.findById(pipeline.getId());
            assertThat(pipelineOption.isDefined()).isTrue();

            Pipeline pipeline = pipelineOption.get();

            assertThat(pipeline.getSteps())
                    .extracting(PipelineStep::getId)
                    .containsExactly(savedPipelineStep.getId());

            pipelineStepService.deletePipelineStep(savedPipelineStep.getId());

            assertThat(pipelineService.findById(pipeline.getId()).get().getSteps())
                    .extracting(PipelineStep::getId)
                    .doesNotContain(savedPipelineStep.getId());
        }

        @Test
        public void savePipelineStep_AddNewStep_AddsToExistingStepsInPipeline() {
            PipelineView pipeline = VavrAssert.assertValid(
                    pipelineService.saveNewPipeline(Populated.createPipelineRequest().build())).getResult();

            PipelineStepView existingPipelineStep = VavrAssert.assertValid(
                    pipelineStepService.savePipelineStep(
                            pipeline.getId(),
                            Populated.pipelineMapStepRequest()
                                    .name("step1")
                                    .schemaInId(schemaIn.getId())
                                    .schemaOutId(schemaOut.getId())
                                    .build()))
                    .getResult();

            Long newSchemaInId = VavrAssert.assertValid(
                    schemaService.validateAndCreateNew(
                            Populated.schema()
                                    .productId(product.getId())
                                    .displayName("MY_THIRD_SCHEMA")
                                    .physicalTableName("MYTS")
                                    .build()))
                    .getResult().getId();

            Long newSchemaOutId = VavrAssert.assertValid(
                    schemaService.validateAndCreateNew(
                            Populated.schema().productId(product.getId())
                                    .displayName("MY_FOURTH_SCHEMA")
                                    .physicalTableName("MYFOS")
                                    .build()))
                    .getResult()
                    .getId();

            VavrAssert.assertValid(
                    pipelineStepService.savePipelineStep(
                            pipeline.getId(), Populated.pipelineMapStepRequest()
                                    .name("step2")
                                    .schemaInId(newSchemaInId)
                                    .schemaOutId(newSchemaOutId)
                                    .build()));

            Pipeline updatedPipeline = VavrAssert.assertValid(pipelineService.findWithValidation(pipeline.getId()))
                    .getResult();

            assertThat(updatedPipeline.getSteps())
                    .hasSize(2);

            PipelineMapStep newlyCreatedStep = (PipelineMapStep) updatedPipeline.getSteps()
                    .stream()
                    .filter(step -> step.getName().equals("step2"))
                    .findFirst()
                    .get();

            soft.assertThat(newlyCreatedStep.getId())
                    .isNotEqualTo(existingPipelineStep.getId());
            soft.assertThat(newlyCreatedStep.getName())
                    .isEqualTo("step2");
            soft.assertThat(newlyCreatedStep.getType())
                    .isEqualTo(TransformationType.MAP);
            soft.assertThat(newlyCreatedStep.getSchemaInId())
                    .isEqualTo(newSchemaInId);
            soft.assertThat(newlyCreatedStep.getSchemaOutId())
                    .isEqualTo(newSchemaOutId);
        }

        @Test
        public void savePipelineStep_NewPipelineStep_AddsStepToPipeline() {
            PipelineView pipeline = VavrAssert.assertValid(
                    pipelineService.saveNewPipeline(Populated.createPipelineRequest().build())).getResult();

            PipelineMapStepRequest mapStepRequest = Populated.pipelineMapStepRequest()
                    .name("my pipeline step")
                    .description("my pipeline step description")
                    .schemaInId(schemaIn.getId())
                    .schemaOutId(schemaOut.getId())
                    .selects(singleton(SelectRequest.builder()
                            .select("NAME")
                            .outputFieldId(outField.getId())
                            .build()))
                    .filters(asList("A==1", "B==2", "C==3"))
                    .build();

            PipelineStepView pipelineStep = VavrAssert.assertValid(
                    pipelineStepService.savePipelineStep(pipeline.getId(), mapStepRequest))
                    .getResult();

            Pipeline updatedPipeline = VavrAssert.assertValid(pipelineService.findWithValidation(pipeline.getId()))
                    .getResult();

            assertThat(updatedPipeline.getSteps())
                    .hasSize(1);

            PipelineMapStep createdStep = (PipelineMapStep) updatedPipeline.getSteps().iterator().next();

            soft.assertThat(createdStep.getId())
                    .isEqualTo(pipelineStep.getId());
            soft.assertThat(createdStep.getName())
                    .isEqualTo("my pipeline step");
            soft.assertThat(createdStep.getDescription())
                    .isEqualTo("my pipeline step description");
            soft.assertThat(createdStep.getSchemaInId())
                    .isEqualTo(schemaIn.getId());
            soft.assertThat(createdStep.getSchemaOutId())
                    .isEqualTo(schemaOut.getId());
            soft.assertThat(createdStep.getType())
                    .isEqualTo(TransformationType.MAP);
            soft.assertThat(createdStep.getSelects())
                    .containsExactly(
                            Populated.select()
                                    .select("NAME")
                                    .outputFieldId(findField(schemaOut, "NAME").getId())
                                    .build());
            soft.assertThat(createdStep.getFilters())
                    .containsExactlyInAnyOrder("A==1", "B==2", "C==3");
        }

        @Test
        public void savePipelineStep_PipelineNotFound_ShouldReturnError() {
            Validation<UpdatePipelineError, PipelineStepView> stepValidation =
                    pipelineStepService.savePipelineStep(234L, Populated.pipelineMapStepRequest().build());

            assertThat(stepValidation.isInvalid()).isTrue();
            assertThat(stepValidation.getError().getPipelineNotFoundError())
                    .isEqualTo(CRUDFailure.notFoundIds("Pipeline"));
        }

        @Test
        public void savePipelineStep_SchemasNotFound_ReturnsError() {
            PipelineView pipeline = VavrAssert.assertValid(
                    pipelineService.saveNewPipeline(Populated.createPipelineRequest().build())).getResult();

            Validation<UpdatePipelineError, PipelineStepView> step = pipelineStepService.savePipelineStep(
                    pipeline.getId(),
                    Populated.pipelineMapStepRequest()
                            .selects(singleton(SelectRequest.builder().select("VERSION").build()))
                            .schemaInId(2342L)
                            .schemaOutId(232L)
                            .build());

            assertThat(step.isInvalid()).isTrue();
            assertThat(step.getError().getStepExecutionResult().getErrors()).containsExactly(
                    ErrorResponse.valueOf(
                            "Could not find Schema for ids [2342]",
                            "NOT_FOUND"),
                    ErrorResponse.valueOf(
                            "Could not find Schema for ids [232]",
                            "NOT_FOUND"));
        }

        @Test
        public void updatePipelineStep_ChangeProperties_UpdatesExistingStepInPipeline() {
            PipelineView pipeline = VavrAssert.assertValid(
                    pipelineService.saveNewPipeline(Populated.createPipelineRequest().build())).getResult();

            PipelineStepView existingPipelineStep = VavrAssert.assertValid(
                    pipelineStepService.savePipelineStep(
                            pipeline.getId(),
                            Populated.pipelineMapStepRequest()
                                    .name("step1")
                                    .schemaInId(schemaIn.getId())
                                    .schemaOutId(schemaOut.getId())
                                    .build()))
                    .getResult();

            Schema newSchemaIn = VavrAssert.assertValid(schemaService.validateAndCreateNew(
                    Populated.schema()
                            .productId(product.getId())
                            .displayName("MY_THIRD_SCHEMA")
                            .physicalTableName("MYTS")
                            .build()))
                    .getResult();

            FieldDto newInFieldD = fieldService.save(newSchemaIn.getId(), intFieldRequest("D").build()).get();

            Schema newSchemaOut = VavrAssert.assertValid(schemaService.validateAndCreateNew(
                    Populated.schema().productId(product.getId())
                            .displayName("MY_FOURTH_SCHEMA")
                            .physicalTableName("MYFOS")
                            .build()))
                    .getResult();

            FieldDto newOutFieldX = fieldService.save(newSchemaOut.getId(), intFieldRequest("X").build()).get();

            VavrAssert.assertValid(pipelineStepService.updatePipelineStep(
                    pipeline.getId(),
                    existingPipelineStep.getId(),
                    Populated.pipelineAggregationStepRequest()
                            .name("step1 - New Name")
                            .description("Now this is an aggregation step")
                            .schemaInId(newSchemaIn.getId())
                            .schemaOutId(newSchemaOut.getId())
                            .selects(singleton(SelectRequest.builder()
                                    .select("SUM(D)")
                                    .outputFieldId(newOutFieldX.getId())
                                    .build()))
                            .groupings(asList("A", "B"))
                            .filters(Collections.singletonList("C=1"))
                            .build()));

            Pipeline updatedPipeline = VavrAssert.assertValid(pipelineService.findWithValidation(pipeline.getId()))
                    .getResult();

            assertThat(updatedPipeline.getSteps())
                    .hasSize(1);

            PipelineAggregationStep updatedStep =
                    (PipelineAggregationStep) updatedPipeline.getSteps().iterator().next();

            soft.assertThat(updatedStep.getName())
                    .isEqualTo("step1 - New Name");
            soft.assertThat(updatedStep.getDescription())
                    .isEqualTo("Now this is an aggregation step");
            soft.assertThat(updatedStep.getSelects())
                    .containsOnly(
                            Select.builder()
                                    .select("SUM(D)")
                                    .outputFieldId(newOutFieldX.getId())
                                    .isWindow(false)
                                    .window(Window.none())
                                    .build());
            soft.assertThat(updatedStep.getFilters())
                    .containsOnly("C=1");
            soft.assertThat(updatedStep.getGroupings())
                    .containsOnly("A", "B");
            soft.assertThat(updatedStep.getType())
                    .isEqualTo(TransformationType.AGGREGATION);
            soft.assertThat(updatedStep.getSchemaInId())
                    .isEqualTo(newSchemaIn.getId());
            soft.assertThat(updatedStep.getSchemaOutId())
                    .isEqualTo(newSchemaOut.getId());
        }

        @Test
        public void updatePipelineStep_StepHasTests_AssociatesTestsWithUpdatedStep() {
            PipelineView pipeline = VavrAssert.assertValid(
                    pipelineService.saveNewPipeline(Populated.createPipelineRequest().build())).getResult();

            PipelineStepView existingPipelineStep = VavrAssert.assertValid(
                    pipelineStepService.savePipelineStep(
                            pipeline.getId(),
                            Populated.pipelineMapStepRequest()
                                    .name("step1")
                                    .schemaInId(schemaIn.getId())
                                    .schemaOutId(schemaOut.getId())
                                    .build()))
                    .getResult();

            Identifiable stepTestId = VavrAssert.assertValid(pipelineStepTestService.create(
                    CreateStepTestRequest.builder()
                            .pipelineStepId(existingPipelineStep.getId())
                            .build()))
                    .getResult();

            PipelineStepView updatedStep = VavrAssert.assertValid(pipelineStepService.updatePipelineStep(
                    pipeline.getId(),
                    existingPipelineStep.getId(),
                    Populated.pipelineAggregationStepRequest()
                            .schemaInId(schemaIn.getId())
                            .schemaOutId(schemaOut.getId())
                            .build()))
                    .getResult();

            VavrAssert.assertValid(pipelineStepTestService.findById(stepTestId.getId()))
                    .extracting(StepTestView::getPipelineStepId)
                    .withResult(updatedStep.getId());
        }

        @Test
        public void updatePipelineStep_PipelineDoesNotExist_ReturnsError() {
            Validation<UpdatePipelineError, PipelineStepView> result = pipelineStepService.updatePipelineStep(
                    101L, 201L, Populated.pipelineAggregationStepRequest().build());

            UpdatePipelineError pipelineError = VavrAssert.assertFailed(result)
                    .getValidation();
            assertThat(pipelineError.getPipelineNotFoundError().toErrorResponse())
                    .isEqualTo(
                            ErrorResponse.valueOf("Could not find Pipeline for ids [101]", "NOT_FOUND"));
        }

        @Test
        public void updatePipelineStep_PipelineStepDoesNotExist_ReturnsError() {
            PipelineView pipeline = VavrAssert.assertValid(
                    pipelineService.saveNewPipeline(Populated.createPipelineRequest().build())).getResult();

            Validation<UpdatePipelineError, PipelineStepView> result = pipelineStepService.updatePipelineStep(
                    pipeline.getId(), 201L, Populated.pipelineAggregationStepRequest().build());

            UpdatePipelineError error = VavrAssert.assertFailed(result)
                    .getValidation();

            assertThat(error.getPipelineNotFoundError().toErrorResponse())
                    .isEqualTo(ErrorResponse.valueOf("Could not find PipelineStep for ids [201]", "NOT_FOUND"));
        }

        @Test
        public void updatePipelineStep_SchemasDoNotExist_ReturnsError() {
            PipelineView pipeline = VavrAssert.assertValid(
                    pipelineService.saveNewPipeline(Populated.createPipelineRequest().build())).getResult();

            PipelineStepView existingPipelineStep = VavrAssert.assertValid(
                    pipelineStepService.savePipelineStep(
                            pipeline.getId(),
                            Populated.pipelineMapStepRequest()
                                    .schemaInId(schemaIn.getId())
                                    .schemaOutId(schemaOut.getId())
                                    .build()))
                    .getResult();

            Long newSchemaInId = VavrAssert.assertValid(schemaService.validateAndCreateNew(
                    Populated.schema()
                            .productId(product.getId())
                            .displayName("MY_THIRD_SCHEMA")
                            .physicalTableName("MYTS")
                            .build()))
                    .getResult()
                    .getId();

            Validation<UpdatePipelineError, PipelineStepView> updateResult =
                    pipelineStepService.updatePipelineStep(pipeline.getId(), existingPipelineStep.getId(),
                            Populated.pipelineAggregationStepRequest()
                                    .schemaInId(newSchemaInId)
                                    .schemaOutId(999L)
                                    .build());

            UpdatePipelineError pipelineError = VavrAssert.assertFailed(updateResult)
                    .getValidation();
            assertThat(pipelineError.getStepExecutionResult().getErrors())
                    .contains(ErrorResponse.valueOf("Could not find Schema for ids [999]", "NOT_FOUND"));
        }
    }

    public static class MapAndAggregationStepCreateUpdate {

        @Rule
        public JUnitSoftAssertions soft = new JUnitSoftAssertions();

        private FieldService fieldService;
        private PipelineService pipelineService;
        private PipelineStepService pipelineStepService;
        private PipelineStepExecutor mockSparkSqlExecutor;

        private Schema schemaIn;
        private Schema schemaOut;

        @Before
        public void setUp() {
            Integer csvImportMaxLines = 100;

            SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("user", ""));
            SchemaServiceFixtureFactory schemaServiceFixtureFactory = SchemaServiceFixtureFactory.create();
            SchemaService schemaService = schemaServiceFixtureFactory.getSchemaService();
            fieldService = schemaServiceFixtureFactory.getFieldService();

            PipelineServiceFixtureFactory pipelineServiceFixtureFactory =
                    PipelineServiceFixtureFactory.create(schemaServiceFixtureFactory, csvImportMaxLines);
            PipelineServiceFixtureFactory.Exports exports = pipelineServiceFixtureFactory.getExports();

            mockSparkSqlExecutor = pipelineServiceFixtureFactory.getTestHelpers().getPipelineStepExecutor();
            pipelineService = exports.getPipelineService();
            pipelineStepService = exports.getPipelineStepService();
            ProductConfigService productConfigService = ProductServiceFixtureFactory.create().getProductService();

            PipelineView pipeline = VavrAssert.assertValid(
                    pipelineService.saveNewPipeline(Populated.createPipelineRequest().name("my pipeline").build()))
                    .getResult();

            ProductConfig product = VavrAssert.assertValid(
                    productConfigService.createProductConfig(Populated.newProductRequest("my product").build()))
                    .getResult();
            schemaIn =
                    VavrAssert.assertValid(schemaService.validateAndCreateNew(Populated.schema()
                            .productId(product.getId())
                            .displayName("MY_FIRST_SCHEMA")
                            .physicalTableName("MYFS")
                            .fields(newHashSet(DesignField.Populated.stringField("NAME").build()))
                            .build())).getResult();
            schemaOut =
                    VavrAssert.assertValid(schemaService.validateAndCreateNew(Populated.schema()
                            .productId(product.getId())
                            .displayName("MY_SECOND_SCHEMA")
                            .physicalTableName("MYSS")
                            .fields(newHashSet(DesignField.Populated.stringField("NAME").build()))
                            .build())).getResult();
        }

        @Test
        public void savePipelineStep_NewPipelineMapStep_ReturnsPipelineView() {
            PipelineView pipeline = VavrAssert.assertValid(
                    pipelineService.saveNewPipeline(Populated.createPipelineRequest().build())).getResult();

            FieldDto outField = fieldService.save(schemaOut.getId(), stringFieldRequest("NAME").build()).get();

            Validation<UpdatePipelineError, PipelineStepView> createdStepValidation =
                    pipelineStepService.savePipelineStep(
                            pipeline.getId(),
                            Populated.pipelineMapStepRequest()
                                    .name("my pipeline step")
                                    .description("my pipeline step description")
                                    .schemaInId(schemaIn.getId())
                                    .schemaOutId(schemaOut.getId())
                                    .selects(singleton(
                                            SelectRequest.builder()
                                                    .select("NAME")
                                                    .outputFieldId(outField.getId())
                                                    .build()))
                                    .filters(asList("A==1", "B==2", "C==3"))
                                    .build());

            assertThat(createdStepValidation.isValid()).isTrue();

            PipelineMapStepView createdStep = (PipelineMapStepView) createdStepValidation.get();

            soft.assertThat(createdStep.getId())
                    .isNotNull();
            soft.assertThat(createdStep.getName())
                    .isEqualTo("my pipeline step");
            soft.assertThat(createdStep.getDescription())
                    .isEqualTo("my pipeline step description");
            soft.assertThat(createdStep.getSchemaInId())
                    .isEqualTo(schemaIn.getId());
            soft.assertThat(createdStep.getSchemaOutId())
                    .isEqualTo(schemaOut.getId());
            soft.assertThat(createdStep.getType())
                    .isEqualTo(TransformationType.MAP);
            soft.assertThat(createdStep.getSelects())
                    .containsExactly(
                            SelectView.builder()
                                    .select("NAME")
                                    .outputFieldId(findField(schemaOut, "NAME").getId())
                                    .hasWindow(false)
                                    .build());
            soft.assertThat(createdStep.getFilters())
                    .containsExactlyInAnyOrder("A==1", "B==2", "C==3");
        }

        @Test
        public void savePipelineStep_NewPipelineAggregationStep_ReturnsPipelineView() {
            PipelineView pipeline = VavrAssert.assertValid(
                    pipelineService.saveNewPipeline(Populated.createPipelineRequest().build())).getResult();

            FieldDto outputNameField = fieldService.save(schemaOut.getId(), stringFieldRequest("NAME").build()).get();

            Validation<UpdatePipelineError, PipelineStepView> createdStepValidation =
                    pipelineStepService.savePipelineStep(
                            pipeline.getId(),
                            Populated.pipelineAggregationStepRequest()
                                    .name("my pipeline step")
                                    .description("my pipeline step description")
                                    .schemaInId(schemaIn.getId())
                                    .schemaOutId(schemaOut.getId())
                                    .selects(singleton(
                                            SelectRequest.builder()
                                                    .select("NAME")
                                                    .outputFieldId(outputNameField.getId())
                                                    .build()))
                                    .filters(asList("A==1", "B==2", "C==3"))
                                    .groupings(asList("A", "B", "C"))
                                    .build());

            assertThat(createdStepValidation.isValid()).isTrue();

            PipelineAggregationStepView createdStep = (PipelineAggregationStepView) createdStepValidation.get();

            soft.assertThat(createdStep.getId())
                    .isNotNull();
            soft.assertThat(createdStep.getName())
                    .isEqualTo("my pipeline step");
            soft.assertThat(createdStep.getDescription())
                    .isEqualTo("my pipeline step description");
            soft.assertThat(createdStep.getSchemaInId())
                    .isEqualTo(schemaIn.getId());
            soft.assertThat(createdStep.getSchemaOutId())
                    .isEqualTo(schemaOut.getId());
            soft.assertThat(createdStep.getType())
                    .isEqualTo(TransformationType.AGGREGATION);
            soft.assertThat(createdStep.getSelects())
                    .containsExactly(
                            SelectView.builder()
                                    .select("NAME")
                                    .outputFieldId(findField(schemaOut, "NAME").getId())
                                    .hasWindow(false)
                                    .build());
            soft.assertThat(createdStep.getFilters())
                    .containsExactlyInAnyOrder("A==1", "B==2", "C==3");
            soft.assertThat(createdStep.getGroupings())
                    .containsExactly("A", "B", "C");
        }

        @Test
        public void savePipelineStep_NewStepInvalid_ReturnsError() {
            PipelineView pipeline = VavrAssert.assertValid(
                    pipelineService.saveNewPipeline(Populated.createPipelineRequest().build())).getResult();

            FieldDto outputNameField = fieldService.save(schemaOut.getId(), stringFieldRequest("NAME").build()).get();

            when(mockSparkSqlExecutor.executeFullTransformation(any(), any()))
                    .thenReturn(Validation.invalid(
                            singletonList(ErrorResponse.valueOf("Ooops", "OP"))));

            when(mockSparkSqlExecutor.transformOutputFieldsIndividually(any(), any()))
                    .thenReturn(singletonList(SelectResult.error(
                            "NAME",
                            1L,
                            null,
                            singletonList(ErrorResponse.valueOf("Ooops", "OP")))));

            Validation<UpdatePipelineError, PipelineStepView> createdStepValidation =
                    pipelineStepService.savePipelineStep(
                            pipeline.getId(),
                            Populated.pipelineAggregationStepRequest()
                                    .schemaInId(schemaIn.getId())
                                    .schemaOutId(schemaOut.getId())
                                    .selects(singleton(
                                            SelectRequest.builder()
                                                    .select("NAME")
                                                    .outputFieldId(outputNameField.getId())
                                                    .build()))
                                    .filters(asList("A==1", "B==2", "C==3"))
                                    .groupings(asList("A", "B", "C"))
                                    .build());
            UpdatePipelineError error = VavrAssert.assertFailed(createdStepValidation)
                    .getValidation();

            StepExecutionResult stepExecutionResult = error.getStepExecutionResult();
            assertThat(stepExecutionResult.isSuccessful())
                    .isFalse();

            assertThat(stepExecutionResult.getSelectsExecutionErrors().getIndividualErrors())
                    .containsExactly(SelectResult.error(
                            "NAME",
                            1L,
                            null,
                            singletonList(ErrorResponse.valueOf("Ooops", "OP"))));

            assertThat(stepExecutionResult.getErrors())
                    .containsExactly(ErrorResponse.valueOf("Ooops", "OP"));
        }
    }

    public static class JoinStepCreateUpdate {

        @Rule
        public JUnitSoftAssertions soft = new JUnitSoftAssertions();

        private SchemaService schemaService;
        private FieldService fieldService;
        private PipelineService pipelineService;
        private PipelineStepService pipelineStepService;

        private ProductConfig product;
        private Schema schemaOut;

        @Before
        public void setUp() {
            Integer csvImportMaxLines = 100;

            SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("user", ""));
            SchemaServiceFixtureFactory schemaServiceFixtureFactory = SchemaServiceFixtureFactory.create();
            schemaService = schemaServiceFixtureFactory.getSchemaService();
            fieldService = schemaServiceFixtureFactory.getFieldService();

            PipelineServiceFixtureFactory pipelineServiceFixtureFactory =
                    PipelineServiceFixtureFactory.create(schemaServiceFixtureFactory, csvImportMaxLines);
            PipelineServiceFixtureFactory.Exports exports = pipelineServiceFixtureFactory.getExports();

            pipelineService = exports.getPipelineService();
            pipelineStepService = exports.getPipelineStepService();
            ProductConfigService productConfigService = ProductServiceFixtureFactory.create().getProductService();

            PipelineView pipeline = VavrAssert.assertValid(
                    pipelineService.saveNewPipeline(Populated.createPipelineRequest().name("my pipeline").build()))
                    .getResult();

            product = VavrAssert.assertValid(
                    productConfigService.createProductConfig(Populated.newProductRequest("my product").build()))
                    .getResult();

            schemaOut =
                    VavrAssert.assertValid(schemaService.validateAndCreateNew(Populated.schema()
                            .productId(product.getId())
                            .displayName("MY_SECOND_SCHEMA")
                            .physicalTableName("MYSS")
                            .fields(newHashSet(
                                    DesignField.Populated.stringField("NAME").build(),
                                    DesignField.Populated.stringField("TITLE").build()))
                            .build()))
                            .getResult();
        }

        @Test
        public void savePipelineStep_NewPipelineJoinStep_ReturnsPipelineView() {
            PipelineView pipeline = VavrAssert.assertValid(
                    pipelineService.saveNewPipeline(Populated.createPipelineRequest().build())).getResult();

            FieldDto outputNameField = fieldService.save(schemaOut.getId(), stringFieldRequest("NAME").build()).get();
            FieldDto outputTitleField = fieldService.save(schemaOut.getId(), stringFieldRequest("TITLE").build()).get();

            Schema leftSchema = VavrAssert.assertValid(
                    schemaService.validateAndCreateNew(Populated.schema()
                            .productId(product.getId())
                            .displayName("Left Table")
                            .physicalTableName("LEFT")
                            .build()
                    )).getResult();

            Schema rightSchema = VavrAssert.assertValid(
                    schemaService.validateAndCreateNew(Populated.schema()
                            .productId(product.getId())
                            .displayName("Right Table")
                            .physicalTableName("RIGHT")
                            .build()
                    )).getResult();

            FieldDto leftJoinFieldId =
                    fieldService.save(leftSchema.getId(), longFieldRequest().name("ID").build()).get();
            FieldDto leftJoinFieldName =
                    fieldService.save(leftSchema.getId(), stringFieldRequest().name("NAME").build()).get();
            FieldDto rightJoinFieldId =
                    fieldService.save(rightSchema.getId(), longFieldRequest().name("ID").build()).get();
            FieldDto rightJoinFieldName =
                    fieldService.save(rightSchema.getId(), stringFieldRequest().name("NAME").build()).get();

            PipelineStepView createdStepValidation = VavrAssert.assertValid(
                    pipelineStepService.savePipelineStep(
                            pipeline.getId(),
                            Populated.pipelineJoinStepRequest()
                                    .name("Join Step")
                                    .description("Join to get name and job title")
                                    .schemaOutId(schemaOut.getId())
                                    .selects(newHashSet(
                                            SelectRequest.builder()
                                                    .select("LEFT.NAME")
                                                    .outputFieldId(outputNameField.getId())
                                                    .build(),
                                            SelectRequest.builder()
                                                    .select("RIGHT.JOB_TITLE")
                                                    .outputFieldId(outputTitleField.getId())
                                                    .build()))
                                    .joins(newHashSet(Populated.joinRequest()
                                            .leftSchemaId(leftSchema.getId())
                                            .joinFields(asList(
                                                    JoinFieldRequest.builder()
                                                            .leftFieldId(leftJoinFieldId.getId())
                                                            .rightFieldId(rightJoinFieldId.getId())
                                                            .build(),
                                                    JoinFieldRequest.builder()
                                                            .leftFieldId(leftJoinFieldName.getId())
                                                            .rightFieldId(rightJoinFieldName.getId())
                                                            .build()))
                                            .rightSchemaId(rightSchema.getId())
                                            .joinType(JoinType.LEFT)
                                            .build()))
                                    .build()))
                    .getResult();

            PipelineJoinStepView createdStep = (PipelineJoinStepView) createdStepValidation;

            soft.assertThat(createdStep.getId())
                    .isNotNull();
            soft.assertThat(createdStep.getName())
                    .isEqualTo("Join Step");
            soft.assertThat(createdStep.getDescription())
                    .isEqualTo("Join to get name and job title");
            soft.assertThat(createdStep.getSchemaOutId())
                    .isEqualTo(schemaOut.getId());
            soft.assertThat(createdStep.getType())
                    .isEqualTo(TransformationType.JOIN);
            soft.assertThat(createdStep.getSelects())
                    .containsExactlyInAnyOrder(
                            SelectView.builder()
                                    .select("LEFT.NAME")
                                    .outputFieldId(findField(schemaOut, "NAME").getId())
                                    .hasWindow(false)
                                    .build(),
                            SelectView.builder()
                                    .select("RIGHT.JOB_TITLE")
                                    .outputFieldId(findField(schemaOut, "TITLE").getId())
                                    .hasWindow(false)
                                    .build());

            assertThat(createdStep.getJoins())
                    .hasSize(1);
            soft.assertThat(createdStep.getJoins().get(0).getLeftSchemaId())
                    .isEqualTo(leftSchema.getId());
            soft.assertThat(createdStep.getJoins().get(0).getRightSchemaId())
                    .isEqualTo(rightSchema.getId());
            soft.assertThat(createdStep.getJoins().get(0).getJoinType())
                    .isEqualTo(JoinType.LEFT);
            soft.assertThat(createdStep.getJoins().get(0).getJoinFields()).extracting("leftFieldId")
                    .containsExactlyInAnyOrder(leftJoinFieldId.getId(), leftJoinFieldName.getId());
            soft.assertThat(createdStep.getJoins().get(0).getJoinFields()).extracting("rightFieldId")
                    .containsExactlyInAnyOrder(rightJoinFieldId.getId(), rightJoinFieldName.getId());
        }

        @Test
        public void savePipelineStep_JoinInputSchemasNotFound_ReturnsErrors() {
            PipelineView pipeline = VavrAssert.assertValid(
                    pipelineService.saveNewPipeline(Populated.createPipelineRequest().build())).getResult();

            FieldDto outputNameField = fieldService.save(schemaOut.getId(), stringFieldRequest("NAME").build()).get();
            FieldDto outputTitleField = fieldService.save(schemaOut.getId(), stringFieldRequest("TITLE").build()).get();

            Schema leftSchema = VavrAssert.assertValid(
                    schemaService.validateAndCreateNew(Populated.schema()
                            .productId(product.getId())
                            .displayName("Left Table")
                            .physicalTableName("LEFT")
                            .build()
                    )).getResult();

            FieldDto leftJoinIdField =
                    fieldService.save(leftSchema.getId(), longFieldRequest().name("ID").build()).get();
            FieldDto leftJoinNameField =
                    fieldService.save(leftSchema.getId(), stringFieldRequest("NAME").build()).get();

            Schema rightSchema = VavrAssert.assertValid(
                    schemaService.validateAndCreateNew(Populated.schema()
                            .productId(product.getId())
                            .displayName("Right Table")
                            .physicalTableName("RIGHT")
                            .build()
                    )).getResult();

            FieldDto rightJoinIdField =
                    fieldService.save(rightSchema.getId(), longFieldRequest().name("ID").build()).get();
            FieldDto rightJoinNameField =
                    fieldService.save(rightSchema.getId(), stringFieldRequest().name("NAME").build()).get();

            fieldService.save(rightSchema.getId(), stringFieldRequest("JOB_TITLE").build()).get();

            Validation<UpdatePipelineError, PipelineStepView> savePipelineStepResult =
                    pipelineStepService.savePipelineStep(
                            pipeline.getId(),
                            Populated.pipelineJoinStepRequest()
                                    .name("Join Step")
                                    .description("Join to get name and job title")
                                    .schemaOutId(schemaOut.getId())
                                    .selects(newHashSet(
                                            SelectRequest.builder()
                                                    .select("LEFT.NAME")
                                                    .outputFieldId(outputNameField.getId())
                                                    .build(),
                                            SelectRequest.builder()
                                                    .select("RIGHT.JOB_TITLE")
                                                    .outputFieldId(outputTitleField.getId())
                                                    .build()))
                                    .joins(newHashSet(
                                            Populated.joinRequest()
                                                    .rightSchemaId(rightSchema.getId())
                                                    .joinFields(asList(
                                                            JoinFieldRequest.builder()
                                                                    .leftFieldId(leftJoinIdField.getId())
                                                                    .rightFieldId(rightJoinIdField.getId())
                                                                    .build(),
                                                            JoinFieldRequest.builder()
                                                                    .leftFieldId(leftJoinNameField.getId())
                                                                    .rightFieldId(rightJoinNameField.getId())
                                                                    .build()))
                                                    .leftSchemaId(leftSchema.getId())
                                                    .build(),
                                            Populated.joinRequest()
                                                    .rightSchemaId(1001L)
                                                    .leftSchemaId(1002L)
                                                    .build()))
                                    .build());

            VavrAssert.assertCollectionFailure
                    (savePipelineStepResult.mapError(updatePipelineError -> updatePipelineError
                            .getStepExecutionResult()
                            .getErrors()))
                    .withOnlyFailures(ErrorResponse.valueOf("Could not find Schema for ids [1001, 1002]", "NOT_FOUND"));
        }
    }

    public static class WindowStep {

        @Rule
        public JUnitSoftAssertions soft = new JUnitSoftAssertions();

        private FieldService fieldService;
        private PipelineService pipelineService;
        private PipelineStepService pipelineStepService;

        private Schema schemaIn;
        private Schema schemaOut;

        @Before
        public void setUp() {
            Integer csvImportMaxLines = 100;

            SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("user", ""));
            SchemaServiceFixtureFactory schemaServiceFixtureFactory = SchemaServiceFixtureFactory.create();
            SchemaService schemaService = schemaServiceFixtureFactory.getSchemaService();
            fieldService = schemaServiceFixtureFactory.getFieldService();
            PipelineServiceFixtureFactory pipelineServiceFixtureFactory =
                    PipelineServiceFixtureFactory.create(schemaServiceFixtureFactory, csvImportMaxLines);
            PipelineServiceFixtureFactory.Exports exports = pipelineServiceFixtureFactory.getExports();

            pipelineService = exports.getPipelineService();
            pipelineStepService = exports.getPipelineStepService();
            ProductConfigService productConfigService = ProductServiceFixtureFactory.create().getProductService();

            PipelineView pipeline = VavrAssert.assertValid(
                    pipelineService.saveNewPipeline(Populated.createPipelineRequest().name("my pipeline").build()))
                    .getResult();

            ProductConfig product = VavrAssert.assertValid(
                    productConfigService.createProductConfig(Populated.newProductRequest("my product").build()))
                    .getResult();
            schemaIn =
                    VavrAssert.assertValid(schemaService.validateAndCreateNew(Populated.schema()
                            .productId(product.getId())
                            .displayName("Product revenue")
                            .physicalTableName("REVENUE")
                            .fields(newHashSet(
                                    DesignField.Populated.stringField("PRODUCT").build(),
                                    DesignField.Populated.stringField("CATEGORY").build(),
                                    intField("REVENUE").build()))
                            .build())).getResult();
            schemaOut =
                    VavrAssert.assertValid(schemaService.validateAndCreateNew(Populated.schema()
                            .productId(product.getId())
                            .displayName("Popular products")
                            .physicalTableName("POPULAR")
                            .fields(newHashSet(
                                    DesignField.Populated.stringField("PRODUCT").build(),
                                    DesignField.Populated.stringField("CATEGORY").build(),
                                    intField("REVENUE").build(),
                                    DesignField.Populated.stringField("RANK").build()))
                            .build())).getResult();
        }

        @Test
        public void savePipelineStep_NewPipelineWindowStep_ReturnsPipelineView() {
            PipelineView pipeline = VavrAssert.assertValid(
                    pipelineService.saveNewPipeline(Populated.createPipelineRequest().build())).getResult();
            Long productFieldId =
                    fieldService.save(schemaOut.getId(), longFieldRequest().name("PRODUCT").build()).get().getId();
            Long categoryFieldId =
                    fieldService.save(schemaOut.getId(), longFieldRequest().name("CATEGORY").build()).get().getId();
            Long revenueFieldId =
                    fieldService.save(schemaOut.getId(), longFieldRequest().name("REVENUE").build()).get().getId();
            Long rankFieldId =
                    fieldService.save(schemaOut.getId(), longFieldRequest().name("RANK").build()).get().getId();

            Validation<UpdatePipelineError, PipelineStepView> createdStepValidation =
                    pipelineStepService.savePipelineStep(
                            pipeline.getId(),
                            Populated.pipelineWindowStepRequest()
                                    .name("my pipeline step")
                                    .description("my pipeline step description")
                                    .schemaInId(schemaIn.getId())
                                    .schemaOutId(schemaOut.getId())
                                    .filters(singleton("CATEGORY != LAPTOP"))
                                    .selects(newHashSet(
                                            SelectRequest.builder()
                                                    .select("PRODUCT")
                                                    .outputFieldId(productFieldId)
                                                    .build(),
                                            SelectRequest.builder()
                                                    .select("CATEGORY")
                                                    .outputFieldId(categoryFieldId)
                                                    .build(),
                                            SelectRequest.builder()
                                                    .select("REVENUE")
                                                    .outputFieldId(revenueFieldId)
                                                    .build(),
                                            SelectRequest.builder()
                                                    .select("rank()")
                                                    .hasWindow(true)
                                                    .window(WindowRequest.builder()
                                                            .partitionBy(singleton("CATEGORY"))
                                                            .orderBy(singletonList(OrderRequest.builder()
                                                                    .fieldName("REVENUE")
                                                                    .direction(OrderDirection.DESC)
                                                                    .priority(1)
                                                                    .build()))
                                                            .build())
                                                    .outputFieldId(rankFieldId)
                                                    .build()))
                                    .build());

            PipelineWindowStepView createdStep =
                    (PipelineWindowStepView) VavrAssert.assertValid(createdStepValidation).getResult();

            soft.assertThat(createdStep.getId())
                    .isNotNull();
            soft.assertThat(createdStep.getName())
                    .isEqualTo("my pipeline step");
            soft.assertThat(createdStep.getDescription())
                    .isEqualTo("my pipeline step description");
            soft.assertThat(createdStep.getSchemaInId())
                    .isEqualTo(schemaIn.getId());
            soft.assertThat(createdStep.getSchemaOutId())
                    .isEqualTo(schemaOut.getId());
            soft.assertThat(createdStep.getType())
                    .isEqualTo(TransformationType.WINDOW);
            soft.assertThat(createdStep.getFilters())
                    .contains("CATEGORY != LAPTOP");
            soft.assertThat(createdStep.getSelects())
                    .containsExactlyInAnyOrder(
                            SelectView.builder()
                                    .select("PRODUCT")
                                    .outputFieldId(productFieldId)
                                    .hasWindow(false)
                                    .build(),
                            SelectView.builder()
                                    .select("CATEGORY")
                                    .outputFieldId(categoryFieldId)
                                    .hasWindow(false)
                                    .build(),
                            SelectView.builder()
                                    .select("REVENUE")
                                    .outputFieldId(revenueFieldId)
                                    .hasWindow(false)
                                    .build(),
                            SelectView.builder()
                                    .select("rank()")
                                    .hasWindow(true)
                                    .window(WindowView.builder()
                                            .partitionBy(singleton("CATEGORY"))
                                            .orderBy(singletonList(OrderView.builder()
                                                    .fieldName("REVENUE")
                                                    .direction(OrderDirection.DESC)
                                                    .priority(1)
                                                    .build()))
                                            .build())
                                    .outputFieldId(rankFieldId)
                                    .build());
        }
    }

    public static class UnionStep {

        @Rule
        public JUnitSoftAssertions soft = new JUnitSoftAssertions();

        private SchemaService schemaService;
        private FieldService fieldService;
        private PipelineService pipelineService;
        private PipelineStepService pipelineStepService;
        private ProductConfig product;

        @Before
        public void setUp() {
            SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("user", ""));
            SchemaServiceFixtureFactory schemaServiceFixtureFactory = SchemaServiceFixtureFactory.create();
            schemaService = schemaServiceFixtureFactory.getSchemaService();
            fieldService = schemaServiceFixtureFactory.getFieldService();
            PipelineServiceFixtureFactory pipelineServiceFixtureFactory =
                    PipelineServiceFixtureFactory.create(schemaServiceFixtureFactory, 100);
            PipelineServiceFixtureFactory.Exports exports = pipelineServiceFixtureFactory.getExports();

            pipelineService = exports.getPipelineService();
            pipelineStepService = exports.getPipelineStepService();
            ProductConfigService productConfigService = ProductServiceFixtureFactory.create().getProductService();

            product = VavrAssert.assertValid(
                    productConfigService.createProductConfig(Populated.newProductRequest("my product").build()))
                    .getResult();
        }

        @Test
        public void savePipelineStep_AddNewUnionStep_AddsToExistingStepsInPipeline() {
            PipelineView pipeline = VavrAssert.assertValid(
                    pipelineService.saveNewPipeline(Populated.createPipelineRequest().build())).getResult();

            Schema schemaA = VavrAssert.assertValid(schemaService.validateAndCreateNew(
                    emptySchema("A").productId(product.getId()).build())).getResult();
            FieldDto schemaAIDField = fieldService.save(schemaA.getId(), intFieldRequest("A_ID").build()).get();

            Schema schemaB = VavrAssert.assertValid(schemaService.validateAndCreateNew(
                    emptySchema("B").productId(product.getId()).build())).getResult();
            FieldDto schemaBIDField = fieldService.save(schemaB.getId(), intFieldRequest("B_ID").build()).get();

            Long schemaC = VavrAssert.assertValid(
                    schemaService.validateAndCreateNew(
                            emptySchema("C").productId(product.getId())
                                    .build()))
                    .getResult()
                    .getId();

            FieldDto schemaCIDField = fieldService.save(schemaC, intFieldRequest("C_ID").build()).get();

            PipelineStepView newlyCreatedStep = VavrAssert.assertValid(
                    pipelineStepService.savePipelineStep(
                            pipeline.getId(), Populated.pipelineUnionStepRequest()
                                    .name("step2")
                                    .unionSchemas(ImmutableMap.of(
                                            schemaA.getId(), UnionRequest.builder()
                                                    .selects(singletonList(SelectRequest.builder()
                                                            .select("A_ID")
                                                            .outputFieldId(schemaCIDField.getId())
                                                            .build()))
                                                    .filters(singletonList("A_NOTHER_FIELD > 2"))
                                                    .build(),
                                            schemaB.getId(), UnionRequest.builder()
                                                    .selects(singletonList(SelectRequest.builder()
                                                            .select("B_ID")
                                                            .outputFieldId(schemaCIDField.getId())
                                                            .build()))
                                                    .build()))
                                    .schemaOutId(schemaC)
                                    .build()))
                    .getResult();

            soft.assertThat(newlyCreatedStep.getName())
                    .isEqualTo("step2");
            soft.assertThat(newlyCreatedStep.getType())
                    .isEqualTo(TransformationType.UNION);

            PipelineUnionStepView unionStepView = (PipelineUnionStepView) newlyCreatedStep;
            soft.assertThat(unionStepView.getSchemaOutId())
                    .isEqualTo(schemaC);
            soft.assertThat(unionStepView.getUnions())
                    .contains(
                            entry(schemaA.getId(), UnionView.builder()
                                    .selects(singletonList(SelectView.builder()
                                            .select("A_ID")
                                            .outputFieldId(schemaCIDField.getId())
                                            .build()))
                                    .filters(Collections.singletonList("A_NOTHER_FIELD > 2"))
                                    .build()),
                            entry(schemaB.getId(), UnionView.builder()
                                    .selects(singletonList(SelectView.builder()
                                            .select("B_ID")
                                            .outputFieldId(schemaCIDField.getId())
                                            .build()))
                                    .build()));
        }
    }

    public static class ScriptletStep {

        @Rule
        public JUnitSoftAssertions soft = new JUnitSoftAssertions();

        private SchemaService schemaService;
        private PipelineService pipelineService;
        private PipelineStepService pipelineStepService;
        private ProductConfig product;

        @Before
        public void setUp() {
            SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("user", ""));
            SchemaServiceFixtureFactory schemaServiceFixtureFactory = SchemaServiceFixtureFactory.create();
            schemaService = schemaServiceFixtureFactory.getSchemaService();
            PipelineServiceFixtureFactory pipelineServiceFixtureFactory =
                    PipelineServiceFixtureFactory.create(schemaServiceFixtureFactory, 100);
            PipelineServiceFixtureFactory.Exports exports = pipelineServiceFixtureFactory.getExports();

            pipelineService = exports.getPipelineService();
            pipelineStepService = exports.getPipelineStepService();
            ProductConfigService productConfigService = ProductServiceFixtureFactory.create().getProductService();

            product = VavrAssert.assertValid(
                    productConfigService.createProductConfig(Populated.newProductRequest("my product").build()))
                    .getResult();
        }

        @Test
        public void savePipelineStep_AddNewScriptletStep_AddsToExistingStepsInPipeline() {
            PipelineView pipeline = VavrAssert.assertValid(
                    pipelineService.saveNewPipeline(Populated.createPipelineRequest().build())).getResult();

            Schema schemaA = VavrAssert.assertValid(schemaService.validateAndCreateNew(
                    emptySchema("A").productId(product.getId()).build())).getResult();

            Schema schemaB = VavrAssert.assertValid(schemaService.validateAndCreateNew(
                    emptySchema("B").productId(product.getId()).build())).getResult();

            PipelineStepView newlyCreatedStep = VavrAssert.assertValid(
                    pipelineStepService.savePipelineStep(
                            pipeline.getId(), Populated.PipelineScriptletStepRequest()
                                    .name("step2")
                                    .jarFile("myFile.jar")
                                    .javaClass("class.scriptlet")
                                    .scriptletInputs(ImmutableSet.of(
                                            Populated.scriptletInputRequest()
                                                    .metadataInput("scriptletInput")
                                                    .schemaInId(schemaA.getId())
                                                    .build()))
                                    .schemaOutId(schemaB.getId())
                                    .build()))
                    .getResult();

            soft.assertThat(newlyCreatedStep.getName())
                    .isEqualTo("step2");
            soft.assertThat(newlyCreatedStep.getType())
                    .isEqualTo(TransformationType.SCRIPTLET);

            PipelineScriptletStepView scriptletStepView = (PipelineScriptletStepView) newlyCreatedStep;
            soft.assertThat(scriptletStepView.getJarFile())
                    .isEqualTo("myFile.jar");
            soft.assertThat(scriptletStepView.getJavaClass())
                    .isEqualTo("class.scriptlet");
            soft.assertThat(scriptletStepView.getScriptletInputs().size()).isEqualTo(1);
            soft.assertThat(scriptletStepView.getScriptletInputs()).containsExactly(ScriptletInputView.builder()
                    .metadataInput("scriptletInput")
                    .schemaInId(schemaA.getId())
                    .build());
            soft.assertThat(scriptletStepView.getSchemaOutId())
                    .isEqualTo(schemaB.getId());
        }
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public static Field findField(final Schema schema, final String fieldName) {
        return schema.getFields().stream()
                .filter(field -> field.getName().equals(fieldName))
                .findFirst()
                .get();
    }
}
