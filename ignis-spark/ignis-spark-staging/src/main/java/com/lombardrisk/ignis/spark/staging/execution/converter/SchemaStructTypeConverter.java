package com.lombardrisk.ignis.spark.staging.execution.converter;

import com.lombardrisk.ignis.spark.api.staging.StagingSchemaValidation;
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
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import java.util.function.Function;

public class SchemaStructTypeConverter implements Function<StagingSchemaValidation, StructType> {

    @Override
    public StructType apply(final StagingSchemaValidation stagingSchemaValidation) {

        StructField[] fields = stagingSchemaValidation.getFields().stream()
                .map(SchemaStructTypeConverter::fieldToStructField)
                .toArray(StructField[]::new);

        return new StructType(fields);
    }

    private static StructField fieldToStructField(final FieldValidation field) {
        return DataTypes.createStructField(
                field.getName(), fieldToDataType(field), field.isNullable(), Metadata.empty());
    }

    private static DataType fieldToDataType(final FieldValidation field) {
        if (field instanceof BooleanFieldValidation) {
            return DataTypes.BooleanType;
        }

        if (field instanceof DateFieldValidation) {
            return DataTypes.DateType;
        }

        if (field instanceof DecimalFieldValidation) {
            DecimalFieldValidation decimalField = (DecimalFieldValidation) field;
            return DataTypes.createDecimalType(decimalField.getPrecision(), decimalField.getScale());
        }

        if (field instanceof DoubleFieldValidation) {
            return DataTypes.DoubleType;
        }

        if (field instanceof FloatFieldValidation) {
            return DataTypes.FloatType;
        }

        if (field instanceof IntegerFieldValidation) {
            return DataTypes.IntegerType;
        }
        if (field instanceof LongFieldValidation) {
            return DataTypes.LongType;
        }

        if (field instanceof StringFieldValidation) {
            return DataTypes.StringType;
        }

        if (field instanceof TimestampFieldValidation) {
            return DataTypes.TimestampType;
        }

        throw new IllegalArgumentException("FieldValidation type not supported " + field);
    }
}
