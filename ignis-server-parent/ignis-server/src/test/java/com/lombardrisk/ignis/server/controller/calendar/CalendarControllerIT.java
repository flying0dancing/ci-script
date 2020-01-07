package com.lombardrisk.ignis.server.controller.calendar;

import com.lombardrisk.ignis.client.external.calendar.CalendarRequest;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.server.config.IntegrationTestConfig;
import com.lombardrisk.ignis.server.config.calendar.CalendarService;
import com.lombardrisk.ignis.server.config.fixture.ServerConfig;
import io.vavr.control.Validation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;

import static com.lombardrisk.ignis.common.json.MapperWrapper.MAPPER;
import static com.lombardrisk.ignis.test.config.AdminUser.BASIC_AUTH;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@IntegrationTestConfig
public class CalendarControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CalendarService calendarService;

    @Test
    public void getAll_ReturnsCalendars() throws Exception {

        when(calendarService.findAll())
                .thenReturn(Arrays.asList(
                        ServerConfig.Populated.calendarHoliday()
                                .id(1L)
                                .name("Xmas Day")
                                .date(LocalDate.of(2019, 12, 25))
                                .product("PRA")
                                .build(),
                        ServerConfig.Populated.calendarHoliday()
                                .id(2L)
                                .name("Chinese New Year")
                                .date(LocalDate.of(2019, 2, 14))
                                .product("HKMA")
                                .build()));

        mockMvc.perform(
                get("/api/v1/calendars")
                        .with(BASIC_AUTH)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Xmas Day"))
                .andExpect(jsonPath("$[0].date").value("2019-12-25"))
                .andExpect(jsonPath("$[0].product").value("PRA"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Chinese New Year"))
                .andExpect(jsonPath("$[1].date").value("2019-02-14"))
                .andExpect(jsonPath("$[1].product").value("HKMA"));
    }

    @Test
    public void createHoliday_ValidHoliday_ReturnsOkResponse() throws Exception {

        when(calendarService.create(any()))
                .thenReturn(Validation.valid(ServerConfig.Populated.calendarHoliday()
                        .id(1L)
                        .name("Xmas Day")
                        .date(LocalDate.of(2019, 12, 25))
                        .product("ENG")
                        .build()));

        CalendarRequest calendarRequest = CalendarRequest.builder()
                .productName("ENG")
                .date(LocalDate.of(2019, 12, 25))
                .name("Xmas Day")
                .build();

        mockMvc.perform(
                post("/api/v1/calendars")
                        .with(BASIC_AUTH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(MAPPER.writeValueAsString(calendarRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Xmas Day"))
                .andExpect(jsonPath("$.date").value("2019-12-25"))
                .andExpect(jsonPath("$.product").value("ENG"));

        verify(calendarService).create(calendarRequest);
    }

    @Test
    public void createHoliday_InValidHoliday_ReturnsBadRequestResponse() throws Exception {
        when(calendarService.create(any()))
                .thenReturn(Validation.invalid(CRUDFailure.notFoundIds("Product", -10L)));

        mockMvc.perform(
                post("/api/v1/calendars")
                        .with(BASIC_AUTH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(MAPPER.writeValueAsString(CalendarRequest.builder().build())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].errorMessage").value("Could not find Product for ids [-10]"))
                .andExpect(jsonPath("$[0].errorCode").value("NOT_FOUND"));
    }

    @Test
    public void updateHoliday_ValidHoliday_ReturnsOkResponse() throws Exception {

        when(calendarService.update(anyLong(), any()))
                .thenReturn(Validation.valid(ServerConfig.Populated.calendarHoliday()
                        .id(1L)
                        .name("Xmas Day")
                        .date(LocalDate.of(2019, 12, 25))
                        .product("PRA")
                        .build()));

        CalendarRequest calendarRequest = CalendarRequest.builder()
                .productName("PRA")
                .date(LocalDate.of(2019, 12, 25))
                .name("Xmas Day")
                .build();

        mockMvc.perform(
                put("/api/v1/calendars/1")
                        .with(BASIC_AUTH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(MAPPER.writeValueAsString(calendarRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Xmas Day"))
                .andExpect(jsonPath("$.date").value("2019-12-25"))
                .andExpect(jsonPath("$.product").value("PRA"));

        verify(calendarService).update(1L, calendarRequest);
    }

    @Test
    public void updateHoliday_InValidHoliday_ReturnsBadRequestResponse() throws Exception {
        when(calendarService.update(anyLong(), any()))
                .thenReturn(Validation.invalid(CRUDFailure.notFoundIds("Calendar", -10L)));

        mockMvc.perform(
                put("/api/v1/calendars/-10")
                        .with(BASIC_AUTH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(MAPPER.writeValueAsString(CalendarRequest.builder().build())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].errorMessage").value("Could not find Calendar for ids [-10]"))
                .andExpect(jsonPath("$[0].errorCode").value("NOT_FOUND"));
    }

    @Test
    public void deleteHoliday_ValidHoliday_ReturnsOkResponse() throws Exception {

        when(calendarService.deleteWithValidation(anyLong()))
                .thenReturn(Validation.valid(ServerConfig.Populated.calendarHoliday()
                        .id(100L)
                        .name("Xmas Day")
                        .date(LocalDate.of(2019, 12, 25))
                        .product("PRA")
                        .build()));

        CalendarRequest calendarRequest = CalendarRequest.builder()
                .productName("PRA")
                .date(LocalDate.of(2019, 12, 25))
                .name("Xmas Day")
                .build();

        mockMvc.perform(
                delete("/api/v1/calendars/100")
                        .with(BASIC_AUTH)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100));

        verify(calendarService).deleteWithValidation(100);
    }

    @Test
    public void deleteHoliday_InValidHoliday_ReturnsBadRequestResponse() throws Exception {
        when(calendarService.deleteWithValidation(anyLong()))
                .thenReturn(Validation.invalid(CRUDFailure.notFoundIds("Calendar", -10L)));

        mockMvc.perform(
                delete("/api/v1/calendars/-10")
                        .with(BASIC_AUTH)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].errorMessage").value("Could not find Calendar for ids [-10]"))
                .andExpect(jsonPath("$[0].errorCode").value("NOT_FOUND"));
    }
}
