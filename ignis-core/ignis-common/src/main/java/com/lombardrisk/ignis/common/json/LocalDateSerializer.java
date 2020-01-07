package com.lombardrisk.ignis.common.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.time.LocalDate;

import static com.lombardrisk.ignis.common.json.LocalDateDeserializer.LOCAL_DATE_FORMAT;

public class LocalDateSerializer extends StdSerializer<LocalDate> {

    private static final long serialVersionUID = 1L;

    public LocalDateSerializer() {
        super(LocalDate.class);
    }

    @Override
    public void serialize(
            final LocalDate value,
            final JsonGenerator jsonGenerator,
            final SerializerProvider provider) throws IOException {
        jsonGenerator.writeString(value.format(LOCAL_DATE_FORMAT));
    }
}
