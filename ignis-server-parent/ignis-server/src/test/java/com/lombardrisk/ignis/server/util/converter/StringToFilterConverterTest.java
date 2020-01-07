package com.lombardrisk.ignis.server.util.converter;

import com.lombardrisk.ignis.data.common.search.CombinedFilter;
import com.lombardrisk.ignis.data.common.search.Filter;
import com.lombardrisk.ignis.data.common.search.FilterExpression;
import com.lombardrisk.ignis.data.common.search.FilterOption;
import com.lombardrisk.ignis.data.common.search.FilterType;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class StringToFilterConverterTest {

    @Test
    public void toFilter_simpleFilter() {
        String simpleFilterJson = ""
                + "{"
                + "  \"expressionType\": \"simple\", "
                + "  \"columnName\": \"my_column_name\","
                + "  \"type\": \"inRange\","
                + "  \"filter\": \"123\","
                + "  \"filterTo\": \"456\","
                + "  \"filterType\": \"number\""
                + "}";

        FilterExpression filterExpression = StringToFilterConverter.toFilter(simpleFilterJson);

        assertThat(filterExpression)
                .isEqualTo(Filter.builder()
                        .columnName("my_column_name")
                        .type(FilterOption.IN_RANGE)
                        .filter("123")
                        .filterTo("456")
                        .filterType(FilterType.NUMBER)
                        .build());
    }

    @Test
    public void toFilter_combinedFilter() {
        String filterJson = ""
                + "{"
                + "  \"expressionType\": \"combined\", \"operator\": \"AND\", "
                + "  \"filters\": ["
                + "    {"
                + "      \"expressionType\": \"simple\", \"columnName\": \"my_column_name\", \"type\": \"inRange\", "
                + "      \"filter\": \"123\", \"filterTo\": \"456\", \"filterType\": \"number\""
                + "    }, "
                + "    {"
                + "      \"expressionType\": \"simple\", \"columnName\": \"other_column_name\", \"type\": \"startsWith\", "
                + "      \"filter\": \"123\", \"filterTo\": null, \"filterType\": \"text\""
                + "    }"
                + "  ]"
                + "}";

        FilterExpression filterExpression = StringToFilterConverter.toFilter(filterJson);

        assertThat(filterExpression)
                .isEqualTo(CombinedFilter.and(
                        Filter.builder()
                                .columnName("my_column_name")
                                .type(FilterOption.IN_RANGE)
                                .filter("123")
                                .filterTo("456")
                                .filterType(FilterType.NUMBER)
                                .build(),
                        Filter.builder()
                                .columnName("other_column_name")
                                .type(FilterOption.STARTS_WITH)
                                .filter("123")
                                .filterType(FilterType.TEXT)
                                .build()));
    }

    @Test
    public void toFilter_nestedFilters() {
        String filterJson = "{"
                + "  \"expressionType\": \"combined\", \"operator\": \"OR\","
                + "  \"filters\": ["
                + "    {"
                + "      \"expressionType\": \"simple\", \"columnName\": \"B\", \"type\": \"equals\","
                + "      \"filter\": \"def\", \"filterTo\": null, \"filterType\": \"text\""
                + "    },"
                + "    {"
                + "      \"expressionType\": \"combined\", \"operator\": \"AND\", \"filters\": ["
                + "      {"
                + "        \"expressionType\": \"simple\", \"columnName\": \"A\", \"type\": \"startsWith\","
                + "        \"filter\": \"abc\", \"filterTo\": null, \"filterType\": \"text\""
                + "      }, {"
                + "        \"expressionType\": \"simple\", \"columnName\": \"C\", \"type\": \"endsWith\","
                + "        \"filter\": \"ghi\", \"filterTo\": null, \"filterType\": \"text\""
                + "      }"
                + "    ]"
                + "    }"
                + "  ]"
                + "}";

        FilterExpression nestedFilter = StringToFilterConverter.toFilter(filterJson);

        assertThat(nestedFilter)
                .isEqualTo(CombinedFilter.or(
                        Filter.builder()
                                .columnName("B")
                                .type(FilterOption.EQUALS)
                                .filter("def")
                                .filterType(FilterType.TEXT)
                                .build(),
                        CombinedFilter.and(
                                Filter.builder()
                                        .columnName("A")
                                        .type(FilterOption.STARTS_WITH)
                                        .filter("abc")
                                        .filterType(FilterType.TEXT)
                                        .build(),
                                Filter.builder()
                                        .columnName("C")
                                        .type(FilterOption.ENDS_WITH)
                                        .filter("ghi")
                                        .filterType(FilterType.TEXT)
                                        .build())));
    }

    @Test
    public void toFilter_filterOptionCamelCase_ConvertsToEnum() {
        String filterJson = ""
                + "{"
                + "  \"expressionType\": \"combined\", \"operator\": \"AND\", "
                + "  \"filters\": ["
                + "    {\"expressionType\": \"simple\", \"type\": \"equals\"}, "
                + "    {\"expressionType\": \"simple\", \"type\": \"notEqual\"}, "
                + "    {\"expressionType\": \"simple\", \"type\": \"contains\"}, "
                + "    {\"expressionType\": \"simple\", \"type\": \"notContains\"}, "
                + "    {\"expressionType\": \"simple\", \"type\": \"startsWith\"}, "
                + "    {\"expressionType\": \"simple\", \"type\": \"endsWith\"}, "
                + "    {\"expressionType\": \"simple\", \"type\": \"lessThan\"}, "
                + "    {\"expressionType\": \"simple\", \"type\": \"lessThanOrEqual\"}, "
                + "    {\"expressionType\": \"simple\", \"type\": \"greaterThan\"}, "
                + "    {\"expressionType\": \"simple\", \"type\": \"greaterThanOrEqual\"}, "
                + "    {\"expressionType\": \"simple\", \"type\": \"inRange\"}  "
                + "  ]"
                + "}";

        FilterExpression filterExpression = StringToFilterConverter.toFilter(filterJson);

        assertThat(filterExpression)
                .isInstanceOf(CombinedFilter.class);

        List<FilterExpression> filterExpressions = ((CombinedFilter) filterExpression).getFilters();

        assertThat(filterExpressions)
                .hasOnlyElementsOfType(Filter.class);

        List<Filter> filters = filterExpressions.stream().map(Filter.class::cast).collect(Collectors.toList());

        assertThat(filters)
                .extracting(Filter::getType)
                .containsExactly(FilterOption.values());
    }
}