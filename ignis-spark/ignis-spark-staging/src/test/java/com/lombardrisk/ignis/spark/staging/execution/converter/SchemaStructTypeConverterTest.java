package com.lombardrisk.ignis.spark.staging.execution.converter;

import com.lombardrisk.ignis.spark.api.fixture.Populated;
import com.lombardrisk.ignis.spark.api.staging.StagingSchemaValidation;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.junit.Test;

import java.util.Arrays;

import static com.google.common.collect.Sets.newLinkedHashSet;
import static org.apache.spark.sql.types.DataTypes.BooleanType;
import static org.apache.spark.sql.types.DataTypes.DateType;
import static org.apache.spark.sql.types.DataTypes.DoubleType;
import static org.apache.spark.sql.types.DataTypes.FloatType;
import static org.apache.spark.sql.types.DataTypes.IntegerType;
import static org.apache.spark.sql.types.DataTypes.LongType;
import static org.apache.spark.sql.types.DataTypes.StringType;
import static org.apache.spark.sql.types.DataTypes.TimestampType;
import static org.apache.spark.sql.types.DataTypes.createDecimalType;
import static org.apache.spark.sql.types.DataTypes.createStructField;
import static org.apache.spark.sql.types.Metadata.empty;
import static org.assertj.core.api.Assertions.assertThat;

public class SchemaStructTypeConverterTest {

    @Test
    public void apply_AllFieldTypes_CreatesStructType() {
        StagingSchemaValidation schemaValidation = Populated.stagingSchemaValidation()
                .fields(newLinkedHashSet(
                        Arrays.asList(
                                Populated.booleanFieldValidation("boolean").nullable(false).build(),
                                Populated.dateFieldValidation("date").nullable(true).build(),
                                Populated.decimalFieldValidation("decimal").precision(10).scale(2).build(),
                                Populated.doubleFieldValidation("double").build(),
                                Populated.floatFieldValidation("float").build(),
                                Populated.intFieldValidation("int").build(),
                                Populated.longFieldValidation("long").build(),
                                Populated.stringFieldValidation("string").build(),
                                Populated.timestampFieldValidation("timestamp").build())))
                .build();

        StructType structType = new SchemaStructTypeConverter().apply(schemaValidation);

        assertThat(structType).isEqualTo(new StructType(new StructField[]{
                createStructField("boolean", BooleanType, false, empty()),
                createStructField("date", DateType, true, empty()),
                createStructField("decimal", createDecimalType(10, 2), false, empty()),
                createStructField("double", DoubleType, false, empty()),
                createStructField("float", FloatType, false, empty()),
                createStructField("int", IntegerType, false, empty()),
                createStructField("long", LongType, false, empty()),
                createStructField("string", StringType, false, empty()),
                createStructField("timestamp", TimestampType, false, empty())
        }));
    }
}