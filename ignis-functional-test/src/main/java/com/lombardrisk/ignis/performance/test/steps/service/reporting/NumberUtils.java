package com.lombardrisk.ignis.performance.test.steps.service.reporting;

import lombok.experimental.UtilityClass;

import java.math.BigDecimal;

@UtilityClass
public class NumberUtils {
    private static final int SCALE_MILLION = -6;
    private static final int SCALE_THOUSAND = -3;
    private static final int ONE_MILLION = 1_000_000;
    private static final int ONE_THOUSAND = 1000;

    public static String scaleNumber(final long number) {
        if (number >= ONE_MILLION) {
            return String.format("%dM", new BigDecimal(number).scaleByPowerOfTen(SCALE_MILLION).intValue());
        }
        if (number >= ONE_THOUSAND) {
            return String.format("%dK", new BigDecimal(number).scaleByPowerOfTen(SCALE_THOUSAND).intValue());
        }
        return String.valueOf(number);
    }
}
