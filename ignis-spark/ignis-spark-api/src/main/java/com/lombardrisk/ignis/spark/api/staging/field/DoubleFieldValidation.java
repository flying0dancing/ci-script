package com.lombardrisk.ignis.spark.api.staging.field;

import com.lombardrisk.ignis.api.table.validation.ValidatableDoubleField;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString(callSuper = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Data
public class DoubleFieldValidation extends FieldValidation<Double> implements ValidatableDoubleField {

    private static final long serialVersionUID = 563687749506849911L;

    @Builder
    public DoubleFieldValidation(final String name, final boolean nullable) {
        super(name, nullable);
    }
}
