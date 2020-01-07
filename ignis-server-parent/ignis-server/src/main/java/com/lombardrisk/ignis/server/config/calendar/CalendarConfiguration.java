package com.lombardrisk.ignis.server.config.calendar;

import com.lombardrisk.ignis.server.config.product.ProductServiceConfiguration;
import com.lombardrisk.ignis.server.controller.calendar.CalendarController;
import com.lombardrisk.ignis.server.controller.calendar.ProductWorkingDaysController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CalendarConfiguration {

    private final ProductServiceConfiguration productServiceConfiguration;
    private final CalendarHolidayRepository calendarHolidayRepository;
    private final ProductWorkingDayRepository productWorkingDayRepository;

    @Autowired
    public CalendarConfiguration(
            final ProductServiceConfiguration productServiceConfiguration,
            final CalendarHolidayRepository calendarHolidayRepository,
            final ProductWorkingDayRepository productWorkingDayRepository) {
        this.productServiceConfiguration = productServiceConfiguration;
        this.calendarHolidayRepository = calendarHolidayRepository;
        this.productWorkingDayRepository = productWorkingDayRepository;
    }

    @Bean
    public CalendarService calendarService() {
        return new CalendarService(productServiceConfiguration.productConfigService(),
                productWorkingDayService(),
                calendarHolidayRepository);
    }

    @Bean
    public ProductWorkingDayService productWorkingDayService() {
        return new ProductWorkingDayService(
                productWorkingDayRepository,
                productServiceConfiguration.productConfigService());
    }

    @Bean
    public CalendarController calendarController() {
        return new CalendarController(calendarService());
    }

    @Bean
    public ProductWorkingDaysController productWorkingDaysController() {
        return new ProductWorkingDaysController(productWorkingDayService());
    }
}
