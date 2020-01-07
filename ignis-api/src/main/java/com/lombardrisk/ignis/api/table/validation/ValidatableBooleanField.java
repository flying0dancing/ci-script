package com.lombardrisk.ignis.api.table.validation;

import io.vavr.control.Validation;

public interface ValidatableBooleanField extends ValidatableField<Boolean> {

    default Validation<String, String> doValidate(final String value) {
        if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
            return Validation.valid(value);
        }

        String error = String.format(
                "Field [%s] expected to be a boolean, - actual: %s", getName().toUpperCase(), value);
        return Validation.invalid(error);
    }

    default Boolean doParse(final String value) {
        return Boolean.valueOf(value);
    }
}
