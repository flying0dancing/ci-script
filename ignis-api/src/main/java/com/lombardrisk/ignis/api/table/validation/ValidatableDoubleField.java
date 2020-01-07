package com.lombardrisk.ignis.api.table.validation;

import io.vavr.control.Try;
import io.vavr.control.Validation;

import static com.lombardrisk.ignis.api.table.validation.NumberValidation.isNumberCreatable;

public interface ValidatableDoubleField extends ValidatableField<Double> {

    default Validation<String, String> doValidate(final String value) {
        return isNumberCreatable(getName(), value)
                .flatMap(this::checkRange);
    }

    default Validation<String, String> checkRange(final String value) {
        Try<Double> doubleConvertTry = Try.of(() -> doParse(value));
        if (doubleConvertTry.isSuccess()) {
            return Validation.valid(value);
        }
        String error = String.format(
                "Field [%s] expected to be a double - actual: %s", getName().toUpperCase(), value);

        return Validation.invalid(error);
    }

    default Double doParse(final String value) {
        return Double.parseDouble(value);
    }
}
