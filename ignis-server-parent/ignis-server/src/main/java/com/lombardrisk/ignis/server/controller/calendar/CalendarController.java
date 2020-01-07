package com.lombardrisk.ignis.server.controller.calendar;

import com.lombardrisk.ignis.client.external.calendar.CalendarRequest;
import com.lombardrisk.ignis.client.external.path.api;
import com.lombardrisk.ignis.data.common.Identifiable;
import com.lombardrisk.ignis.server.config.calendar.CalendarHoliday;
import com.lombardrisk.ignis.server.config.calendar.CalendarService;
import com.lombardrisk.ignis.web.common.response.FcrResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
public class CalendarController {

    private final CalendarService calendarService;

    public CalendarController(final CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    @ResponseBody
    @PostMapping(
            value = api.external.v1.Calendars,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public FcrResponse<CalendarHoliday> createCalendar(
            @RequestBody final CalendarRequest calendarRequest) {

        log.info("Creating calendar holiday for product [{}]", calendarRequest.getProductName());

        return calendarService.create(calendarRequest)
                .fold(FcrResponse::crudFailure, FcrResponse::okResponse);
    }

    @ResponseBody
    @PutMapping(value = api.external.v1.calendars.byId)
    public FcrResponse<CalendarHoliday> updateCalendar(
            @PathVariable(value = api.Params.ID) final Long calendarId,
            @RequestBody final CalendarRequest calendarRequest) {

        log.info("Updating Calendar [{}]", calendarId);

        return calendarService.update(calendarId, calendarRequest)
                .fold(FcrResponse::crudFailure, FcrResponse::okResponse);
    }

    @ResponseBody
    @DeleteMapping(value = api.external.v1.calendars.byId)
    public FcrResponse<Identifiable> deleteCalendar(
            @PathVariable(value = api.Params.ID) final Long calendarId) {

        log.info("Deleting Calendar [{}]", calendarId);

        return calendarService.deleteWithValidation(calendarId)
                .map(Identifiable::toIdentifiable)
                .fold(FcrResponse::crudFailure, FcrResponse::okResponse);
    }

    @ResponseBody
    @GetMapping(value = api.external.v1.Calendars)
    public FcrResponse<List<CalendarHoliday>> getAll() {
        log.info("Find all calendar holidays");

        return FcrResponse.okResponse(calendarService.findAll());
    }
}
