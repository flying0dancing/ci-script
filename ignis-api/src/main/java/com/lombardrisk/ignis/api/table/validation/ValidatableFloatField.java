package com.lombardrisk.ignis.api.table.validation;

import io.vavr.control.Try;
import io.vavr.control.Validation;

import static com.lombardrisk.ignis.api.table.validation.NumberValidation.isNumberCreatable;

public interface ValidatableFloatField extends ValidatableField<Float> {

    default Validation<String, String> doValidate(final String value) {
        return isNumberCreatable(getName(), value)
                .flatMap(this::checkRange);
    }

    default Validation<String, String> checkRange(final String value) {

        Try<Float> floatTry = Try.of(() -> doParse(value));
        if (floatTry.isSuccess()) {
            return Validation.valid(value);
        }

        return Validation.invalid(String.format(
                "Field [%s] expected to be a float - actual: %s", getName(), value));
    }

    default Float doParse(final String value) {
        return Float.parseFloat(value);
    }
}
