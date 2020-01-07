package com.lombardrisk.ignis.data.common.failure;

import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.data.common.failure.CrudOperation;
import com.lombardrisk.ignis.data.common.error.ErrorResponse;
import org.junit.Test;

import static com.lombardrisk.ignis.data.common.failure.CRUDFailure.Type.ENTITY_IS_ASSOCIATED;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

public class EntityAssociationFailureTest {

    @Test
    public void getErrorMessage_ReturnsErrorMessage() {
        CRUDFailure crudFailure = CRUDFailure.entityAssociated(
                123L,
                "ProductConfig",
                "Dataset",
                asList(1L, 2L),
                CrudOperation.DELETE);

        assertThat(crudFailure.getErrorMessage())
                .isEqualTo("Could not DELETE ProductConfig with id 123 "
                        + "because it has associated Datasets with ids [1, 2]");
    }

    @Test
    public void getType_ReturnsType() {
        CRUDFailure crudFailure = CRUDFailure.entityAssociated(123L, null, null, emptyList(), null);
        assertThat(crudFailure.getType())
                .isEqualTo(ENTITY_IS_ASSOCIATED);
    }

    @Test
    public void getErrorCode_ReturnsErrorCode() {
        CRUDFailure crudFailure = CRUDFailure.entityAssociated(123L, null, null, emptyList(), null);
        assertThat(crudFailure.getErrorCode())
                .isEqualTo("ENTITY_IS_ASSOCIATED");
    }

    @Test
    public void toErrorResponse_ReturnsErrorCodeAndMessage() {
        CRUDFailure crudFailure = CRUDFailure.entityAssociated(
                123L,
                "ProductConfig",
                "Dataset",
                asList(1L, 2L),
                CrudOperation.DELETE);

        assertThat(crudFailure.toErrorResponse())
                .isEqualTo(ErrorResponse.valueOf(
                        "Could not DELETE ProductConfig with id 123 "
                                + "because it has associated Datasets with ids [1, 2]",
                        "ENTITY_IS_ASSOCIATED"));
    }
}
