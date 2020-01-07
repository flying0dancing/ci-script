package com.lombardrisk.ignis.design.server.pipeline.converter;

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
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import java.util.Comparator;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.lombardrisk.ignis.pipeline.step.api.DatasetFieldName.ROW_KEY;

public class SchemaStructTypeConverter implements Function<Set<Field>, StructType> {

    @Override
    public StructType apply(final Set<Field> schema) {

        LongField rowKeyField = LongField.builder().id(-1L).name(ROW_KEY.name()).build();
        StructField[] fields = Stream.concat(Stream.of(rowKeyField), schema.stream())
                .sorted(Comparator.comparing(Field::getId))
                .map(SchemaStructTypeConverter::fieldToStructField)
                .toArray(StructField[]::new);

        return new StructType(fields);
    }

    public static StructField fieldToStructField(final Field field) {
        return DataTypes.createStructField(
                //Null value is ignored in pipeline steps as struct type is not applied
                //nullable field is only important for Staging
                field.getName(), fieldToDataType(field), true, Metadata.empty());
    }

    private static DataType fieldToDataType(final Field field) {
        if (field instanceof BooleanField) {
            return DataTypes.BooleanType;
        }

        if (field instanceof DateField) {
            return DataTypes.DateType;
        }

        if (field instanceof DecimalField) {
            DecimalField decimalField = (DecimalField) field;
            return DataTypes.createDecimalType(decimalField.getPrecision(), decimalField.getScale());
        }

        if (field instanceof DoubleField) {
            return DataTypes.DoubleType;
        }

        if (field instanceof FloatField) {
            return DataTypes.FloatType;
        }

        if (field instanceof IntField) {
            return DataTypes.IntegerType;
        }
        if (field instanceof LongField) {
            return DataTypes.LongType;
        }

        if (field instanceof StringField) {
            return DataTypes.StringType;
        }

        if (field instanceof TimestampField) {
            return DataTypes.TimestampType;
        }

        throw new IllegalArgumentException("Field type not supported " + field);
    }
}
