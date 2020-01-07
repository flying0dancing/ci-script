package com.lombardrisk.ignis.server.product.table.model;

import com.lombardrisk.ignis.api.table.validation.ValidatableDoubleField;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    public DoubleField(final Long id, final String name, final boolean nullable) {
        super(id, name, FieldTypes.DOUBLE, nullable);
    }

    @Override
    public String toColumnType() {
        return "DOUBLE";
    }
}
