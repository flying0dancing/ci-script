package com.lombardrisk.ignis.api.table.validation;

import io.vavr.control.Validation;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.math.NumberUtils;

@UtilityClass
public class NumberValidation {

    public static Validation<String, String> isNumberCreatable(final String name, final String value) {
        if (!NumberUtils.isCreatable(value)) {
            String error = String.format("Field [%s] expected numeric value - actual: %s", name, value);

            return Validation.invalid(error);
        }
        return Validation.valid(value);
    }
}
