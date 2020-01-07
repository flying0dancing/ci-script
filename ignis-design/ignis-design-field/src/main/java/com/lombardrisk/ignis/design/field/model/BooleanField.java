package com.lombardrisk.ignis.design.field.model;

import com.lombardrisk.ignis.api.table.validation.ValidatableBooleanField;
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
@DiscriminatorValue(value = "boolean")
public class BooleanField extends Field<Boolean> implements ValidatableBooleanField {

    private static final long serialVersionUID = -861410092195378277L;

    @Builder
    public BooleanField(final Long id, final Long schemaId, final String name, final boolean nullable) {
        super(id, schemaId, name, nullable);
    }

    @Override
    public BooleanField copy() {
        return BooleanField.builder()
                .name(getName())
                .nullable(isNullable())
                .build();
    }

    @Override
    public Boolean generateData() {
        return RandomUtils.nextBoolean();
    }
}
