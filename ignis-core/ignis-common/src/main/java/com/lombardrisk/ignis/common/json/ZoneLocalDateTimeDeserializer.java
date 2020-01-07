package com.lombardrisk.ignis.common.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class ZoneLocalDateTimeDeserializer extends StdDeserializer<ZonedDateTime> {

    public ZoneLocalDateTimeDeserializer() {
        super(ZonedDateTime.class);
    }

    @Override
    public ZonedDateTime deserialize(
            final JsonParser jsonParser, final DeserializationContext ctxt) throws IOException {

        String string = jsonParser.getText().trim();
        return ZonedDateTime.parse(string,
                DateTimeFormatter.ofPattern(ZonedLocalDateTimeSerializer.ZONE_DATE_TIME_FORMAT));
    }
}
