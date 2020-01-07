package com.lombardrisk.ignis.server.product.table.model;

import com.lombardrisk.ignis.api.table.validation.ValidatableStringField;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import static org.apache.commons.lang3.StringUtils.EMPTY;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Getter
@Setter
@Entity
@DiscriminatorValue(value = "string")
public class StringField extends Field<String> implements ValidatableStringField {

    private static final long serialVersionUID = -6131362594992386011L;

    @Column(name = "MAX_LENGTH")
    private Integer maxLength;

    @Column(name = "MIN_LENGTH")
    private Integer minLength;

    @Column(name = "REGULAR_EXPRESSION")
    private String regularExpression;

    @Builder
    public StringField(
            final Long id,
            final String name,
            final boolean nullable,
            final Integer maxLength,
            final Integer minLength,
            final String regularExpression) {
        super(id, name, FieldTypes.STRING, nullable);
        this.maxLength = maxLength;
        this.minLength = minLength;
        this.regularExpression = regularExpression;
    }

    @Override
    public String toColumnType() {
        String precision = getMaxLength() != null ? "(" + getMaxLength() + ")" : EMPTY;
        return "VARCHAR" + precision;
    }
}
