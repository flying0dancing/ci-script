package com.lombardrisk.ignis.data.common.search;

import com.google.common.collect.ImmutableSet;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.control.Try;
import io.vavr.control.Validation;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Set;

import static com.lombardrisk.ignis.data.common.search.FilterOption.CONTAINS;
import static com.lombardrisk.ignis.data.common.search.FilterOption.ENDS_WITH;
import static com.lombardrisk.ignis.data.common.search.FilterOption.EQUALS;
import static com.lombardrisk.ignis.data.common.search.FilterOption.GREATER_THAN;
import static com.lombardrisk.ignis.data.common.search.FilterOption.GREATER_THAN_OR_EQUAL;
import static com.lombardrisk.ignis.data.common.search.FilterOption.IN_RANGE;
import static com.lombardrisk.ignis.data.common.search.FilterOption.LESS_THAN;
import static com.lombardrisk.ignis.data.common.search.FilterOption.LESS_THAN_OR_EQUAL;
import static com.lombardrisk.ignis.data.common.search.FilterOption.NOT_CONTAINS;
import static com.lombardrisk.ignis.data.common.search.FilterOption.NOT_EQUAL;
import static com.lombardrisk.ignis.data.common.search.FilterOption.STARTS_WITH;

@UtilityClass
@Slf4j
public class FilterValidator {

    private static final ImmutableSet<FilterOption> supportedTextOperators = ImmutableSet.of(
            EQUALS, NOT_EQUAL, CONTAINS, NOT_CONTAINS, STARTS_WITH, ENDS_WITH);

    private static final ImmutableSet<FilterOption> supportedNumberOperators = ImmutableSet.of(
            EQUALS, NOT_EQUAL, LESS_THAN, LESS_THAN_OR_EQUAL, GREATER_THAN, GREATER_THAN_OR_EQUAL, IN_RANGE);

    public static <T> Validation<CRUDFailure, Specification<T>> validateText(final Filter filter) {
        return validateSearchOperator(filter, supportedTextOperators)
                .map(FilterValidator::toTextSpecification);
    }

    public static <T> Validation<CRUDFailure, Specification<T>> validateNumber(final Filter filter) {
        return validateSearchOperator(filter, supportedNumberOperators)
                .flatMap(FilterValidator::validateFilterNumber);
    }

    public static <T> Validation<CRUDFailure, Specification<T>> validateDate(final Filter filter, final ZoneId zoneId) {
        return validateSearchOperator(filter, supportedNumberOperators)
                .flatMap(f -> FilterValidator.validateFilterDate(f, zoneId));
    }

    private static Validation<CRUDFailure, Filter> validateSearchOperator(
            final Filter filter, final Set<FilterOption> validFilterOptions) {

        if (validFilterOptions.contains(filter.getType())) {
            return Validation.valid(filter);
        }

        return Validation.invalid(unsupportedOperatorFailure(filter.getType(), filter.getColumnName()));
    }

    private static <T> Validation<CRUDFailure, Specification<T>> validateFilterNumber(final Filter filter) {
        return Try.of(() -> parseFilterFromToAsDouble(filter))
                .onFailure(throwable -> log.warn("Failed to parse search parameter as number", throwable))
                .map(doubles -> FilterValidator.<T>toNumberSpecification(filter, doubles._1, doubles._2))
                .toValid(cannotParseFilter(filter));
    }

    private static Tuple2<Double, Double> parseFilterFromToAsDouble(final Filter filter) {
        Double filterFrom = Double.valueOf(filter.getFilter());
        Double filterTo = filter.getFilterTo() != null ? Double.valueOf(filter.getFilterTo()) : null;

        return Tuple.of(filterFrom, filterTo);
    }

    private static <T> Validation<CRUDFailure, Specification<T>> validateFilterDate(
            final Filter filter, final ZoneId zoneId) {

        return Try.of(() -> parseFilterFromToAsDate(filter, zoneId))
                .onFailure(throwable -> log.warn("Failed to parse search parameter as date", throwable))
                .map(dates -> FilterValidator.<T>toDateSpecification(filter, dates._1, dates._2))
                .toValid(cannotParseFilter(filter));
    }

    private static Tuple2<Date, Date> parseFilterFromToAsDate(final Filter filter, final ZoneId zoneId) {
        Date filterFrom = toDate(filter.getDateFrom(), zoneId);
        Date filterTo = filter.getDateTo() != null ? toDate(filter.getDateTo(), zoneId) : null;

        return Tuple.of(filterFrom, filterTo);
    }

    private static Date toDate(final String date, final ZoneId zoneId) {
        LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
        return Date.from(localDate.atStartOfDay(zoneId).toInstant());
    }

    private static <T> TextSpecification<T> toTextSpecification(final Filter filter) {
        return TextSpecification.<T>builder()
                .key(filter.getColumnName())
                .value(filter.getFilter())
                .option(filter.getType())
                .build();
    }

    private static <T> Specification<T> toNumberSpecification(final Filter filter, final Double from, final Double to) {
        return NumberSpecification.<T>builder()
                .key(filter.getColumnName())
                .option(filter.getType())
                .value(from)
                .to(to)
                .build();
    }

    private static <T> Specification<T> toDateSpecification(final Filter filter, final Date from, final Date to) {
        return DateSpecification.<T>builder()
                .key(filter.getColumnName())
                .option(filter.getType())
                .value(from)
                .to(to)
                .build();
    }

    private static CRUDFailure cannotParseFilter(final Filter filter) {
        String message = String.format("Cannot parse '%s' as %s", filter.getColumnName(), filter.getFilterType());
        return CRUDFailure.invalidRequestParameter("search", message);
    }

    private static CRUDFailure unsupportedOperatorFailure(final FilterOption option, final String key) {
        String message = String.format("Unsupported search option '%s' for property '%s'", option, key);
        return CRUDFailure.invalidRequestParameter("search", message);
    }
}
