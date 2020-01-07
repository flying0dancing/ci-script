package com.lombardrisk.ignis.server.controller.security;

import com.lombardrisk.ignis.common.json.MapperWrapper;
import com.lombardrisk.ignis.server.config.IntegrationTestConfig;
import com.lombardrisk.ignis.server.user.model.SecUser;
import com.lombardrisk.ignis.test.config.UserTestFixture;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static com.lombardrisk.ignis.test.config.AdminUser.BASIC_AUTH;
import static com.lombardrisk.ignis.test.config.AdminUser.USERNAME;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@IntegrationTestConfig
public class AuthenticationControllerIT {

    @Autowired
    private MockMvc authMockMvc;

    @Autowired
    private UserTestFixture userTestFixture;

    @Test
    public void saveUser_userExisted_400() throws Exception {
        userTestFixture.addUser("previouslyCreated", "password");

        SecUser newUser = new SecUser("previouslyCreated", "password");
        String json = MapperWrapper.MAPPER.writeValueAsString(newUser);

        authMockMvc.perform(
                post("/auth/users")
                        .with(BASIC_AUTH)
                        .contentType(APPLICATION_JSON)
                        .content(json))

                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].errorMessage",
                        equalTo("Cannot create user with username [previouslyCreated] because it already exists")))
                .andExpect(jsonPath("$[0].errorCode", is(nullValue())));
    }

    @Test
    public void login_200() throws Exception {
        authMockMvc.perform(
                post("/auth/login")
                        .with(BASIC_AUTH))

                .andExpect(status().isOk());
    }

    @Test
    public void login_wrongUser_401() throws Exception {
        authMockMvc.perform(
                post("/auth/login")
                        .with(httpBasic("wrongUser", "password")))

                .andExpect(status().isUnauthorized());
    }

    @Test
    public void login_wrongPassword_401() throws Exception {
        authMockMvc.perform(
                post("/auth/login")
                        .with(httpBasic(USERNAME, "666")))

                .andExpect(status().isUnauthorized());
    }

    @Test
    public void logout_200() throws Exception {
        authMockMvc.perform(
                post("/auth/logout")
                        .with(BASIC_AUTH))

                .andExpect(status().isOk());
    }

    @Test
    public void logout_notLogin_401() throws Exception {
        authMockMvc.perform(
                post("/auth/logout"))

                .andExpect(status().isUnauthorized());
    }
}
