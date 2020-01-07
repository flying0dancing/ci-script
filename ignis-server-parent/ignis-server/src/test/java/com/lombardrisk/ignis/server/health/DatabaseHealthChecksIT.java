package com.lombardrisk.ignis.server.health;

import com.lombardrisk.ignis.server.config.IntegrationTestConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static com.lombardrisk.ignis.test.config.AdminUser.BASIC_AUTH;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@IntegrationTestConfig
public class DatabaseHealthChecksIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void health_DataSourcesValid_ReturnsJsonHealthStatus() throws Exception {
        mockMvc.perform(
                get("/actuator/health")
                        .with(BASIC_AUTH)
                        .contentType(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.details.dbHealthCheck.status").value("UP"))
                .andExpect(jsonPath("$.details.dbHealthCheck.details.database").exists())
                .andExpect(jsonPath("$.details.phoenixHealthCheck.status").value("UP"))
                .andExpect(jsonPath("$.details.phoenixHealthCheck.details.database").value("H2"));
    }
}