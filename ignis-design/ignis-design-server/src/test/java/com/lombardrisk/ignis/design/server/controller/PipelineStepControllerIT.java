package com.lombardrisk.ignis.design.server.controller;

import com.lombardrisk.ignis.client.design.pipeline.SyntaxCheckRequest;
import com.lombardrisk.ignis.client.design.pipeline.aggregation.PipelineAggregationStepRequest;
import com.lombardrisk.ignis.client.design.pipeline.error.SelectResult;
import com.lombardrisk.ignis.client.design.pipeline.error.StepExecutionResult;
import com.lombardrisk.ignis.client.design.pipeline.error.UpdatePipelineError;
import com.lombardrisk.ignis.client.design.pipeline.join.PipelineJoinStepRequest;
import com.lombardrisk.ignis.client.design.pipeline.select.SelectRequest;
import com.lombardrisk.ignis.client.design.pipeline.select.SelectView;
import com.lombardrisk.ignis.client.design.pipeline.union.PipelineUnionStepRequest;
import com.lombardrisk.ignis.data.common.error.ErrorResponse;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.design.server.DesignStudioTestConfig;
import com.lombardrisk.ignis.design.server.fixtures.Design;
import com.lombardrisk.ignis.design.server.fixtures.Design.Populated;
import com.lombardrisk.ignis.design.server.pipeline.PipelineStepService;
import io.vavr.control.Validation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static com.lombardrisk.ignis.common.json.MapperWrapper.MAPPER;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@DesignStudioTestConfig
public class PipelineStepControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PipelineStepService pipelineStepService;

    @Before
    public void setUp() {
        when(pipelineStepService.savePipelineStep(any(), any()))
                .thenReturn(Validation.valid(Populated.pipelineMapStepView().build()));

        when(pipelineStepService.updatePipelineStep(any(), any(), any()))
                .thenReturn(Validation.valid(Populated.pipelineMapStepView().build()));
    }

    @Test
    public void savePipelineStep_ReturnsOkResponseWithEntity() throws Exception {
        when(pipelineStepService.savePipelineStep(any(), any()))
                .thenReturn(Validation.valid(Populated.pipelineMapStepView()
                        .id(3453L)
                        .name("my pipeline step")
                        .description("my pipeline step description")
                        .schemaInId(12L)
                        .schemaOutId(83L)
                        .selects(singletonList(SelectView.builder()
                                .outputFieldId(153415L)
                                .select("my select")
                                .build()))
                        .build()));

        mockMvc.perform(
                post("/api/v1/pipelines/234/steps")
                        .contentType(APPLICATION_JSON)
                        .content(MAPPER.writeValueAsString(Populated.pipelineMapStepRequest().build())))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3453L))
                .andExpect(jsonPath("$.name").value("my pipeline step"))
                .andExpect(jsonPath("$.description").value("my pipeline step description"))
                .andExpect(jsonPath("$.schemaInId").value(12L))
                .andExpect(jsonPath("$.schemaOutId").value(83L))
                .andExpect(jsonPath("$.selects[0].select").value("my select"))
                .andExpect(jsonPath("$.selects[0].outputFieldId").value(153415L))
                .andExpect(jsonPath("$.type").value("MAP"));
    }

    @Test
    public void savePipelineStep_InvalidStepConfig_ReturnsBadRequest() throws Exception {
        when(pipelineStepService.savePipelineStep(any(), any()))
                .thenReturn(Validation.invalid(UpdatePipelineError.builder()
                        .pipelineNotFoundError(CRUDFailure.notFoundIds("Pipeline", 234L))
                        .stepExecutionResult(StepExecutionResult.builder()
                                .selectsExecutionErrors(StepExecutionResult.SelectsExecutionErrors.builder()
                                        .individualErrors(singletonList(
                                                SelectResult.error(
                                                        "TEST", 1L, null,
                                                        singletonList(
                                                                ErrorResponse.valueOf("Ooops", "OP")))))
                                        .build())
                                .build())
                        .build()));

        mockMvc.perform(
                post("/api/v1/pipelines/234/steps")
                        .contentType(APPLICATION_JSON)
                        .content(MAPPER.writeValueAsString(Populated.pipelineMapStepRequest().build())))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.pipelineNotFoundError.errorCode").value("NOT_FOUND"))
                .andExpect(jsonPath("$.pipelineNotFoundError.errorMessage").value(
                        "Could not find Pipeline for ids [234]"))
                .andExpect(jsonPath(
                        "$.stepExecutionResult.selectsExecutionErrors.individualErrors[0].outputFieldName")
                        .value("TEST"))
                .andExpect(jsonPath(
                        "$.stepExecutionResult.selectsExecutionErrors.individualErrors[0].outputFieldId")
                        .value(1))
                .andExpect(jsonPath(
                        "$.stepExecutionResult.selectsExecutionErrors.individualErrors[0].valid")
                        .value(false))
                .andExpect(jsonPath(
                        "$.stepExecutionResult.selectsExecutionErrors.successful")
                        .value(false))
                .andExpect(jsonPath(
                        "$.stepExecutionResult.selectsExecutionErrors.successful")
                        .value(false));
    }

    @Test
    public void savePipelineStep_InvalidUnionStep_ReturnsErrorResponse() throws Exception {
        mockMvc.perform(
                post("/api/v1/pipelines/234/steps")
                        .contentType(APPLICATION_JSON)
                        .content(MAPPER.writeValueAsString(Populated.pipelineUnionStepRequest()
                                .unionSchemas(singletonMap(1L, null))
                                .build())))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].errorMessage").value("size must be between 2 and 2147483647"))
                .andExpect(jsonPath("$[0].errorCode").value("unionSchemas"));
    }

    @Test
    public void savePipelineStep_UnionStep_CallsSaveWithRequest() throws Exception {
        PipelineUnionStepRequest stepRequest = Populated.pipelineUnionStepRequest().build();

        mockMvc.perform(
                post("/api/v1/pipelines/234/steps")
                        .contentType(APPLICATION_JSON)
                        .content(MAPPER.writeValueAsString(stepRequest)))
                .andExpect(status().isOk());

        verify(pipelineStepService).savePipelineStep(234L, stepRequest);
    }

    @Test
    public void savePipelineStep_AggregationStep_CallsSaveWithRequest() throws Exception {
        PipelineAggregationStepRequest stepRequest = Populated.pipelineAggregationStepRequest().build();

        mockMvc.perform(
                post("/api/v1/pipelines/234/steps")
                        .contentType(APPLICATION_JSON)
                        .content(MAPPER.writeValueAsString(stepRequest)))
                .andExpect(status().isOk());

        verify(pipelineStepService).savePipelineStep(234L, stepRequest);
    }

    @Test
    public void savePipelineStep_JoinStep_CallsSaveWithRequest() throws Exception {
        PipelineJoinStepRequest stepRequest = Populated.pipelineJoinStepRequest()
                .selects(singleton(SelectRequest.builder().select("column").outputFieldId(3245324L).build()))
                .build();

        mockMvc.perform(
                post("/api/v1/pipelines/234/steps")
                        .contentType(APPLICATION_JSON)
                        .content(MAPPER.writeValueAsString(stepRequest)))
                .andExpect(status().isOk());

        verify(pipelineStepService).savePipelineStep(234L, stepRequest);
    }

    @Test
    public void updatePipelineStep_ReturnsOkResponseWithEntity() throws Exception {
        when(pipelineStepService.updatePipelineStep(any(), any(), any()))
                .thenReturn(Validation.valid(Populated.pipelineMapStepView()
                        .id(3453L)
                        .name("my pipeline step")
                        .description("my pipeline step description")
                        .schemaInId(12L)
                        .schemaOutId(83L)
                        .selects(singletonList(SelectView.builder()
                                .outputFieldId(153415L)
                                .select("my select")
                                .build()))
                        .build()));

        mockMvc.perform(
                put("/api/v1/pipelines/234/steps/567")
                        .contentType(APPLICATION_JSON)
                        .content(MAPPER.writeValueAsString(Populated.pipelineMapStepRequest().build())))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3453L))
                .andExpect(jsonPath("$.name").value("my pipeline step"))
                .andExpect(jsonPath("$.description").value("my pipeline step description"))
                .andExpect(jsonPath("$.schemaInId").value(12L))
                .andExpect(jsonPath("$.schemaOutId").value(83L))
                .andExpect(jsonPath("$.selects[0].select").value("my select"))
                .andExpect(jsonPath("$.selects[0].outputFieldId").value(153415L))
                .andExpect(jsonPath("$.type").value("MAP"));
    }

    @Test
    public void updatePipelineStep_AggregationStep_CallsSaveWithRequest() throws Exception {
        PipelineAggregationStepRequest stepRequest = Populated.pipelineAggregationStepRequest().build();

        mockMvc.perform(
                put("/api/v1/pipelines/234/steps/567")
                        .contentType(APPLICATION_JSON)
                        .content(MAPPER.writeValueAsString(stepRequest)))
                .andExpect(status().isOk());

        verify(pipelineStepService).updatePipelineStep(234L, 567L, stepRequest);
    }

    @Test
    public void updatePipelineStep_JoinStep_CallsSaveWithRequest() throws Exception {
        PipelineJoinStepRequest stepRequest = Populated.pipelineJoinStepRequest()
                .selects(singleton(SelectRequest.builder().select("column").outputFieldId(3245324L).build()))
                .build();

        mockMvc.perform(
                put("/api/v1/pipelines/234/steps/567")
                        .contentType(APPLICATION_JSON)
                        .content(MAPPER.writeValueAsString(stepRequest)))
                .andExpect(status().isOk());

        verify(pipelineStepService).updatePipelineStep(234L, 567L, stepRequest);
    }

    @Test
    public void deletePipelineStep_ReturnsOkResponseWithEntity() throws Exception {
        when(pipelineStepService.deletePipelineStep(anyLong()))
                .thenReturn(Validation.valid(Design.Populated.pipelineMapStep().id(21L).build()));

        mockMvc.perform(
                delete("/api/v1/pipelines/234/steps/21"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("21"));
    }

    @Test
    public void checkSyntax_ReturnsOkResponseWithEntity() throws Exception {
        when(pipelineStepService.checkSyntax(any()))
                .thenReturn(Validation.valid(SelectResult.error("TEST", 1L, null,
                        singletonList(ErrorResponse.valueOf("Something went wrong", "Oops")))));

        SyntaxCheckRequest syntaxCheckRequest = Populated.syntaxCheckRequest().build();
        mockMvc.perform(
                post("/api/v1/pipelines/syntax")
                        .contentType(APPLICATION_JSON)
                        .content(MAPPER.writeValueAsString(syntaxCheckRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.outputFieldName").value("TEST"))
                .andExpect(jsonPath("$.outputFieldId").value(1))
                .andExpect(jsonPath("$.unionSchemaId").isEmpty())
                .andExpect(jsonPath("$.errors[0].errorMessage").value("Something went wrong"))
                .andExpect(jsonPath("$.errors[0].errorCode").value("Oops"));

        verify(pipelineStepService).checkSyntax(syntaxCheckRequest);
    }

    @Test
    public void checkSyntax_ErrorInExecution_ReturnsBadRequest() throws Exception {
        when(pipelineStepService.checkSyntax(any()))
                .thenReturn(Validation.invalid(
                        singletonList(ErrorResponse.valueOf("Something went wrong", "Oops"))));

        mockMvc.perform(
                post("/api/v1/pipelines/syntax")
                        .contentType(APPLICATION_JSON)
                        .content(MAPPER.writeValueAsString(Populated.syntaxCheckRequest().build())))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].errorMessage").value("Something went wrong"))
                .andExpect(jsonPath("$[0].errorCode").value("Oops"));
    }
}
