package com.lombardrisk.ignis.design.server.pipeline;

import com.lombardrisk.ignis.design.server.DesignStudioTestConfig;
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
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@DesignStudioTestConfig
public class SparkUDFServiceIT {

    @Autowired
    private SparkSession sparkSession;

    private SparkUDFService sparkUDFService;

    @Before
    public void setUp() {
        sparkUDFService = new SparkUDFService(sparkSession);
    }

    @Test
    public void setupWorkingDaysFunction_CalculatesWorkingDaysIgnoringHolidays() {
        sparkUDFService.registerUdfs(null);

        Date firstJun = Date.valueOf(LocalDate.of(2018, 6, 1));
        Date firstJan = Date.valueOf(LocalDate.of(2019, 1, 1));

        StructType structType = new StructType(new StructField[]{
                new StructField("start", DataTypes.DateType, false, Metadata.empty()),
                new StructField("end", DataTypes.DateType, false, Metadata.empty())
        });

        List<Row> data = Collections.singletonList(RowFactory.create(firstJun, firstJan));
        Dataset<Row> dataset = sparkSession.createDataFrame(data, structType);

        sparkSession.sqlContext().registerDataFrameAsTable(dataset, "input");

        assertThat(sparkSession.sql("SELECT workingDaysBetween(end, start) from input")
                .collectAsList())
                .extracting(row -> row.get(0))
                .containsExactly(153);
    }
}
