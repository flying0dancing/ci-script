package com.lombardrisk.ignis.server.controller.security;

import com.lombardrisk.ignis.common.json.MapperWrapper;
import com.lombardrisk.ignis.server.config.IntegrationTestConfig;
import com.lombardrisk.ignis.server.user.model.SecUserPassword;
import com.lombardrisk.ignis.test.config.UserTestFixture;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static com.lombardrisk.ignis.test.config.AdminUser.BASIC_AUTH;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@IntegrationTestConfig
public class UserControllerIT {

    private static final String USER_URL_PREFIX = "/api/internal/users";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserTestFixture userTestFixture;

    @Test
    public void deleteUser_ok() throws Exception {
        userTestFixture.addUser("UserToBeDeleted", "password");
        mockMvc
                .perform(delete(USER_URL_PREFIX + "/" + "UserToBeDeleted")
                        .with(BASIC_AUTH))
                .andExpect(status().isNoContent());
    }

    @Test
    public void deleteUser_forbidden() throws Exception {
        userTestFixture.addUser("userThatCannotDeleteHimself", "password");

        mockMvc
                .perform(delete(USER_URL_PREFIX + "/" + "userThatCannotDeleteHimself")
                        .with(httpBasic("userThatCannotDeleteHimself", "password")))
                .andExpect(status().isForbidden());
    }

    @Test
    public void deleteUser_userNotFound() throws Exception {
        mockMvc
                .perform(delete(USER_URL_PREFIX + "/" + "noneExistentUser")
                        .with(BASIC_AUTH))
                .andExpect(status().isNotFound());
    }

    @Test
    public void findUserByName_200() throws Exception {
        userTestFixture.addUser("created", "password");

        ResultActions resultActions = mockMvc.perform(get(USER_URL_PREFIX + "/" + "created")
                .with(BASIC_AUTH));
        resultActions.andExpect(status().isOk());
    }

    @Test
    public void findUserByName_noAuthentication_401() throws Exception {
        ResultActions resultActions = mockMvc
                .perform(get(USER_URL_PREFIX + "/" + "admin"));

        resultActions.andExpect(status().isUnauthorized());
    }

    @Test
    public void findUserByName_wrongUsername_404() throws Exception {
        mockMvc
                .perform(get(USER_URL_PREFIX + "/" + "errorUsername")
                        .with(BASIC_AUTH))
                .andExpect(status().isNotFound());
    }

    @Test
    public void findAllUsersInfo_200() throws Exception {
        mockMvc.perform(get(USER_URL_PREFIX)
                .with(BASIC_AUTH))
                .andExpect(status().isOk());
    }

    @Test
    public void changePassword_CorrectPassword_LoggedInCorrectly() throws Exception {
        userTestFixture.addUser("newlyCreatedUser", "oldPassword");

        SecUserPassword password = new SecUserPassword();
        password.setOldPassword("oldPassword");
        password.setNewPassword("newPassword");

        String json = MapperWrapper.MAPPER.writeValueAsString(password);

        mockMvc.perform(put(USER_URL_PREFIX + "/" + "currentUser")
                .with(httpBasic("newlyCreatedUser", "oldPassword"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk());

        mockMvc.perform(get(USER_URL_PREFIX + "/" + "currentUser")
                .with(httpBasic("newlyCreatedUser", "newPassword")))
                .andExpect(status().isOk());
    }

    @Test
    public void changePassword_wrongPassword_returnsBadRequest() throws Exception {
        userTestFixture.addUser("userWhoForgotHisPassword", "oldPassword");

        SecUserPassword password = new SecUserPassword();
        password.setOldPassword("wrongPassword");
        password.setNewPassword("newPassword");

        String json = MapperWrapper.MAPPER.writeValueAsString(password);

        mockMvc.perform(put(USER_URL_PREFIX + "/" + "currentUser")
                .with(httpBasic("userWhoForgotHisPassword", "oldPassword"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].errorCode").value("INCORRECT_PASSWORD"))
                .andExpect(jsonPath("$[0].errorMessage").value("Incorrect password"));
    }
}
