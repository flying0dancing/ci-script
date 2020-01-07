package com.lombardrisk.ignis.common.time;

import lombok.Getter;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;

@Getter
public class TimeSource {

    private final Clock clock;

    public TimeSource(final Clock clock) {
        this.clock = clock;
    }

    public LocalDateTime nowAsLocalDateTime() {
        return LocalDateTime.now(clock);
    }

    public LocalDate nowAsLocalDate() {
        return LocalDate.now(clock);
    }

    public Date nowAsDate() {
        return Date.from(Instant.now(clock));
    }

    public Date fromLocalDate(final LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(clock.getZone()).toInstant());
    }

    public Date fromLocalDateTime(final LocalDateTime localDateTime) {
        return Date.from(localDateTime.toInstant(ZoneOffset.of(clock.getZone().getId())));
    }

    public LocalDateTime fromDate(final Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), clock.getZone());
    }

    public LocalDate localDateFromDate(final Date date) {
        return fromDate(date).toLocalDate();
    }

    public ZonedDateTime toZonedDateTime(final LocalDateTime createdTime) {
        return ZonedDateTime.of(createdTime, clock.getZone());
    }

    public ZonedDateTime toZonedDateTime(final Date date) {
        return ZonedDateTime.of(fromDate(date), clock.getZone());
    }
}
