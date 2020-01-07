package com.lombardrisk.ignis.server.controller;

import com.lombardrisk.ignis.feature.IgnisFeature;
import com.lombardrisk.ignis.server.config.IntegrationTestConfig;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.repository.FeatureState;

import static com.lombardrisk.ignis.test.config.AdminUser.BASIC_AUTH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(Enclosed.class)
public class FeaturesControllerIT {

    @RunWith(SpringRunner.class)
    @IntegrationTestConfig
    public static class FeaturesGet {

        @Autowired
        private FeatureManager featureManager;

        @Autowired
        private MockMvc mockMvc;

        @Test
        public void getFeatures_Active_ReturnsFeatures() throws Exception {
            featureManager.setFeatureState(new FeatureState(IgnisFeature.APPEND_DATASETS, true));

            mockMvc.perform(
                    get("/api/internal/features")
                            .with(BASIC_AUTH)
                            .contentType(APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].name", equalTo("APPEND_DATASETS")))
                    .andExpect(jsonPath("$[0].active", equalTo(true)));
        }

        @Test
        public void getFeatures_Inactive_ReturnsFeatures() throws Exception {
            featureManager.setFeatureState(new FeatureState(IgnisFeature.APPEND_DATASETS, false));

            mockMvc.perform(
                    get("/api/internal/features")
                            .with(BASIC_AUTH)
                            .contentType(APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].name", equalTo("APPEND_DATASETS")))
                    .andExpect(jsonPath("$[0].active", equalTo(false)));
        }
    }

    @RunWith(SpringRunner.class)
    @IntegrationTestConfig
    @TestPropertySource(properties = {
            "togglz.console.enabled=true",
            "IGNIS_HOST=localhost",
            "ignis.host=localhost",
            "env.hostname=localhost",
            "ignis.home=." })
    public static class FeaturesConsoleEnabled {

        @Autowired
        private FeatureManager featureManager;

        @Autowired
        private MockMvc mockMvc;

        @Test
        public void updateFeature_FeatureMadeInActive_FeatureIsInactive() throws Exception {
            featureManager.setFeatureState(new FeatureState(IgnisFeature.APPEND_DATASETS, true));

            mockMvc.perform(
                    post("/api/internal/features/APPEND_DATASETS")
                            .content(" { \"enabled\" : false } ")
                            .with(BASIC_AUTH)
                            .contentType(APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name", equalTo("APPEND_DATASETS")))
                    .andExpect(jsonPath("$.active", equalTo(false)));

            assertThat(featureManager.isActive(IgnisFeature.APPEND_DATASETS))
                    .isFalse();
        }

        @Test
        public void updateFeature_FeatureMadeActive_FeatureIsActive() throws Exception {
            featureManager.setFeatureState(new FeatureState(IgnisFeature.APPEND_DATASETS, false));

            mockMvc.perform(
                    post("/api/internal/features/APPEND_DATASETS")
                            .content(" { \"enabled\" : true } ")
                            .with(BASIC_AUTH)
                            .contentType(APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name", equalTo("APPEND_DATASETS")))
                    .andExpect(jsonPath("$.active", equalTo(true)));

            assertThat(featureManager.isActive(IgnisFeature.APPEND_DATASETS))
                    .isTrue();
        }

        @Test
        public void updateFeature_FeatureNotFound_ReturnsError() throws Exception {
            featureManager.setFeatureState(new FeatureState(IgnisFeature.APPEND_DATASETS, false));

            mockMvc.perform(
                    post("/api/internal/features/OOPS")
                            .content(" { \"enabled\" : true } ")
                            .with(BASIC_AUTH)
                            .contentType(APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$[0].errorMessage", equalTo("Cannot find feature with name 'OOPS'")))
                    .andExpect(jsonPath("$[0].errorCode", equalTo("NOT_FOUND")));

        }
    }

    @RunWith(SpringRunner.class)
    @IntegrationTestConfig
    @TestPropertySource(properties = {
            "togglz.console.enabled=false",
            "IGNIS_HOST=localhost",
            "ignis.host=localhost",
            "env.hostname=localhost",
            "ignis.home=." })
    public static class FeaturesConsoleDisabled {

        @Autowired
        private FeatureManager featureManager;

        @Autowired
        private MockMvc mockMvc;

        @Test
        public void updateFeature_FeatureFlagConsoleDisabled_DoesntAllowModificationOfFeatures() throws Exception {
            featureManager.setFeatureState(new FeatureState(IgnisFeature.APPEND_DATASETS, false));

            mockMvc.perform(
                    post("/api/internal/features/APPEND_DATASETS")
                            .content(" { \"enabled\" : true } ")
                            .with(BASIC_AUTH)
                            .contentType(APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$[0].errorCode", equalTo("NOT_ALLOWED")))
                    .andExpect(jsonPath("$[0].errorMessage", equalTo("Updating feature toggles is not allowed")));

            assertThat(featureManager.isActive(IgnisFeature.APPEND_DATASETS))
                    .isFalse();
        }
    }
}
