package com.lombardrisk.ignis.server.job.util;

import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.control.Try;
import io.vavr.control.Validation;
import lombok.experimental.UtilityClass;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static java.util.Collections.singletonList;

@UtilityClass
public class ReferenceDateConverter {

    public static Validation<CRUDFailure, LocalDate> convertReferenceDate(final String referenceDate) {

        Try<LocalDate> dateParse = Try.of(() -> LocalDate.parse(referenceDate, DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        if (dateParse.isFailure()) {
            List<Tuple2<String, String>> invalidParams = singletonList(Tuple.of("referenceDate", referenceDate));
            return Validation.invalid(
                    CRUDFailure.invalidRequestParameters(invalidParams));
        }

        return Validation.valid(dateParse.get());
    }
}
