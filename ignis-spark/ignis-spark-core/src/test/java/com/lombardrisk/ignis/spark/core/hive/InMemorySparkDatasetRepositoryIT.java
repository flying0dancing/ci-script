package com.lombardrisk.ignis.spark.core.hive;

import com.google.common.collect.ImmutableList;
import com.lombardrisk.ignis.spark.TestApplication;
import com.lombardrisk.ignis.spark.api.DatasetTableLookup;
import com.lombardrisk.ignis.spark.core.phoenix.ranged.PhoenixTableRowKey;
import io.vavr.Tuple2;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class)
public class InMemorySparkDatasetRepositoryIT {

    @Autowired
    private InMemoryRowKeyedDatasetRepository inMemorySparkDatasetRepository;

    @Autowired
    private SparkSession sparkSession;

    @Autowired
    private JavaSparkContext javaSparkContext;

    @Test
    public void readDataFrame() throws Exception {
        List<Row> rowList = ImmutableList.of(RowFactory.create(1L));
        StructField structField =
                new StructField("RUN_KEY", DataTypes.LongType, false, Metadata.empty());
        StructType structType = new StructType(ImmutableList.of(structField).toArray(new StructField[]{}));
        org.apache.spark.sql.Dataset<Row> datasetRow =
                sparkSession.createDataFrame(javaSparkContext.parallelize(rowList), structType);

        datasetRow.createTempView("dataset");

        DatasetTableLookup dataset = DatasetTableLookup.builder()
                .datasetName("dataset")
                .predicate("1=1")
                .build();

        org.apache.spark.sql.Dataset<Row> row = inMemorySparkDatasetRepository.readDataFrame(dataset);

        assertThat(row.count()).isEqualTo(1L);
    }

    @Test
    public void saveDataFrame() {
        List<Row> rowList = ImmutableList.of(RowFactory.create("test", 1L));
        StructField structField1 = new StructField("test", DataTypes.StringType, false, Metadata.empty());
        StructField structField2 =
                new StructField("RUN_KEY", DataTypes.LongType, false, Metadata.empty());
        StructType structType = new StructType(new StructField[]{ structField1, structField2 });

        org.apache.spark.sql.Dataset<Row> datasetRow =
                sparkSession.createDataFrame(javaSparkContext.parallelize(rowList), structType);
        String datasetSchema = "THE_DATASET_SCHEMA";

        org.apache.spark.sql.Dataset<Row> result =
                inMemorySparkDatasetRepository.saveDataFrame(datasetRow, datasetSchema, 2876543L);

        assertThat(result.schema().fields().length)
                .isEqualTo(3);
    }

    @Test
    public void buildPredicate() {
        String predicate = inMemorySparkDatasetRepository.buildPredicate(1L);
        Tuple2<Long, Long> rowKeyRange = PhoenixTableRowKey.of(1L).getRowKeyRange();

        assertThat(predicate)
                .isEqualTo("ROW_KEY >= " + rowKeyRange._1 + " and ROW_KEY <= " + rowKeyRange._2);
    }
}