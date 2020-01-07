package com.lombardrisk.ignis.spark.api.staging.field;

import com.lombardrisk.ignis.api.table.validation.ValidatableFloatField;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString(callSuper = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Data
public class FloatFieldValidation extends FieldValidation<Float> implements ValidatableFloatField {

    private static final long serialVersionUID = -8892805874851403584L;

    @Builder
    public FloatFieldValidation(final String name, final boolean nullable) {
        super(name, nullable);
    }
}
