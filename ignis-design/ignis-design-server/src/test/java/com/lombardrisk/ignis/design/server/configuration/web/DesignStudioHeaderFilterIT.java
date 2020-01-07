package com.lombardrisk.ignis.design.server.configuration.web;

import com.lombardrisk.ignis.design.server.DesignStudioTestConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@RunWith(SpringRunner.class)
@DesignStudioTestConfig
public class DesignStudioHeaderFilterIT {

    @Autowired
    protected WebApplicationContext context;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(new DesignStudioHeaderFilter()).build();
    }

    @Test
    public void filter_ReturnsDesignStudioHeaders() throws Exception {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("design-studio-quiet", "doesn't matter what this is");
        httpHeaders.add("design-studio-other", "this wont be returned");

        MvcResult mvcResult = mockMvc.perform(
                get("/api/v1/pipelines").headers(httpHeaders))
                .andDo(print())
                .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();

        assertThat(response.getHeader("design-studio-quiet"))
                .isEqualTo("true");
        assertThat(response.getHeader("design-studio-other"))
                .isNull();
    }
}