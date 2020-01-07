package com.lombardrisk.ignis.web.common.response;

import com.lombardrisk.ignis.data.common.error.ErrorResponse;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

public interface FcrResponse<T>  {

    List<ErrorResponse> getErrorResponses();

    T body();

    static <T> FcrResponse<T> okResponse(final T body) {
        return new OkResponse<>(body, HttpStatus.OK);
    }

    static <T> FcrResponse<T> badRequest(final ErrorResponse errorResponse) {
        return badRequest(singletonList(errorResponse));
    }

    static <T> FcrResponse<T> badRequest(final List<ErrorResponse> errorResponses) {
        return new FailedResponse<>(errorResponses, HttpStatus.BAD_REQUEST);
    }

    static <T> FcrResponse<T> internalServerError(final List<ErrorResponse> errorResponses) {
        return new FailedResponse<>(errorResponses, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    static <T> FcrResponse<T> notFound(final List<ErrorResponse> errorResponses) {
        return new FailedResponse<>(errorResponses, HttpStatus.NOT_FOUND);
    }

    static <T> FcrResponse<T> unauthorized(final List<ErrorResponse> errorResponses) {
        return new FailedResponse<>(errorResponses, HttpStatus.UNAUTHORIZED);
    }

    static <T> FcrResponse<T> crudFailure(final CRUDFailure failure) {
        return new FailedResponse<>(singletonList(failure.toErrorResponse()), HttpStatus.BAD_REQUEST);
    }

    static <T> FcrResponse<T> crudFailure(final List<CRUDFailure> failures) {
        List<ErrorResponse> errorResponses = failures.stream()
                .map(CRUDFailure::toErrorResponse)
                .collect(toList());

        return new FailedResponse<>(errorResponses, HttpStatus.BAD_REQUEST);
    }

    final class OkResponse<T> extends ResponseEntity<T> implements FcrResponse<T> {

        private OkResponse(final T body, final HttpStatus status) {
            super(body, status);
        }

        @Override
        public List<ErrorResponse> getErrorResponses() {
            throw new NoSuchFieldError("Left projection not present");
        }

        @Override
        public T body() {
            return getBody();
        }
    }

    final class FailedResponse<T> extends ResponseEntity<List<ErrorResponse>> implements FcrResponse<T> {

        private FailedResponse(final List<ErrorResponse> errorResponses, final HttpStatus status) {
            super(errorResponses, status);
        }

        @Override
        public List<ErrorResponse> getErrorResponses() {
            return getBody();
        }

        @Override
        public T body() {
            throw new NoSuchFieldError("Right projection not present");
        }
    }
}
