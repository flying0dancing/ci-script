package com.lombardrisk.ignis.server.product.table.model;

import com.lombardrisk.ignis.api.table.validation.ValidatableDecimalField;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

import static com.lombardrisk.ignis.server.product.table.model.FieldTypes.DECIMAL;

@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@NoArgsConstructor
@Entity
@DiscriminatorValue(value = "decimal")
public class DecimalField extends Field<BigDecimal> implements ValidatableDecimalField {

    private static final long serialVersionUID = 3196685304820948693L;

    @Column(name = "DEC_SCALE")
    @NotNull
    private Integer scale;

    @Column(name = "DEC_PRECISION")
    @NotNull
    @Min(1)
    @Max(38)
    private Integer precision;

    @AssertTrue(message = "scale needs to be less than or equal to precision")
    boolean isScaleLessThanOrEqualToPrecision() {
        return scale != null &&
                precision != null &&
                scale <= precision;
    }

    @Builder
    public DecimalField(
            final Long id,
            final String name,
            final boolean nullable,
            final Integer scale,
            final Integer precision) {

        super(id, name, DECIMAL, nullable);
        this.scale = scale;
        this.precision = precision;
    }

    @Override
    public String toColumnType() {
        return "DECIMAL(" + getPrecision() + "," + getScale() + ")";
    }
}
