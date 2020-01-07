package com.lombardrisk.ignis.common.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * To be removed when task to included JavaTimeModule is done
 */
public class ZonedLocalDateTimeSerializer extends StdSerializer<ZonedDateTime> {

    private static final long serialVersionUID = 1L;
    static final String ZONE_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";

    public ZonedLocalDateTimeSerializer() {
        super(ZonedDateTime.class);
    }

    @Override
    public void serialize(
            final ZonedDateTime value,
            final JsonGenerator jsonGenerator,
            final SerializerProvider provider) throws IOException {
        jsonGenerator.writeString(value.format(DateTimeFormatter.ofPattern(ZONE_DATE_TIME_FORMAT)));
    }
}
