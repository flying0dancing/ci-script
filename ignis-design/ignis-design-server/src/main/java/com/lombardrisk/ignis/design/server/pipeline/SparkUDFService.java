package com.lombardrisk.ignis.design.server.pipeline;

import com.lombardrisk.ignis.api.calendar.HolidayCalendar;
import lombok.AllArgsConstructor;
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
import java.time.LocalDate;
import java.util.Arrays;

@AllArgsConstructor
public class SparkUDFService {

    private final SparkSession sparkSession;

    public void registerUdfs(final LocalDate testReferenceDate) {
        UserDefinedFunction referenceDateFunction = getReferenceDateFunction(testReferenceDate);
        sparkSession.udf().register("get_reference_date", referenceDateFunction);

        setupHolidayCalendarFunction();
    }

    private void setupHolidayCalendarFunction() {
        HolidayCalendar holidayCalendar = HolidayCalendar.builder().build();
        JFunction2<Date, Date, Integer> f2 = holidayCalendar::workingDaysBetween;
        Seq<DataType> inputTypes = JavaConversions.asScalaBuffer(Arrays.asList(
                DataTypes.DateType,
                DataTypes.DateType));

        Option<Seq<DataType>> apply = Option.apply(inputTypes);
        UserDefinedFunction workingDaysFunction = new UserDefinedFunction(f2, DataTypes.IntegerType, apply);
        sparkSession.udf().register("workingDaysBetween", workingDaysFunction);
    }

    private UserDefinedFunction getReferenceDateFunction(final LocalDate testReferenceDate) {
        JFunction0<Date> referenceDateFunction = () ->
                Date.valueOf(testReferenceDate != null ? testReferenceDate : LocalDate.now());

        return new UserDefinedFunction(referenceDateFunction, DataTypes.DateType, Option.empty());
    }
}
