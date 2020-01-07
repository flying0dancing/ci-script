package com.lombardrisk.ignis.spark.core.schema;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.junit.Test;

import static com.lombardrisk.ignis.pipeline.step.api.DatasetFieldName.ROW_KEY;
import static com.lombardrisk.ignis.spark.core.phoenix.PhoenixJdbcDialect.ROW_KEY_METADATA;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class DatasetTableSchemaTest {

    @Test
    public void rowKeyRangedSchema_RowKeyAlreadyPresent_DoesNotAdd() {
        StructField rowKey = new StructField("ROW_KEY", DataTypes.LongType, false, Metadata.empty());
        StructField name = new StructField("NAME", DataTypes.StringType, false, Metadata.empty());

        DatasetTableSchema phoenixTableSchema = DatasetTableSchema.rowKeyRangedSchema(
                "testTable",
                new StructType(new StructField[]{ name, rowKey }));

        assertThat(phoenixTableSchema.getColumns())
                .containsOnly(new Column("NAME"), new Column("ROW_KEY"));
    }

    @Test
    public void rowKeyRangedSchema_RowKeyColumnNotPresent_AddsRowKeyColumn() {
        StructField name = new StructField("NAME", DataTypes.StringType, false, Metadata.empty());
        StructField description = new StructField("DESCRIPTION", DataTypes.LongType, false, Metadata.empty());

        DatasetTableSchema phoenixTableSchema = DatasetTableSchema.rowKeyRangedSchema(
                "testTable",
                new StructType(new StructField[]{ name, description }));

        assertThat(phoenixTableSchema.getColumns())
                .containsOnly(new Column("ROW_KEY"), new Column("NAME"), new Column("DESCRIPTION"));
    }

    @Test
    public void isPrimaryKey_OnePrimaryKey_ReturnsTrue() {
        StructField id = new StructField("ID", DataTypes.LongType, false, Metadata.empty());
        StructField name = new StructField("NAME", DataTypes.StringType, false, Metadata.empty());

        DatasetTableSchema phoenixTableSchema = new DatasetTableSchema(
                "testTable",
                new StructType(new StructField[]{ id, name }),
                DatasetTableSchema.Type.DYNAMIC,
                singletonList(id));

        assertThat(phoenixTableSchema.isPrimaryKey(id))
                .isTrue();
        assertThat(phoenixTableSchema.isPrimaryKey(name))
                .isFalse();
    }

    @Test
    public void createTableDDL_validationResultsSchema_ReturnsTrue() {
        DatasetTableSchema tableSchema = DatasetTableSchema.validationResultsTableSchema();

        assertThat(tableSchema.createTableDDL(100))
                .isEqualTo("CREATE TABLE VALIDATION_RULE_RESULTS (DATASET_ROW_KEY BIGINT NOT NULL, "
                        + "VALIDATION_RULE_ID INTEGER NOT NULL, DATASET_ID BIGINT, RESULT_TYPE VARCHAR, "
                        + "ERROR_MESSAGE VARCHAR , CONSTRAINT pk PRIMARY KEY (DATASET_ROW_KEY, VALIDATION_RULE_ID)) "
                        + "SALT_BUCKETS=100");
    }

    @Test
    public void createTableDDL_rowKeySchema_ReturnsTrue() {
        StructField id = new StructField("ID", DataTypes.LongType, false, Metadata.empty());
        StructField name = new StructField("NAME", DataTypes.StringType, false, Metadata.empty());

        DatasetTableSchema tableSchema =
                DatasetTableSchema.rowKeyRangedSchema("test_table", new StructType(new StructField[]{ id, name }));

        assertThat(tableSchema.createTableDDL(100))
                .isEqualTo("CREATE TABLE test_table "
                        + "(ROW_KEY BIGINT NOT NULL, ID BIGINT, NAME VARCHAR , CONSTRAINT pk PRIMARY KEY (ROW_KEY)) SALT_BUCKETS=100");
    }

    @Test
    public void createTableDDL_NoPrimaryKeyDefined_DDLWithoutConstraint() {
        StructField id = new StructField("ID", DataTypes.LongType, false, Metadata.empty());

        DatasetTableSchema tableSchema = new DatasetTableSchema(
                "test_table",
                new StructType(new StructField[]{ id }),
                DatasetTableSchema.Type.DYNAMIC,
                emptyList());

        assertThat(tableSchema.createTableDDL(123))
                .isEqualTo("CREATE TABLE test_table (ID BIGINT ) SALT_BUCKETS=123");
    }

    @Test
    public void createTableDDL_UnMappedStructType_ThrowsIllegalArgumentException() {
        StructField theBigShort = DataTypes.createStructField("BIG_SHORT", DataTypes.ShortType, false);

        DatasetTableSchema tableSchema = new DatasetTableSchema(
                "test_table",
                new StructType(new StructField[]{ theBigShort }),
                DatasetTableSchema.Type.DYNAMIC,
                emptyList());

        assertThatThrownBy(() -> tableSchema.createTableDDL(123))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Can not get jdbc type for data type ShortType");
    }

    @Test
    public void isPrimaryKey_rowKeyField_ReturnsTrue() {
        StructField id = new StructField("ID", DataTypes.LongType, false, Metadata.empty());
        StructField name = new StructField("NAME", DataTypes.StringType, false, Metadata.empty());

        DatasetTableSchema tableSchema = DatasetTableSchema.rowKeyRangedSchema(
                "test_table", new StructType(new StructField[]{ id, name }));

        assertThat(tableSchema.isPrimaryKey(
                new StructField(ROW_KEY.getName(), DataTypes.LongType, false, ROW_KEY_METADATA)))
                .isTrue();
    }

    @Test
    public void isPrimaryKey_OtherField_ReturnsFalse() {
        StructField id = new StructField("ID", DataTypes.LongType, false, Metadata.empty());
        StructField name = new StructField("NAME", DataTypes.StringType, false, Metadata.empty());

        DatasetTableSchema tableSchema = DatasetTableSchema.rowKeyRangedSchema(
                "test_table", new StructType(new StructField[]{ id, name }));

        assertThat(tableSchema.isPrimaryKey(name))
                .isFalse();
    }

    @Test
    public void isPrimaryKey_NoPrimaryKeyDefined_ReturnsFalse() {
        StructField id = new StructField("ID", DataTypes.LongType, false, Metadata.empty());

        DatasetTableSchema tableSchema = new DatasetTableSchema(
                "test_table",
                new StructType(new StructField[]{ id }),
                DatasetTableSchema.Type.DYNAMIC,
                emptyList());

        assertThat(tableSchema.isPrimaryKey(id))
                .isFalse();
    }
}
