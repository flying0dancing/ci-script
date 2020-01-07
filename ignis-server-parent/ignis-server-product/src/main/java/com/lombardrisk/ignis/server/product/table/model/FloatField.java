package com.lombardrisk.ignis.server.product.table.model;

import com.lombardrisk.ignis.api.table.validation.ValidatableFloatField;
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
@DiscriminatorValue(value = "float")
public class FloatField extends Field<Float> implements ValidatableFloatField {

    private static final long serialVersionUID = 9185106912067197887L;

    @Builder
    public FloatField(final Long id, final String name, final boolean nullable) {
        super(id, name, FieldTypes.FLOAT, nullable);
    }

    @Override
    public String toColumnType() {
        return "FLOAT";
    }
}
