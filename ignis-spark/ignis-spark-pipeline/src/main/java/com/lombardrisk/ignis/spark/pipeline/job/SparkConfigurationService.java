package com.lombardrisk.ignis.spark.pipeline.job;

import com.lombardrisk.ignis.api.calendar.HolidayCalendar;
import com.lombardrisk.ignis.spark.api.pipeline.spark.SparkFunctionConfig;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.expressions.UserDefinedFunction;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.DataTypes;
import scala.Option;
import scala.collection.JavaConversions;
import scala.collection.Seq;
import scala.compat.java8.JFunction0;
import scala.compat.java8.JFunction2;

import java.sql.Date;
import java.util.Arrays;
import java.util.Collections;

@Slf4j
@AllArgsConstructor
public class SparkConfigurationService {

    private final SparkSession sparkSession;

    public void setupUserDefinedFunctions(final SparkFunctionConfig sparkFunctionConfig) {
        log.info("Setting up user defined functions");
        setUpReferenceDateFunction(sparkFunctionConfig);

        if (sparkFunctionConfig.getHolidayCalendar() != null) {
            setupHolidayCalendarFunction(sparkFunctionConfig.getHolidayCalendar());
        }
    }

    private void setupHolidayCalendarFunction(final HolidayCalendar holidayCalendar) {
        JFunction2<Date, Date, Integer> f2 = holidayCalendar::workingDaysBetween;
        Seq<DataType> inputTypes = JavaConversions.asScalaBuffer(Arrays.asList(
                DataTypes.DateType,
                DataTypes.DateType));

        Option<Seq<DataType>> apply = Option.apply(inputTypes);
        UserDefinedFunction workingDaysFunction = new UserDefinedFunction(f2, DataTypes.IntegerType, apply);
        sparkSession.udf().register("workingDaysBetween", workingDaysFunction);
    }

    private void setUpReferenceDateFunction(final SparkFunctionConfig sparkFunctionConfig) {
        if (sparkFunctionConfig.getReferenceDate() != null) {
            Date referenceDate = Date.valueOf(sparkFunctionConfig.getReferenceDate());
            JFunction0<Date> f0 = () -> referenceDate;

            Option<Seq<DataType>> inputs = Option.apply(JavaConversions.asScalaBuffer(Collections.emptyList()));

            UserDefinedFunction userDefinedFunction =
                    new UserDefinedFunction(f0, DataTypes.DateType, inputs);
            sparkSession.udf().register("get_reference_date", userDefinedFunction);
        }
    }
}
