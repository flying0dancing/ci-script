package com.lombardrisk.ignis.server.config.calendar;

import com.lombardrisk.ignis.api.calendar.HolidayCalendar;
import com.lombardrisk.ignis.api.calendar.YearMonth;
import com.lombardrisk.ignis.client.external.calendar.CalendarRequest;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.data.common.service.JpaCRUDService;
import com.lombardrisk.ignis.server.product.productconfig.ProductConfigService;
import com.lombardrisk.ignis.server.product.productconfig.model.ProductConfig;
import io.vavr.control.Validation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.lombardrisk.ignis.api.calendar.HolidayCalendar.DEFAULT_WORKING_WEEK;
import static java.util.stream.Collectors.toList;

@Slf4j
public class CalendarService implements JpaCRUDService<CalendarHoliday> {

    private final ProductConfigService productConfigService;
    private final ProductWorkingDayService productWorkingDayService;
    private final CalendarHolidayRepository calendarHolidayRepository;

    public CalendarService(
            final ProductConfigService productConfigService,
            final ProductWorkingDayService productWorkingDayService,
            final CalendarHolidayRepository calendarHolidayRepository) {
        this.productConfigService = productConfigService;
        this.productWorkingDayService = productWorkingDayService;
        this.calendarHolidayRepository = calendarHolidayRepository;
    }

    @Override
    public JpaRepository<CalendarHoliday, Long> repository() {
        return calendarHolidayRepository;
    }

    @Override
    public String entityName() {
        return "Holiday";
    }

    public Validation<CRUDFailure, CalendarHoliday> create(final CalendarRequest calendarRequest) {
        String productName = calendarRequest.getProductName();
        Validation<CRUDFailure, ProductConfig> product = productConfigService.findByName(productName);
        if (product.isInvalid()) {
            log.error("Cannot find product by name [{}]", productName);
            return Validation.invalid(product.getError());
        }

        CalendarHoliday calendarHoliday = calendarHolidayRepository.save(CalendarHoliday.builder()
                .product(productName)
                .date(calendarRequest.getDate())
                .name(calendarRequest.getName())
                .build());

        return Validation.valid(calendarHoliday);
    }

    public Validation<CRUDFailure, CalendarHoliday> update(
            final Long calendarId,
            final CalendarRequest calendarRequest) {
        Validation<CRUDFailure, CalendarHoliday> calendar = findWithValidation(calendarId);
        if (calendar.isInvalid()) {
            log.error("Cannot find CalendarHoliday for id [{}]", calendarId);
            return Validation.invalid(calendar.getError());
        }

        CalendarHoliday calendarHoliday = calendar.get();
        calendarHoliday.setDate(calendarRequest.getDate());
        calendarHoliday.setName(calendarRequest.getName());

        return Validation.valid(calendarHolidayRepository.save(calendarHoliday));
    }

    @Transactional
    public Validation<CRUDFailure, HolidayCalendar> createHolidayCalendar(
            final Long productId,
            final LocalDate startInclusive,
            final LocalDate endInclusive) {

        Validation<CRUDFailure, ProductConfig> product = productConfigService.findWithValidation(productId);
        if (product.isInvalid()) {
            log.error("Cannot find product by id [{}]", productId);
            return Validation.invalid(product.getError());
        }

        Stream<CalendarHoliday> holidays =
                calendarHolidayRepository.findAllByProductAndDateGreaterThanEqualAndDateLessThanEqual(
                        product.get().getName(), startInclusive, endInclusive);

        Map<YearMonth, List<Integer>> groupedHolidays = holidays.collect(
                Collectors.groupingBy(
                        calendarHoliday -> new YearMonth(
                                calendarHoliday.getDate().getYear(),
                                calendarHoliday.getDate().getMonth()),
                        Collectors.mapping(
                                calendarHoliday -> calendarHoliday.getDate()
                                        .getDayOfMonth(),
                                toList())));

        log.info("Found holidays for [{}] months", groupedHolidays.size());

        HashSet<Integer> workingDays = productWorkingDayService.findByProductName(product.get().getName())
                .map(DayOfWeek::getValue)
                .collect(Collectors.toCollection(HashSet::new));

        if (workingDays.isEmpty()) {
            workingDays = DEFAULT_WORKING_WEEK.stream()
                    .map(DayOfWeek::getValue)
                    .collect(Collectors.toCollection(HashSet::new));
        }

        log.debug("The product [{}] has the following working days [{}]", product.get().getName(), workingDays);
        return Validation.valid(
                HolidayCalendar.builder()
                        .holidaysByYearAndMonth(groupedHolidays)
                        .workingDaysOfWeek(workingDays)
                        .build());
    }
}
