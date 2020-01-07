package com.lombardrisk.ignis.server.product.table.model;

import com.lombardrisk.ignis.api.table.validation.ValidatableBooleanField;
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
@DiscriminatorValue(value = "boolean")
public class BooleanField extends Field<Boolean> implements ValidatableBooleanField {

    private static final long serialVersionUID = -861410092195378277L;

    @Builder
    public BooleanField(final Long id, final String name, final boolean nullable) {
        super(id, name, FieldTypes.BOOLEAN, nullable);
    }

    @Override
    public String toColumnType() {
        return "BOOLEAN";
    }
}
