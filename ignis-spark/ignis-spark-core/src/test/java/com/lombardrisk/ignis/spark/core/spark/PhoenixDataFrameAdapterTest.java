package com.lombardrisk.ignis.spark.core.spark;

import com.google.common.collect.ImmutableList;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.catalyst.encoders.RowEncoder;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class PhoenixDataFrameAdapterTest {

    @Test
    public void handleLowerCaseColumns_DatasetHasLowercaseAndAllUpperCaseColumns_ConvertsOnlyColumnsWithLowercase() {
        SparkSession session = SparkSession.builder()
                .master("local[*]")
                .getOrCreate();

        List<Row> gandalf = ImmutableList.of(
                RowFactory.create(1L, "Do", "Not"),
                RowFactory.create(2L, "It", "Should"));
        Dataset<Row> dataset = session.createDataset(gandalf, RowEncoder.apply(new StructType(new StructField[]{
                DataTypes.createStructField("id", DataTypes.LongType, false, Metadata.empty()),
                DataTypes.createStructField("COL_1", DataTypes.StringType, false, Metadata.empty()),
                DataTypes.createStructField("Col_2", DataTypes.StringType, false, Metadata.empty())
        })));

        assertThat(PhoenixDataFrameAdapter.handleLowerCaseColumns(dataset).columns())
                .containsExactly("\"id\"", "COL_1", "\"Col_2\"");
    }
}
