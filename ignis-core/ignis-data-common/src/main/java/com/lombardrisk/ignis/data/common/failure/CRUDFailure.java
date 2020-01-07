package com.lombardrisk.ignis.data.common.failure;

import com.google.common.collect.ImmutableList;
import com.lombardrisk.ignis.data.common.error.ErrorCoded;
import com.lombardrisk.ignis.data.common.error.ErrorResponse;
import io.vavr.Tuple;
import io.vavr.Tuple2;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Object to define errors, validations and error message for generic CRUD based operations
 *
 * These objects are always "Bad Request" errors
 */
public interface CRUDFailure extends ErrorCoded {

    String ID_INDEX_NAME = "id";

    static NotFoundFailure<Long> notFoundIds(final String entityName, final Iterable<Long> idsNotFound) {
        return new NotFoundFailure<>(ID_INDEX_NAME, entityName, ImmutableList.copyOf(idsNotFound));
    }

    static NotFoundFailure<Long> notFoundIds(final String entityName, final Long... idsNotFound) {
        return new NotFoundFailure<>(ID_INDEX_NAME, entityName, ImmutableList.copyOf(idsNotFound));
    }

    static <INDEX> NotFoundFailure<INDEX> notFoundIndices(
            final String indexName,
            final String entityName,
            final Iterable<INDEX> idsNotFound) {
        return new NotFoundFailure<>(indexName, entityName, ImmutableList.copyOf(idsNotFound));
    }

    static NotFoundByFailure.NotFoundByFailureBuilder cannotFind(final String subject) {
        return NotFoundByFailure.builder().subject(subject);
    }

    static <T> InvalidRequestParamsFailure<T> invalidRequestParameters(final List<Tuple2<String, T>> invalidParams) {
        return new InvalidRequestParamsFailure<>(ImmutableList.copyOf(invalidParams));
    }

    static <T> InvalidRequestParamsFailure<T> invalidRequestParameter(final String paramName, T param) {
        return new InvalidRequestParamsFailure<>(ImmutableList.of(Tuple.of(paramName, param)));
    }

    static InvalidRequestFailure.InvalidRequestFailureBuilder invalidParameters() {
        return new InvalidRequestFailure.InvalidRequestFailureBuilder();
    }

    enum Type {
        NOT_FOUND,
        INVALID_REQUEST_PARAMETERS,
        ENTITY_IS_ASSOCIATED,
        CONSTRAINT_VIOLATION
    }

    static ConstraintFailure constraintFailure(final String message) {
        return new ConstraintFailure(message);
    }

    static EntityAssociationFailure entityAssociated(
            final long id,
            final String entityName,
            final String associatedEntityName,
            final List<Long> associatedEntityIds,
            final CrudOperation crudOperation) {

        return new EntityAssociationFailure(id, entityName, associatedEntityName, associatedEntityIds, crudOperation);
    }

    Type getType();

    String getErrorMessage();

    default String getErrorCode() {
        return getType().name();
    }

    default ErrorResponse toErrorResponse() {
        return ErrorResponse.valueOf(getErrorMessage(), getErrorCode());
    }

    static List<ErrorResponse> toErrorResponses(final List<CRUDFailure> crudFailures) {
        return crudFailures.stream().map(CRUDFailure::toErrorResponse).collect(Collectors.toList());
    }
}
