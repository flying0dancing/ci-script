package com.lombardrisk.ignis.design.server.productconfig.validation;

import com.lombardrisk.ignis.client.design.pipeline.CreatePipelineRequest;
import com.lombardrisk.ignis.client.design.pipeline.PipelineStepView;
import com.lombardrisk.ignis.client.design.pipeline.PipelineView;
import com.lombardrisk.ignis.client.design.pipeline.error.StepExecutionResult;
import com.lombardrisk.ignis.client.design.productconfig.NewProductConfigRequest;
import com.lombardrisk.ignis.client.design.productconfig.validation.PipelineCheck;
import com.lombardrisk.ignis.client.design.productconfig.validation.PipelineTask;
import com.lombardrisk.ignis.client.design.productconfig.validation.ProductConfigTaskList;
import com.lombardrisk.ignis.client.design.productconfig.validation.ValidationError;
import com.lombardrisk.ignis.client.design.productconfig.validation.ValidationTask;
import com.lombardrisk.ignis.common.assertions.VavrAssert;
import com.lombardrisk.ignis.data.common.error.ErrorResponse;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.design.server.fixtures.Design;
import com.lombardrisk.ignis.design.server.pipeline.PipelineService;
import com.lombardrisk.ignis.design.server.pipeline.PipelineStepSelectsValidator;
import com.lombardrisk.ignis.design.server.pipeline.PipelineStepService;
import com.lombardrisk.ignis.design.server.productconfig.ProductConfig;
import com.lombardrisk.ignis.design.server.productconfig.ProductConfigService;
import com.lombardrisk.ignis.design.server.productconfig.fixture.ProductServiceFixtureFactory;
import com.lombardrisk.ignis.design.server.productconfig.fixture.SchemaServiceFixtureFactory;
import com.lombardrisk.ignis.design.server.productconfig.schema.Schema;
import com.lombardrisk.ignis.design.server.productconfig.schema.SchemaService;
import com.lombardrisk.ignis.design.server.productconfig.schema.request.CreateSchemaRequest;
import io.vavr.control.Validation;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static com.lombardrisk.ignis.client.design.productconfig.validation.TaskStatus.FAILED;
import static com.lombardrisk.ignis.client.design.productconfig.validation.TaskStatus.PENDING;
import static com.lombardrisk.ignis.client.design.productconfig.validation.TaskStatus.RUNNING;
import static com.lombardrisk.ignis.client.design.productconfig.validation.TaskStatus.SUCCESS;
import static com.lombardrisk.ignis.client.design.productconfig.validation.TaskType.PIPELINE_GRAPH;
import static com.lombardrisk.ignis.client.design.productconfig.validation.TaskType.PIPELINE_STEP;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ProductConfigValidatorTest {

    @Rule
    public JUnitSoftAssertions soft = new JUnitSoftAssertions();

    @Mock
    private PipelineStepSelectsValidator stepSelectsValidator;

    private ProductConfigValidator validator;
    private ProductConfigService productService;
    private SchemaService schemaService;
    private PipelineService pipelineService;
    private PipelineStepService pipelineStepService;

    private ProductConfig productConfig;
    private Schema schema1;
    private Schema schema2;
    private Schema schema3;
    private PipelineView pipeline1;
    private PipelineView pipeline2;
    private PipelineStepView pipeline1Step1;
    private PipelineStepView pipeline1Step2;
    private PipelineStepView pipeline2Step1;

    @Before
    public void setUp() {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("user", ""));
        SchemaServiceFixtureFactory schemaServiceFactory = SchemaServiceFixtureFactory.create();
        ProductServiceFixtureFactory productServiceFactory =
                ProductServiceFixtureFactory.create(schemaServiceFactory, 100);

        productService = productServiceFactory.getProductService();
        pipelineService = productServiceFactory.getPipelineDependencies().getPipelineService();
        schemaService = schemaServiceFactory.getSchemaService();
        pipelineStepService = productServiceFactory.getPipelineDependencies().getPipelineStepService();

        when(stepSelectsValidator.validate(any()))
                .thenReturn(StepExecutionResult.builder().build());

        PipelineValidator pipelineValidator = new PipelineValidator(
                stepSelectsValidator,
                productServiceFactory.getPipelineDependencies().getPipelinePlanGenerator());

        validator = new ProductConfigValidator(productServiceFactory.getProductPipelineRepository(), pipelineValidator,
                productService);

        productConfig =
                VavrAssert.assertValid(productService.createProductConfig(NewProductConfigRequest.builder()
                        .name("product1")
                        .version("productVersion")
                        .build())).getResult();

        schema1 = VavrAssert.assertValid(
                productService.createNewSchemaOnProduct(productConfig.getId(), CreateSchemaRequest.builder()
                        .displayName("display 1").physicalTableName("schemaName 1")
                        .build()))
                .getResult();

        schema2 = VavrAssert.assertValid(
                productService.createNewSchemaOnProduct(productConfig.getId(), CreateSchemaRequest.builder()
                        .displayName("display 2").physicalTableName("schemaName 2")
                        .build()))
                .getResult();

        schema3 = VavrAssert.assertValid(
                productService.createNewSchemaOnProduct(productConfig.getId(), CreateSchemaRequest.builder()
                        .displayName("display 3").physicalTableName("schemaName 3")
                        .build()))
                .getResult();

        pipeline1 = VavrAssert.assertValid(
                pipelineService.saveNewPipeline(CreatePipelineRequest.builder()
                        .productId(this.productConfig.getId())
                        .name("the first pipeline")
                        .build()))
                .getResult();

        pipeline2 = VavrAssert.assertValid(
                pipelineService.saveNewPipeline(CreatePipelineRequest.builder()
                        .productId(this.productConfig.getId())
                        .name("the second pipeline")
                        .build()))
                .getResult();

        pipeline1Step1 = VavrAssert.assertValid(
                pipelineStepService.savePipelineStep(pipeline1.getId(), Design.Populated.pipelineMapStepRequest()
                        .name("Pipeline 1 Step 1")
                        .schemaInId(schema1.getId())
                        .schemaOutId(schema2.getId())
                        .build()))
                .getResult();

        pipeline1Step2 = VavrAssert.assertValid(
                pipelineStepService.savePipelineStep(pipeline1.getId(), Design.Populated.pipelineMapStepRequest()
                        .name("Pipeline 1 Step 2")
                        .schemaInId(schema2.getId())
                        .schemaOutId(schema3.getId())
                        .build()))
                .getResult();

        pipeline2Step1 = VavrAssert.assertValid(
                pipelineStepService.savePipelineStep(pipeline2.getId(), Design.Populated.pipelineMapStepRequest()
                        .name("Pipeline 2 Step 1")
                        .schemaInId(schema1.getId())
                        .schemaOutId(schema3.getId())
                        .build()))
                .getResult();
    }

    @Test
    public void productConfigTaskList_ProductNotFound_ReturnsFailure() {
        Validation<CRUDFailure, ProductConfigTaskList> result = validator.productConfigTaskList(199L);

        assertThat(result.getError())
                .isEqualTo(CRUDFailure.notFoundIds("ProductConfig", 199L));
    }

    @Test
    public void productConfigTaskList_ProductWithoutPipeline_ReturnsEmptyTaskList() {
        ProductConfig emptyProductConfig = VavrAssert.assertValid(
                productService.createProductConfig(NewProductConfigRequest.builder()
                        .name("empty product")
                        .build()))
                .getResult();

        ProductConfigTaskList taskList = VavrAssert.assertValid(
                validator.productConfigTaskList(emptyProductConfig.getId())).getResult();

        assertThat(taskList.getProductId())
                .isEqualTo(emptyProductConfig.getId());

        assertThat(taskList.getPipelineTasks())
                .isEmpty();
    }

    @Test
    public void productConfigTaskList_ProductWithPipelines_ReturnsTaskList() {
        ProductConfigTaskList taskList = VavrAssert.assertValid(
                validator.productConfigTaskList(productConfig.getId())).getResult();

        assertThat(taskList.getProductId())
                .isEqualTo(productConfig.getId());

        assertThat(taskList.getPipelineTasks())
                .containsOnly(
                        entry(pipeline1.getId(), PipelineTask.builder()
                                .pipelineId(pipeline1.getId())
                                .name("the first pipeline")
                                .status(PENDING)
                                .tasks(asList(
                                        PipelineCheck.builder()
                                                .pipelineId(pipeline1.getId())
                                                .name("Graph validation")
                                                .status(PENDING)
                                                .type(PIPELINE_GRAPH)
                                                .build(),
                                        PipelineCheck.builder()
                                                .pipelineId(pipeline1.getId())
                                                .pipelineStepId(pipeline1Step1.getId())
                                                .name("Pipeline 1 Step 1")
                                                .status(PENDING)
                                                .type(PIPELINE_STEP)
                                                .build(),
                                        PipelineCheck.builder()
                                                .pipelineId(pipeline1.getId())
                                                .pipelineStepId(pipeline1Step2.getId())
                                                .name("Pipeline 1 Step 2")
                                                .status(PENDING)
                                                .type(PIPELINE_STEP)
                                                .build()))
                                .build()),
                        entry(pipeline2.getId(), PipelineTask.builder()
                                .pipelineId(pipeline2.getId())
                                .name("the second pipeline")
                                .status(PENDING)
                                .tasks(asList(
                                        PipelineCheck.builder()
                                                .pipelineId(pipeline2.getId())
                                                .name("Graph validation")
                                                .status(PENDING)
                                                .type(PIPELINE_GRAPH)
                                                .build(),
                                        PipelineCheck.builder()
                                                .pipelineId(pipeline2.getId())
                                                .pipelineStepId(pipeline2Step1.getId())
                                                .name("Pipeline 2 Step 1")
                                                .status(PENDING)
                                                .type(PIPELINE_STEP)
                                                .build()))
                                .build()));
    }

    @Test
    public void validateProduct_ProductConfigDoesNotExist_ReturnsError() {
        Flux<ValidationTask> validationEventFlux = validator.validateProduct(2342342342L);

        StepVerifier.create(validationEventFlux)
                .expectNext(ValidationError.builder()
                        .message("ProductConfig with id " + 2342342342L + " not found")
                        .build())
                .expectComplete()
                .verify();
    }

    @Test
    public void validateProduct_ProductConfigWithoutPipeline_ReturnsNoEvents() {
        ProductConfig emptyProductConfig = VavrAssert.assertValid(
                productService.createProductConfig(NewProductConfigRequest.builder()
                        .name("empty product")
                        .build()))
                .getResult();

        Flux<ValidationTask> validationEventFlux = validator.validateProduct(emptyProductConfig.getId());

        StepVerifier.create(validationEventFlux)
                .expectComplete()
                .verify();

        verifyZeroInteractions(stepSelectsValidator);
    }

    @Test
    public void validateProduct_ProductConfigMultiplePipeline_ReturnsEvents() {
        when(stepSelectsValidator.validate(any()))
                .thenReturn(StepExecutionResult.builder().build())
                .thenReturn(StepExecutionResult.builder()
                        .errors(singletonList(
                                ErrorResponse.valueOf("Failed to validate step", "Validation Failure")))
                        .build())
                .thenReturn(StepExecutionResult.builder().build());

        Flux<ValidationTask> validationEventFlux = validator.validateProduct(productConfig.getId());

        PipelineCheck pipeline1GraphCheck = PipelineCheck.builder()
                .pipelineId(pipeline1.getId())
                .name("Graph validation")
                .type(PIPELINE_GRAPH)
                .build();

        PipelineCheck pipeline1Step1Check = PipelineCheck.builder()
                .pipelineId(pipeline1.getId())
                .pipelineStepId(pipeline1Step1.getId())
                .name("Pipeline 1 Step 1")
                .type(PIPELINE_STEP)
                .build();

        PipelineCheck pipeline1Step2Check = PipelineCheck.builder()
                .pipelineId(pipeline1.getId())
                .pipelineStepId(pipeline1Step2.getId())
                .name("Pipeline 1 Step 2")
                .type(PIPELINE_STEP)
                .build();

        PipelineTask pipeline1Task = PipelineTask.builder()
                .pipelineId(pipeline1.getId())
                .name("the first pipeline")
                .tasks(asList(pipeline1Step1Check, pipeline1Step2Check))
                .build();

        PipelineCheck pipeline2GraphCheck = PipelineCheck.builder()
                .pipelineId(pipeline2.getId())
                .name("Graph validation")
                .type(PIPELINE_GRAPH)
                .build();

        PipelineCheck pipeline2Step1Check = PipelineCheck.builder()
                .pipelineId(pipeline2.getId())
                .pipelineStepId(pipeline2Step1.getId())
                .name("Pipeline 2 Step 1")
                .type(PIPELINE_STEP)
                .build();

        PipelineTask pipeline2Task = PipelineTask.builder()
                .pipelineId(pipeline2.getId())
                .name("the second pipeline")
                .tasks(singletonList(pipeline2Step1Check))
                .build();

        StepVerifier.create(validationEventFlux)
                .expectNext(pipeline1Task
                        .withStatus(PENDING)
                        .withTasks(asList(
                                pipeline1GraphCheck.withStatus(PENDING),
                                pipeline1Step1Check.withStatus(PENDING),
                                pipeline1Step2Check.withStatus(PENDING))))
                .expectNext(pipeline1Task
                        .withStatus(PENDING)
                        .withTasks(asList(
                                pipeline1GraphCheck.withStatus(SUCCESS),
                                pipeline1Step1Check.withStatus(PENDING),
                                pipeline1Step2Check.withStatus(PENDING))))
                .expectNext(pipeline1Task
                        .withStatus(RUNNING)
                        .withTasks(asList(
                                pipeline1GraphCheck.withStatus(SUCCESS),
                                pipeline1Step1Check
                                        .withStatus(RUNNING)
                                        .withMessage("Validating step Pipeline 1 Step 1"),
                                pipeline1Step2Check
                                        .withStatus(PENDING))))
                .expectNext(pipeline1Task
                        .withStatus(RUNNING)
                        .withTasks(asList(
                                pipeline1GraphCheck.withStatus(SUCCESS),
                                pipeline1Step1Check
                                        .withStatus(SUCCESS)
                                        .withMessage("Validated step Pipeline 1 Step 1"),
                                pipeline1Step2Check
                                        .withStatus(PENDING))))
                .expectNext(pipeline1Task
                        .withStatus(RUNNING)
                        .withTasks(asList(
                                pipeline1GraphCheck.withStatus(SUCCESS),
                                pipeline1Step1Check
                                        .withStatus(SUCCESS)
                                        .withMessage("Validated step Pipeline 1 Step 1"),
                                pipeline1Step2Check
                                        .withStatus(RUNNING)
                                        .withMessage("Validating step Pipeline 1 Step 2"))))
                .expectNext(pipeline1Task
                        .withStatus(FAILED)
                        .withTasks(asList(
                                pipeline1GraphCheck.withStatus(SUCCESS),
                                pipeline1Step1Check
                                        .withStatus(SUCCESS)
                                        .withMessage("Validated step Pipeline 1 Step 1"),
                                pipeline1Step2Check
                                        .withStatus(FAILED)
                                        .withMessage("Failed to validate step Pipeline 1 Step 2"))))
                .expectNext(pipeline2Task
                        .withStatus(PENDING)
                        .withTasks(asList(
                                pipeline2GraphCheck.withStatus(PENDING),
                                pipeline2Step1Check.withStatus(PENDING))))
                .expectNext(pipeline2Task
                        .withStatus(PENDING)
                        .withTasks(asList(
                                pipeline2GraphCheck.withStatus(SUCCESS),
                                pipeline2Step1Check.withStatus(PENDING)
                        )))
                .expectNext(pipeline2Task
                        .withStatus(RUNNING)
                        .withTasks(asList(
                                pipeline2GraphCheck.withStatus(SUCCESS),
                                pipeline2Step1Check
                                        .withStatus(RUNNING)
                                        .withMessage("Validating step Pipeline 2 Step 1")
                        )))
                .expectNext(pipeline2Task
                        .withStatus(SUCCESS)
                        .withTasks(asList(
                                pipeline2GraphCheck.withStatus(SUCCESS),
                                pipeline2Step1Check
                                        .withStatus(SUCCESS)
                                        .withMessage("Validated step Pipeline 2 Step 1")
                        )))
                .expectComplete()
                .verify();

        verify(stepSelectsValidator, times(3)).validate(any());
    }
}
