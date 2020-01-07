package com.lombardrisk.ignis.server.controller.security;

import com.lombardrisk.ignis.client.internal.path.auth;
import com.lombardrisk.ignis.data.common.error.ErrorResponse;
import com.lombardrisk.ignis.server.security.exception.UserExistsException;
import com.lombardrisk.ignis.server.user.UserService;
import com.lombardrisk.ignis.server.user.model.SecUser;
import com.lombardrisk.ignis.server.user.model.SecUserDto;
import com.lombardrisk.ignis.web.common.response.FcrResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import static java.util.Collections.singletonList;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@Slf4j
public class AuthenticationController {

    private final UserService userService;
    private final SecurityContextLogoutHandler securityContextLogoutHandler;

    public AuthenticationController(
            final UserService userService,
            final SecurityContextLogoutHandler securityContextLogoutHandler) {
        this.userService = userService;
        this.securityContextLogoutHandler = securityContextLogoutHandler;
    }

    @PostMapping(path = auth.Login)
    public void login() {
        // noop
    }

    @PostMapping(path = auth.Logout)
    public void logout(final HttpServletRequest request) {
        securityContextLogoutHandler.logout(request, null, null);
    }

    @PostMapping(path = auth.Users, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public FcrResponse<SecUserDto> save(@Valid @RequestBody final SecUser user) {
        try {
            SecUserDto savedUserDto = SecUserDto.from(userService.save(user));
            return FcrResponse.okResponse(savedUserDto);
        } catch (@SuppressWarnings("squid:S1166") UserExistsException e) {
            String message = String.format(
                    "Cannot create user with username [%s] because it already exists", user.getUsername());
            log.warn(message);

            return FcrResponse.badRequest(singletonList(ErrorResponse.valueOf(message, null)));
        }
    }
}
