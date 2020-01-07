package com.lombardrisk.ignis.spark.api.staging.field;

import com.lombardrisk.ignis.api.table.validation.ValidatableDecimalField;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

@ToString(callSuper = true)
@NoArgsConstructor
@Getter
@EqualsAndHashCode(callSuper = true)
@Data
public class DecimalFieldValidation extends FieldValidation<BigDecimal> implements ValidatableDecimalField {

    private static final long serialVersionUID = 271652267601035142L;

    private Integer scale;
    private Integer precision;

    @Builder
    public DecimalFieldValidation(
            final String name,
            final boolean nullable,
            final Integer scale,
            final Integer precision) {
        super(name, nullable);
        this.scale = scale;
        this.precision = precision;
    }
}
