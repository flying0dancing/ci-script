package com.lombardrisk.ignis.client.design.pipeline.error;

import com.lombardrisk.ignis.data.common.error.ErrorResponse;
import lombok.Data;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public interface SelectResult {

    List<ErrorResponse> getErrors();

    String getOutputFieldName();

    Long getOutputFieldId();

    boolean isValid();

    <T> T fold(final Function<Error, T> errorHandler, Function<Success, T> successHandler);

    static Success success(final String outputFieldName, final Long outputFieldId, final Long unionSchemaId) {
        return new Success(outputFieldName, outputFieldId, unionSchemaId);
    }

    static Error error(
            final String outputFieldName,
            final Long outputFieldId,
            final Long unionSchemaId,
            final List<ErrorResponse> errorResponses) {
        return new Error(errorResponses, outputFieldName, outputFieldId, unionSchemaId);
    }

    @Data
    class Success implements SelectResult {

        private final List<ErrorResponse> errors = Collections.emptyList();
        private final String outputFieldName;
        private final Long outputFieldId;
        private final Long unionSchemaId;

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public <T> T fold(
                final Function<Error, T> errorHandler, final Function<Success, T> successHandler) {
            return successHandler.apply(this);
        }
    }

    @Data
    class Error implements SelectResult {

        private final List<ErrorResponse> errors;
        private final String outputFieldName;
        private final Long outputFieldId;
        private final Long unionSchemaId;

        @Override
        public boolean isValid() {
            return false;
        }

        @Override
        public <T> T fold(
                final Function<Error, T> errorHandler, final Function<Success, T> successHandler) {
            return errorHandler.apply(this);
        }
    }
}
