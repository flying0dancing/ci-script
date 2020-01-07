package com.lombardrisk.ignis.design.field.model;

import com.lombardrisk.ignis.api.table.validation.ValidatableDoubleField;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.RandomUtils;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Getter
@Setter
@NoArgsConstructor
@Entity
@DiscriminatorValue(value = "double")
public class DoubleField extends Field<Double> implements ValidatableDoubleField {

    private static final long serialVersionUID = 8454812749123898666L;

    @Builder
    public DoubleField(final Long id, final Long schemaId, final String name, final boolean nullable) {
        super(id, schemaId, name, nullable);
    }

    @Override
    public DoubleField copy() {
        return DoubleField.builder()
                .name(getName())
                .nullable(isNullable())
                .build();
    }

    @Override
    public Double generateData() {
        return RandomUtils.nextDouble();
    }
}
