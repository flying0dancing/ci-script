package com.lombardrisk.ignis.server.config.calendar;

import com.lombardrisk.ignis.common.assertions.VavrAssert;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.server.config.fixture.ServerConfig;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.collect.Sets.newHashSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ProductWorkingDayServiceTest {

    @Mock
    private ProductWorkingDayRepository productWorkingDayRepository;

    @Mock
    private ProductConfigService productConfigService;

    @InjectMocks
    private ProductWorkingDayService productWorkingDayService;

    @Before
    public void setUp() throws Exception {
        when(productConfigService.findWithValidation(anyLong()))
                .thenReturn(Validation.valid(ProductPopulated.productConfig().build()));
    }

    @Test
    public void findByProduct_ReturnsDaysOfWeek() {
        when(productWorkingDayRepository.findAllByProduct(anyString()))
                .thenReturn(Stream.of(
                        ServerConfig.Populated.productWorkingDay().dayOfWeek(DayOfWeek.MONDAY).build(),
                        ServerConfig.Populated.productWorkingDay().dayOfWeek(DayOfWeek.TUESDAY).build(),
                        ServerConfig.Populated.productWorkingDay().dayOfWeek(DayOfWeek.WEDNESDAY).build(),
                        ServerConfig.Populated.productWorkingDay().dayOfWeek(DayOfWeek.THURSDAY).build()));

        List<DayOfWeek> dayOfWeeks = productWorkingDayService.findByProductName("Test")
                .collect(Collectors.toList());

        assertThat(dayOfWeeks)
                .containsExactlyInAnyOrder(
                        DayOfWeek.MONDAY,
                        DayOfWeek.TUESDAY,
                        DayOfWeek.WEDNESDAY,
                        DayOfWeek.THURSDAY);
    }

    @Test
    public void updateWorkingDays_ProductNotFound_ReturnsError() {
        when(productConfigService.findWithValidation(anyLong()))
                .thenReturn(Validation.invalid(CRUDFailure.notFoundIds("Product", 123L)));

        VavrAssert.assertFailed(productWorkingDayService.updateWorkingDays(-10L, Collections.emptyList()))
                .withFailure(CRUDFailure.notFoundIds("Product", 123L));

        verify(productWorkingDayRepository, never()).deleteAllByProduct(anyString());
        verify(productWorkingDayRepository, never()).saveAll(any());
    }

    @Test
    public void updateWorkingDays_DeletesOldWorkingDays() {
        when(productConfigService.findWithValidation(anyLong()))
                .thenReturn(Validation.valid(ProductPopulated.productConfig("PRA").build()));

        VavrAssert.assertValid(productWorkingDayService.updateWorkingDays(1L, Collections.emptyList()));

        verify(productWorkingDayRepository).deleteAllByProduct("PRA");
    }

    @Test
    public void updateWorkingDays_SavesNewWorkingDays() {
        when(productConfigService.findWithValidation(anyLong()))
                .thenReturn(Validation.valid(ProductPopulated.productConfig("PRA").build()));

        VavrAssert.assertValid(productWorkingDayService.updateWorkingDays(
                1L,
                Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.FRIDAY)));

        verify(productWorkingDayRepository).saveAll(
                newHashSet(
                        ServerConfig.Populated.productWorkingDay()
                                .product("PRA")
                                .dayOfWeek(DayOfWeek.MONDAY)
                                .build(),
                        ServerConfig.Populated.productWorkingDay()
                                .product("PRA")
                                .dayOfWeek(DayOfWeek.FRIDAY)
                                .build()));
    }
}

