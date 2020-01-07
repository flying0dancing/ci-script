package com.lombardrisk.ignis.common.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class ZonedLocalDateTimeSerializerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void serialize_UTCTimezone_ReturnsISOStringWithZoneComponent() throws Exception {
        TestObject testObject = new TestObject(ZonedDateTime.of(
                LocalDateTime.of(2001, 1, 1, 0, 0, 0),
                ZoneId.of("UTC")));

        String writeValueAsString = objectMapper.writeValueAsString(testObject);

        assertThat(writeValueAsString).isEqualTo("{\"zonedDateTime\":\"2001-01-01T00:00:00+0000\"}");
    }

    @Test
    public void serialize_PanamaTimezone_ReturnsISOStringWithZoneComponent() throws Exception {
        TestObject testObject = new TestObject(ZonedDateTime.of(
                LocalDateTime.of(2001, 1, 1, 0, 0, 0),
                ZoneId.of("America/Panama")));

        String writeValueAsString = objectMapper.writeValueAsString(testObject);

        assertThat(writeValueAsString).isEqualTo("{\"zonedDateTime\":\"2001-01-01T00:00:00-0500\"}");
    }

    @AllArgsConstructor
    @Data
    public static class TestObject {

        @JsonSerialize(using = ZonedLocalDateTimeSerializer.class)
        private ZonedDateTime zonedDateTime;
    }
}
