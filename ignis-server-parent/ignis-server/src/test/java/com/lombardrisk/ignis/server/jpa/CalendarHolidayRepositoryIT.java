package com.lombardrisk.ignis.server.jpa;

import com.lombardrisk.ignis.server.config.IntegrationTestConfig;
import com.lombardrisk.ignis.server.config.calendar.CalendarHoliday;
import com.lombardrisk.ignis.server.config.calendar.CalendarHolidayRepository;
import com.lombardrisk.ignis.server.config.fixture.ServerConfig;
import com.lombardrisk.ignis.server.product.fixture.ProductPopulated;
import com.lombardrisk.ignis.server.product.productconfig.ProductConfigRepository;
import com.lombardrisk.ignis.server.product.productconfig.model.ProductConfig;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@IntegrationTestConfig
public class CalendarHolidayRepositoryIT {

    @Autowired
    private ProductConfigRepository productConfigRepository;
    @Autowired
    private CalendarHolidayRepository calendarHolidayRepository;
    @Autowired
    private EntityManager entityManager;

    @Rule
    public final JUnitSoftAssertions soft = new JUnitSoftAssertions();

    @Test
    public void saveAndRetrieve() {
        ProductConfig productConfig = productConfigRepository.save(ProductPopulated.productConfig("PRA").build());

        CalendarHoliday calendarHoliday = calendarHolidayRepository.save(ServerConfig.Populated.calendarHoliday()
                .product("PRA")
                .date(LocalDate.of(2001, 1, 1))
                .name("Milleniummmm")
                .build());

        entityManager.flush();
        entityManager.clear();

        CalendarHoliday retrieved = calendarHolidayRepository.getOne(calendarHoliday.getId());
        soft.assertThat(retrieved.getProduct())
                .isEqualTo("PRA");
        soft.assertThat(retrieved.getName())
                .isEqualTo("Milleniummmm");
        soft.assertThat(retrieved.getDate())
                .isEqualTo(LocalDate.of(2001, 1, 1));
    }

    @Test
    public void findAllByProductId_ReturnsOnlyHolidaysForProduct() {
        ProductConfig hongKong = productConfigRepository.save(ProductPopulated.productConfig("HK").build());
        ProductConfig uk = productConfigRepository.save(ProductPopulated.productConfig("UK").build());

        calendarHolidayRepository.save(ServerConfig.Populated.calendarHoliday()
                .product(uk.getName())
                .date(LocalDate.of(2019, 12, 25))
                .build());
        calendarHolidayRepository.save(ServerConfig.Populated.calendarHoliday()
                .product(uk.getName())
                .date(LocalDate.of(2019, 12, 26))
                .build());

        calendarHolidayRepository.save(ServerConfig.Populated.calendarHoliday()
                .product(hongKong.getName())
                .date(LocalDate.of(2019, 5, 5))
                .build());

        assertThat(calendarHolidayRepository.findAllByProduct(uk.getName()))
                .extracting(CalendarHoliday::getDate)
                .containsExactlyInAnyOrder(
                        LocalDate.of(2019, 12, 25),
                        LocalDate.of(2019, 12, 26));
    }

    @Test
    public void findByProductAndDates_ReturnsAllDatesInRange() {
        ProductConfig uk = productConfigRepository.save(ProductPopulated.productConfig("UK").build());
        calendarHolidayRepository.save(ServerConfig.Populated.calendarHoliday()
                .product(uk.getName())
                .date(LocalDate.of(2019, 12, 25))
                .build());
        calendarHolidayRepository.save(ServerConfig.Populated.calendarHoliday()
                .product(uk.getName())
                .date(LocalDate.of(2019, 12, 26))
                .build());
        calendarHolidayRepository.save(ServerConfig.Populated.calendarHoliday()
                .product(uk.getName())
                .date(LocalDate.of(2020, 1, 1))
                .build());

        List<CalendarHoliday> holidays =
                calendarHolidayRepository.findAllByProductAndDateGreaterThanEqualAndDateLessThanEqual(
                        "UK",
                        LocalDate.of(2019, 12, 25),
                        LocalDate.of(2020, 1, 1))
                        .collect(Collectors.toList());

        assertThat(holidays)
                .extracting(CalendarHoliday::getDate)
                .containsExactlyInAnyOrder(
                        LocalDate.of(2019, 12, 25),
                        LocalDate.of(2019, 12, 26),
                        LocalDate.of(2020, 1, 1));
    }
}
