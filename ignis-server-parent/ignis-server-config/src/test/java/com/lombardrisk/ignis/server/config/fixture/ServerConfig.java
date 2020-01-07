package com.lombardrisk.ignis.server.config.fixture;

import com.lombardrisk.ignis.api.calendar.HolidayCalendar;
import com.lombardrisk.ignis.api.calendar.YearMonth;
import com.lombardrisk.ignis.server.config.calendar.CalendarHoliday;
import com.lombardrisk.ignis.server.config.calendar.ProductWorkingDay;
import lombok.experimental.UtilityClass;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;
import java.util.Collections;

@UtilityClass
public class ServerConfig {

    @UtilityClass
    public static class Populated {

        public static CalendarHoliday.CalendarHolidayBuilder calendarHoliday() {
            return CalendarHoliday.builder()
                    .product("ENG")
                    .name("Christmas Day")
                    .date(LocalDate.of(1991, 12, 25));
        }

        public static ProductWorkingDay.ProductWorkingDayBuilder productWorkingDay() {
            return ProductWorkingDay.builder()
                    .product("ENG")
                    .dayOfWeek(DayOfWeek.MONDAY);
        }

        public static HolidayCalendar.HolidayCalendarBuilder holidayCalendar() {
            return HolidayCalendar.builder()
                    .holiday(new YearMonth(2019, Month.JANUARY), Collections.singletonList(1))
                    .holiday(new YearMonth(2018, Month.DECEMBER), Arrays.asList(25, 26));
        }
    }
}
