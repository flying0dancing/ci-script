package com.lombardrisk.ignis.api.calendar;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;

import java.time.Month;

public class YearMonthKeyDeserializer extends KeyDeserializer {

    @Override
    public Object deserializeKey(final String key, final DeserializationContext ctxt) {
        String[] arr = key.split("-");
        return new YearMonth(Integer.valueOf(arr[0]), Month.valueOf(arr[1]));
    }
}
