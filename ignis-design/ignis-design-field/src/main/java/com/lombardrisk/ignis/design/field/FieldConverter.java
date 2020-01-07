package com.lombardrisk.ignis.design.field;

import com.lombardrisk.ignis.client.design.schema.field.FieldDto;
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

public class FieldConverter implements Function1<Field, FieldDto> {

    private static final long serialVersionUID = -7889806233945286840L;

    @Override
    public FieldDto apply(final Field field) {
        if (DecimalField.class.isAssignableFrom(field.getClass())) {
            return buildFieldDto((DecimalField) field);
        }

        if (DateField.class.isAssignableFrom(field.getClass())) {
            return buildFieldDto((DateField) field);
        }

        if (TimestampField.class.isAssignableFrom(field.getClass())) {
            return buildFieldDto((TimestampField) field);
        }

        if (StringField.class.isAssignableFrom(field.getClass())) {
            return buildFieldDto((StringField) field);
        }

        if (BooleanField.class.isAssignableFrom(field.getClass())) {
            return buildFieldDto((BooleanField) field);
        }

        if (DoubleField.class.isAssignableFrom(field.getClass())) {
            return buildFieldDto((DoubleField) field);
        }

        if (FloatField.class.isAssignableFrom(field.getClass())) {
            return buildFieldDto((FloatField) field);
        }

        if (IntField.class.isAssignableFrom(field.getClass())) {
            return buildFieldDto((IntField) field);
        }

        if (LongField.class.isAssignableFrom(field.getClass())) {
            return buildFieldDto((LongField) field);
        }

        throw new IllegalArgumentException("Field ["
                + field.getClass().getSimpleName()
                + "] is not of a recognised type");
    }

    private FieldDto.DecimalFieldDto buildFieldDto(final DecimalField field) {
        return FieldDto.DecimalFieldDto.builder()
                .id(field.getId())
                .name(field.getName())
                .nullable(field.isNullable())
                .scale(field.getScale())
                .precision(field.getPrecision())
                .build();
    }

    private FieldDto.DateFieldDto buildFieldDto(final DateField field) {
        return FieldDto.DateFieldDto.builder()
                .id(field.getId())
                .name(field.getName())
                .nullable(field.isNullable())
                .format(field.getFormat())
                .build();
    }

    private FieldDto.TimestampFieldDto buildFieldDto(final TimestampField field) {
        return FieldDto.TimestampFieldDto.builder()
                .id(field.getId())
                .name(field.getName())
                .nullable(field.isNullable())
                .format(field.getFormat())
                .build();
    }

    private FieldDto.StringFieldDto buildFieldDto(final StringField field) {
        return FieldDto.StringFieldDto.builder()
                .id(field.getId())
                .name(field.getName())
                .nullable(field.isNullable())
                .maxLength(field.getMaxLength())
                .minLength(field.getMinLength())
                .regularExpression(field.getRegularExpression())
                .build();
    }

    private FieldDto.BooleanFieldDto buildFieldDto(final BooleanField field) {
        return FieldDto.BooleanFieldDto.builder()
                .id(field.getId())
                .name(field.getName())
                .nullable(field.isNullable())
                .build();
    }

    private FieldDto.DoubleFieldDto buildFieldDto(final DoubleField field) {
        return FieldDto.DoubleFieldDto.builder()
                .id(field.getId())
                .name(field.getName())
                .nullable(field.isNullable())
                .build();
    }

    private FieldDto.FloatFieldDto buildFieldDto(final FloatField field) {
        return FieldDto.FloatFieldDto.builder()
                .id(field.getId())
                .name(field.getName())
                .nullable(field.isNullable())
                .build();
    }

    private FieldDto.IntegerFieldDto buildFieldDto(final IntField field) {
        return FieldDto.IntegerFieldDto.builder()
                .id(field.getId())
                .name(field.getName())
                .nullable(field.isNullable())
                .build();
    }

    private FieldDto.LongFieldDto buildFieldDto(final LongField field) {
        return FieldDto.LongFieldDto.builder()
                .id(field.getId())
                .name(field.getName())
                .nullable(field.isNullable())
                .build();
    }
}
