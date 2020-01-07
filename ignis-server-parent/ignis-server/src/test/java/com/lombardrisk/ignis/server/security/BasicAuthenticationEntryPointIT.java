package com.lombardrisk.ignis.server.security;

import com.lombardrisk.ignis.client.internal.path.api;
import com.lombardrisk.ignis.server.config.IntegrationTestConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static com.lombardrisk.ignis.test.config.AdminUser.BASIC_AUTH;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@IntegrationTestConfig
public class BasicAuthenticationEntryPointIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void navigateToSwagger_Unauthenticated_RedirectsToLogin() throws Exception {
        mockMvc.perform(
                get("/swagger-ui.html"))
                .andExpect(redirectedUrl("/fcrengine/login?redirect=%2Fswagger-ui.html"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    public void navigateToNonSwaggerPage_Unauthenticated_RedirectsToLogin() throws Exception {
        mockMvc.perform(
                get("/" + api.internal.Features))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void navigateToSwagger_Authenticated_ReturnsSwaggerPage() throws Exception {
        mockMvc.perform(
                get("/swagger-ui.html")
                        .with(BASIC_AUTH))
                .andExpect(status().isOk());
    }
}
