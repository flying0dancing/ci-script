package com.lombardrisk.ignis.web.common.config;

import com.lombardrisk.ignis.common.log.CorrelationId;
import lombok.Data;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CorrelationIdFilterTest {

    @Mock
    private HttpServletResponse httpServletResponse;
    @Mock
    private HttpServletRequest httpServletRequest;

    @Before
    public void setup() {
        when(httpServletRequest.getRequestURI())
                .thenReturn("/fcrengine/api/v1/jobs");
    }

    @Test
    public void filter_CorrelationIdSetInFilterChain() throws Exception {
        AtomicString correlationIdInThread = new AtomicString();

        new CorrelationIdFilter(() -> "a bad id generator")
                .doFilter(httpServletRequest, httpServletResponse,
                        (request, response) -> correlationIdInThread.setInner(CorrelationId.getCorrelationId()));

        assertThat(correlationIdInThread.getInner())
                .isEqualTo("a bad id generator");
    }

    @Test
    public void filter_CorrelationIdSetInHttpResponse() throws Exception {
        AtomicString correlationIdInThread = new AtomicString();

        new CorrelationIdFilter(() -> "id generator")
                .doFilter(httpServletRequest, httpServletResponse,
                        (request, response) -> correlationIdInThread.setInner(CorrelationId.getCorrelationId()));

        verify(httpServletResponse).setHeader("Correlation-Id", "id generator");
    }

    @Test
    public void filter_IfCorrelationIdHeaderSet_CorrelationIdTakenFromRequest() throws Exception {
        AtomicString correlationIdInThread = new AtomicString();

        when(httpServletRequest.getHeader("Correlation-Id"))
                .thenReturn("Header Id");

        new CorrelationIdFilter(() -> null)
                .doFilter(httpServletRequest, httpServletResponse,
                        (request, response) -> correlationIdInThread.setInner(CorrelationId.getCorrelationId()));

        assertThat(correlationIdInThread.getInner())
                .isEqualTo("Header Id");
    }

    @Test
    public void filter_CorrelationIdCleanedUpAfter() throws Exception {
        AtomicString correlationIdInThread = new AtomicString();

        new CorrelationIdFilter(() -> "a").doFilter(httpServletRequest, httpServletResponse,
                (request, response) -> correlationIdInThread.setInner(CorrelationId.getCorrelationId()));

        assertThat(CorrelationId.getCorrelationId()).isNull();
    }

    @Test
    public void filter_NonApiRequests_DoesNotGenerateCorrelationId() throws IOException, ServletException {
        when(httpServletRequest.getRequestURI())
                .thenReturn("/fcrengine/swagger-ui.html");

        AtomicString nextFilter = new AtomicString();
        new CorrelationIdFilter(() -> null)
                .doFilter(httpServletRequest, httpServletResponse,
                        (request, response) -> nextFilter.setInner("next filter called"));

        verifyZeroInteractions(httpServletResponse);

        assertThat(nextFilter.getInner())
                .isEqualTo("next filter called");
    }

    @Test
    public void filter_AuthRequests_GeneratesCorrelationIdHeader() throws IOException, ServletException {
        when(httpServletRequest.getRequestURI())
                .thenReturn("/fcrengine/auth/login");

        new CorrelationIdFilter(() -> null)
                .doFilter(httpServletRequest, httpServletResponse,
                        (request, response) -> {
                        });

        verify(httpServletResponse).setHeader(any(), any());
    }

    @Data
    private static class AtomicString {

        private String inner;
    }
}