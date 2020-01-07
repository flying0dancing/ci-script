package com.lombardrisk.ignis.spark.core.fixture;

import org.apache.spark.sql.AnalysisException;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.StructType;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class DatasetFixture {

    public Dataset<Row> createDataset(
            final SparkSession spark,
            final String schemaName,
            final StructType structType,
            final List<List<?>> rowValues) throws AnalysisException {

        Dataset<Row> dataFrame = spark.createDataFrame(
                rowValues.stream()
                        .map(rowItems -> RowFactory.create(rowItems.toArray()))
                        .collect(toList()),
                structType)
                .as(schemaName);

        dataFrame.createTempView(schemaName);
        return dataFrame;
    }
}
