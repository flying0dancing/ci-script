package com.lombardrisk.ignis.server.controller.pipeline;

import com.lombardrisk.ignis.client.external.fixture.ExternalClient;
import com.lombardrisk.ignis.client.external.pipeline.export.TransformationType;
import com.lombardrisk.ignis.client.external.pipeline.view.PipelineEdgeView;
import com.lombardrisk.ignis.client.external.pipeline.view.PipelineStepInvocationDatasetView;
import com.lombardrisk.ignis.client.external.pipeline.view.SchemaDetailsView;
import com.lombardrisk.ignis.data.common.error.ErrorResponse;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.server.config.IntegrationTestConfig;
import com.lombardrisk.ignis.server.dataset.pipeline.invocation.PipelineInvocationService;
import com.lombardrisk.ignis.server.product.fixture.ProductPopulated;
import com.lombardrisk.ignis.server.product.pipeline.PipelineJpaRepository;
import com.lombardrisk.ignis.server.product.pipeline.PipelineService;
import com.lombardrisk.ignis.server.product.pipeline.model.Pipeline;
import io.vavr.control.Validation;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Sets.newLinkedHashSet;
import static com.lombardrisk.ignis.test.config.AdminUser.BASIC_AUTH;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(Enclosed.class)
public class PipelineControllerIT {

    @RunWith(SpringRunner.class)
    @IntegrationTestConfig
    public static class GetPipelines {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private PipelineJpaRepository pipelineDetailsRepository;

        @Test
        public void getPipelines_ReturnsOkResponseWithEntity() throws Exception {
            Pipeline pipelineDetails = ProductPopulated.pipeline()
                    .id(1234L)
                    .name("name")
                    .steps(newLinkedHashSet(singletonList(
                            ProductPopulated.mapPipelineStep()
                                    .name("step1")
                                    .description("Step One")
                                    .selects(newLinkedHashSet(asList(
                                            ProductPopulated.select().select("ATOMIC").outputFieldId(321412L).build(),
                                            ProductPopulated.select()
                                                    .select("BOMBASTIC")
                                                    .outputFieldId(2345325L)
                                                    .build())))
                                    .schemaIn(ProductPopulated.schemaDetails()
                                            .id(1L)
                                            .displayName("table 1")
                                            .physicalTableName("tbl1")
                                            .version(1)
                                            .build())
                                    .schemaOut(ProductPopulated.schemaDetails()
                                            .id(2L)
                                            .displayName("table 2")
                                            .physicalTableName("tbl2")
                                            .version(2)
                                            .build())
                                    .build())))
                    .build();

            when(pipelineDetailsRepository.findAll())
                    .thenReturn(singletonList(pipelineDetails));

            mockMvc.perform(
                    get("/api/v1/pipelines")
                            .with(BASIC_AUTH))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id", equalTo(1234)))
                    .andExpect(jsonPath("$[0].name", equalTo("name")))
                    .andExpect(jsonPath("$[0].steps[0].name", equalTo("step1")))
                    .andExpect(jsonPath("$[0].steps[0].description", equalTo("Step One")))
                    .andExpect(jsonPath("$[0].steps[0].type", equalTo("MAP")))
                    .andExpect(jsonPath("$[0].steps[0].schemaIn.id", equalTo(1)))
                    .andExpect(jsonPath("$[0].steps[0].schemaIn.displayName", equalTo("table 1")))
                    .andExpect(jsonPath("$[0].steps[0].schemaIn.physicalTableName", equalTo("tbl1")))
                    .andExpect(jsonPath("$[0].steps[0].schemaIn.version", equalTo(1)))
                    .andExpect(jsonPath("$[0].steps[0].schemaOut.id", equalTo(2)))
                    .andExpect(jsonPath("$[0].steps[0].schemaOut.displayName", equalTo("table 2")))
                    .andExpect(jsonPath("$[0].steps[0].schemaOut.physicalTableName", equalTo("tbl2")))
                    .andExpect(jsonPath("$[0].steps[0].schemaOut.version", equalTo(2)));
        }
    }

    @RunWith(SpringRunner.class)
    @IntegrationTestConfig
    public static class GetRequiredSchemas {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private PipelineService pipelineService;

        @Test
        public void getRequiredSchemas_WithErrorResponses_ReturnsBadRequest() throws Exception {
            when(pipelineService.getRequiredSchemas(anyLong()))
                    .thenReturn(Validation.invalid(
                            singletonList(ErrorResponse.valueOf("some error message", "some error code"))));

            mockMvc.perform(
                    get("/api/v1/pipelines/45253325/schemas")
                            .with(BASIC_AUTH))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$[0].errorCode", equalTo("some error code")))
                    .andExpect(jsonPath("$[0].errorMessage", equalTo("some error message")));
        }

        @Test
        public void getRequiredSchemas_WithSchemaDetails_ReturnsOkResponse() throws Exception {
            when(pipelineService.getRequiredSchemas(anyLong()))
                    .thenReturn(Validation.valid(newHashSet(
                            SchemaDetailsView.builder()
                                    .id(346L).version(11).displayName("display name 1").physicalTableName("phys1")
                                    .build())));

            mockMvc.perform(
                    get("/api/v1/pipelines/45253325/schemas")
                            .with(BASIC_AUTH))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id", equalTo(346)))
                    .andExpect(jsonPath("$[0].version", equalTo(11)))
                    .andExpect(jsonPath("$[0].displayName", equalTo("display name 1")))
                    .andExpect(jsonPath("$[0].physicalTableName", equalTo("phys1")));
        }

        @Test
        public void getRequiredSchemas_CallsPipelineServiceWithPipelineId() throws Exception {
            when(pipelineService.getRequiredSchemas(anyLong()))
                    .thenReturn(Validation.valid(newHashSet(
                            SchemaDetailsView.builder()
                                    .id(346L).version(11).displayName("display name 1").physicalTableName("phys1")
                                    .build())));

            mockMvc.perform(
                    get("/api/v1/pipelines/45253325/schemas")
                            .with(BASIC_AUTH))
                    .andDo(print())
                    .andExpect(status().isOk());

            verify(pipelineService).getRequiredSchemas(45253325L);
        }
    }

    @RunWith(SpringRunner.class)
    @IntegrationTestConfig
    public static class GetEdges {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private PipelineService pipelineService;

        @Test
        public void getEdges_WithErrorResponses_ReturnsBadRequest() throws Exception {
            when(pipelineService.findPipelineEdgeData(anyLong()))
                    .thenReturn(Validation.invalid(CRUDFailure.notFoundIds("Pipeline", 1L)));

            mockMvc.perform(
                    get("/api/v1/pipelines/45253325/edges")
                            .with(BASIC_AUTH))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$[0].errorCode", equalTo("NOT_FOUND")))
                    .andExpect(jsonPath("$[0].errorMessage", equalTo("Could not find Pipeline for ids [1]")));
        }

        @Test
        public void getEdges_PipelineExists_ReturnsOkResponseEdgeData() throws Exception {
            when(pipelineService.findPipelineEdgeData(anyLong()))
                    .thenReturn(Validation.valid(singletonList(
                            new PipelineEdgeView(2L, 3L,
                                    ExternalClient.Populated.pipelineStepView()
                                            .name("Map Step")
                                            .build())
                    )));

            mockMvc.perform(
                    get("/api/v1/pipelines/45253325/edges")
                            .with(BASIC_AUTH))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andExpect(jsonPath("$[0].source", equalTo(2)))
                    .andExpect(jsonPath("$[0].target", equalTo(3)))
                    .andExpect(jsonPath("$[0].pipelineStep.name", equalTo("Map Step")));
        }
    }

    @RunWith(SpringRunner.class)
    @IntegrationTestConfig
    public static class GetPipelineInvocations {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private PipelineInvocationService pipelineInvocationService;

        @Test
        public void getPipelineInvocations_ReturnsInvocationsWithMetadata() throws Exception {
            when(pipelineInvocationService.findAllInvocations())
                    .thenReturn(singletonList(ExternalClient.Populated.pipelineInvocationView()
                            .id(1L)
                            .name("Job Name")
                            .createdTime(ZonedDateTime.of(
                                    LocalDateTime.of(2001, 1, 1, 1, 1, 1),
                                    ZoneId.of("UTC")))
                            .pipelineId(100L)
                            .referenceDate(LocalDate.of(2001, 1, 1))
                            .entityCode("entity")
                            .build()));

            mockMvc.perform(
                    get("/api/v1/pipelineInvocations")
                            .with(BASIC_AUTH))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id", equalTo(1)))
                    .andExpect(jsonPath("$[0].name", equalTo("Job Name")))
                    .andExpect(jsonPath("$[0].createdTime", equalTo("2001-01-01T01:01:01+0000")))
                    .andExpect(jsonPath("$[0].pipelineId", equalTo(100)))
                    .andExpect(jsonPath("$[0].referenceDate", equalTo("2001-01-01")))
                    .andExpect(jsonPath("$[0].entityCode", equalTo("entity")));
        }

        @Test
        public void getPipelineInvocations_ReturnsInvocationsWithStepInvocations() throws Exception {
            when(pipelineInvocationService.findAllInvocations())
                    .thenReturn(singletonList(ExternalClient.Populated.pipelineInvocationView()
                            .invocationSteps(singletonList(ExternalClient.Populated.pipelineStepInvocationView()
                                    .id(200L)
                                    .datasetsIn(singleton(PipelineStepInvocationDatasetView.builder()
                                            .datasetId(12L)
                                            .datasetRunKey(2L)
                                            .build()))
                                    .inputPipelineStepIds(singleton(201L))
                                    .build()))
                            .build()));

            mockMvc.perform(
                    get("/api/v1/pipelineInvocations")
                            .with(BASIC_AUTH))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].invocationSteps[0].id", equalTo(200)))
                    .andExpect(jsonPath("$[0].invocationSteps[0].datasetsIn[0].datasetId", equalTo(12)))
                    .andExpect(jsonPath("$[0].invocationSteps[0].datasetsIn[0].datasetRunKey", equalTo(2)))
                    .andExpect(jsonPath("$[0].invocationSteps[0].inputPipelineStepIds[0]", equalTo(201)));
        }

        @Test
        public void getPipelineInvocations_ReturnsInvocationsWithStepInvocationsAndStepDetails() throws Exception {
            when(pipelineInvocationService.findAllInvocations())
                    .thenReturn(singletonList(ExternalClient.Populated.pipelineInvocationView()
                            .invocationSteps(singletonList(ExternalClient.Populated.pipelineStepInvocationView()
                                    .pipelineStep(ExternalClient.Populated.pipelineStepView()
                                            .id(92L)
                                            .name("StepName")
                                            .description("This step does stuff")
                                            .type(com.lombardrisk.ignis.client.external.pipeline.export.TransformationType.MAP)
                                            .schemaIn(ExternalClient.Populated.schemaDetailsView()
                                                    .id(1L)
                                                    .displayName("tableOne")
                                                    .physicalTableName("tbl1")
                                                    .version(1)
                                                    .build())
                                            .schemaOut(ExternalClient.Populated.schemaDetailsView()
                                                    .id(2L)
                                                    .displayName("tableTwo")
                                                    .physicalTableName("tbl2")
                                                    .version(1)
                                                    .build())
                                            .build())
                                    .build()))
                            .build()));

            mockMvc.perform(
                    get("/api/v1/pipelineInvocations")
                            .with(BASIC_AUTH))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].invocationSteps[0].pipelineStep.id", equalTo(92)))
                    .andExpect(jsonPath("$[0].invocationSteps[0].pipelineStep.name", equalTo("StepName")))
                    .andExpect(jsonPath(
                            "$[0].invocationSteps[0].pipelineStep.description",
                            equalTo("This step does stuff")))
                    .andExpect(jsonPath("$[0].invocationSteps[0].pipelineStep.type", equalTo("MAP")))
                    .andExpect(jsonPath("$[0].invocationSteps[0].pipelineStep.schemaIn.id", equalTo(1)))
                    .andExpect(jsonPath(
                            "$[0].invocationSteps[0].pipelineStep.schemaIn.displayName",
                            equalTo("tableOne")))
                    .andExpect(jsonPath(
                            "$[0].invocationSteps[0].pipelineStep.schemaIn.physicalTableName",
                            equalTo("tbl1")))
                    .andExpect(jsonPath("$[0].invocationSteps[0].pipelineStep.schemaIn.version", equalTo(1)))
                    .andExpect(jsonPath("$[0].invocationSteps[0].pipelineStep.schemaOut.id", equalTo(2)))
                    .andExpect(jsonPath(
                            "$[0].invocationSteps[0].pipelineStep.schemaOut.displayName",
                            equalTo("tableTwo")))
                    .andExpect(jsonPath(
                            "$[0].invocationSteps[0].pipelineStep.schemaOut.physicalTableName",
                            equalTo("tbl2")))
                    .andExpect(jsonPath("$[0].invocationSteps[0].pipelineStep.schemaOut.version", equalTo(1)));
        }

        @Test
        public void getPipelineInvocations_WithServiceRequestId_ReturnsInvocationByServiceRequestId() throws Exception {
            when(pipelineInvocationService.findInvocationsByServiceRequestId(anyLong()))
                    .thenReturn(singletonList(ExternalClient.Populated.pipelineInvocationView()
                            .id(123456L)
                            .name("Job Name")
                            .createdTime(ZonedDateTime.of(
                                    LocalDateTime.of(2001, 1, 1, 1, 1, 1),
                                    ZoneId.of("UTC")))
                            .pipelineId(100L)
                            .referenceDate(LocalDate.of(2001, 1, 1))
                            .entityCode("entity")
                            .invocationSteps(singletonList(
                                    ExternalClient.Populated.pipelineStepInvocationView()
                                            .id(547L)
                                            .datasetsIn(singleton(PipelineStepInvocationDatasetView.builder()
                                                    .datasetId(111L)
                                                    .datasetRunKey(11L)
                                                    .build()))
                                            .inputPipelineStepIds(newHashSet(325L, 326L))
                                            .pipelineStep(ExternalClient.Populated.pipelineStepView()
                                                    .id(92L)
                                                    .name("StepName")
                                                    .description("This step does stuff")
                                                    .type(TransformationType.MAP)
                                                    .schemaIn(ExternalClient.Populated.schemaDetailsView()
                                                            .id(1L)
                                                            .displayName("tableOne")
                                                            .physicalTableName("tbl1")
                                                            .version(1)
                                                            .build())
                                                    .schemaOut(ExternalClient.Populated.schemaDetailsView()
                                                            .id(2L)
                                                            .displayName("tableTwo")
                                                            .physicalTableName("tbl2")
                                                            .version(1)
                                                            .build())
                                                    .build())
                                            .build()))
                            .build()));

            mockMvc.perform(
                    get("/api/v1/pipelineInvocations?jobId=123456")
                            .with(BASIC_AUTH))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id", equalTo(123456)))
                    .andExpect(jsonPath("$[0].name", equalTo("Job Name")))
                    .andExpect(jsonPath("$[0].createdTime", equalTo("2001-01-01T01:01:01+0000")))
                    .andExpect(jsonPath("$[0].pipelineId", equalTo(100)))
                    .andExpect(jsonPath("$[0].referenceDate", equalTo("2001-01-01")))
                    .andExpect(jsonPath("$[0].entityCode", equalTo("entity")))
                    .andExpect(jsonPath("$[0].invocationSteps[0].pipelineStep.id", equalTo(92)))
                    .andExpect(jsonPath("$[0].invocationSteps[0].pipelineStep.name", equalTo("StepName")))
                    .andExpect(jsonPath(
                            "$[0].invocationSteps[0].pipelineStep.description",
                            equalTo("This step does stuff")))
                    .andExpect(jsonPath("$[0].invocationSteps[0].pipelineStep.type", equalTo("MAP")))
                    .andExpect(jsonPath("$[0].invocationSteps[0].pipelineStep.schemaIn.id", equalTo(1)))
                    .andExpect(jsonPath(
                            "$[0].invocationSteps[0].pipelineStep.schemaIn.displayName",
                            equalTo("tableOne")))
                    .andExpect(jsonPath(
                            "$[0].invocationSteps[0].pipelineStep.schemaIn.physicalTableName",
                            equalTo("tbl1")))
                    .andExpect(jsonPath("$[0].invocationSteps[0].pipelineStep.schemaIn.version", equalTo(1)))
                    .andExpect(jsonPath("$[0].invocationSteps[0].pipelineStep.schemaOut.id", equalTo(2)))
                    .andExpect(jsonPath(
                            "$[0].invocationSteps[0].pipelineStep.schemaOut.displayName",
                            equalTo("tableTwo")))
                    .andExpect(jsonPath(
                            "$[0].invocationSteps[0].pipelineStep.schemaOut.physicalTableName",
                            equalTo("tbl2")))
                    .andExpect(jsonPath("$[0].invocationSteps[0].pipelineStep.schemaOut.version", equalTo(1)));
        }

        @Test
        public void getPipelineInvocations_WithServiceRequestId_CallsPipelineServiceWithServiceRequestId() throws Exception {
            when(pipelineInvocationService.findInvocationsByServiceRequestId(anyLong()))
                    .thenReturn(
                            singletonList(ExternalClient.Populated.pipelineInvocationView().build()));

            mockMvc.perform(
                    get("/api/v1/pipelineInvocations?jobId=123456")
                            .with(BASIC_AUTH))
                    .andDo(print())
                    .andExpect(status().isOk());

            verify(pipelineInvocationService).findInvocationsByServiceRequestId(123456L);
            verifyNoMoreInteractions(pipelineInvocationService);
        }
    }
}
