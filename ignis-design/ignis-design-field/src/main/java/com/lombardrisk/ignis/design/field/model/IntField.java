package com.lombardrisk.ignis.design.field.model;

import com.lombardrisk.ignis.api.table.validation.ValidatableIntField;
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
@DiscriminatorValue(value = "integer")
public class IntField extends Field<Integer> implements ValidatableIntField {

    private static final long serialVersionUID = -3058323108502837951L;

    @Builder
    public IntField(final Long id, final Long schemaId, final String name, final boolean nullable) {
        super(id, schemaId, name, nullable);
    }

    @Override
    public IntField copy() {
        return IntField.builder()
                .name(getName())
                .nullable(isNullable())
                .build();
    }

    @Override
    public Integer generateData() {
        return RandomUtils.nextInt();
    }
}
