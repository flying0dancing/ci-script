package com.lombardrisk.ignis.spark.pipeline.job;

import com.lombardrisk.ignis.api.calendar.HolidayCalendar;
import com.lombardrisk.ignis.api.calendar.YearMonth;
import com.lombardrisk.ignis.spark.api.pipeline.spark.SparkFunctionConfig;
import com.lombardrisk.ignis.spark.pipeline.TestPipelineApplication;
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
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.Date;
import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { TestPipelineApplication.class, })
public class SparkConfigurationServiceIT {

    @Rule
    public JUnitSoftAssertions soft = new JUnitSoftAssertions();

    @Autowired
    private SparkSession sparkSession;

    private SparkConfigurationService sparkConfigurationService;

    @Before
    public void setUp() {
        sparkConfigurationService = new SparkConfigurationService(sparkSession);
    }

    @Test
    public void workingDaysBetweenUDF_returnsWorkingDays() {
        sparkConfigurationService.setupUserDefinedFunctions(SparkFunctionConfig.builder()
                .holidayCalendar(HolidayCalendar.builder()
                        .holiday(new YearMonth(2018, Month.DECEMBER), Arrays.asList(24, 25))
                        .holiday(new YearMonth(2019, Month.JANUARY), Collections.singletonList(1))
                        .build())
                .build());

        Date firstJun = toDate(LocalDate.of(2018, 6, 1));
        Date firstJan = toDate(LocalDate.of(2019, 1, 1));

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
                .containsExactly(150);
    }

    private Date toDate(final LocalDate of) {
        return Date.valueOf(of);
    }
}
