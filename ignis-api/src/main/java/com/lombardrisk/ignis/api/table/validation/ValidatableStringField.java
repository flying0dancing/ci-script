package com.lombardrisk.ignis.api.table.validation;

import io.vavr.control.Validation;
import org.apache.commons.lang3.StringUtils;

public interface ValidatableStringField extends ValidatableField<String> {

    String getRegularExpression();

    Integer getMaxLength();

    Integer getMinLength();

    default Validation<String, String> doValidate(final String value) {
        Integer maxLength = getMaxLength();
        if (maxLength != null && value.length() > maxLength) {
            return Validation.invalid(String.format("Field [%s] expected to have a maximum length of %d - actual: %d",
                    getName(), maxLength, value.length())
            );
        }

        Integer minLength = getMinLength();
        if (minLength != null && value.length() < minLength) {
            return Validation.invalid(String.format("Field [%s] expected to have a minimum length of %d - actual: %d",
                    getName(), minLength, value.length())
            );
        }

        String regularExpression = getRegularExpression();
        if (StringUtils.isNotEmpty(regularExpression)
                && !CachedPattern.compile(regularExpression).matches(value)) {
            return Validation.invalid(String.format("Field [%s] expected to match the regex %s - actual: %s",
                    getName(), regularExpression, value));
        }
        return Validation.valid(value);
    }

    default String doParse(final String value) {
        return value;
    }
}
