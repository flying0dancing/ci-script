package com.lombardrisk.ignis.design.field.model;

import com.lombardrisk.ignis.api.table.validation.ValidatableFloatField;
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
@DiscriminatorValue(value = "float")
public class FloatField extends Field<Float> implements ValidatableFloatField {

    private static final long serialVersionUID = 9185106912067197887L;

    @Builder
    public FloatField(final Long id, final Long schemaId, final String name, final boolean nullable) {
        super(id, schemaId, name, nullable);
    }

    @Override
    public FloatField copy() {
        return FloatField.builder()
                .name(getName())
                .nullable(isNullable())
                .build();
    }

    @Override
    public Float generateData() {
        return RandomUtils.nextFloat();
    }
}
