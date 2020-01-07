package com.lombardrisk.ignis.server.controller.calendar;

import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.server.config.IntegrationTestConfig;
import com.lombardrisk.ignis.server.config.calendar.ProductWorkingDayService;
import com.lombardrisk.ignis.server.config.fixture.ServerConfig;
import io.vavr.control.Validation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.lombardrisk.ignis.common.json.MapperWrapper.MAPPER;
import static com.lombardrisk.ignis.test.config.AdminUser.BASIC_AUTH;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@IntegrationTestConfig
public class ProductWorkingDaysControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductWorkingDayService productWorkingDayService;

    @Test
    public void getAll_ProductWorkingDays() throws Exception {

        when(productWorkingDayService.findAll())
                .thenReturn(Arrays.asList(
                        ServerConfig.Populated.productWorkingDay()
                                .id(1L)
                                .product("PRA")
                                .dayOfWeek(DayOfWeek.MONDAY)
                                .build(),
                        ServerConfig.Populated.productWorkingDay()
                                .id(2L)
                                .product("PRA")
                                .dayOfWeek(DayOfWeek.TUESDAY)
                                .build()));

        mockMvc.perform(
                get("/api/v1/workingDays")
                        .with(BASIC_AUTH)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].product").value("PRA"))
                .andExpect(jsonPath("$[0].dayOfWeek").value("MONDAY"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].product").value("PRA"))
                .andExpect(jsonPath("$[1].dayOfWeek").value("TUESDAY"));
    }

    @Test
    public void updateWorkingDays_Invalid_ReturnsBadRequest() throws Exception {

        when(productWorkingDayService.updateWorkingDays(anyLong(), any()))
                .thenReturn(Validation.invalid(CRUDFailure.notFoundIds("Product", -1L)));

        List<DayOfWeek> workingDays = Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY);

        mockMvc.perform(
                put("/api/v1/productConfigs/-1/workingDays")
                        .with(BASIC_AUTH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(MAPPER.writeValueAsString(workingDays)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].errorCode").value("NOT_FOUND"))
                .andExpect(jsonPath("$[0].errorMessage").value("Could not find Product for ids [-1]"));

        verify(productWorkingDayService).updateWorkingDays(-1L, workingDays);
    }

    @Test
    public void updateWorkingDays_Valid_ReturnsNewWorkingDays() throws Exception {

        when(productWorkingDayService.updateWorkingDays(anyLong(), any()))
                .thenReturn(Validation.valid(Collections.singletonList(ServerConfig.Populated.productWorkingDay()
                        .id(2L)
                        .product("PRA")
                        .dayOfWeek(DayOfWeek.MONDAY)
                        .build())));

        mockMvc.perform(
                put("/api/v1/productConfigs/-1/workingDays")
                        .with(BASIC_AUTH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(MAPPER.writeValueAsString(Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].product").value("PRA"))
                .andExpect(jsonPath("$[0].dayOfWeek").value("MONDAY"));

        verify(productWorkingDayService).updateWorkingDays(-1L, Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY));
    }
}
