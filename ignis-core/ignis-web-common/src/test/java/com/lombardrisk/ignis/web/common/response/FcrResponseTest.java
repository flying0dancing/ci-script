package com.lombardrisk.ignis.web.common.response;

import com.lombardrisk.ignis.data.common.error.ErrorResponse;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.data.common.failure.NotFoundFailure;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class FcrResponseTest {

    @Test
    public void badRequest_ListOfErrors_ReturnsBadRequestWithListBody() {
        NotFoundFailure someEntity1 = CRUDFailure.notFoundIds("someEntity1", 100L);
        NotFoundFailure someEntity2 = CRUDFailure.notFoundIds("someEntity2", 100L);
        NotFoundFailure someEntity3 = CRUDFailure.notFoundIds("someEntity3", 100L);
        NotFoundFailure someEntity4 = CRUDFailure.notFoundIds("someEntity4", 100L);

        FcrResponse<Object> responseEntity =
                FcrResponse.crudFailure(
                        Arrays.asList(someEntity1, someEntity2, someEntity3, someEntity4));

        assertThat(responseEntity.getErrorResponses())
                .contains(
                        someEntity1.toErrorResponse(),
                        someEntity2.toErrorResponse(),
                        someEntity3.toErrorResponse(),
                        someEntity4.toErrorResponse());
    }

    @Test
    public void badRequest_ReturnsBadRequestStatusCode() {
        FcrResponse<Object> responseEntity =
                FcrResponse.crudFailure(CRUDFailure.notFoundIds("someEntity", 100L));

        assertThat((ResponseEntity) responseEntity)
                .extracting(ResponseEntity::getStatusCode)
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void badRequest_CrudFailure_ReturnsErrorResponseBody() {
        FcrResponse<Object> responseEntity =
                FcrResponse.crudFailure(CRUDFailure.notFoundIds("someEntity", 100L));

        assertThat((ResponseEntity) responseEntity)
                .extracting(ResponseEntity::getBody)
                .isEqualTo(singletonList(
                        ErrorResponse.valueOf(
                                "Could not find someEntity for ids [100]",
                                "NOT_FOUND")));
    }

    @Test
    public void badRequest_getRight_ThrowsException() {
        FcrResponse<Object> responseEntity =
                FcrResponse.crudFailure(CRUDFailure.notFoundIds("someEntity", 100L));

        assertThatThrownBy(responseEntity::body)
                .isInstanceOf(NoSuchFieldError.class)
                .hasMessage("Right projection not present");
    }

    @Test
    public void badRequest_getLeft_ReturnsBody() {
        NotFoundFailure notFound = CRUDFailure.notFoundIds("someEntity", 100L);
        FcrResponse<Object> responseEntity =
                FcrResponse.crudFailure(notFound);

        assertThat(responseEntity.getErrorResponses())
                .contains(notFound.toErrorResponse());
    }

    @Test
    public void ok_ReturnsOkStatusCode() {
        FcrResponse<String> responseEntity =
                FcrResponse.okResponse("I am ok");

        assertThat((ResponseEntity) responseEntity)
                .extracting(ResponseEntity::getStatusCode)
                .isEqualTo(HttpStatus.OK);
    }

    @Test
    public void ok_ReturnsRightBody() {
        FcrResponse<String> responseEntity =
                FcrResponse.okResponse("I am ok");

        assertThat((ResponseEntity) responseEntity)
                .extracting(ResponseEntity::getBody)
                .isEqualTo("I am ok");
    }

    @Test
    public void ok_getLeft_ThrowsException() {
        FcrResponse<String> responseEntity =
                FcrResponse.okResponse("Doesnt matter what this is7");

        assertThatThrownBy(responseEntity::getErrorResponses)
                .isInstanceOf(NoSuchFieldError.class)
                .hasMessage("Left projection not present");
    }
}
