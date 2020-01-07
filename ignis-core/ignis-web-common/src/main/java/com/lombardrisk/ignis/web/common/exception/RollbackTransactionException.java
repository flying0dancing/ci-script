package com.lombardrisk.ignis.web.common.exception;

import com.lombardrisk.ignis.common.MapperUtils;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.data.common.error.ErrorResponse;
import io.vavr.control.Validation;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RollbackTransactionException extends Exception {

    private static final long serialVersionUID = 2640423886952538790L;
    private final List<ErrorResponse> errorResponses;

    public static RollbackTransactionException create(final CRUDFailure crudFailure) {
        return new RollbackTransactionException(singletonList(crudFailure.toErrorResponse()));
    }

    public static RollbackTransactionException create(final List<CRUDFailure> crudFailures) {
        return new RollbackTransactionException(MapperUtils.map(crudFailures, CRUDFailure::toErrorResponse));
    }

    public static RollbackTransactionException fromErrors(final ErrorResponse errorResponse) {
        return new RollbackTransactionException(singletonList(errorResponse));
    }

    public static RollbackTransactionException fromErrors(final List<ErrorResponse> errorResponses) {
        return new RollbackTransactionException(errorResponses);
    }

    public <R> Validation<List<ErrorResponse>, R> toValidation() {
        return Validation.invalid(errorResponses);
    }

    @Override
    public String getMessage() {
        String errorsAsMessage = errorResponses.stream()
                .map(error -> "\tcode=" + error.getErrorCode() + ", message=" + error.getErrorMessage())
                .collect(
                        Collectors.joining(",\n"));

        return "ErrorResponse=[\n" + errorsAsMessage + "]";
    }
}
