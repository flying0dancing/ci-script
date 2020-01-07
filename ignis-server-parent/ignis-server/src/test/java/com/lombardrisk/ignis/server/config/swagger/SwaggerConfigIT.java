package com.lombardrisk.ignis.server.config.swagger;

import com.lombardrisk.ignis.server.config.IntegrationTestConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static com.lombardrisk.ignis.test.config.AdminUser.BASIC_AUTH;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@IntegrationTestConfig
public class SwaggerConfigIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void getSwaggerDocs_Unauthorized_Returns401() throws Exception {
        mockMvc.perform(
                get("/v2/api-docs")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void getSwaggerDocs_Authorized_ReturnsDocumentedPaths() throws Exception {
        mockMvc.perform(
                get("/v2/api-docs")
                        .with(BASIC_AUTH)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths[\"/api/v1/jobs\"].get").exists())
                .andExpect(jsonPath("$.paths[\"/api/v1/jobs\"].post").exists())
                .andExpect(jsonPath("$.paths[\"/api/v1/jobs/{id}\"].get").exists())
                .andExpect(jsonPath("$.paths[\"/api/v1/jobs/{id}/stop\"].put").exists())
                .andExpect(jsonPath("$.paths[\"/api/v1/stagingItems\"].get").exists())
                .andExpect(jsonPath("$.paths[\"/api/v1/stagingItems/{id}\"].get").exists())
                .andExpect(jsonPath("$.paths[\"/api/v1/stagingItems/{id}/validationError\"].get").exists());
    }
}