package com.lombardrisk.ignis.spark.api.staging.field;

import com.lombardrisk.ignis.api.table.validation.ValidatableStringField;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@ToString(callSuper = true)
@Getter
@EqualsAndHashCode(callSuper = true)
@Data
public class StringFieldValidation extends FieldValidation<String> implements ValidatableStringField {

    private static final long serialVersionUID = 2559108263282735932L;
    private Integer maxLength;
    private Integer minLength;
    private String regularExpression;

    @Builder
    public StringFieldValidation(
            final String name,
            final boolean nullable,
            final Integer maxLength,
            final Integer minLength,
            final String regularExpression) {
        super(name, nullable);
        this.maxLength = maxLength;
        this.minLength = minLength;
        this.regularExpression = regularExpression;
    }
}
