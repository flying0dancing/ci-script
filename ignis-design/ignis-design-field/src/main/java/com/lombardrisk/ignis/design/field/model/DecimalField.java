package com.lombardrisk.ignis.design.field.model;

import com.lombardrisk.ignis.api.table.validation.ValidatableDecimalField;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.RandomUtils;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.RoundingMode;

@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@NoArgsConstructor
@Entity
@DiscriminatorValue(value = "decimal")
public class DecimalField extends Field<BigDecimal> implements ValidatableDecimalField {

    private static final int TEN = 10;
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
            final Long schemaId,
            final String name,
            final boolean nullable,
            final Integer scale,
            final Integer precision) {
        super(id, schemaId, name, nullable);
        this.scale = scale;
        this.precision = precision;
    }

    @Override
    public DecimalField copy() {
        return DecimalField.builder()
                .name(getName())
                .nullable(isNullable())
                .scale(this.scale)
                .precision(this.precision)
                .build();
    }

    @Override
    public BigDecimal generateData() {
        double upperRandomValue = Math.max(1.0, Math.pow(TEN, scale - precision));
        BigDecimal randomDecimal = BigDecimal.valueOf(RandomUtils.nextDouble(0.0, upperRandomValue));

        //I know this is confusing im sorry, precision is the number of zeros after the decimal point
        //Scale is the number of zeroes total.
        return randomDecimal.setScale(precision, RoundingMode.HALF_UP);
    }
}
