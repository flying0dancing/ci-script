package com.lombardrisk.ignis.server.dataset.result;

import com.google.common.collect.ImmutableMap;
import com.lombardrisk.ignis.data.common.search.CombinedFilter;
import com.lombardrisk.ignis.data.common.search.Filter;
import com.lombardrisk.ignis.data.common.search.FilterExpression;
import com.lombardrisk.ignis.data.common.search.FilterOption;
import com.lombardrisk.ignis.data.common.search.FilterType;
import com.lombardrisk.ignis.data.common.search.SchemaField;
import com.lombardrisk.ignis.server.product.table.model.FieldTypes;
import io.vavr.Function2;
import io.vavr.control.Option;
import lombok.extern.slf4j.Slf4j;

import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class FilterToSQLConverter implements Function2<FilterExpression, Set<SchemaField>, String> {

    private static final long serialVersionUID = 2563596802662186978L;

    private static final String EQUALS = " = ";
    private static final String NOTEQUAL = " != ";
    private static final String LIKE = " LIKE ";
    private static final String NOTLIKE = " NOT LIKE ";
    private static final String LESSTHAN = " < ";
    private static final String LESSTHANOREQUAL = " <= ";
    private static final String GREATERTHAN = " > ";
    private static final String GREATERTHANOREQUAL = " >= ";
    private static final String BETWEEN = " BETWEEN ";
    private static final String AND = " AND ";

    private static final String QUOTE = "'";
    private static final String PERCENT = "%";

    private static final String COLUMN_TYPE_STRING = "String";
    private static final String COLUMN_TYPE_DATE = "Date";
    private static final String COLUMN_TYPE_TIMESTAMP = "Timestamp";

    static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";

    private Map<FilterOption, String> filterToSQLMap = ImmutableMap.<FilterOption, String>builder()
            .put(FilterOption.EQUALS, EQUALS)
            .put(FilterOption.NOT_EQUAL, NOTEQUAL)
            .put(FilterOption.CONTAINS, LIKE)
            .put(FilterOption.NOT_CONTAINS, NOTLIKE)
            .put(FilterOption.STARTS_WITH, LIKE)
            .put(FilterOption.ENDS_WITH, LIKE)
            .put(FilterOption.LESS_THAN, LESSTHAN)
            .put(FilterOption.LESS_THAN_OR_EQUAL, LESSTHANOREQUAL)
            .put(FilterOption.GREATER_THAN, GREATERTHAN)
            .put(FilterOption.GREATER_THAN_OR_EQUAL, GREATERTHANOREQUAL)
            .put(FilterOption.IN_RANGE, BETWEEN)
            .build();

    private final SQLFunctions sqlFunctions;

    public FilterToSQLConverter(final SQLFunctions sqlFunctions) {
        this.sqlFunctions = sqlFunctions;
    }

    @Override
    public String apply(final FilterExpression filter, final Set<SchemaField> schemaFields) {
        StringBuilder sb = new StringBuilder();
        return buildSql(filter, sb, schemaFields).toString();
    }

    private StringBuilder buildSql(
            final FilterExpression filter, final StringBuilder sb,
            final Set<SchemaField> schemaFields) {
        if (filter.getExpressionType() == FilterExpression.ExpressionType.SIMPLE) {
            String filterString = filterToString((Filter) filter, schemaFields);
            return sb.append(filterString);
        }
        if (filter.getExpressionType() == FilterExpression.ExpressionType.COMBINED) {
            CombinedFilter combinedFilter = (CombinedFilter) filter;
            for (int i = 0; i < combinedFilter.getFilters().size(); i++) {
                FilterExpression nestedFilter = combinedFilter.getFilters().get(i);
                boolean alsoCombined = nestedFilter.getExpressionType() == FilterExpression.ExpressionType.COMBINED;

                if (alsoCombined) {
                    sb.append("(");
                    buildSql(nestedFilter, sb, schemaFields);
                    sb.append(")");
                } else {
                    buildSql(nestedFilter, sb, schemaFields);
                }

                if (i != combinedFilter.getFilters().size() - 1) {
                    sb.append(" ").append(combinedFilter.getOperator()).append(" ");
                }
            }
            return sb;
        }
        throw new UnsupportedOperationException("Unsupported filter expression type: " + filter.getExpressionType());
    }

    private String filterToString(final Filter inputFilter, final Set<SchemaField> schemaFields) {
        Filter filter = normaliseDateFilter(inputFilter);
        String operation = filterToSQLMap.get(filter.getType());
        if (operation != null) {
            sanitizeFilter(filter.getFilter());
            sanitizeDateFormat(filter.getDateFormat());
            return getColumnName(filter) + operation + getFilter(filter, schemaFields) + getFilterTo(filter, schemaFields);
        }
        return "";
    }

    private Filter normaliseDateFilter(final Filter filter) {
        if (isFilterOfDateType(filter) || isFilterDateFromHasValue(filter)) {
            filter.setFilter(filter.getDateFrom());
            filter.setFilterTo(filter.getDateTo());
        }
        return filter;
    }

    private boolean isFilterDateFromHasValue(final Filter filter) {
        return filter.getFilter() == null
                && filter.getDateFrom() != null && !filter.getDateFrom().isEmpty();
    }

    private boolean isFilterOfDateType(final Filter filter) {
        return filter.getFilterType() != null && filter.getFilterType() == FilterType.DATE;
    }

    private String getColumnName(final Filter filter) {
        return "\"" + filter.getColumnName() + "\"";
    }

    private void sanitizeFilter(final String filter) {
        Pattern pattern = Pattern.compile("[a-zA-Z0-9.: \\-]*");
        Matcher matcher = pattern.matcher(filter);

        if (!matcher.matches()) {
            log.error("Sanitizing filter failed. Filter not allowed [{}]", filter);
            throw new IllegalArgumentException("Sanitizing filter failed. Filter not allowed '" + filter + "'");
        }
    }

    private void sanitizeDateFormat(final String dateFormat) {
        if (dateFormat != null) {
            try {
                DateTimeFormatter.ofPattern(dateFormat);
            } catch (IllegalArgumentException e) {
                log.error("Sanitizing date format failed. Date format not allowed [{}]", dateFormat);
                throw e;
            }
        }
    }

    private String getFilterTo(final Filter filter, final Set<SchemaField> schemaFields) {
        if (filter.getFilterTo() != null && !filter.getFilterTo().isEmpty()) {
            return !filter.getFilterTo().isEmpty() ? AND + quoteFilter(filter.getFilterTo(), filter, schemaFields) : "";
        }
        return "";
    }

    private String getFilter(Filter filter, final Set<SchemaField> schemaFields) {
        return quoteFilter(getFilterValue(filter), filter, schemaFields);
    }

    private String getFilterValue(Filter filter) {
        switch (filter.getType()) {
            case EQUALS:
            case NOT_EQUAL:
            case IN_RANGE:
                return filter.getFilter();
            case CONTAINS:
            case NOT_CONTAINS:
                return like(filter.getFilter());
            case STARTS_WITH:
                return startsWith(filter.getFilter());
            case ENDS_WITH:
                return endsWith(filter.getFilter());
            default:
                return filter.getFilter();
        }
    }

    private String quoteFilter(String filterValue, Filter filter, final Set<SchemaField> schemaFields) {
        String columnType = getColumnType(filter, schemaFields);
        String dateFormat = filter.getDateFormat() != null ? filter.getDateFormat() : DEFAULT_DATE_FORMAT;

        if (columnType.equalsIgnoreCase(COLUMN_TYPE_STRING)) {
            return quote(filterValue);
        } else if (columnType.equalsIgnoreCase(COLUMN_TYPE_DATE)) {
            return sqlFunctions.toDate(filterValue, dateFormat);
        } else if (columnType.equalsIgnoreCase(COLUMN_TYPE_TIMESTAMP)) {
            return sqlFunctions.toTimestamp(filterValue, dateFormat);
        }
        return filterValue;
    }

    private String getColumnType(final Filter filter, final Set<SchemaField> schemaFields) {
        Option<String> filterColumnName = Option.ofOptional(schemaFields.stream()
                .filter(field -> field.getName().equalsIgnoreCase(filter.getColumnName()))
                .map(SchemaField::getType)
                .findFirst());

        if (!filterColumnName.isEmpty()) {
            return filterColumnName.get();
        }

        return FieldTypes.STRING;
    }

    private String quote(final String value) {
        return QUOTE + value + QUOTE;
    }

    private String like(final String filter) {
        return PERCENT + filter + PERCENT;
    }

    private String startsWith(final String filter) {
        return filter + PERCENT;
    }

    private String endsWith(final String filter) {
        return PERCENT + filter;
    }
}