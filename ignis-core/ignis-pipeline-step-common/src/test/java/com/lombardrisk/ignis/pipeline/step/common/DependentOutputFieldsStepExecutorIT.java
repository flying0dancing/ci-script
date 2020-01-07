package com.lombardrisk.ignis.pipeline.step.common;

import com.lombardrisk.ignis.pipeline.step.api.SelectColumn;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class DependentOutputFieldsStepExecutorIT {

    private SparkSession sparkSession;

    @Before
    public void setUp() {
        sparkSession = SparkSession.builder().master("local[1]").getOrCreate();
    }

    @Test
    public void transform_IntermediateResults_CalculationRunsInCorrectOrder() {
        StructType structType = new StructType(new StructField[]{
                new StructField("Name", DataTypes.StringType, false, Metadata.empty()),
                new StructField("Salary", DataTypes.DoubleType, false, Metadata.empty()),
                new StructField("Department", DataTypes.StringType, false, Metadata.empty()),
                new StructField("TaxRate", DataTypes.DoubleType, false, Metadata.empty())
        });
        Row row = RowFactory.create("Dave", 100000.0, "Sales", 0.1);

        Dataset<Row> input = sparkSession.createDataFrame(singletonList(row), structType);

        List<SelectColumn> selects = Arrays.asList(
                SelectColumn.builder()
                        .select("Salary * (1- TaxRate)")
                        .as("TakeHome")
                        .intermediateResult(true)
                        .build(),
                SelectColumn.builder()
                        .select("TakeHome / 12")
                        .as("MonthlyTakeHome")
                        .intermediateResult(true)
                        .build(),
                SelectColumn.builder()
                        .select("concat(Name, ' works in ', Department, ' and takes home ', MonthlyTakeHome, ' a month')")
                        .as("MonthlyTakeHome")
                        .build());

        Dataset<Row> dataset = new DependentOutputFieldsStepExecutor()
                .transform(selects, input);

        assertThat(dataset.collectAsList())
                .contains(RowFactory.create(90000.0, 7500.0, "Dave works in Sales and takes home 7500.0 a month"));
    }

    @Test
    public void transform_IntermediateResultMarkedIncorrectly_ThrowsError() {
        StructType structType = new StructType(new StructField[]{
                new StructField("Name", DataTypes.StringType, false, Metadata.empty()),
                new StructField("Salary", DataTypes.DoubleType, false, Metadata.empty()),
                new StructField("Department", DataTypes.StringType, false, Metadata.empty()),
                new StructField("TaxRate", DataTypes.DoubleType, false, Metadata.empty())
        });
        Row row = RowFactory.create("Dave", 100000.0, "Sales", 0.1);

        Dataset<Row> input = sparkSession.createDataFrame(singletonList(row), structType);

        List<SelectColumn> selects = Arrays.asList(
                SelectColumn.builder()
                        .select("Salary * (1- TaxRate)")
                        .as("TakeHome")
                        .intermediateResult(false)
                        .build(),
                SelectColumn.builder()
                        .select("TakeHome / 12")
                        .as("MonthlyTakeHome")
                        .intermediateResult(false)
                        .build(),
                SelectColumn.builder()
                        .select("concat(Name, ' works in ', Department, ' and takes home ', MonthlyTakeHome, ' a month')")
                        .as("MonthlyTakeHome")
                        .build());

        assertThatThrownBy(() ->
                new DependentOutputFieldsStepExecutor()
                        .transform(selects, input)
                        .collectAsList())
                .hasMessageContaining("cannot resolve '`TakeHome`'");
    }
}
