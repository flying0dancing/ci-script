package com.lombardrisk.ignis.web.common.exception;

import com.google.common.collect.ImmutableList;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class RollbackTransactionExceptionTest {

    @Test
    public void getMessage_ManyErrors() {
        RollbackTransactionException exception = RollbackTransactionException.create(ImmutableList.of(
                CRUDFailure.notFoundIds("Entity1", 1L),
                CRUDFailure.notFoundIds("Entity2", 2L)));

        assertThat(exception.getMessage())
                .isEqualTo("ErrorResponse=[\n"
                        + "\tcode=NOT_FOUND, message=Could not find Entity1 for ids [1],\n"
                        + "\tcode=NOT_FOUND, message=Could not find Entity2 for ids [2]]");
    }

    @Test
    public void toValidation_ManyErrors() {
        List<CRUDFailure> failures = ImmutableList.of(
                CRUDFailure.notFoundIds("Entity1", 1L),
                CRUDFailure.notFoundIds("Entity2", 2L));

        RollbackTransactionException exception = RollbackTransactionException.create(failures);

        assertThat(exception.toValidation().getError())
                .containsOnly(
                        CRUDFailure.notFoundIds("Entity1", 1L).toErrorResponse(),
                        CRUDFailure.notFoundIds("Entity2", 2L).toErrorResponse());
    }
}
