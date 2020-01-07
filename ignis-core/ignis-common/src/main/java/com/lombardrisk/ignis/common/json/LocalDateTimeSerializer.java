package com.lombardrisk.ignis.common.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * To be removed when task to included JavaTimeModule is done
 */
public class LocalDateTimeSerializer extends StdSerializer<LocalDateTime> {

    private static final long serialVersionUID = 1L;

    public LocalDateTimeSerializer() {
        super(LocalDateTime.class);
    }

    @Override
    public void serialize(
            final LocalDateTime value,
            final JsonGenerator jsonGenerator,
            final SerializerProvider provider) throws IOException {
        jsonGenerator.writeString(value.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }
}
