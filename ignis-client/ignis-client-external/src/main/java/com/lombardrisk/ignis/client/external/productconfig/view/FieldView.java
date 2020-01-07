package com.lombardrisk.ignis.client.external.productconfig.view;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Data
@Builder
public class FieldView {

    private final Long id;
    private final String name;
    private final Type fieldType;

    public static FieldView of(final String name, final Type fieldType) {
        return new FieldView(null, name, fieldType);
    }

    public enum Type {
        STRING,
        DATE,
        TIMESTAMP,
        DECIMAL,
        BOOLEAN,
        DOUBLE,
        FLOAT,
        INTEGER,
        LONG
    }
}
