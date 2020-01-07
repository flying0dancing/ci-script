package com.lombardrisk.ignis.api.calendar;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class YearMonthSerializer extends JsonSerializer<YearMonth> {

    private static final long serialVersionUID = 4656358284396006854L;

    @Override
    public Class<YearMonth> handledType() {
        return YearMonth.class;
    }

    @Override
    public void serialize(
            final YearMonth value,
            final JsonGenerator gen,
            final SerializerProvider provider) throws IOException {
        gen.writeFieldName(keyString(value));
    }

    private String keyString(final YearMonth yearMonth) {
        return yearMonth.getYear() + "-" + yearMonth.getMonth().name();
    }
}
