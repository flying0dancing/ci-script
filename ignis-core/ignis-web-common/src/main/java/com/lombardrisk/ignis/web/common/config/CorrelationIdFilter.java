package com.lombardrisk.ignis.web.common.config;

import com.lombardrisk.ignis.common.log.CorrelationId;
import io.vavr.control.Option;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.function.Supplier;

import static com.lombardrisk.ignis.common.log.CorrelationId.CORRELATION_ID_HEADER;

public class CorrelationIdFilter implements Filter {

    private final Supplier<String> idGenerator;

    public CorrelationIdFilter(final Supplier<String> idGenerator) {
        this.idGenerator = idGenerator;
    }

    @Override
    public void init(final FilterConfig filterConfig) {
        //no-op
    }

    @Override
    public void doFilter(
            final ServletRequest request,
            final ServletResponse response,
            final FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpServletRequest = (HttpServletRequest) request;

        if (isRestRequest(httpServletRequest)) {
            setupCorrelationIdHeader(httpServletRequest, response);
        }
        chain.doFilter(httpServletRequest, response);

        CorrelationId.cleanup();
    }

    private boolean isRestRequest(final HttpServletRequest request) {
        String requestURI = request.getRequestURI();

        return requestURI.contains("/api/")
                || requestURI.contains("/auth/");
    }

    private void setupCorrelationIdHeader(
            final HttpServletRequest httpServletRequest,
            final ServletResponse response) {

        String correlationId = Option.of(httpServletRequest.getHeader(CORRELATION_ID_HEADER))
                .getOrElse(idGenerator);

        CorrelationId.setCorrelationId(correlationId);

        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        httpServletResponse.setHeader(CORRELATION_ID_HEADER, correlationId);

    }

    @Override
    public void destroy() {
        //no-op
    }
}
