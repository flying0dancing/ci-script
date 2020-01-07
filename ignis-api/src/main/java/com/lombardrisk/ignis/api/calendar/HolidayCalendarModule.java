package com.lombardrisk.ignis.api.calendar;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleKeyDeserializers;
import com.fasterxml.jackson.databind.module.SimpleSerializers;

import static java.util.Collections.singletonList;

public class HolidayCalendarModule extends Module {

    @Override
    public String getModuleName() {
        return "Holiday Calendar Module";
    }

    @Override
    public Version version() {
        return Version.unknownVersion();
    }

    @Override
    public void setupModule(final SetupContext context) {
        context.addKeySerializers(new SimpleSerializers(singletonList(new YearMonthSerializer())));
        context.addKeyDeserializers(
                new SimpleKeyDeserializers()
                        .addDeserializer(YearMonth.class, new YearMonthKeyDeserializer()));
    }
}
