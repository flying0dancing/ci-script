package com.lombardrisk.ignis.server.controller.security;

import com.lombardrisk.ignis.client.internal.path.api;
import com.lombardrisk.ignis.config.VariableConstants;
import com.lombardrisk.ignis.data.common.error.ErrorResponse;
import com.lombardrisk.ignis.server.user.UserService;
import com.lombardrisk.ignis.server.user.model.PasswordError;
import com.lombardrisk.ignis.server.user.model.SecUserDto;
import com.lombardrisk.ignis.server.user.model.SecUserPassword;
import com.lombardrisk.ignis.web.common.response.FcrResponse;
import io.vavr.control.Option;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;

@RestController
@Slf4j
public class UserController {

    private final UserService userService;

    public UserController(final UserService userService) {
        this.userService = userService;
    }

    @GetMapping(path = api.internal.users.ByUsername, produces = MediaType.APPLICATION_JSON_VALUE)
    public FcrResponse<SecUserDto> getUserByName(
            @PathVariable(api.Params.USERNAME) final String username) {

        return userService.findByUsername(username)
                .map(SecUserDto::from)
                .map(FcrResponse::okResponse)
                .orElseGet(() -> FcrResponse.notFound(
                        singletonList(ErrorResponse.valueOf("User " + username + " was not found", null))));
    }

    @GetMapping(
            path = api.internal.Users,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public FcrResponse<List<SecUserDto>> getAllUsersInfo() {
        List<SecUserDto> users = userService.findAll().stream()
                .map(SecUserDto::from)
                .sorted()
                .collect(Collectors.toList());

        return FcrResponse.okResponse(users);
    }

    @GetMapping(path = api.internal.users.CurrentUser, produces = MediaType.APPLICATION_JSON_VALUE)
    public FcrResponse<SecUserDto> getUserInfo() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return FcrResponse.unauthorized(singletonList(ErrorResponse.valueOf("Authorization is null", null)));
        }

        User user = (User) auth.getPrincipal();
        String username = user.getUsername();

        return userService.findByUsername(username)
                .map(SecUserDto::from)
                .map(FcrResponse::okResponse)
                .orElseGet(() -> FcrResponse.unauthorized(
                        singletonList(ErrorResponse.valueOf("User " + username + " not found", null))));
    }

    @PutMapping(path = api.internal.users.CurrentUser, consumes = MediaType.APPLICATION_JSON_VALUE)
    public FcrResponse<Boolean> changePassword(
            @Valid @RequestBody final SecUserPassword userPassword) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return FcrResponse.unauthorized(singletonList(ErrorResponse.valueOf(
                    "You need to be logged in to change your password", PasswordError.UNAUTHORIZED.name())));
        }

        User user = (User) auth.getPrincipal();

        Option<ErrorResponse> changePasswordError = userService.changePassword(user.getUsername(), userPassword);
        if (changePasswordError.isDefined()) {
            log.warn("Failed to change password {}", changePasswordError.get());
            return FcrResponse.badRequest(changePasswordError.get());
        }

        return FcrResponse.okResponse(true);
    }

    @DeleteMapping(path = api.internal.users.ByUsername)
    public ResponseEntity deleteUser(@PathVariable(api.Params.USERNAME) final String username) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return new ResponseEntity(HttpHeaders.EMPTY, HttpStatus.UNAUTHORIZED);
        }

        User user = (User) auth.getPrincipal();

        if (!VariableConstants.IGNIS_ADMIN_USERNAME.equalsIgnoreCase(user.getUsername())) {
            log.warn("user {} is not admin, delete user {} is not allowed", user.getUsername(), username);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (VariableConstants.IGNIS_ADMIN_USERNAME.equalsIgnoreCase(username)) {
            log.warn("user {} cannot delete administrator {} ", user.getUsername(), username);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return userService
                .findByUsername(username)
                .map(
                        p -> {
                            userService.deleteUser(p.getId());
                            return ResponseEntity.noContent().build();
                        })
                .orElse(ResponseEntity.notFound().build());
    }
}
