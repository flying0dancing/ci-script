package com.lombardrisk.ignis.common.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import lombok.experimental.UtilityClass;

import java.time.ZoneId;
import java.util.TimeZone;

// In order to use jackson to serialize/deserialize immutable objects,
// we have used this documentation: https://immutables.github.io/json.html
// to add the jackson-datatype-guava dependancy in the POM.xml

@UtilityClass
//FIXME: Make this a bean not a static field
public class MapperWrapper {

    public static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.registerModule(new GuavaModule());
        MAPPER.setTimeZone(TimeZone.getTimeZone(ZoneId.systemDefault()));
    }
}
