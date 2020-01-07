package com.lombardrisk.ignis.server.product.table.view;

import com.lombardrisk.ignis.client.external.productconfig.view.FieldView;
import com.lombardrisk.ignis.server.product.table.model.Field;

import java.util.function.Function;

public class FieldToFieldView implements Function<Field, FieldView> {

    @Override
    public FieldView apply(final Field field) {
        return FieldView.builder()
                .id(field.getId())
                .name(field.getName())
                .fieldType(FieldView.Type.valueOf(field.getFieldType().toUpperCase()))
                .build();
    }
}
