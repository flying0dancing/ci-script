package com.lombardrisk.ignis.design.server.controller;

import com.lombardrisk.ignis.client.design.pipeline.PipelineConnectedGraphs;
import com.lombardrisk.ignis.client.design.pipeline.PipelineDisplayErrorView;
import com.lombardrisk.ignis.client.design.pipeline.PipelineEdgeView;
import com.lombardrisk.ignis.data.common.error.ErrorResponse;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.design.server.DesignStudioTestConfig;
import com.lombardrisk.ignis.design.server.fixtures.Design.Populated;
import com.lombardrisk.ignis.design.server.pipeline.PipelineService;
import io.vavr.control.Validation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static com.google.common.collect.Sets.newLinkedHashSet;
import static com.lombardrisk.ignis.common.json.MapperWrapper.MAPPER;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@DesignStudioTestConfig
public class PipelineControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PipelineService pipelineService;

    @Test
    public void getPipelines_ReturnsOkResponseWithEntity() throws Exception {
        when(pipelineService.findAllPipelines())
                .thenReturn(singletonList(Populated.pipelineView()
                        .id(1234L)
                        .name("my pipeline")
                        .productId(42L)
                        .steps(singleton(Populated.pipelineMapStepView().id(345L).build()))
                        .build()));

        mockMvc.perform(
                get("/api/v1/pipelines"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1234))
                .andExpect(jsonPath("$[0].name").value("my pipeline"))
                .andExpect(jsonPath("$[0].productId").value(42L))
                .andExpect(jsonPath("$[0].steps[0].id").value(345L));
    }

    @Test
    public void getPipeline_Exists_ReturnsOkResponseWithEntity() throws Exception {
        when(pipelineService.findOne(anyLong()))
                .thenReturn(Validation.valid(Populated.pipelineView()
                        .id(1234L)
                        .name("my pipeline")
                        .productId(42L)
                        .steps(singleton(Populated.pipelineMapStepView().id(345L).build()))
                        .build()));

        mockMvc.perform(
                get("/api/v1/pipelines/123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1234))
                .andExpect(jsonPath("$.name").value("my pipeline"))
                .andExpect(jsonPath("$.productId").value(42L))
                .andExpect(jsonPath("$.steps[0].id").value(345L));
    }

    @Test
    public void getPipeline_DoesNotExist_ReturnsBadRequestError() throws Exception {
        when(pipelineService.findOne(anyLong()))
                .thenReturn(Validation.invalid(CRUDFailure.notFoundIds("Pipeline", 123L)));

        mockMvc.perform(
                get("/api/v1/pipelines/123"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].errorCode").value("NOT_FOUND"))
                .andExpect(jsonPath("$[0].errorMessage").value("Could not find Pipeline for ids [123]"));
    }

    @Test
    public void savePipeline_ReturnsOkResponseWithEntity() throws Exception {
        when(pipelineService.saveNewPipeline(any()))
                .thenReturn(Validation.valid(Populated.pipelineView()
                        .id(1234L)
                        .name("my pipeline")
                        .productId(534L)
                        .steps(singleton(Populated.pipelineMapStepView().id(345L).build()))
                        .build()));

        mockMvc.perform(
                post("/api/v1/pipelines")
                        .contentType(APPLICATION_JSON)
                        .content(MAPPER.writeValueAsString(Populated.createPipelineRequest().build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1234))
                .andExpect(jsonPath("$.name").value("my pipeline"))
                .andExpect(jsonPath("$.productId").value(534L))
                .andExpect(jsonPath("$.steps[0].id").value(345L));
    }

    @Test
    public void savePipeline_ServiceReturnsFailure_ReturnsBadRequest() throws Exception {
        when(pipelineService.saveNewPipeline(any()))
                .thenReturn(Validation.invalid(ErrorResponse.valueOf("you shall not pass", "stop")));

        mockMvc.perform(
                post("/api/v1/pipelines")
                        .contentType(APPLICATION_JSON)
                        .content(MAPPER.writeValueAsString(Populated.createPipelineRequest().build())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].errorCode").value("stop"))
                .andExpect(jsonPath("$[0].errorMessage").value("you shall not pass"));
    }

    @Test
    public void updatePipeline_ReturnsOkResponseWithEntity() throws Exception {
        when(pipelineService.updatePipeline(any(), any()))
                .thenReturn(Validation.valid(Populated.pipelineView()
                        .id(1234L)
                        .name("my pipeline")
                        .productId(534L)
                        .steps(singleton(Populated.pipelineMapStepView().id(345L).build()))
                        .build()));

        mockMvc.perform(
                patch("/api/v1/pipelines/12")
                        .contentType(APPLICATION_JSON)
                        .content(MAPPER.writeValueAsString(Populated.createPipelineRequest().build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1234))
                .andExpect(jsonPath("$.name").value("my pipeline"))
                .andExpect(jsonPath("$.productId").value(534L))
                .andExpect(jsonPath("$.steps[0].id").value(345L));
    }

    @Test
    public void getPipelineEdgeData_ReturnsOkResponseWithEntity() throws Exception {
        when(pipelineService.calculatePipelineEdgeData(any()))
                .thenReturn(Validation.valid(new PipelineConnectedGraphs(singletonList(singleton(
                        new PipelineEdgeView(
                                1L,
                                2L,
                                Populated.pipelineMapStepView()
                                        .id(1001L)
                                        .name("Map Step")
                                        .description("Map Step that does mapping stuff")
                                        .build()))))));

        mockMvc.perform(
                get("/api/v1/pipelines/12/edges")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.connectedSets[0][0].source").value(1))
                .andExpect(jsonPath("$.connectedSets[0][0].target").value(2))
                .andExpect(jsonPath("$.connectedSets[0][0].pipelineStep.id").value(1001))
                .andExpect(jsonPath("$.connectedSets[0][0].pipelineStep.name").value("Map Step"))
                .andExpect(jsonPath("$.connectedSets[0][0].pipelineStep.description").value(
                        "Map Step that does mapping stuff"));
    }

    @Test
    public void getPipelineEdgeData_PipelineNotFound_ReturnsBadRequestWithEntityError() throws Exception {
        when(pipelineService.calculatePipelineEdgeData(any()))
                .thenReturn(Validation.invalid(
                        PipelineDisplayErrorView.notFound("NOT_FOUND", "Could not find Pipeline for ids [12]")));

        mockMvc.perform(
                get("/api/v1/pipelines/12/edges")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("NOT_FOUND"))
                .andExpect(jsonPath("$.errorMessage").value("Could not find Pipeline for ids [12]"));
    }

    @Test
    public void getPipelineEdgeData_PipelineHasCycles_ReturnsBadRequestWithEntityError() throws Exception {
        when(pipelineService.calculatePipelineEdgeData(any()))
                .thenReturn(Validation.invalid(
                        PipelineDisplayErrorView.hasCyclesError(newLinkedHashSet(Arrays.asList(1L, 2L)), 20L)));

        mockMvc.perform(
                get("/api/v1/pipelines/12/edges")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.cycleError.parentChain[0]").value(1))
                .andExpect(jsonPath("$.cycleError.parentChain[1]").value(2))
                .andExpect(jsonPath("$.cycleError.cyclicalStep").value(20L))
                .andExpect(jsonPath("$.errorCode").value("PIPELINE_GRAPH_CYCLICAL"))
                .andExpect(jsonPath("$.errorMessage")
                        .value("Pipeline graph cannot contain cycles and could not be rendered"));
    }

    @Test
    public void deletePipeline_ReturnsOkResponseWithEntity() throws Exception {
        when(pipelineService.deleteById(anyLong()))
                .thenReturn(Validation.valid(Populated.pipeline().id(2342L).build()));

        mockMvc.perform(
                delete("/api/v1/pipelines/2342"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("2342"));
    }
}
