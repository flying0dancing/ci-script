package com.lombardrisk.ignis.design.server.feedback;

import com.lombardrisk.ignis.design.server.DesignStudioTestConfig;
import com.lombardrisk.ignis.design.server.configuration.properties.FeedbackProperties;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import static com.lombardrisk.ignis.common.json.MapperWrapper.MAPPER;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@DesignStudioTestConfig
public class FeedbackControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RestTemplate restTemplate;

    @MockBean
    private FeedbackProperties feedbackProperties;

    @Test
    public void saveProductConfig_ServiceReturnsFailure_ReturnsBadRequest() throws Exception {
        when(feedbackProperties.getTeamsUrl())
                .thenReturn("https://myTeam.com/webhook/captainHook");

        FeedbackMessage feedbackMessage = FeedbackMessage.builder()
                .title("Hey you")
                .text("You product is naff!")
                .build();

        mockMvc.perform(
                post("/api/v1/feedback")
                        .contentType(APPLICATION_JSON)
                        .content(MAPPER.writeValueAsString(feedbackMessage)))
                .andExpect(status().isOk());

        verify(restTemplate).postForEntity("https://myTeam.com/webhook/captainHook", feedbackMessage, String.class);
    }
}