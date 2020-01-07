package com.lombardrisk.ignis.spark.fixture;

import com.lombardrisk.ignis.spark.api.staging.DatasetProperties;
import com.lombardrisk.ignis.spark.core.schema.DatasetTableSchema;
import lombok.experimental.UtilityClass;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import java.time.LocalDate;
import java.util.Collections;

@UtilityClass
public class SparkCore {

    @UtilityClass
    public static class Populated {

        public static DatasetTableSchema.DatasetTableSchemaBuilder phoenixTableSchema() {
            return DatasetTableSchema.builder()
                    .structType(structType())
                    .primaryKeys(Collections.singletonList(structField()))
                    .tableName("user");
        }

        public static DatasetProperties.DatasetPropertiesBuilder datasetProperties() {
            return DatasetProperties.builder()
                    .entityCode("entityCode")
                    .referenceDate(LocalDate.of(2018, 1, 1));
        }

        public static StructType structType() {
            StructField id = structField();
            StructField name = new StructField("NAME", DataTypes.StringType, false, Metadata.empty());
            return new StructType(new StructField[]{ id, name });
        }

        private static StructField structField() {
            return new StructField("ID", DataTypes.LongType, false, Metadata.empty());
        }
    }
}
