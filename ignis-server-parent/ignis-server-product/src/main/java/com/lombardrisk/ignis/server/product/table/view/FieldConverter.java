package com.lombardrisk.ignis.server.product.table.view;

import com.lombardrisk.ignis.client.external.productconfig.export.FieldExport;
import com.lombardrisk.ignis.server.product.table.model.BooleanField;
import com.lombardrisk.ignis.server.product.table.model.DateField;
import com.lombardrisk.ignis.server.product.table.model.DecimalField;
import com.lombardrisk.ignis.server.product.table.model.DoubleField;
import com.lombardrisk.ignis.server.product.table.model.Field;
import com.lombardrisk.ignis.server.product.table.model.FloatField;
import com.lombardrisk.ignis.server.product.table.model.IntField;
import com.lombardrisk.ignis.server.product.table.model.LongField;
import com.lombardrisk.ignis.server.product.table.model.StringField;
import com.lombardrisk.ignis.server.product.table.model.TimestampField;
import io.vavr.Function1;

public class FieldConverter implements Function1<Field, FieldExport> {

    private static final long serialVersionUID = -3474579991528456809L;

    @Override
    public FieldExport apply(final Field field) {
        if (DecimalField.class.isAssignableFrom(field.getClass())) {
            return buildFieldView((DecimalField) field);
        }

        if (DateField.class.isAssignableFrom(field.getClass())) {
            return buildFieldView((DateField) field);
        }

        if (TimestampField.class.isAssignableFrom(field.getClass())) {
            return buildFieldView((TimestampField) field);
        }

        if (StringField.class.isAssignableFrom(field.getClass())) {
            return buildFieldView((StringField) field);
        }

        if (BooleanField.class.isAssignableFrom(field.getClass())) {
            return buildFieldView((BooleanField) field);
        }

        if (DoubleField.class.isAssignableFrom(field.getClass())) {
            return buildFieldView((DoubleField) field);
        }

        if (FloatField.class.isAssignableFrom(field.getClass())) {
            return buildFieldView((FloatField) field);
        }

        if (IntField.class.isAssignableFrom(field.getClass())) {
            return buildFieldView((IntField) field);
        }

        if (LongField.class.isAssignableFrom(field.getClass())) {
            return buildFieldView((LongField) field);
        }

        throw new IllegalArgumentException("Field [" + field.getClass().getSimpleName() + "] is not of a recognised type");
    }

    private FieldExport.DecimalFieldExport buildFieldView(final DecimalField field) {
        return FieldExport.DecimalFieldExport.builder()
                .id(field.getId())
                .name(field.getName())
                .nullable(field.isNullable())
                .scale(field.getScale())
                .precision(field.getPrecision())
                .build();
    }

    private FieldExport.DateFieldExport buildFieldView(final DateField field) {
        return FieldExport.DateFieldExport.builder()
                .id(field.getId())
                .name(field.getName())
                .nullable(field.isNullable())
                .format(field.getFormat())
                .build();
    }

    private FieldExport.TimestampFieldExport buildFieldView(final TimestampField field) {
        return FieldExport.TimestampFieldExport.builder()
                .id(field.getId())
                .name(field.getName())
                .nullable(field.isNullable())
                .format(field.getFormat())
                .build();
    }

    private FieldExport.StringFieldExport buildFieldView(final StringField field) {
        return FieldExport.StringFieldExport.builder()
                .id(field.getId())
                .name(field.getName())
                .nullable(field.isNullable())
                .maxLength(field.getMaxLength())
                .minLength(field.getMinLength())
                .regularExpression(field.getRegularExpression())
                .build();
    }

    private FieldExport.BooleanFieldExport buildFieldView(final BooleanField field) {
        return FieldExport.BooleanFieldExport.builder()
                .id(field.getId())
                .name(field.getName())
                .nullable(field.isNullable())
                .build();
    }

    private FieldExport.DoubleFieldExport buildFieldView(final DoubleField field) {
        return FieldExport.DoubleFieldExport.builder()
                .id(field.getId())
                .name(field.getName())
                .nullable(field.isNullable())
                .build();
    }

    private FieldExport.FloatFieldExport buildFieldView(final FloatField field) {
        return FieldExport.FloatFieldExport.builder()
                .id(field.getId())
                .name(field.getName())
                .nullable(field.isNullable())
                .build();
    }

    private FieldExport.IntegerFieldExport buildFieldView(final IntField field) {
        return FieldExport.IntegerFieldExport.builder()
                .id(field.getId())
                .name(field.getName())
                .nullable(field.isNullable())
                .build();
    }

    private FieldExport.LongFieldExport buildFieldView(final LongField field) {
        return FieldExport.LongFieldExport.builder()
                .id(field.getId())
                .name(field.getName())
                .nullable(field.isNullable())
                .build();
    }
}
