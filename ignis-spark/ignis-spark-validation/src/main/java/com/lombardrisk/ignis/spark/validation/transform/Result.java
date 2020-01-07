package com.lombardrisk.ignis.spark.validation.transform;

import com.lombardrisk.ignis.common.lang.ErrorMessage;
import io.vavr.control.Either;

public interface Result<T> {

    T value();

    String errorMessage();

    default Either<ErrorMessage, T> toEither() {
        if (errorMessage() == null) {
            return Either.right(value());
        }

        return Either.left(
                ErrorMessage.of(errorMessage()));
    }
}
