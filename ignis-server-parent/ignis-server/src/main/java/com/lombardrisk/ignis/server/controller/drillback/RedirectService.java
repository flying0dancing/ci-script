package com.lombardrisk.ignis.server.controller.drillback;

import com.lombardrisk.ignis.client.external.path.api;
import com.lombardrisk.ignis.data.common.error.ErrorResponse;
import com.lombardrisk.ignis.server.dataset.model.Dataset;
import lombok.AllArgsConstructor;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@AllArgsConstructor
public class RedirectService {

    private final String contextPath;
    private static final String LOGIN_ROUTE = "login";
    private static final String REDIRECT_PARAM = "redirect";
    private static final String ERROR_ROUTE = "error";
    private static final String DRILLBACK_ROUTE = "drillback";

    public String redirectToLoginWithForwardUrl(final String forwardUrl) throws UnsupportedEncodingException {
        return contextPath + LOGIN_ROUTE + "?" + REDIRECT_PARAM + "=" + encode(forwardUrl);
    }

    public String redirectToDrillback(final Dataset dataset) {
        return contextPath + DRILLBACK_ROUTE + "?"
                + api.Params.PIPELINE_INVOCATION_ID
                + "="
                + dataset.getPipelineInvocationId()
                + "&" + api.Params.PIPELINE_STEP_INVOCATION_ID + "="
                + dataset.getPipelineStepInvocationId();
    }

    public String redirectToErrorPage(final ErrorResponse message) throws UnsupportedEncodingException {
        return contextPath + ERROR_ROUTE
                + "?errorMessage=" + encode(message.getErrorMessage())
                + "&errorTitle=" + encode(message.getErrorCode());
    }

    private String encode(final String value) throws UnsupportedEncodingException {
        return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
    }
}
