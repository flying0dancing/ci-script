package com.lombardrisk.ignis.spark.core.phoenix;

import com.google.common.collect.ImmutableList;
import com.lombardrisk.ignis.pipeline.step.api.DatasetFieldName;
import com.lombardrisk.ignis.spark.core.phoenix.ranged.PhoenixRowKeyDataFrame;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PhoenixRowKeyDataFrameTest {

    private SparkSession spark;

    @Rule
    public JUnitSoftAssertions soft = new JUnitSoftAssertions();

    @Before
    public void setUp() {
        spark = SparkSession.builder().appName("DataFrameHelperTest").master("local[*]").getOrCreate();
    }

    @Test
    public void addRowKeyField_dataFrame_without_rowKey() {
        StructField f1 = new StructField("id", DataTypes.IntegerType, true, Metadata.empty());
        StructField[] fields = { f1 };
        StructType structType = new StructType(fields);

        Row row1 = RowFactory.create(1);
        Dataset<Row> dataFrame = spark.createDataFrame(ImmutableList.of(row1), structType);

        PhoenixRowKeyDataFrame rowKeyDataFrame = new PhoenixRowKeyDataFrame(spark, dataFrame);
        Dataset<Row> newDF = rowKeyDataFrame.withRowKey(1L);

        assertThat(newDF.schema().fieldIndex(DatasetFieldName.ROW_KEY.getName())).isEqualTo(0);
    }

    @Test
    public void addRowKeyField_withNullValue_savesAndReturnsNull() {
        StructField f1 = new StructField("id", DataTypes.IntegerType, true, Metadata.empty());
        StructField f2 = new StructField("column", DataTypes.StringType, true, Metadata.empty());
        StructField[] fields = { f1, f2 };
        StructType structType = new StructType(fields);

        Row row1 = RowFactory.create(1, null);
        Dataset<Row> dataFrame = spark.createDataFrame(ImmutableList.of(row1), structType);

        PhoenixRowKeyDataFrame rowKeyDataFrame = new PhoenixRowKeyDataFrame(spark, dataFrame);
        Dataset<Row> newDF = rowKeyDataFrame.withRowKey(1L);

        assertThat(
                newDF.collectAsList()
                        .get(0)
                        .get(2))
                .isNull();
    }

    @Test
    public void addRowKeyField_dataFrame_with_rowKey() {
        StructField f1 = new StructField("name", DataTypes.StringType, true, Metadata.empty());
        StructField f2 = new StructField("id", DataTypes.IntegerType, true, Metadata.empty());
        StructField[] fields = { f1, f2 };
        StructType structType = new StructType(fields);

        Long newRunKey = 10L;

        Row row1 = RowFactory.create("test", 123);
        Dataset<Row> dataFrame = spark.createDataFrame(ImmutableList.of(row1), structType);

        PhoenixRowKeyDataFrame rowKeyDataFrame = new PhoenixRowKeyDataFrame(spark, dataFrame);
        Dataset<Row> newDF = rowKeyDataFrame.withRowKey(newRunKey);

        assertThat(newDF.schema().fieldIndex(DatasetFieldName.ROW_KEY.getName())).isEqualTo(0);
    }

    @Test
    public void withRowKey_ReturnsDatasetWithRowKeys() {
        StructType nameType = DataTypes.createStructType(ImmutableList.of(
                DataTypes.createStructField("id", DataTypes.LongType, false),
                DataTypes.createStructField("FirstName", DataTypes.StringType, true),
                DataTypes.createStructField("LastName", DataTypes.StringType, true)));

        ImmutableList<Row> data = ImmutableList.of(
                RowFactory.create(101L, "Homer", "Simpson"),
                RowFactory.create(102L, "Fred", "Flintstone"),
                RowFactory.create(103L, "Peter", "Griffin"));

        Dataset<Row> dataset = spark.createDataFrame(data, nameType);

        Dataset<Row> datasetWithRowKeys = new PhoenixRowKeyDataFrame(spark, dataset).withRowKey(1L);

        soft.assertThat(datasetWithRowKeys.schema().fieldNames())
                .containsExactly("ROW_KEY", "id", "FirstName", "LastName");

        soft.assertThat(datasetWithRowKeys.collectAsList())
                .containsExactly(
                        RowFactory.create(4294967296L, 101L, "Homer", "Simpson"),
                        RowFactory.create(4294967297L, 102L, "Fred", "Flintstone"),
                        RowFactory.create(4294967298L, 103L, "Peter", "Griffin"));
    }

    @Test
    public void withRowKeyStartingFrom_OffsetZero_ReturnsDatasetWithRowKeys() {
        StructType nameType = DataTypes.createStructType(ImmutableList.of(
                DataTypes.createStructField("id", DataTypes.LongType, false),
                DataTypes.createStructField("FirstName", DataTypes.StringType, true),
                DataTypes.createStructField("LastName", DataTypes.StringType, true)));

        ImmutableList<Row> data = ImmutableList.of(
                RowFactory.create(101L, "Homer", "Simpson"),
                RowFactory.create(102L, "Fred", "Flintstone"),
                RowFactory.create(103L, "Peter", "Griffin"));

        Dataset<Row> dataset = spark.createDataFrame(data, nameType);

        PhoenixRowKeyDataFrame phoenixDataFrame = new PhoenixRowKeyDataFrame(spark, dataset);

        Dataset<Row> datasetWithRowKeys = phoenixDataFrame.withRowKeyStartingFrom(0L, 1L);

        soft.assertThat(datasetWithRowKeys.schema().fieldNames())
                .containsExactly("ROW_KEY", "id", "FirstName", "LastName");

        soft.assertThat(datasetWithRowKeys.collectAsList())
                .containsExactly(
                        RowFactory.create(4294967296L, 101L, "Homer", "Simpson"),
                        RowFactory.create(4294967297L, 102L, "Fred", "Flintstone"),
                        RowFactory.create(4294967298L, 103L, "Peter", "Griffin"));

        soft.assertThat(datasetWithRowKeys.collectAsList())
                .containsExactlyElementsOf(phoenixDataFrame.withRowKey(1L).collectAsList());
    }

    @Test
    public void withRowKeyStartingFrom_OffsetNonZero_ReturnsDatasetWithRowKeys() {
        StructType nameType = DataTypes.createStructType(ImmutableList.of(
                DataTypes.createStructField("id", DataTypes.LongType, false),
                DataTypes.createStructField("FirstName", DataTypes.StringType, true),
                DataTypes.createStructField("LastName", DataTypes.StringType, true)));

        ImmutableList<Row> data = ImmutableList.of(
                RowFactory.create(101L, "Homer", "Simpson"),
                RowFactory.create(102L, "Fred", "Flintstone"),
                RowFactory.create(103L, "Peter", "Griffin"));

        Dataset<Row> dataset = spark.createDataFrame(data, nameType);

        Dataset<Row> datasetWithRowKeys = new PhoenixRowKeyDataFrame(spark, dataset).withRowKeyStartingFrom(3L, 1L);

        soft.assertThat(datasetWithRowKeys.schema().fieldNames())
                .containsExactly("ROW_KEY", "id", "FirstName", "LastName");

        soft.assertThat(datasetWithRowKeys.collectAsList())
                .containsExactly(
                        RowFactory.create(4294967299L, 101L, "Homer", "Simpson"),
                        RowFactory.create(4294967300L, 102L, "Fred", "Flintstone"),
                        RowFactory.create(4294967301L, 103L, "Peter", "Griffin"));
    }
}
