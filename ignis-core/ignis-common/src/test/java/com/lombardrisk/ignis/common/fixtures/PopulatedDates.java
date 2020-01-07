package com.lombardrisk.ignis.common.fixtures;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.time.FastDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.Date;

import static org.apache.commons.lang3.time.DateFormatUtils.ISO_8601_EXTENDED_DATETIME_FORMAT;
import static org.apache.commons.lang3.time.DateFormatUtils.ISO_8601_EXTENDED_DATE_FORMAT;

@UtilityClass
public class PopulatedDates {
    private static final Logger LOGGER = LoggerFactory.getLogger(PopulatedDates.class);

    /**
     * Parse date using
     * {@link org.apache.commons.lang3.time.DateFormatUtils#ISO_8601_EXTENDED_DATE_FORMAT ISO_8601_EXTENDED_DATE_FORMAT}
     *
     * @param isoDate for format yyyy-MM-dd
     * @return parsed date or 1970-01-01 if {@code isoDate} cannot be parsed
     */
    public static Date toDate(final String isoDate) {
        return parseDate(isoDate, ISO_8601_EXTENDED_DATE_FORMAT);
    }

    /**
     * Parse date using
     * {@link org.apache.commons.lang3.time.DateFormatUtils#ISO_8601_EXTENDED_DATETIME_FORMAT ISO_8601_EXTENDED_DATETIME_FORMAT}
     *
     * @param isoDateTime for format yyyy-MM-dd'T'HH:mm:ss (i.e. 2000-01-01T00:00:00)
     * @return parsed date or 1970-01-01T01:00:00 if {@code isoDateTime} cannot be parsed
     */
    public static Date toDateTime(final String isoDateTime) {
        return parseDate(isoDateTime, ISO_8601_EXTENDED_DATETIME_FORMAT);
    }

    private static Date parseDate(final String date, final FastDateFormat dateFormat) {
        try {
            return dateFormat.parse(date);
        } catch (final ParseException e) {
            LOGGER.error("Expected date with " + dateFormat, e);
            return new Date(0);
        }
    }
}
