package com.lombardrisk.ignis.api.table.validation;

import io.vavr.control.Try;
import io.vavr.control.Validation;

import static com.lombardrisk.ignis.api.table.validation.NumberValidation.isNumberCreatable;

public interface ValidatableLongField extends ValidatableField<Long> {

    default Validation<String, String> doValidate(final String value) {
        return isNumberCreatable(getName(), value)
                .flatMap(this::checkRange);
    }

    default Long doParse(final String value) {
        return Long.parseLong(value);
    }

    default Validation<String, String> checkRange(final String value) {
        Try<Long> result = Try.of(() -> doParse(value));
        if (result.isSuccess()) {
            return Validation.valid(value);
        }
        return Validation.invalid(String.format("Field [%s] expected to be a long - actual: %s", getName(), value));
    }
}
