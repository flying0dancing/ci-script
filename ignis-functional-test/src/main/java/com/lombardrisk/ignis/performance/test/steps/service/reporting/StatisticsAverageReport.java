package com.lombardrisk.ignis.performance.test.steps.service.reporting;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

@Data
public class StatisticsAverageReport {

    private static final BigDecimal TWO = new BigDecimal(2);
    private static final double NINETY_PERCENT = .9;

    @JsonIgnore
    private final int total;
    @JsonProperty("min")
    private final long min;
    @JsonProperty("max")
    private final long max;
    @JsonProperty("median")
    private final double median;
    @JsonProperty("90th")
    private final double ninetyPercentile;

    public static <T> StatisticsAverageReport build(final List<T> data, final Function<T, Long> toLong) {
        if (data.isEmpty()) {
            return empty();
        }

        List<Long> sorted = data.stream()
                .map(toLong)
                .sorted(Long::compareTo)
                .collect(toList());

        return new StatisticsAverageReport(
                data.size(),
                getMin(sorted),
                getMax(sorted),
                getMedian(sorted),
                calculatePercentile(sorted, NINETY_PERCENT));
    }

    private static StatisticsAverageReport empty() {
        return new StatisticsAverageReport(0, 0L, 0L, 0D, 0D);
    }

    private static long getMin(final List<Long> sorted) {
        return sorted.get(0);
    }

    private static long getMax(final List<Long> sorted) {
        return sorted.get(sorted.size() - 1);
    }

    private static double getMedian(final List<Long> sorted) {
        boolean evenNumberOfItems = sorted.size() % TWO.intValue() == 0;

        if (evenNumberOfItems) {
            int middle = sorted.size() / TWO.intValue();

            return BigDecimal.valueOf(sorted.get(middle - 1))
                    .add(BigDecimal.valueOf(sorted.get(middle)))
                    .divide(TWO, TWO.intValue(), RoundingMode.HALF_UP)
                    .doubleValue();
        }

        int middle = sorted.size() / TWO.intValue();
        return Double.valueOf(sorted.get(middle));
    }

    private static double calculatePercentile(final List<Long> sorted, final double percentile) {
        if (sorted.size() == 1) {
            return sorted.get(0);
        }

        BigDecimal rank = BigDecimal.valueOf((sorted.size() - 1) * percentile + 1);

        int integerPart = rank.intValue();
        double fractionalPart = rank.remainder(BigDecimal.ONE).doubleValue();

        return (sorted.get(integerPart) - sorted.get(integerPart - 1)) * fractionalPart + sorted.get(integerPart - 1);
    }
}
