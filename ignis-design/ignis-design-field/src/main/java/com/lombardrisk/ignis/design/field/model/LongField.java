package com.lombardrisk.ignis.design.field.model;

import com.lombardrisk.ignis.api.table.validation.ValidatableLongField;
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
@DiscriminatorValue(value = "long")
public class LongField extends Field<Long> implements ValidatableLongField {

    private static final long serialVersionUID = -4646568966374564469L;

    @Builder
    public LongField(final Long id, final Long schemaId, final String name, final boolean nullable) {
        super(id, schemaId, name, nullable);
    }

    @Override
    public LongField copy() {
        return LongField.builder()
                .name(getName())
                .nullable(isNullable())
                .build();
    }

    @Override
    public Long generateData() {
        return RandomUtils.nextLong();
    }
}
