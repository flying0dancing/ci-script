package com.lombardrisk.ignis.common.json;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.Test;

import java.time.LocalDate;

import static com.lombardrisk.ignis.common.json.MapperWrapper.MAPPER;
import static org.assertj.core.api.Assertions.assertThat;

public class LocalDateSerializerTest {

    @Test
    public void serializeToIsoDate_ReturnsString() throws Exception {
        TestClass value = new TestClass(LocalDate.of(1991, 1, 1));
        String valueAsString = MAPPER.writeValueAsString(value);

        assertThat(valueAsString)
                .isEqualTo("{\"testDate\":\"1991-01-01\"}");
    }

    @Test
    public void deserializeToIsoDate_ReturnsObject() throws Exception {

        TestClass expected = new TestClass(LocalDate.of(1991, 1, 1));
        String valueAsString = "{\"testDate\":\"1991-01-01\"}";

        assertThat(MAPPER.readValue(valueAsString, TestClass.class))
                .isEqualTo(expected);
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class TestClass {

        @JsonSerialize(using = LocalDateSerializer.class)
        @JsonDeserialize(using = LocalDateDeserializer.class)
        private LocalDate testDate;
    }
}