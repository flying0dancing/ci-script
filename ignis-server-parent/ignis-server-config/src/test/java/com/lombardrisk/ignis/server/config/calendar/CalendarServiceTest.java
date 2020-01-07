package com.lombardrisk.ignis.server.config.calendar;

import com.lombardrisk.ignis.api.calendar.HolidayCalendar;
import com.lombardrisk.ignis.api.calendar.YearMonth;
import com.lombardrisk.ignis.client.external.calendar.CalendarRequest;
import com.lombardrisk.ignis.common.assertions.VavrAssert;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.server.product.fixture.ProductPopulated;
import com.lombardrisk.ignis.server.product.productconfig.ProductConfigService;
import io.vavr.control.Validation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

import static com.google.common.collect.Sets.newHashSet;
import static com.lombardrisk.ignis.api.calendar.HolidayCalendar.DEFAULT_WORKING_WEEK;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CalendarServiceTest {

    @Mock
    private CalendarHolidayRepository calendarHolidayRepository;

    @Mock
    private ProductWorkingDayService productWorkingDayService;

    @Mock
    private ProductConfigService productConfigService;

    @InjectMocks
    private CalendarService calendarService;

    @Before
    public void setUp() {
        when(productConfigService.findWithValidation(anyLong()))
                .thenReturn(Validation.valid(ProductPopulated.productConfig().build()));
        when(productConfigService.findByName(anyString()))
                .thenReturn(Validation.valid(ProductPopulated.productConfig().build()));
        when(productWorkingDayService.findByProductName(anyString()))
                .thenReturn(Stream.of(DEFAULT_WORKING_WEEK).flatMap(Collection::stream));
        when(calendarHolidayRepository.save(any()))
                .then(invocation -> {
                    CalendarHoliday holiday = invocation.getArgument(0);
                    if (holiday.getId() == null) {
                        holiday.setId(1L);
                    }
                    return holiday;
                });
    }

    @Test
    public void create_ProductDoesntExist_ReturnsError() {
        when(productConfigService.findByName(any()))
                .thenReturn(Validation.invalid(CRUDFailure.cannotFind("Product")
                        .with("name", "10")
                        .asFailure()));

        VavrAssert.assertFailed(calendarService.create(CalendarRequest.builder().name("10").build()))
                .withFailure(CRUDFailure.cannotFind("Product").with("name", "10").asFailure());
    }

    @Test
    public void create_CalendarValid_SavesCalendar() {
        CalendarRequest request = CalendarRequest.builder()
                .name("Xmas Day")
                .productName("ENG")
                .date(LocalDate.of(2019, 12, 25))
                .build();

        VavrAssert.assertValid(calendarService.create(request))
                .withResult(CalendarHoliday.builder()
                        .id(1L)
                        .product("ENG")
                        .date(LocalDate.of(2019, 12, 25))
                        .name("Xmas Day")
                        .build());
    }

    @Test
    public void update_CalendarDoesntExist_ReturnsError() {
        when(calendarHolidayRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        VavrAssert.assertFailed(calendarService.update(-1L, CalendarRequest.builder().build()))
                .withFailure(CRUDFailure.notFoundIds("Holiday", -1L));
    }

    @Test
    public void update_CalendarValid_SavesCalendar() {

        CalendarHoliday existingHoliday = CalendarHoliday.builder()
                .id(20L)
                .name("Xmas Day")
                .product("ENG")
                .date(LocalDate.of(2019, 12, 26))
                .build();

        when(calendarHolidayRepository.findById(anyLong()))
                .thenReturn(Optional.of(existingHoliday));

        CalendarRequest request = CalendarRequest.builder()
                .name("Boxing Day")
                .date(LocalDate.of(2019, 12, 26))
                .build();

        VavrAssert.assertValid(calendarService.update(20L, request))
                .withResult(CalendarHoliday.builder()
                        .id(20L)
                        .product("ENG")
                        .date(LocalDate.of(2019, 12, 26))
                        .name("Boxing Day")
                        .build());
    }

    @Test
    public void createHolidayCalendar_ProductNotFound_ReturnsError() {
        when(productConfigService.findWithValidation(anyLong()))
                .thenReturn(Validation.invalid(CRUDFailure.notFoundIds("Product", -10L)));

        VavrAssert.assertFailed(calendarService.createHolidayCalendar(-10L, null, null))
                .withFailure(CRUDFailure.notFoundIds("Product", -10L));
    }

    @Test
    public void createHolidayCalendar_MultipleHolidaysForProduct_ReturnsGroupedHolidayCalendar() {
        when(calendarHolidayRepository.findAllByProductAndDateGreaterThanEqualAndDateLessThanEqual(
                anyString(), any(), any())).thenReturn(Stream.of(
                CalendarHoliday.builder()
                        .date(LocalDate.of(2019, 1, 1))
                        .build(),
                CalendarHoliday.builder()
                        .date(LocalDate.of(2018, 12, 26))
                        .build(),
                CalendarHoliday.builder()
                        .date(LocalDate.of(2018, 12, 25))
                        .build()));

        HolidayCalendar result = VavrAssert.assertValid(calendarService.createHolidayCalendar(
                -10L,
                LocalDate.of(2019, 2, 1),
                LocalDate.of(2018, 6, 1)))
                .getResult();

        assertThat(result)
                .isEqualTo(HolidayCalendar.builder()
                        .holiday(new YearMonth(2019, Month.JANUARY), singletonList(1))
                        .holiday(new YearMonth(2018, Month.DECEMBER), Arrays.asList(26, 25))
                        .build());
    }

    @Test
    public void createHolidayCalendar_WorkingWeekForProduct_ReturnsHolidayCalendarWithWorkingWeekDefined() {
        when(calendarHolidayRepository.findAllByProductAndDateGreaterThanEqualAndDateLessThanEqual(
                anyString(), any(), any())).thenReturn(Stream.empty());

        when(productWorkingDayService.findByProductName(anyString()))
                .thenReturn(Stream.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY));

        HolidayCalendar result = VavrAssert.assertValid(calendarService.createHolidayCalendar(
                -10L,
                LocalDate.of(2019, 2, 1),
                LocalDate.of(2018, 6, 1)))
                .getResult();

        assertThat(result)
                .isEqualTo(HolidayCalendar.builder()
                        .workingDaysOfWeek(newHashSet(1, 2, 3))
                        .build());
    }
}
