package com.lombardrisk.ignis.design.server.configuration.web;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

class DesignStudioHeaderFilter implements Filter {

    private static final String DESIGN_STUDIO_QUIET_ERROR = "design-studio-quiet";

    @Override
    public void init(final FilterConfig filterConfig) {
        //no-initialization needed
    }

    @Override
    public void doFilter(
            final ServletRequest request,
            final ServletResponse response,
            final FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;

        String quietErrorHeader = httpServletRequest.getHeader(DESIGN_STUDIO_QUIET_ERROR);
        if (quietErrorHeader != null) {
            httpServletResponse.setHeader(DESIGN_STUDIO_QUIET_ERROR, "true");
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        //no-destroy needed
    }
}
