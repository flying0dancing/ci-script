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

public class FieldExportConverter implements Function1<FieldExport, Field> {

    private static final long serialVersionUID = -4433184252426870123L;

    @Override
    public Field apply(final FieldExport fieldExport) {
        if (FieldExport.DecimalFieldExport.class.isAssignableFrom(fieldExport.getClass())) {
            return buildField((FieldExport.DecimalFieldExport) fieldExport);
        }

        if (FieldExport.DateFieldExport.class.isAssignableFrom(fieldExport.getClass())) {
            return buildField((FieldExport.DateFieldExport) fieldExport);
        }

        if (FieldExport.TimestampFieldExport.class.isAssignableFrom(fieldExport.getClass())) {
            return buildField((FieldExport.TimestampFieldExport) fieldExport);
        }

        if (FieldExport.StringFieldExport.class.isAssignableFrom(fieldExport.getClass())) {
            return buildField((FieldExport.StringFieldExport) fieldExport);
        }

        if (FieldExport.BooleanFieldExport.class.isAssignableFrom(fieldExport.getClass())) {
            return buildField((FieldExport.BooleanFieldExport) fieldExport);
        }

        if (FieldExport.DoubleFieldExport.class.isAssignableFrom(fieldExport.getClass())) {
            return buildField((FieldExport.DoubleFieldExport) fieldExport);
        }

        if (FieldExport.FloatFieldExport.class.isAssignableFrom(fieldExport.getClass())) {
            return buildField((FieldExport.FloatFieldExport) fieldExport);
        }

        if (FieldExport.IntegerFieldExport.class.isAssignableFrom(fieldExport.getClass())) {
            return buildField((FieldExport.IntegerFieldExport) fieldExport);
        }

        if (FieldExport.LongFieldExport.class.isAssignableFrom(fieldExport.getClass())) {
            return buildField((FieldExport.LongFieldExport) fieldExport);
        }

        throw new IllegalArgumentException("Field [" + fieldExport.getClass().getSimpleName() + "] is not of a recognised type");
    }

    private DecimalField buildField(final FieldExport.DecimalFieldExport fieldView) {
        return DecimalField.builder()
                .name(fieldView.getName())
                .nullable(fieldView.isNullable())
                .scale(fieldView.getScale())
                .precision(fieldView.getPrecision())
                .build();
    }

    private DateField buildField(final FieldExport.DateFieldExport fieldView) {
        return DateField.builder()
                .name(fieldView.getName())
                .nullable(fieldView.isNullable())
                .format(fieldView.getFormat())
                .build();
    }

    private TimestampField buildField(final FieldExport.TimestampFieldExport fieldView) {
        return TimestampField.builder()
                .name(fieldView.getName())
                .nullable(fieldView.isNullable())
                .format(fieldView.getFormat())
                .build();
    }

    private StringField buildField(final FieldExport.StringFieldExport fieldView) {
        return StringField.builder()
                .name(fieldView.getName())
                .nullable(fieldView.isNullable())
                .maxLength(fieldView.getMaxLength())
                .minLength(fieldView.getMinLength())
                .regularExpression(fieldView.getRegularExpression())
                .build();
    }

    private BooleanField buildField(final FieldExport.BooleanFieldExport fieldView) {
        return BooleanField.builder()
                .name(fieldView.getName())
                .nullable(fieldView.isNullable())
                .build();
    }

    private DoubleField buildField(final FieldExport.DoubleFieldExport fieldView) {
        return DoubleField.builder()
                .name(fieldView.getName())
                .nullable(fieldView.isNullable())
                .build();
    }

    private FloatField buildField(final FieldExport.FloatFieldExport fieldView) {
        return FloatField.builder()
                .name(fieldView.getName())
                .nullable(fieldView.isNullable())
                .build();
    }

    private IntField buildField(final FieldExport.IntegerFieldExport fieldView) {
        return IntField.builder()
                .name(fieldView.getName())
                .nullable(fieldView.isNullable())
                .build();
    }

    private LongField buildField(final FieldExport.LongFieldExport fieldView) {
        return LongField.builder()
                .name(fieldView.getName())
                .nullable(fieldView.isNullable())
                .build();
    }
}
