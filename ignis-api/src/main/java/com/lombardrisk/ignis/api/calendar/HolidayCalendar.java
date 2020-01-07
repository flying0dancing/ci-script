package com.lombardrisk.ignis.api.calendar;

import com.google.common.collect.ImmutableSet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

import java.io.Serializable;
import java.sql.Date;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HolidayCalendar implements Serializable {

    private static final long serialVersionUID = -4284262071731306004L;
    public static final Set<DayOfWeek> DEFAULT_WORKING_WEEK = ImmutableSet.of(
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY);

    @Singular("holiday")
    private Map<YearMonth, List<Integer>> holidaysByYearAndMonth;

    private HashSet<Integer> workingDaysOfWeek;

    public int workingDaysBetween(final Date from, final Date to) {
        if (from == null || to == null) {
            return 0;
        }
        return workingDaysBetween(from.toLocalDate(), to.toLocalDate());
    }

    public int workingDaysBetween(final LocalDate from, final LocalDate to) {
        if (from == null || to == null) {
            return 0;
        }

        if (from.isBefore(to)) {
            return -calcDifference(to, from);
        }
        return calcDifference(from, to);
    }

    private int calcDifference(final LocalDate from, final LocalDate to) {
        return calculateWorkingDays(from, to, holidaysByYearAndMonth, workingDaysOfWeek);
    }

    private static int calculateWorkingDays(
            final LocalDate from,
            final LocalDate to,
            final Map<YearMonth, List<Integer>> holidaysByYearAndMonth,
            final HashSet<Integer> workingDaysOfWeek) {

        LocalDate currentDate = from;
        int numberOfDays = 0;
        YearMonth currentMonth = null;
        List<Integer> currentMonthHolidays = Collections.emptyList();

        if (from.equals(to)) {
            return 0;
        }

        while (currentDate.isAfter(to) || currentDate.equals(to)) {

            YearMonth yearMonth = new YearMonth(currentDate.getYear(), currentDate.getMonth());
            if (!yearMonth.equals(currentMonth)) {
                currentMonth = yearMonth;
                currentMonthHolidays = holidaysByYearAndMonth.getOrDefault(currentMonth, Collections.emptyList());
            }

            if (workingDaysOfWeek.contains(currentDate.getDayOfWeek().getValue())
                    && !currentMonthHolidays.contains(currentDate.getDayOfMonth())) {

                numberOfDays++;
            }

            currentDate = currentDate.minusDays(1);
        }

        return numberOfDays;
    }

    @SuppressWarnings("squid:S1068")
    public static class HolidayCalendarBuilder {

        private HashSet<Integer> workingDaysOfWeek = DEFAULT_WORKING_WEEK.stream()
                .map(DayOfWeek::getValue)
                .collect(Collectors.toCollection(HashSet::new));
    }
}
