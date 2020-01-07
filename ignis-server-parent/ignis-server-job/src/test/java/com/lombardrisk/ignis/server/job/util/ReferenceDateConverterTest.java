package com.lombardrisk.ignis.server.job.util;

import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import io.vavr.Tuple;
import io.vavr.control.Validation;
import org.junit.Test;

import java.time.LocalDate;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class ReferenceDateConverterTest {

    @Test
    public void convert_ValidDateFormat_ReturnsDate() {
        Validation<CRUDFailure, LocalDate> dateResult = ReferenceDateConverter.convertReferenceDate("31/12/1991");

        assertThat(dateResult.get()).isEqualTo(LocalDate.of(1991, 12, 31));
    }

    @Test
    public void convert_InvalidDateFormat_ReturnsDate() {
        Validation<CRUDFailure, LocalDate> dateResult = ReferenceDateConverter.convertReferenceDate("1991-12-31");

        assertThat(dateResult.getError())
                .isEqualTo(CRUDFailure.invalidRequestParameters(Collections.singletonList(Tuple.of(
                        "referenceDate",
                        "1991-12-31"))));
    }
}
