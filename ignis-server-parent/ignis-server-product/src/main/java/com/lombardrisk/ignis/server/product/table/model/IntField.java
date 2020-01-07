package com.lombardrisk.ignis.server.product.table.model;

import com.lombardrisk.ignis.api.table.validation.ValidatableIntField;
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
@DiscriminatorValue(value = "integer")
public class IntField extends Field<Integer> implements ValidatableIntField {

    private static final long serialVersionUID = -3058323108502837951L;

    @Builder
    public IntField(final Long id, final String name, final boolean nullable) {
        super(id, name, FieldTypes.INT, nullable);
    }

    @Override
    public String toColumnType() {
        return "INTEGER";
    }
}
