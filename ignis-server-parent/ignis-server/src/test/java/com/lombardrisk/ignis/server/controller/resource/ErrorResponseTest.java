package com.lombardrisk.ignis.server.controller.resource;

import com.google.common.collect.ImmutableSet;
import com.lombardrisk.ignis.data.common.error.ErrorResponse;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ErrorResponseTest {

    @Test
    public void valueOf_ErrorMessageAndCode_ReturnsErrorResponse() {
        ErrorResponse errorResponse =
                ErrorResponse.valueOf("err msg", "c1");

        assertThat(errorResponse.getErrorMessage())
                .isEqualTo("err msg");
        assertThat(errorResponse.getErrorCode())
                .isEqualTo("c1");
    }

    @Test
    public void valueOf_EmptyErrorMessageAndCode_ReturnsErrorResponseWithNullFields() {
        ErrorResponse errorResponse =
                ErrorResponse.valueOf("     ", "   ");

        assertThat(errorResponse.getErrorMessage())
                .isNull();
        assertThat(errorResponse.getErrorCode())
                .isNull();
    }

    @Test
    public void valueOf_NullErrorMessageAndCode_ReturnsErrorResponseWithNullFields() {
        ErrorResponse errorResponse =
                ErrorResponse.valueOf(null, null);

        assertThat(errorResponse.getErrorMessage())
                .isNull();
        assertThat(errorResponse.getErrorCode())
                .isNull();
    }

    @Test
    public void hashcodeAndEquals_SetOfErrorResponses_DoesNotStoreDuplicates() {
        ImmutableSet<ErrorResponse> uniqueErrorResponses =
                ImmutableSet.of(
                        ErrorResponse.valueOf("err 1", "E1"),
                        ErrorResponse.valueOf("err 2", "E1"),
                        ErrorResponse.valueOf("err 2", "E2"),
                        ErrorResponse.valueOf("err 1", "E2"),
                        ErrorResponse.valueOf("err 1", "E1"),
                        ErrorResponse.valueOf("err 2", "E2"));

        assertThat(uniqueErrorResponses)
                .hasSize(4)
                .contains(
                        ErrorResponse.valueOf("err 2", "E1"),
                        ErrorResponse.valueOf("err 1", "E1"),
                        ErrorResponse.valueOf("err 1", "E2"),
                        ErrorResponse.valueOf("err 2", "E2"));
    }
}
