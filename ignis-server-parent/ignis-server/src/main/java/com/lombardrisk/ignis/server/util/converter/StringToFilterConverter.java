package com.lombardrisk.ignis.server.util.converter;

import com.lombardrisk.ignis.common.json.MapperWrapper;
import com.lombardrisk.ignis.data.common.search.FilterExpression;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
@UtilityClass
public class StringToFilterConverter {

    public FilterExpression toFilter(final String search) {
        if (search != null && !search.isEmpty()) {
            try {
                return MapperWrapper.MAPPER.readValue(search, FilterExpression.class);
            } catch (IOException e) {
                log.error("Error mapping json search string {}. Exception is ", search, e);
                throw new IllegalStateException(e);
            }
        }
        return null;
    }
}
