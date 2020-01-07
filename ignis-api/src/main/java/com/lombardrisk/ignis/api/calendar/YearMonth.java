package com.lombardrisk.ignis.api.calendar;

import lombok.Value;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;

@Value
public class YearMonth implements Serializable {

    private static final long serialVersionUID = 707785937038952809L;
    private final Integer year;
    private final Month month;

    public LocalDate startOfMonth() {
        return LocalDate.of(year, month.getValue(), 1);
    }

    public LocalDate endOfMonth() {
        int length = month.length(Year.isLeap(year));
        return LocalDate.of(year, month.getValue(), length);
    }
}
