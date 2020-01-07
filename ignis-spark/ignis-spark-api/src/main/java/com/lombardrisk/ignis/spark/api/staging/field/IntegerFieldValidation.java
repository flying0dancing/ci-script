package com.lombardrisk.ignis.spark.api.staging.field;

import com.lombardrisk.ignis.api.table.validation.ValidatableIntField;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString(callSuper = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Data
public class IntegerFieldValidation extends FieldValidation<Integer> implements ValidatableIntField {

    private static final long serialVersionUID = -5070612112577293719L;

    @Builder
    public IntegerFieldValidation(final String name, final boolean nullable) {
        super(name, nullable);
    }
}
