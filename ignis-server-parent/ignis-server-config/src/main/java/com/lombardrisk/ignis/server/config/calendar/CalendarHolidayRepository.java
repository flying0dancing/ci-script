package com.lombardrisk.ignis.server.config.calendar;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

public interface CalendarHolidayRepository extends JpaRepository<CalendarHoliday, Long> {

    List<CalendarHoliday> findAllByProduct(final String productName);

    Stream<CalendarHoliday> findAllByProductAndDateGreaterThanEqualAndDateLessThanEqual(
            final String product,
            final LocalDate start,
            final LocalDate end);
}
