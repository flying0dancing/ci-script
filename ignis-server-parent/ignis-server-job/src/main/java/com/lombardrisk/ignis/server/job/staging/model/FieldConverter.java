package com.lombardrisk.ignis.server.job.staging.model;

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
import com.lombardrisk.ignis.spark.api.staging.field.BooleanFieldValidation;
import com.lombardrisk.ignis.spark.api.staging.field.DateFieldValidation;
import com.lombardrisk.ignis.spark.api.staging.field.DecimalFieldValidation;
import com.lombardrisk.ignis.spark.api.staging.field.DoubleFieldValidation;
import com.lombardrisk.ignis.spark.api.staging.field.FieldValidation;
import com.lombardrisk.ignis.spark.api.staging.field.FloatFieldValidation;
import com.lombardrisk.ignis.spark.api.staging.field.IntegerFieldValidation;
import com.lombardrisk.ignis.spark.api.staging.field.LongFieldValidation;
import com.lombardrisk.ignis.spark.api.staging.field.StringFieldValidation;
import com.lombardrisk.ignis.spark.api.staging.field.TimestampFieldValidation;
import io.vavr.Function1;

public class FieldConverter implements Function1<Field, FieldValidation> {

    private static final long serialVersionUID = -7889806233945286840L;

    @Override
    public FieldValidation apply(final Field field) {
        if (DecimalField.class.isAssignableFrom(field.getClass())) {
            return buildFieldValidation((DecimalField) field);
        }

        if (DateField.class.isAssignableFrom(field.getClass())) {
            return buildFieldValidation((DateField) field);
        }

        if (TimestampField.class.isAssignableFrom(field.getClass())) {
            return buildFieldValidation((TimestampField) field);
        }

        if (StringField.class.isAssignableFrom(field.getClass())) {
            return buildFieldValidation((StringField) field);
        }

        if (BooleanField.class.isAssignableFrom(field.getClass())) {
            return buildFieldValidation((BooleanField) field);
        }

        if (DoubleField.class.isAssignableFrom(field.getClass())) {
            return buildFieldValidation((DoubleField) field);
        }

        if (FloatField.class.isAssignableFrom(field.getClass())) {
            return buildFieldValidation((FloatField) field);
        }

        if (IntField.class.isAssignableFrom(field.getClass())) {
            return buildFieldValidation((IntField) field);
        }

        if (LongField.class.isAssignableFrom(field.getClass())) {
            return buildFieldValidation((LongField) field);
        }

        throw new IllegalArgumentException("Field ["
                + field.getClass().getSimpleName()
                + "] is not of a recognised type");
    }

    private DecimalFieldValidation buildFieldValidation(final DecimalField field) {
        return DecimalFieldValidation.builder()
                .name(field.getName())
                .nullable(field.isNullable())
                .scale(field.getScale())
                .precision(field.getPrecision())
                .build();
    }

    private DateFieldValidation buildFieldValidation(final DateField field) {
        return DateFieldValidation.builder()
                .name(field.getName())
                .nullable(field.isNullable())
                .format(field.getFormat())
                .build();
    }

    private TimestampFieldValidation buildFieldValidation(final TimestampField field) {
        return TimestampFieldValidation.builder()
                .name(field.getName())
                .nullable(field.isNullable())
                .format(field.getFormat())
                .build();
    }

    private StringFieldValidation buildFieldValidation(final StringField field) {
        return StringFieldValidation.builder()
                .name(field.getName())
                .nullable(field.isNullable())
                .maxLength(field.getMaxLength())
                .minLength(field.getMinLength())
                .regularExpression(field.getRegularExpression())
                .build();
    }

    private BooleanFieldValidation buildFieldValidation(final BooleanField field) {
        return BooleanFieldValidation.builder()
                .name(field.getName())
                .nullable(field.isNullable())
                .build();
    }

    private DoubleFieldValidation buildFieldValidation(final DoubleField field) {
        return DoubleFieldValidation.builder()
                .name(field.getName())
                .nullable(field.isNullable())
                .build();
    }

    private FloatFieldValidation buildFieldValidation(final FloatField field) {
        return FloatFieldValidation.builder()
                .name(field.getName())
                .nullable(field.isNullable())
                .build();
    }

    private IntegerFieldValidation buildFieldValidation(final IntField field) {
        return IntegerFieldValidation.builder()
                .name(field.getName())
                .nullable(field.isNullable())
                .build();
    }

    private LongFieldValidation buildFieldValidation(final LongField field) {
        return LongFieldValidation.builder()
                .name(field.getName())
                .nullable(field.isNullable())
                .build();
    }
}
