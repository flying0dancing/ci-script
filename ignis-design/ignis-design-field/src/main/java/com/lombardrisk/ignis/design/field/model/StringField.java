package com.lombardrisk.ignis.design.field.model;

import com.lombardrisk.ignis.api.table.validation.ValidatableStringField;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.RandomStringUtils;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Getter
@Setter
@Entity
@DiscriminatorValue(value = "string")
public class StringField extends Field<String> implements ValidatableStringField {

    private static final long serialVersionUID = -6131362594992386011L;
    private static final int RANDOM_STRING_SIZE = 10;

    @Column(name = "MAX_LENGTH")
    private Integer maxLength;

    @Column(name = "MIN_LENGTH")
    private Integer minLength;

    @Column(name = "REGULAR_EXPRESSION")
    private String regularExpression;

    @Builder
    public StringField(
            final Long id,
            final Long schemaId,
            final String name,
            final boolean nullable,
            final Integer maxLength,
            final Integer minLength,
            final String regularExpression) {
        super(id, schemaId, name, nullable);
        this.maxLength = maxLength;
        this.minLength = minLength;
        this.regularExpression = regularExpression;
    }


    @Override
    public StringField copy() {
        return StringField.builder()
                .name(getName())
                .nullable(isNullable())
                .maxLength(this.maxLength)
                .minLength(this.minLength)
                .regularExpression(this.regularExpression)
                .build();
    }

    @Override
    public String generateData() {
        return RandomStringUtils.random(RANDOM_STRING_SIZE);
    }
}
