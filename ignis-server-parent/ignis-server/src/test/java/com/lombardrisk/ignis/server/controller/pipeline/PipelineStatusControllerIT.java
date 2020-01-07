package com.lombardrisk.ignis.server.controller.pipeline;

import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.server.config.IntegrationTestConfig;
import com.lombardrisk.ignis.server.dataset.fixture.DatasetPopulated;
import com.lombardrisk.ignis.server.dataset.pipeline.invocation.PipelineInvocationService;
import io.vavr.control.Validation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static com.lombardrisk.ignis.test.config.AdminUser.BASIC_AUTH;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@IntegrationTestConfig
public class PipelineStatusControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PipelineInvocationService pipelineInvocationService;

    @Test
    public void updatePipelineStepInvocationStatus_PipelineWithStepInvocationFound_UpdatesStatus() throws Exception {
        String requestBody = "{\"pipelineStepInvocationId\":666, \"pipelineStepStatus\":\"SUCCESS\"}";

        when(pipelineInvocationService.updateStepInvocationStatus(any(), any(), any()))
                .thenReturn(Validation.valid(DatasetPopulated.pipelineInvocation()
                        .id(12345L)
                        .build()));

        mockMvc.perform(
                post("/api/internal/pipelineInvocations/12345")
                        .with(BASIC_AUTH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", equalTo(12345)));
    }

    @Test
    public void updatePipelineStepInvocationStatus_CallsPipelineServiceWithArguments() throws Exception {
        String requestBody = "{\"pipelineStepInvocationId\":666, \"pipelineStepStatus\":\"FAILED\"}";

        when(pipelineInvocationService.updateStepInvocationStatus(any(), any(), any()))
                .thenReturn(Validation.valid(DatasetPopulated.pipelineInvocation()
                        .id(12345L)
                        .build()));

        mockMvc.perform(
                post("/api/internal/pipelineInvocations/12345")
                        .with(BASIC_AUTH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk());

        verify(pipelineInvocationService).updateStepInvocationStatus(12345L, 666L, "FAILED");
    }

    @Test
    public void updatePipelineStepInvocationStatus_InvalidRequest_ReturnsBadRequest() throws Exception {
        String requestBody = "{\"pipelineStepInvocationId\":666, \"pipelineStepStatus\":\"SUCCESS\"}";

        when(pipelineInvocationService.updateStepInvocationStatus(any(), any(), any()))
                .thenReturn(Validation.invalid(CRUDFailure.cannotFind("pipeline invocation").asFailure()));

        mockMvc.perform(
                post("/api/internal/pipelineInvocations/12345")
                        .with(BASIC_AUTH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].errorCode", equalTo("NOT_FOUND")));
    }

    @Test
    public void updatePipelineStepInvocationStatus_InvalidStatusType_ReturnsBadRequest() throws Exception {
        String requestBody = "{\"pipelineStepInvocationId\":666, \"pipelineStepStatus\":\"invalid_step_status\"}";

        mockMvc.perform(
                post("/api/internal/pipelineInvocations/12345")
                        .with(BASIC_AUTH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}
