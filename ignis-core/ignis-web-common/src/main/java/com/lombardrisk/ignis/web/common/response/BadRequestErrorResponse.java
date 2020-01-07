package com.lombardrisk.ignis.web.common.response;

import com.lombardrisk.ignis.data.common.error.ErrorResponse;
import com.lombardrisk.ignis.data.common.error.ErrorCoded;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.springframework.http.MediaType.APPLICATION_JSON;

public final class BadRequestErrorResponse extends ResponseEntity<List<ErrorResponse>> {

    private static final HttpHeaders JSON_CONTENT_HEADER = new HttpHeaders();

    static {
        JSON_CONTENT_HEADER.setContentType(APPLICATION_JSON);
    }

    private BadRequestErrorResponse(final HttpHeaders headers, final ErrorResponse errorResponse) {
        super(singletonList(errorResponse), headers, HttpStatus.BAD_REQUEST);
    }

    private BadRequestErrorResponse(final HttpHeaders headers, final List<ErrorResponse> errorResponse) {
        super(errorResponse, headers, HttpStatus.BAD_REQUEST);
    }

    public static BadRequestErrorResponse valueOf(final String runtimeErrorMessage, final ErrorCoded errorCode) {
        return new BadRequestErrorResponse(
                JSON_CONTENT_HEADER,
                ErrorResponse.valueOf(runtimeErrorMessage, errorCode.getErrorCode()));
    }

    public static BadRequestErrorResponse valueOf(final List<ErrorResponse> errorResponses) {
        return new BadRequestErrorResponse(JSON_CONTENT_HEADER, errorResponses);
    }

    public static BadRequestErrorResponse valueOf(final ErrorResponse errorResponse) {
        return new BadRequestErrorResponse(JSON_CONTENT_HEADER, singletonList(errorResponse));
    }
}
