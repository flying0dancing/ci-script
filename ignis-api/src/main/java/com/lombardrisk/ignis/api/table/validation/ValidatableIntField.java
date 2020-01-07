package com.lombardrisk.ignis.api.table.validation;

import io.vavr.control.Try;
import io.vavr.control.Validation;

import static com.lombardrisk.ignis.api.table.validation.NumberValidation.isNumberCreatable;

public interface ValidatableIntField extends ValidatableField<Integer> {

    default Integer doParse(final String value) {
        return Integer.parseInt(value);
    }

    default Validation<String, String> doValidate(final String value) {
        return isNumberCreatable(getName(), value)
                .flatMap(this::checkRange);
    }

    default Validation<String, String> checkRange(final String value) {
        Try<Integer> result = Try.of(() -> doParse(value));
        if (result.isSuccess()) {
            return Validation.valid(value);
        }
        return Validation.invalid(String.format("Field [%s] expected to be a integer - actual: %s", getName(), value));
    }
}
