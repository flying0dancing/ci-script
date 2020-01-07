package com.lombardrisk.ignis.common.time;

import org.junit.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;

public class TimeSourceTest {

    @Test
    public void nowAsLocalDateTime_ClockFixed() {
        TimeSource timeSource = new TimeSource(Clock.fixed(Instant.ofEpochMilli(100000), ZoneId.of("GMT")));

        assertThat(timeSource.nowAsLocalDateTime())
                .isEqualTo(LocalDateTime.of(1970, 1, 1, 0, 1, 40));
    }

    @Test
    public void nowAsLocalDate_ClockFixed() {
        TimeSource timeSource = new TimeSource(Clock.fixed(Instant.ofEpochMilli(100000), ZoneId.of("GMT")));

        assertThat(timeSource.nowAsLocalDate())
                .isEqualTo(LocalDate.of(1970, 1, 1));
    }

    @Test
    public void toZonedDateTime_AddsClockTimeZoneComponent() {
        TimeSource timeSource = new TimeSource(Clock.fixed(Instant.ofEpochMilli(100000), ZoneId.of("UTC")));

        ZonedDateTime zonedDateTime = timeSource.toZonedDateTime(LocalDateTime.of(1991, 1, 1, 0, 0, 0));

        assertThat(zonedDateTime)
                .isEqualTo(ZonedDateTime.of(1991, 1, 1, 0, 0, 0, 0, ZoneId.of("UTC")));
    }

    @Test
    public void toZonedDateTime_DateArgument() {
        Date date = new Calendar.Builder()
                .setDate(1989, 5, 22)
                .setTimeOfDay(23, 59, 59)
                .setTimeZone(TimeZone.getTimeZone("UTC"))
                .build()
                .getTime();

        TimeSource timeSource = new TimeSource(Clock.fixed(Instant.ofEpochMilli(100000), ZoneId.of("UTC")));

        ZonedDateTime zonedDateTime = timeSource.toZonedDateTime(date);

        assertThat(zonedDateTime)
                .isEqualTo(ZonedDateTime.of(1989, 6, 22, 23, 59, 59, 0, ZoneId.of("UTC")));
    }
}
