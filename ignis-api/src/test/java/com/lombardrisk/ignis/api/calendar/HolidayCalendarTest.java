package com.lombardrisk.ignis.api.calendar;

import org.junit.Test;

import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;
import java.util.Collections;

import static com.google.common.collect.Sets.newHashSet;
import static org.assertj.core.api.Assertions.assertThat;

public class HolidayCalendarTest {

    @Test
    public void workingDaysBetween_ChristmasHolidays_ExcludesWeekendsAndHolidays() {
        LocalDate fifthJan = LocalDate.of(2019, 1, 5);
        LocalDate xmasEve = LocalDate.of(2018, 12, 24);
        int daysBetween = HolidayCalendar.builder()
                .holiday(new YearMonth(2018, Month.DECEMBER), Arrays.asList(25, 26))
                .holiday(new YearMonth(2019, Month.JANUARY), Collections.singletonList(1))
                .build()
                .workingDaysBetween(fifthJan, xmasEve);

        assertThat(daysBetween).isEqualTo(7);
    }

    @Test
    public void workingDaysBetween_ThursdayToSunday_ReturnsTwo() {
        LocalDate janThurs = LocalDate.of(2019, 1, 17);
        LocalDate janSun = LocalDate.of(2019, 1, 20);

        int daysBetween = HolidayCalendar.builder().build()
                .workingDaysBetween(janSun, janThurs);

        assertThat(daysBetween).isEqualTo(2);
    }

    @Test
    public void workingDaysBetween_ThursdayToMonday_ReturnsThree() {
        LocalDate janThurs = LocalDate.of(2019, 1, 17);
        LocalDate janMon = LocalDate.of(2019, 1, 21);

        int daysBetween = HolidayCalendar.builder().build()
                .workingDaysBetween(janMon, janThurs);

        assertThat(daysBetween).isEqualTo(3);
    }

    @Test
    public void workingDaysBetween_ThursdayToMondayAndMondayIsAHoliday_ReturnsTwo() {
        LocalDate janThurs = LocalDate.of(2019, 1, 17);
        LocalDate janMon = LocalDate.of(2019, 1, 21);

        int daysBetween = HolidayCalendar.builder()
                .holiday(new YearMonth(2019, Month.JANUARY), Collections.singletonList(21))
                .build()
                .workingDaysBetween(janMon, janThurs);

        assertThat(daysBetween).isEqualTo(2);
    }

    @Test
    public void workingDaysBetween_SameDay_ReturnsZero() {
        LocalDate janThurs = LocalDate.of(2019, 1, 17);

        int daysBetween = HolidayCalendar.builder()
                .holiday(new YearMonth(2019, Month.JANUARY), Collections.singletonList(21))
                .build()
                .workingDaysBetween(janThurs, janThurs);

        assertThat(daysBetween).isEqualTo(0);
    }

    @Test
    public void workingDaysBetween_AllHolidays_ReturnsZero() {
        LocalDate xmasDay = LocalDate.of(2018, 12, 25);
        LocalDate boxingDay = LocalDate.of(2018, 12, 26);

        int daysBetween = HolidayCalendar.builder()
                .holiday(new YearMonth(2018, Month.DECEMBER), Arrays.asList(25, 26))
                .build()
                .workingDaysBetween(xmasDay, boxingDay);

        assertThat(daysBetween).isEqualTo(0);
    }



    @Test
    public void workingDaysBetween_MonSunFourDayWorkingWeek_ReturnsZero() {
        LocalDate janMon = LocalDate.of(2019, 1, 7);
        LocalDate janSun = LocalDate.of(2019, 1, 13);

        int daysBetween = HolidayCalendar.builder()
                .holiday(new YearMonth(2018, Month.DECEMBER), Arrays.asList(25, 26))
                .workingDaysOfWeek(newHashSet(1,2,3,4))
                .build()
                .workingDaysBetween(janSun, janMon);

        assertThat(daysBetween).isEqualTo(4);
    }

}
