package com.lombardrisk.ignis.server.security;

import com.lombardrisk.ignis.server.controller.drillback.RedirectService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.lombardrisk.ignis.server.config.security.WebSecurityConfiguration.SWAGGER_HOME;

/**
 * To disable browser popup authentication dialog.
 */
@AllArgsConstructor
public class BasicAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final RedirectService redirectService;

    @Override
    public void commence(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final AuthenticationException authException) throws IOException {

        if (request.getRequestURI().endsWith(SWAGGER_HOME)) {
            response.sendRedirect(redirectService.redirectToLoginWithForwardUrl(SWAGGER_HOME));
        } else {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());
        }
    }
}
