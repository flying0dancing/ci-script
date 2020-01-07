package com.lombardrisk.ignis.spark.validation.function;

import com.lombardrisk.ignis.spark.validation.transform.Result;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ValidationResult implements Result<Boolean> {

    private Boolean value;
    private String error;

    public static ValidationResult of(final Boolean value, final String error) {
        return new ValidationResult(value, error);
    }

    @Override
    public Boolean value() {
        return value;
    }

    @Override
    public String errorMessage() {
        return error;
    }
}
