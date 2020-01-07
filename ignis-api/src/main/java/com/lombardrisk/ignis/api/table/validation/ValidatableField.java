package com.lombardrisk.ignis.api.table.validation;

import io.vavr.control.Validation;
import org.apache.commons.lang3.StringUtils;

public interface ValidatableField<T extends Comparable> {

    String getName();

    boolean isNullable();

    default Validation<String, String> validate(final String value) {
        if (StringUtils.isBlank(value)) {

            if (!isNullable()) {
                return Validation.invalid(String.format("Field [%s] is not nullable", getName()));
            }

            return Validation.valid(value);
        }

        return doValidate(value);
    }

    default T parse(final String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }

        return doParse(value);
    }

    Validation<String, String> doValidate(String value);

    T doParse(String value);
}
