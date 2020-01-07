package com.lombardrisk.ignis.design.server.productconfig.export.converter;

import com.lombardrisk.ignis.client.external.productconfig.export.FieldExport;
import com.lombardrisk.ignis.common.function.IsoMorphicFunction1;
import com.lombardrisk.ignis.design.field.model.BooleanField;
import com.lombardrisk.ignis.design.field.model.DateField;
import com.lombardrisk.ignis.design.field.model.DecimalField;
import com.lombardrisk.ignis.design.field.model.DoubleField;
import com.lombardrisk.ignis.design.field.model.Field;
import com.lombardrisk.ignis.design.field.model.FloatField;
import com.lombardrisk.ignis.design.field.model.IntField;
import com.lombardrisk.ignis.design.field.model.LongField;
import com.lombardrisk.ignis.design.field.model.StringField;
import com.lombardrisk.ignis.design.field.model.TimestampField;
import io.vavr.Function1;

public class FieldExportConverter implements IsoMorphicFunction1<Field, FieldExport> {

    private static final long serialVersionUID = -7889806233945286840L;

    @Override
    public FieldExport apply(final Field field) {
        if (DecimalField.class.isAssignableFrom(field.getClass())) {
            return buildFieldExport((DecimalField) field);
        }

        if (DateField.class.isAssignableFrom(field.getClass())) {
            return buildFieldExport((DateField) field);
        }

        if (TimestampField.class.isAssignableFrom(field.getClass())) {
            return buildFieldExport((TimestampField) field);
        }

        if (StringField.class.isAssignableFrom(field.getClass())) {
            return buildFieldExport((StringField) field);
        }

        if (BooleanField.class.isAssignableFrom(field.getClass())) {
            return buildFieldExport((BooleanField) field);
        }

        if (DoubleField.class.isAssignableFrom(field.getClass())) {
            return buildFieldExport((DoubleField) field);
        }

        if (FloatField.class.isAssignableFrom(field.getClass())) {
            return buildFieldExport((FloatField) field);
        }

        if (IntField.class.isAssignableFrom(field.getClass())) {
            return buildFieldExport((IntField) field);
        }

        if (LongField.class.isAssignableFrom(field.getClass())) {
            return buildFieldExport((LongField) field);
        }

        throw new IllegalArgumentException("Field ["
                + field.getClass().getSimpleName()
                + "] is not of a recognised type");
    }

    @Override
    public Function1<FieldExport, Field> inverse() {
        return this::convertFieldExportToField;
    }

    public Field convertFieldExportToField(final FieldExport fieldExport) {
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

        throw new IllegalArgumentException("Field ["
                + fieldExport.getClass().getSimpleName()
                + "] is not of a recognised type");
    }

    private FieldExport.DecimalFieldExport buildFieldExport(final DecimalField field) {
        return FieldExport.DecimalFieldExport.builder()
                .id(field.getId())
                .name(field.getName())
                .nullable(field.isNullable())
                .scale(field.getScale())
                .precision(field.getPrecision())
                .build();
    }

    private DecimalField buildField(final FieldExport.DecimalFieldExport field) {
        return DecimalField.builder()
                .name(field.getName())
                .nullable(field.isNullable())
                .scale(field.getScale())
                .precision(field.getPrecision())
                .build();
    }

    private FieldExport.DateFieldExport buildFieldExport(final DateField field) {
        return FieldExport.DateFieldExport.builder()
                .id(field.getId())
                .name(field.getName())
                .nullable(field.isNullable())
                .format(field.getFormat())
                .build();
    }

    private DateField buildField(final FieldExport.DateFieldExport field) {
        return DateField.builder()
                .name(field.getName())
                .nullable(field.isNullable())
                .format(field.getFormat())
                .build();
    }

    private FieldExport.TimestampFieldExport buildFieldExport(final TimestampField field) {
        return FieldExport.TimestampFieldExport.builder()
                .id(field.getId())
                .name(field.getName())
                .nullable(field.isNullable())
                .format(field.getFormat())
                .build();
    }

    private TimestampField buildField(final FieldExport.TimestampFieldExport field) {
        return TimestampField.builder()
                .name(field.getName())
                .nullable(field.isNullable())
                .format(field.getFormat())
                .build();
    }

    private FieldExport.StringFieldExport buildFieldExport(final StringField field) {
        return FieldExport.StringFieldExport.builder()
                .id(field.getId())
                .name(field.getName())
                .nullable(field.isNullable())
                .maxLength(field.getMaxLength())
                .minLength(field.getMinLength())
                .regularExpression(field.getRegularExpression())
                .build();
    }

    private StringField buildField(final FieldExport.StringFieldExport field) {
        return StringField.builder()
                .name(field.getName())
                .nullable(field.isNullable())
                .maxLength(field.getMaxLength())
                .minLength(field.getMinLength())
                .regularExpression(field.getRegularExpression())
                .build();
    }

    private FieldExport.BooleanFieldExport buildFieldExport(final BooleanField field) {
        return FieldExport.BooleanFieldExport.builder()
                .id(field.getId())
                .name(field.getName())
                .nullable(field.isNullable())
                .build();
    }

    private BooleanField buildField(final FieldExport.BooleanFieldExport field) {
        return BooleanField.builder()
                .name(field.getName())
                .nullable(field.isNullable())
                .build();
    }

    private FieldExport.DoubleFieldExport buildFieldExport(final DoubleField field) {
        return FieldExport.DoubleFieldExport.builder()
                .id(field.getId())
                .name(field.getName())
                .nullable(field.isNullable())
                .build();
    }

    private DoubleField buildField(final FieldExport.DoubleFieldExport field) {
        return DoubleField.builder()
                .name(field.getName())
                .nullable(field.isNullable())
                .build();
    }

    private FieldExport.FloatFieldExport buildFieldExport(final FloatField field) {
        return FieldExport.FloatFieldExport.builder()
                .id(field.getId())
                .name(field.getName())
                .nullable(field.isNullable())
                .build();
    }

    private FloatField buildField(final FieldExport.FloatFieldExport field) {
        return FloatField.builder()
                .name(field.getName())
                .nullable(field.isNullable())
                .build();
    }

    private FieldExport.IntegerFieldExport buildFieldExport(final IntField field) {
        return FieldExport.IntegerFieldExport.builder()
                .id(field.getId())
                .name(field.getName())
                .nullable(field.isNullable())
                .build();
    }

    private IntField buildField(final FieldExport.IntegerFieldExport field) {
        return IntField.builder()
                .name(field.getName())
                .nullable(field.isNullable())
                .build();
    }

    private FieldExport.LongFieldExport buildFieldExport(final LongField field) {
        return FieldExport.LongFieldExport.builder()
                .id(field.getId())
                .name(field.getName())
                .nullable(field.isNullable())
                .build();
    }

    private LongField buildField(final FieldExport.LongFieldExport field) {
        return LongField.builder()
                .name(field.getName())
                .nullable(field.isNullable())
                .build();
    }
}
