package com.lombardrisk.ignis.data.common.search;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import io.vavr.control.Validation;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.data.jpa.domain.Specification;

import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
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
import static com.lombardrisk.ignis.data.common.search.FilterType.DATE;
import static com.lombardrisk.ignis.data.common.search.FilterType.NUMBER;

@SuppressWarnings("unchecked")
public class FilterValidatorTest {

    @Rule
    public JUnitSoftAssertions soft = new JUnitSoftAssertions();

    @Test
    public void validateText_UnsupportedOption_ReturnsError() {
        Set<FilterOption> textFilterOptions = ImmutableSet.of(
                EQUALS, NOT_EQUAL, CONTAINS, NOT_CONTAINS, STARTS_WITH, ENDS_WITH);

        Set<FilterOption> invalidOptions = newHashSet(FilterOption.values());
        invalidOptions.removeAll(textFilterOptions);

        for (FilterOption operator : invalidOptions) {
            Filter filter = Filter.builder().columnName("columnName").type(operator).filter("value").build();

            Validation<CRUDFailure, Specification<Object>> specification =
                    FilterValidator.validateText(filter);

            soft.assertThat(specification.isInvalid())
                    .isTrue();

            soft.assertThat(specification.getError().getErrorMessage())
                    .contains("Unsupported search option '" + operator + "' for property 'columnName'");
        }
    }

    @Test
    public void validateText_SupportedOption_ReturnsSpecification() {
        List<FilterOption> validOptions = ImmutableList.of(
                EQUALS, NOT_EQUAL, CONTAINS, NOT_CONTAINS, STARTS_WITH, ENDS_WITH);

        for (FilterOption operator : validOptions) {
            Filter filter = Filter.builder().columnName("columnName").type(operator).filter("value").build();

            Validation<CRUDFailure, Specification<Object>> specification =
                    FilterValidator.validateText(filter);

            soft.assertThat(specification.isValid())
                    .isTrue();

            soft.assertThat(specification.get())
                    .isInstanceOf(TextSpecification.class);

            soft.assertThat((TextSpecification) specification.get())
                    .extracting(TextSpecification::getKey, TextSpecification::getOption, TextSpecification::getValue)
                    .containsExactly("columnName", operator, "value");
        }
    }

    @Test
    public void validateNumber_UnsupportedOption_ReturnsError() {
        Set<FilterOption> numberOptions = ImmutableSet.of(
                EQUALS, NOT_EQUAL, LESS_THAN, LESS_THAN_OR_EQUAL, GREATER_THAN, GREATER_THAN_OR_EQUAL, IN_RANGE);

        Set<FilterOption> invalidOptions = newHashSet(FilterOption.values());
        invalidOptions.removeAll(numberOptions);

        for (FilterOption operator : invalidOptions) {
            Filter filter = Filter.builder().columnName("columnName").type(operator).filter("123").build();

            Validation<CRUDFailure, Specification<Object>> specification =
                    FilterValidator.validateNumber(filter);

            soft.assertThat(specification.isInvalid())
                    .isTrue();

            soft.assertThat(specification.getError().getErrorMessage())
                    .contains("Unsupported search option '" + operator + "' for property 'columnName'");
        }
    }

    @Test
    public void validateNumber_InvalidNumberFilter_ReturnsError() {
        Filter filter = Filter.builder()
                .columnName("columnName").type(EQUALS).filter("not a number").filterType(NUMBER).build();

        Validation<CRUDFailure, Specification<Object>> specification =
                FilterValidator.validateNumber(filter);

        soft.assertThat(specification.isInvalid())
                .isTrue();

        soft.assertThat(specification.getError().getErrorMessage())
                .contains("Cannot parse 'columnName' as NUMBER");
    }

    @Test
    public void validateNumber_InvalidNumberFilterTo_ReturnsError() {
        Filter filter = Filter.builder()
                .columnName("columnName").type(IN_RANGE)
                .filter("1234")
                .filterTo("not a number")
                .filterType(NUMBER)
                .build();

        Validation<CRUDFailure, Specification<Object>> specification =
                FilterValidator.validateNumber(filter);

        soft.assertThat(specification.isInvalid())
                .isTrue();

        soft.assertThat(specification.getError().getErrorMessage())
                .contains("Cannot parse 'columnName' as NUMBER");
    }

    @Test
    public void validateNumber_SupportedOptionAndValidNumber_ReturnsSpecification() {
        List<FilterOption> validOptions = ImmutableList.of(
                EQUALS, NOT_EQUAL, LESS_THAN, LESS_THAN_OR_EQUAL, GREATER_THAN, GREATER_THAN_OR_EQUAL);

        for (FilterOption operator : validOptions) {
            Filter filter = Filter.builder()
                    .columnName("columnName")
                    .type(operator)
                    .filter("123")
                    .filterTo("456")
                    .build();

            Validation<CRUDFailure, Specification<Object>> specification =
                    FilterValidator.validateNumber(filter);

            soft.assertThat(specification.isValid())
                    .isTrue();

            soft.assertThat(specification.get())
                    .isInstanceOf(NumberSpecification.class);

            soft.assertThat((NumberSpecification) specification.get())
                    .extracting(NumberSpecification::getKey,
                            NumberSpecification::getOption,
                            NumberSpecification::getValue,
                            NumberSpecification::getTo)
                    .containsExactly("columnName", operator, 123D, 456D);
        }
    }

    @Test
    public void validateDate_UnsupportedOption_ReturnsError() {
        Set<FilterOption> dateOptions = ImmutableSet.of(
                EQUALS, NOT_EQUAL, LESS_THAN, LESS_THAN_OR_EQUAL, GREATER_THAN, GREATER_THAN_OR_EQUAL, IN_RANGE);

        Set<FilterOption> invalidOptions = newHashSet(FilterOption.values());
        invalidOptions.removeAll(dateOptions);

        for (FilterOption operator : invalidOptions) {
            Filter filter = Filter.builder().columnName("columnName").type(operator).dateFrom("2019-01-01").build();

            Validation<CRUDFailure, Specification<Object>> specification =
                    FilterValidator.validateDate(filter, ZoneId.systemDefault());

            soft.assertThat(specification.isInvalid())
                    .isTrue();

            soft.assertThat(specification.getError().getErrorMessage())
                    .contains("Unsupported search option '" + operator + "' for property 'columnName'");
        }
    }

    @Test
    public void validateDate_InvalidDateFilter_ReturnsError() {
        Filter filter = Filter.builder()
                .columnName("columnName").type(EQUALS).dateFrom("not a date").filterType(DATE).build();

        Validation<CRUDFailure, Specification<Object>> specification =
                FilterValidator.validateDate(filter, ZoneId.systemDefault());

        soft.assertThat(specification.isInvalid())
                .isTrue();

        soft.assertThat(specification.getError().getErrorMessage())
                .contains("Cannot parse 'columnName' as DATE");
    }

    @Test
    public void validateDate_InvalidDateFilterTo_ReturnsError() {
        Date date = new Date();

        Filter filter = Filter.builder()
                .columnName("columnName")
                .type(IN_RANGE)
                .dateFrom("2019-01-01")
                .dateTo("not a date")
                .filterType(DATE)
                .build();

        Validation<CRUDFailure, Specification<Object>> specification =
                FilterValidator.validateDate(filter, ZoneId.systemDefault());

        soft.assertThat(specification.isInvalid())
                .isTrue();

        soft.assertThat(specification.getError().getErrorMessage())
                .contains("Cannot parse 'columnName' as DATE");
    }

    @Test
    public void validateDate_SupportedOptionAndValidDate_ReturnsSpecification() {
        List<FilterOption> validOptions = ImmutableList.of(
                EQUALS, NOT_EQUAL, LESS_THAN, LESS_THAN_OR_EQUAL, GREATER_THAN, GREATER_THAN_OR_EQUAL);

        Date date1 = new Calendar.Builder().setDate(2019, 0, 1).build().getTime();
        Date date2 = new Calendar.Builder().setDate(2019, 0, 2).build().getTime();

        for (FilterOption operator : validOptions) {
            Filter filter = Filter.builder()
                    .columnName("columnName")
                    .type(operator)
                    .dateFrom("2019-01-01")
                    .dateTo("2019-01-02")
                    .build();

            Validation<CRUDFailure, Specification<Object>> specification =
                    FilterValidator.validateDate(filter, ZoneId.systemDefault());

            soft.assertThat(specification.isValid())
                    .isTrue();

            soft.assertThat(specification.get())
                    .isInstanceOf(DateSpecification.class);

            soft.assertThat((DateSpecification) specification.get())
                    .extracting(DateSpecification::getKey,
                            DateSpecification::getOption,
                            DateSpecification::getValue,
                            DateSpecification::getTo)
                    .containsExactly("columnName", operator, date1, date2);
        }
    }
}