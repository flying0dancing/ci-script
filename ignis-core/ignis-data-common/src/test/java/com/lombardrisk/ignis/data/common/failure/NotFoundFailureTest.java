package com.lombardrisk.ignis.data.common.failure;

import com.google.common.collect.ImmutableList;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.data.common.error.ErrorResponse;
import org.junit.Test;

import static com.lombardrisk.ignis.data.common.failure.CRUDFailure.Type.NOT_FOUND;
import static com.lombardrisk.ignis.data.common.failure.CRUDFailure.cannotFind;
import static org.assertj.core.api.Assertions.assertThat;

public class NotFoundFailureTest {

    @Test
    public void getErrorMessage_NotFoundWithNameAndIds_ReturnsErrorMessageWithNameAndIds() {
        CRUDFailure crudFailure = CRUDFailure.notFoundIds("ENTITY", ImmutableList.of(1L, 2L, 3L));
        assertThat(crudFailure.getErrorMessage())
                .isEqualTo("Could not find ENTITY for ids [1, 2, 3]");
    }

    @Test
    public void getErrorMessage_NotFound_ReturnsTypeNotFound() {
        CRUDFailure crudFailure = CRUDFailure.notFoundIds(null, ImmutableList.of());
        assertThat(crudFailure.getType())
                .isEqualTo(NOT_FOUND);
    }

    @Test
    public void getErrorCode_NotFound_ReturnsNotFound() {
        CRUDFailure crudFailure = CRUDFailure.notFoundIds(null, ImmutableList.of());
        assertThat(crudFailure.getErrorCode())
                .isEqualTo("NOT_FOUND");
    }

    @Test
    public void toErrorResponse_NotFound_ReturnsErrorCodeAndMessage() {
        CRUDFailure crudFailure = CRUDFailure.notFoundIds("test", ImmutableList.of(1L, 2L));

        assertThat(crudFailure.toErrorResponse())
                .isEqualTo(ErrorResponse.valueOf("Could not find test for ids [1, 2]", "NOT_FOUND"));
    }

    @Test
    public void toErrorResponse_NotFoundBy_ReturnsErrorCodeAndMessage() {
        CRUDFailure crudFailure = cannotFind("the bunny")
                .with("prop1", 1L)
                .with("prop2", "BBB")
                .asFailure();

        assertThat(crudFailure.toErrorResponse())
                .isEqualTo(ErrorResponse.valueOf("Cannot find the bunny with prop1 '1', with prop2 'BBB'", "NOT_FOUND"));
    }
}
