package com.lombardrisk.ignis.web.common.response;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public interface EitherResponse<L, R> {

    static <T, V> RightResponse<T, V> okResponse(final V rightValue) {
        return new RightResponse<>(rightValue, HttpStatus.OK);
    }

    static <T, V> LeftResponse<T, V> badRequest(final T leftValue) {
        return new LeftResponse<>(leftValue, HttpStatus.BAD_REQUEST);
    }

    final class RightResponse<L, R> extends ResponseEntity<R> implements EitherResponse<L, R> {

        private RightResponse(final R body, final HttpStatus status) {
            super(body, status);
        }
    }

    final class LeftResponse<L, R> extends ResponseEntity<L> implements EitherResponse<L, R> {

        private LeftResponse(final L body, final HttpStatus status) {
            super(body, status);
        }
    }
}
