package com.lombardrisk.ignis.server.product.table.model;

import com.lombardrisk.ignis.api.table.validation.ValidatableLongField;
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
@DiscriminatorValue(value = "long")
public class LongField extends Field<Long> implements ValidatableLongField {

    private static final long serialVersionUID = -4646568966374564469L;

    @Builder
    public LongField(final Long id, final String name, final boolean nullable) {
        super(id, name, FieldTypes.LONG, nullable);
    }

    @Override
    public String toColumnType() {
        return "BIGINT";
    }
}
