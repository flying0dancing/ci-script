package com.lombardrisk.ignis.server.controller.resource;

import com.google.common.collect.ImmutableList;
import com.lombardrisk.ignis.data.common.error.ErrorResponse;
import com.lombardrisk.ignis.web.common.response.BadRequestErrorResponse;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class BadRequestErrorResponseTest {

    @Test
    public void valueOf_ErrorMessageAndCode_ReturnsBadRequestErrorResponse() {
        BadRequestErrorResponse badRequestErrorResponse =
                BadRequestErrorResponse.valueOf("oopsy", () -> "daisy");

        assertThat(badRequestErrorResponse.getStatusCodeValue())
                .isEqualTo(400);
        assertThat(badRequestErrorResponse.getBody().get(0).getErrorMessage())
                .isEqualTo("oopsy");
        assertThat(badRequestErrorResponse.getBody().get(0).getErrorCode())
                .isEqualTo("daisy");
    }

    @Test
    public void valueOf_ErrorResponses_ReturnsBadRequestErrorResponse() {
        BadRequestErrorResponse badRequestErrorResponse =
                BadRequestErrorResponse.valueOf(
                        ImmutableList.of(
                                ErrorResponse.valueOf("err1", "code1"),
                                ErrorResponse.valueOf("err5", "code5")));

        assertThat(badRequestErrorResponse.getStatusCodeValue())
                .isEqualTo(400);
        assertThat(badRequestErrorResponse.getBody())
                .hasSize(2);
        assertThat(badRequestErrorResponse.getBody().get(0).getErrorMessage())
                .isEqualTo("err1");
        assertThat(badRequestErrorResponse.getBody().get(0).getErrorCode())
                .isEqualTo("code1");
        assertThat(badRequestErrorResponse.getBody().get(1).getErrorMessage())
                .isEqualTo("err5");
        assertThat(badRequestErrorResponse.getBody().get(1).getErrorCode())
                .isEqualTo("code5");
    }
}
