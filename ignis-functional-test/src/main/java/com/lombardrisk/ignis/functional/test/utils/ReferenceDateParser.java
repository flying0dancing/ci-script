package com.lombardrisk.ignis.functional.test.utils;

import io.vavr.control.Try;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import static org.apache.commons.lang3.time.DateUtils.parseDate;

@UtilityClass
public class ReferenceDateParser {

    private static final String DATE_FORMAT = "dd/MM/yyyy";

    public static LocalDate convertToLocalDate(final String stringDate) {
        return LocalDate.parse(stringDate, DateTimeFormatter.ofPattern(DATE_FORMAT));
    }

    public static Date convertToDate(final String stringDate) {
        return Try.of(() -> parseDate(stringDate, DATE_FORMAT))
                .getOrElseThrow(throwable -> new DateTimeException("Can't parse reference date", throwable));
    }

    public static String convertToString(final Date date) {
        return DateFormatUtils.format(date, DATE_FORMAT);
    }
}
