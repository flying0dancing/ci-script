package com.lombardrisk.ignis.api.table.validation;

import io.vavr.control.Try;
import io.vavr.control.Validation;
import org.apache.commons.lang3.time.DateUtils;

import java.sql.Date;
import java.text.ParseException;

public interface ValidatableDateField extends ValidatableField<Date> {

    String getFormat();

    default java.sql.Date doParse(final String value) {
        return new java.sql.Date(
                ValidatableDateField.dateValue(value, getFormat()).getTime());
    }

    default Validation<String, String> doValidate(final String value) {
        return validateDate(getName(), value, getFormat());
    }

    static java.util.Date dateValue(final String value, final String format) {
        try {
            return DateUtils.parseDate(value, format);
        } catch (ParseException e) {
            String msg = String.format("Failed to parse date: %s using format: %s", value, format);
            throw new IllegalArgumentException(msg, e);
        }
    }

    static Validation<String, String> validateDate(final String name, final String value, final String format) {
        if (value == null || format == null) {
            return Validation.invalid("No format or value specified");
        }

        Try<java.util.Date> dateTry = Try.of(() -> dateValue(value, format));

        if (dateTry.isSuccess()) {
            return Validation.valid(value);
        }

        String errorMessage = String.format("Field [%s] expected to be a date with format %s - actual: %s",
                name, format, value);
        return Validation.invalid(errorMessage);
    }
}
