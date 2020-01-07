package com.lombardrisk.ignis.data.common.failure;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

import static com.lombardrisk.ignis.data.common.failure.CRUDFailure.Type.CONSTRAINT_VIOLATION;

@Data
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class ConstraintFailure<T> implements CRUDFailure {

    private final String message;

    @Override
    public Type getType() {
        return CONSTRAINT_VIOLATION;
    }

    @Override
    public String getErrorMessage() {
        return message;
    }
}
