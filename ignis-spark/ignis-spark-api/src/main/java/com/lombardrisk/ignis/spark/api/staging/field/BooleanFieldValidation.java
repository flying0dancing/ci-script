package com.lombardrisk.ignis.spark.api.staging.field;

import com.lombardrisk.ignis.api.table.validation.ValidatableBooleanField;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString(callSuper = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Data
public class BooleanFieldValidation extends FieldValidation<Boolean> implements ValidatableBooleanField {

    private static final long serialVersionUID = -2123152311947702434L;

    @Builder
    public BooleanFieldValidation(final String name, final boolean nullable) {
        super(name, nullable);
    }
}
