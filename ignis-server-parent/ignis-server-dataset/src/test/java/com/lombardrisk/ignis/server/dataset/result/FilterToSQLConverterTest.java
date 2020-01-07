package com.lombardrisk.ignis.server.dataset.result;

import com.lombardrisk.ignis.data.common.search.CombinedFilter;
import com.lombardrisk.ignis.data.common.search.Filter;
import com.lombardrisk.ignis.data.common.search.FilterExpression;
import com.lombardrisk.ignis.data.common.search.SchemaField;
import org.junit.Test;

import java.util.HashSet;

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
import static com.lombardrisk.ignis.data.common.search.FilterType.TEXT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class FilterToSQLConverterTest {

    private FilterToSQLConverter filterToSQLConverter = new FilterToSQLConverter(new PhoenixSQLFunctions());

    @Test
    public void apply_Equals_returnsSQL() {
        Filter dataset = aFilter().columnName("NAME").type(EQUALS).filter("John Templeton").build();

        assertThat(filterToSQLConverter.apply(dataset, new HashSet<>())).isEqualTo("\"NAME\" = 'John Templeton'");
    }

    @Test
    public void apply_NotEqual_returnsSQL() {
        Filter dataset = aFilter().columnName("NAME").type(NOT_EQUAL).filter("John Templeton").build();

        assertThat(filterToSQLConverter.apply(dataset, new HashSet<>())).isEqualTo("\"NAME\" != 'John Templeton'");
    }

    @Test
    public void apply_Contains_returnsSQL() {
        Filter dataset = aFilter().columnName("NAME").type(CONTAINS).filter("ed").build();

        assertThat(filterToSQLConverter.apply(dataset, new HashSet<>())).isEqualTo("\"NAME\" LIKE '%ed%'");
    }

    @Test
    public void apply_NotContains_returnsSQL() {
        Filter dataset = aFilter().columnName("NAME").type(NOT_CONTAINS).filter("ed").build();

        assertThat(filterToSQLConverter.apply(dataset, new HashSet<>())).isEqualTo("\"NAME\" NOT LIKE '%ed%'");
    }

    @Test
    public void apply_StartsWith_returnsSQL() {
        Filter dataset = aFilter().columnName("NAME").type(STARTS_WITH).filter("John").build();

        assertThat(filterToSQLConverter.apply(dataset, new HashSet<>())).isEqualTo("\"NAME\" LIKE 'John%'");
    }

    @Test
    public void apply_EndsWith_returnsSQL() {
        Filter dataset = aFilter().columnName("NAME").type(ENDS_WITH).filter("on").build();

        assertThat(filterToSQLConverter.apply(dataset, new HashSet<>())).isEqualTo("\"NAME\" LIKE '%on'");
    }

    @Test
    public void apply_LessThan_returnsSQL() {
        Filter dataset = aFilter().columnName("AMOUNT").type(LESS_THAN).filter("500").build();

        assertThat(filterToSQLConverter.apply(dataset, newHashSet(SchemaField.builder()
                .name("AMOUNT")
                .type("long")
                .build()))).isEqualTo("\"AMOUNT\" < 500");
    }

    @Test
    public void apply_LessThanOrEqual_returnsSQL() {
        Filter dataset = aFilter().columnName("AMOUNT").type(LESS_THAN_OR_EQUAL).filter("500").build();

        assertThat(filterToSQLConverter.apply(dataset, newHashSet(SchemaField.builder()
                .name("AMOUNT")
                .type("long")
                .build()))).isEqualTo("\"AMOUNT\" <= 500");
    }

    @Test
    public void apply_GreaterThan_returnsSQL() {
        Filter dataset = aFilter().columnName("AMOUNT").type(GREATER_THAN).filter("500").build();

        assertThat(filterToSQLConverter.apply(dataset, newHashSet(SchemaField.builder()
                .name("AMOUNT")
                .type("long")
                .build()))).isEqualTo("\"AMOUNT\" > 500");
    }

    @Test
    public void apply_GreaterThanOrEqual_returnsSQL() {
        Filter dataset = aFilter().columnName("AMOUNT").type(GREATER_THAN_OR_EQUAL).filter("500").build();

        assertThat(filterToSQLConverter.apply(dataset, newHashSet(SchemaField.builder()
                .name("AMOUNT")
                .type("long")
                .build()))).isEqualTo("\"AMOUNT\" >= 500");
    }

    @Test
    public void apply_InRange_returnsSQL() {
        Filter dataset = aFilter().filterType(DATE).columnName("DATE").type(IN_RANGE)
                .dateFrom("2019-08-09").dateTo("2019-08-20").build();

        assertThat(filterToSQLConverter.apply(dataset, new HashSet<>())).isEqualTo(
                "\"DATE\" BETWEEN '2019-08-09' AND '2019-08-20'");
    }

    @Test
    public void apply_DateEquals_returnsSQL() {
        Filter dataset = aFilter().columnName("DATE").type(EQUALS).dateFrom("2019-08-09").build();

        assertThat(filterToSQLConverter.apply(dataset, newHashSet(SchemaField.builder()
                .name("DATE")
                .type("date")
                .build()))).isEqualTo("\"DATE\" = TO_DATE('2019-08-09', 'yyyy-MM-dd')");
    }

    @Test
    public void apply_DateGreaterThan_returnsSQL() {
        Filter dataset = aFilter().columnName("DATE").type(GREATER_THAN).dateFrom("2019-08-09").build();

        assertThat(filterToSQLConverter.apply(dataset, newHashSet(SchemaField.builder()
                .name("DATE")
                .type("date")
                .build()))).isEqualTo("\"DATE\" > TO_DATE('2019-08-09', 'yyyy-MM-dd')");
    }

    @Test
    public void apply_DateInRange_returnsSQL() {
        Filter dataset = aFilter().columnName("DATE").type(IN_RANGE).dateFrom("2019-08-09").dateTo("2019-08-20").build();

        assertThat(filterToSQLConverter.apply(dataset, newHashSet(SchemaField.builder()
                .name("DATE")
                .type("date")
                .build()))).isEqualTo("\"DATE\" BETWEEN TO_DATE('2019-08-09', 'yyyy-MM-dd') AND TO_DATE('2019-08-20', 'yyyy-MM-dd')");
    }

    @Test
    public void apply_TimestampEquals_returnsSQL() {
        Filter dataset = aFilter().columnName("TIMESTAMP").type(EQUALS).dateFrom("2019-08-09").build();

        assertThat(filterToSQLConverter.apply(dataset, newHashSet(SchemaField.builder()
                .name("TIMESTAMP")
                .type("timestamp")
                .build()))).isEqualTo("\"TIMESTAMP\" = TO_TIMESTAMP('2019-08-09', 'yyyy-MM-dd')");
    }

    @Test
    public void apply_SanitzeQuote_returnsSQL() {
        Filter dataset = aFilter().columnName("NAME").type(CONTAINS).filter("abc\"def").build();

        assertThatThrownBy(() -> filterToSQLConverter.apply(dataset, new HashSet<>()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Sanitizing filter failed. Filter not allowed 'abc\"def'");
    }

    @Test
    public void apply_SanitzeSemicolon_returnsSQL() {
        Filter dataset = aFilter().columnName("NAME").type(CONTAINS).filter("abc;def").build();

        assertThatThrownBy(() -> filterToSQLConverter.apply(dataset, new HashSet<>()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Sanitizing filter failed. Filter not allowed 'abc;def'");
    }

    @Test
    public void apply_SanitzeLike_returnsSQL() {
        Filter dataset = aFilter().columnName("NAME").type(CONTAINS).filter("%de%").build();

        assertThatThrownBy(() -> filterToSQLConverter.apply(dataset, new HashSet<>()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Sanitizing filter failed. Filter not allowed '%de%'");
    }

    @Test
    public void apply_SanitzeAllowDecimal_returnsSQL() {
        Filter dataset = aFilter()
                .columnName("AMOUNT")
                .type(EQUALS)
                .filter("2200.00")
                .build();

        assertThat(filterToSQLConverter.apply(dataset, newHashSet(SchemaField.builder()
                .name("AMOUNT")
                .type("long")
                .build())))
                .isEqualTo("\"AMOUNT\" = 2200.00");
    }

    @Test
    public void apply_CombinedAndExpression_ReturnsSQL() {
        FilterExpression filter = CombinedFilter.and(
                aFilter().columnName("A").type(STARTS_WITH).filter("abc").build(),
                aFilter().columnName("B").type(ENDS_WITH).filter("def").build(),
                aFilter().columnName("C").type(CONTAINS).filter("ghi").build());

        String sql = filterToSQLConverter.apply(filter, new HashSet<>());

        assertThat(sql)
                .isEqualTo("\"A\" LIKE 'abc%' "
                        + "AND \"B\" LIKE '%def' "
                        + "AND \"C\" LIKE '%ghi%'");
    }

    @Test
    public void apply_CombinedOrExpression_ReturnsSQL() {
        FilterExpression filter = CombinedFilter.or(
                aFilter().columnName("A").type(STARTS_WITH).filter("abc").build(),
                aFilter().columnName("B").type(ENDS_WITH).filter("def").build(),
                aFilter().columnName("C").type(CONTAINS).filter("ghi").build());

        String sql = filterToSQLConverter.apply(filter, new HashSet<>());

        assertThat(sql)
                .isEqualTo("\"A\" LIKE 'abc%' "
                        + "OR \"B\" LIKE '%def' "
                        + "OR \"C\" LIKE '%ghi%'");
    }

    @Test
    public void apply_NestedExpression_ReturnsSQL() {
        FilterExpression filter1 = CombinedFilter.or(
                aFilter().columnName("A").type(STARTS_WITH).filter("abc").filterType(TEXT).build(),
                CombinedFilter.and(
                        aFilter().columnName("B").type(CONTAINS).filter("def").build(),
                        aFilter().columnName("C").type(EQUALS).filter("ghi").build()));

        FilterExpression filter2 = CombinedFilter.or(
                CombinedFilter.and(
                        aFilter().columnName("B").type(CONTAINS).filter("def").build(),
                        aFilter().columnName("C").type(EQUALS).filter("ghi").build()),
                aFilter().columnName("A").type(STARTS_WITH).filter("abc").build());

        FilterExpression filter3 = CombinedFilter.or(
                CombinedFilter.and(
                        aFilter().columnName("A").type(EQUALS).filter("abc").build(),
                        CombinedFilter.or(
                                aFilter().columnName("B").type(LESS_THAN).filter("123").build(),
                                aFilter().columnName("C").type(GREATER_THAN).filter("456").build())),
                CombinedFilter.and(
                        CombinedFilter.and(
                                aFilter().columnName("B").type(GREATER_THAN_OR_EQUAL).filter("999").build(),
                                aFilter().columnName("D").type(ENDS_WITH).filter("xyz").build()),
                        aFilter().columnName("A").type(EQUALS).filter("def").build()),
                aFilter().columnName("Z").type(GREATER_THAN_OR_EQUAL).filter("999").build()
        );

        assertThat(filterToSQLConverter.apply(filter1, new HashSet<>()))
                .isEqualTo("\"A\" LIKE 'abc%' OR (\"B\" LIKE '%def%' AND \"C\" = 'ghi')");

        assertThat(filterToSQLConverter.apply(filter2, new HashSet<>()))
                .isEqualTo("(\"B\" LIKE '%def%' AND \"C\" = 'ghi') OR \"A\" LIKE 'abc%'");

        assertThat(filterToSQLConverter.apply(
                filter3,
                newHashSet(SchemaField.builder()
                        .name("A")
                        .type("string")
                        .build(), SchemaField.builder()
                        .name("B")
                        .type("long")
                        .build(), SchemaField.builder()
                        .name("C")
                        .type("long")
                        .build(), SchemaField.builder()
                        .name("D")
                        .type("string")
                        .build(), SchemaField.builder()
                        .name("Z")
                        .type("long")
                        .build())))
                .isEqualTo("(\"A\" = 'abc' AND (\"B\" < 123 OR \"C\" > 456)) "
                        + "OR ((\"B\" >= 999 AND \"D\" LIKE '%xyz') AND \"A\" = 'def') "
                        + "OR \"Z\" >= 999");
    }

    @Test
    public void apply_FilterWithDateFormat() {
        FilterExpression invalidFilter = Filter.builder()
                .columnName("SomeDate")
                .filterType(DATE)
                .type(IN_RANGE)
                .dateFrom("10-11-1998 12:01:09")
                .dateTo("11-11-1998 16:51:33")
                .dateFormat("dd-MM-yyyy HH:mm:ss")
                .build();

        SchemaField schemaField = SchemaField.builder()
                .name("SomeDate")
                .type("date")
                .build();

        String sql = filterToSQLConverter.apply(invalidFilter, newHashSet(schemaField));

        assertThat(sql)
                .isEqualTo("\"SomeDate\" BETWEEN "
                        + "TO_DATE('10-11-1998 12:01:09', 'dd-MM-yyyy HH:mm:ss') "
                        + "AND "
                        + "TO_DATE('11-11-1998 16:51:33', 'dd-MM-yyyy HH:mm:ss')");
    }

    @Test
    public void apply_InvalidDateFormat() {
        FilterExpression invalidFilter = Filter.builder()
                .columnName("SomeDate")
                .filterType(DATE)
                .type(EQUALS)
                .dateFrom("01-01-2001")
                .dateFormat("this is not a valid date format")
                .build();

        SchemaField schemaField = SchemaField.builder()
                .name("SomeDate")
                .type("date")
                .build();

        assertThatThrownBy(() -> filterToSQLConverter.apply(invalidFilter, newHashSet(schemaField)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown pattern letter");
    }

    private Filter.FilterBuilder aFilter() {
        return Filter.builder();
    }
}
