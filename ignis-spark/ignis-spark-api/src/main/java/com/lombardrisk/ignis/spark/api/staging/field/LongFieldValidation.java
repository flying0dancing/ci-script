package com.lombardrisk.ignis.spark.api.staging.field;

import com.lombardrisk.ignis.api.table.validation.ValidatableLongField;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString(callSuper = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Data
public class LongFieldValidation extends FieldValidation<Long> implements ValidatableLongField {

    private static final long serialVersionUID = -8610293518595920969L;

    @Builder
    public LongFieldValidation(final String name, final boolean nullable) {
        super(name, nullable);
    }
}
