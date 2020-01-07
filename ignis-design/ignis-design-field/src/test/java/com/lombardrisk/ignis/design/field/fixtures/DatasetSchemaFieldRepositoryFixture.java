package com.lombardrisk.ignis.design.field.fixtures;

import com.lombardrisk.ignis.design.field.api.DatasetSchemaFieldRepository;
import com.lombardrisk.ignis.design.field.api.FieldRepository;
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
import lombok.AllArgsConstructor;

import java.util.function.Function;

@AllArgsConstructor
public class DatasetSchemaFieldRepositoryFixture implements DatasetSchemaFieldRepository {

    private final FieldRepository fieldRepository;

    @Override
    public void updateDecimalField(
            final Long id, final String name, final Boolean nullable, final Integer precision, final Integer scale) {
        updateField(id, field -> {
            DecimalField decimalField = new DecimalField();
            decimalField.setId(field.getId());
            decimalField.setName(name);
            decimalField.setNullable(nullable);
            decimalField.setScale(scale);
            decimalField.setPrecision(precision);
            return decimalField;
        });
    }

    @Override
    public void updateStringField(
            final Long id,
            final String name,
            final Boolean nullable,
            final Integer maxLength,
            final Integer minLength,
            final String regularExpression) {

        updateField(id, field -> {
            StringField stringField = new StringField();
            stringField.setId(field.getId());
            stringField.setName(name);
            stringField.setNullable(nullable);
            stringField.setMinLength(minLength);
            stringField.setMaxLength(maxLength);
            stringField.setRegularExpression(regularExpression);
            return stringField;
        });
    }

    @Override
    public void updateDateField(
            final Long id, final String name, final Boolean nullable, final String fieldType, final String dateFormat) {

        updateField(id, field -> {
            if (fieldType.equals("timestamp")) {
                TimestampField timestampField = new TimestampField();
                timestampField.setId(field.getId());
                timestampField.setName(name);
                timestampField.setNullable(nullable);
                timestampField.setFormat(dateFormat);
                return timestampField;
            }

            DateField dateField = new DateField();
            dateField.setId(field.getId());
            dateField.setName(name);
            dateField.setNullable(nullable);
            dateField.setFormat(dateFormat);
            return dateField;
        });
    }

    @Override
    public void updateNumericField(
            final Long id, final String name, final Boolean nullable, final String fieldType) {

        updateField(id, field -> {
            Field numericField = createNumericField(fieldType);
            numericField.setId(field.getId());
            numericField.setName(name);
            numericField.setNullable(nullable);
            return numericField;
        });
    }

    private static Field createNumericField(final String fieldType) {
        if ("long".equals(fieldType)) {
            return new LongField();
        }
        if ("integer".equals(fieldType)) {
            return new IntField();
        }
        if ("double".equals(fieldType)) {
            return new DoubleField();
        }
        if ("float".equals(fieldType)) {
            return new FloatField();
        }
        if ("boolean".equals(fieldType)) {
            return new BooleanField();
        }

        throw new IllegalArgumentException("Field type not supported: " + fieldType);
    }

    private void updateField(final Long id, final Function<Field, Field> fieldUpdater) {
        fieldRepository.findById(id)
                .map(fieldUpdater)
                .map(fieldRepository::save);
    }
}
