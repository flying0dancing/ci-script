package com.lombardrisk.ignis.data.common.failure;

import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.data.common.error.ErrorResponse;
import io.vavr.Tuple;
import org.junit.Test;

import java.util.Arrays;

import static com.lombardrisk.ignis.data.common.failure.CRUDFailure.Type.INVALID_REQUEST_PARAMETERS;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

public class InvalidRequestParamsFailureTest {

    @Test
    public void getErrorMessage_InvalidRequest_ReturnsErrorMessageWithParameterNames() {
        CRUDFailure crudFailure = CRUDFailure.invalidRequestParameters(Arrays.asList(
                Tuple.of("param", "x"), Tuple.of("param", "y")));

        assertThat(crudFailure.getErrorMessage())
                .isEqualTo("Supplied parameters are invalid [(param, x), (param, y)]");
    }

    @Test
    public void getErrorMessage_InvalidRequest_ReturnsType() {
        CRUDFailure crudFailure = CRUDFailure.invalidRequestParameters(emptyList());
        assertThat(crudFailure.getType())
                .isEqualTo(INVALID_REQUEST_PARAMETERS);
    }

    @Test
    public void getErrorCode_InvalidRequest_ReturnsInvalidRequest() {
        CRUDFailure crudFailure = CRUDFailure.invalidRequestParameters(emptyList());
        assertThat(crudFailure.getErrorCode())
                .isEqualTo("INVALID_REQUEST_PARAMETERS");
    }

    @Test
    public void toErrorResponse_InvalidRequest_ReturnsErrorCodeAndMessage() {
        CRUDFailure crudFailure = CRUDFailure.invalidRequestParameters(Arrays.asList(
                Tuple.of("param", "x"), Tuple.of("param", "y")));

        assertThat(crudFailure.toErrorResponse())
                .isEqualTo(ErrorResponse.valueOf(
                        "Supplied parameters are invalid [(param, x), (param, y)]",
                        "INVALID_REQUEST_PARAMETERS"));
    }
}
