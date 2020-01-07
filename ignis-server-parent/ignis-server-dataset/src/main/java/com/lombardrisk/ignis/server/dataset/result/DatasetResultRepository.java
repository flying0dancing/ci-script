package com.lombardrisk.ignis.server.dataset.result;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.lombardrisk.ignis.api.util.StepFilterUtils;
import com.lombardrisk.ignis.client.external.productconfig.export.FieldTypes;
import com.lombardrisk.ignis.common.MapperUtils;
import com.lombardrisk.ignis.data.common.search.CombinedFilter;
import com.lombardrisk.ignis.data.common.search.Filter;
import com.lombardrisk.ignis.data.common.search.FilterExpression;
import com.lombardrisk.ignis.data.common.search.FilterOption;
import com.lombardrisk.ignis.data.common.search.FilterType;
import com.lombardrisk.ignis.data.common.search.SchemaField;
import com.lombardrisk.ignis.server.dataset.model.Dataset;
import com.lombardrisk.ignis.server.dataset.phoenix.PhoenixResultRowMapper;
import com.lombardrisk.ignis.server.dataset.phoenix.QueryUtils;
import com.lombardrisk.ignis.server.product.pipeline.PipelineStepService;
import com.lombardrisk.ignis.server.product.pipeline.model.PipelineStep;
import com.lombardrisk.ignis.server.product.pipeline.model.TransformationType;
import com.lombardrisk.ignis.server.product.pipeline.select.PipelineFilter;
import com.lombardrisk.ignis.server.product.table.model.Field;
import com.lombardrisk.ignis.server.product.table.model.LongField;
import com.lombardrisk.ignis.server.product.table.model.StringField;
import com.lombardrisk.ignis.server.product.table.model.Table;
import io.vavr.Tuple;
import io.vavr.control.Option;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.lombardrisk.ignis.api.util.StepFilterUtils.getStepFilterWithQuotes;
import static com.lombardrisk.ignis.pipeline.step.api.DatasetFieldName.ROW_KEY;
import static com.lombardrisk.ignis.server.dataset.phoenix.PhoenixResultRowMapper.QueryColumn;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

@AllArgsConstructor
public class DatasetResultRepository {

    private static final String FCR_SYS_FILTERED = "FCR_SYS__FILTERED";
    private static final String AND = " AND ";

    private final JdbcTemplate phoenixJdbcTemplate;
    private final FilterToSQLConverter filterToSQLConverter;
    private final PipelineStepService pipelineStepService;

    @Transactional
    public DatasetRowData queryDataset(
            final Dataset dataset,
            final Pageable pageRequest,
            final FilterExpression searchFilter) {

        Table schema = dataset.getSchema();

        ImmutableList.Builder<QueryColumn> fieldsToQuery = ImmutableList.<QueryColumn>builder()
                .addAll(MapperUtils.map(schema.getFields(), QueryColumn::fromField));

        String filterQuery = getFilterQuery(schema, searchFilter);

        return queryDatasetRows(dataset, pageRequest, schema, fieldsToQuery.build(), filterQuery);
    }

    @Transactional
    public DatasetRowData findDrillbackOutputDatasetRowData(
            final Dataset dataset,
            final PipelineStep drillBackStep,
            final Pageable pageRequest,
            final FilterExpression searchFilter) {

        Table schema = dataset.getSchema();

        ImmutableList.Builder<QueryColumn> fieldsToQuery = ImmutableList.<QueryColumn>builder()
                .add(QueryColumn.builder()
                        .name(ROW_KEY.getName())
                        .expression(ROW_KEY.getName())
                        .build())
                .addAll(MapperUtils.map(schema.getFields(), QueryColumn::fromField))
                .addAll(pipelineStepDrillBackColumns(drillBackStep));

        String filterQuery = getFilterQuery(schema, searchFilter);

        return queryDatasetRows(dataset, pageRequest, schema, fieldsToQuery.build(), filterQuery);
    }

    @Transactional
    public DatasetRowData findDrillbackInputDatasetRowData(
            final Dataset dataset,
            final PipelineStep drillBackStep,
            final Pageable pageRequest,
            final Boolean showOnlyDrillbackRows,
            final Long outputTableRowKey,
            final FilterExpression searchFilter) {

        Table schema = dataset.getSchema();

        ImmutableList.Builder<QueryColumn> fieldsToQuery = ImmutableList.<QueryColumn>builder()
                .add(QueryColumn.builder()
                        .name(ROW_KEY.getName())
                        .expression(ROW_KEY.getName())
                        .build())
                .addAll(MapperUtils.map(schema.getFields(), QueryColumn::fromField));

        Option<String> pipelineStepFilter = getPipelineStepFilters(drillBackStep, schema);

        if (pipelineStepFilter.isDefined()) {
            fieldsToQuery.add(toFilteredQueryColumn(pipelineStepFilter.get()));
        }

        if (showOnlyDrillbackRows) {
            Map<String, String> drillbackColumnMap =
                    queryPipelineStepDrillbackColumns(drillBackStep, schema, outputTableRowKey);

            String filterQuery = pipelineStepFilter
                    .map(stepFilter -> getFilterQuery(schema, searchFilter, drillbackColumnMap, stepFilter))
                    .getOrElse(getFilterQuery(schema, searchFilter, drillbackColumnMap));

            return queryDatasetRows(dataset, pageRequest, schema, fieldsToQuery.build(), filterQuery);
        }

        String filterQuery = getFilterQuery(schema, searchFilter);

        return queryDatasetRows(dataset, pageRequest, schema,
                fieldsToQuery.build(),
                filterQuery);
    }

    private Option<String> getPipelineStepFilters(final PipelineStep drillBackStep, final Table schema) {
        Set<String> validDrillBackFilters = drillBackStep.getFilters();

        if (TransformationType.UNION.equals(drillBackStep.getType())) {
            validDrillBackFilters = drillBackStep.getPipelineFilters().stream()
                    .filter(filter -> filter.getUnionSchemaId().equals(schema.getId()))
                    .map(PipelineFilter::getFilter)
                    .collect(toSet());
        }

        return toStepFilterSqlString(checkFiltersSyntax(validDrillBackFilters), schema);
    }

    private Set<String> checkFiltersSyntax(final Set<String> filters) {
        return filters.stream()
                .map(StepFilterUtils::replaceDoubleEqualsByEquals)
                .collect(toSet());
    }

    private Map<String, String> queryPipelineStepDrillbackColumns(
            final PipelineStep pipelineStep, final Table inputSchema, final Long rowKey) {

        String drillbackColumns = pipelineStepService.getDrillbackColumnLinks(pipelineStep).stream()
                .filter(drillbackColumnLink -> drillbackColumnLink.getInputSchema()
                        .equals(inputSchema.getPhysicalTableName()))
                .map(drillbackColumnLink -> String.format("\"%s\" AS \"%s\"",
                        drillbackColumnLink.toDrillbackColumn(), drillbackColumnLink.getInputColumn()))
                .collect(Collectors.joining(", "));

        if (StringUtils.isBlank(drillbackColumns)) {
            return emptyMap();
        }

        String outputSchemaName = pipelineStep.getOutput().getPhysicalTableName();

        String query = String.format("SELECT %s FROM %s WHERE ROW_KEY = ?", drillbackColumns, outputSchemaName);

        Map<String, Object> drillbackValues = phoenixJdbcTemplate.queryForMap(query, rowKey);

        return drillbackValues.entrySet().stream()
                .map(e -> Tuple.of(e.getKey(), convertDrillbackValue(e.getValue())))
                .collect(toMap(tuple -> tuple._1, tuple -> tuple._2));
    }

    private String convertDrillbackValue(final Object drillbackValue) {
        return drillbackValue == null ? "null" : drillbackValue.toString();
    }

    private QueryColumn toFilteredQueryColumn(final String filterSql) {
        return QueryColumn.builder()
                .expression("CASE when (" + filterSql + ") then false else true end ")
                .name(FCR_SYS_FILTERED)
                .build();
    }

    private Option<String> toStepFilterSqlString(final Set<String> filters, final Table schema) {
        if (filters.isEmpty()) {
            return Option.none();
        }

        Set<String> schemaFieldNames = schema.getFields().stream().map(Field::getName).collect(toSet());

        String filterString = filters.stream()
                .map(filter -> getStepFilterWithQuotes(filter, schemaFieldNames))
                .collect(Collectors.joining(AND));

        return Option.of(filterString);
    }

    private List<QueryColumn> pipelineStepDrillBackColumns(final PipelineStep pipelineStep) {
        return pipelineStepService.getDrillbackColumnLinks(pipelineStep).stream()
                .map(drillbackColumnLink -> "\"" + drillbackColumnLink.toDrillbackColumn() + "\"")
                .map(physicalColumn -> QueryColumn.builder()
                        .name(physicalColumn)
                        .expression(physicalColumn)
                        .build())
                .collect(Collectors.toList());
    }

    private DatasetRowData queryDatasetRows(
            final Dataset dataset,
            final Pageable pageRequest,
            final Table schema,
            final List<QueryColumn> fieldsToQuery,
            final String filterQuery) {

        String rowCountQuery = buildRowCountQuery(schema, dataset, filterQuery);
        String datasetQuery = buildQuery(fieldsToQuery, schema, dataset, pageRequest, filterQuery);

        Long totalRowCount = phoenixJdbcTemplate.queryForObject(rowCountQuery, Long.class);

        List<Map<String, Object>> result = phoenixJdbcTemplate.query(
                datasetQuery, new PhoenixResultRowMapper(fieldsToQuery));

        return DatasetRowData.builder()
                .dataset(dataset)
                .resultData(new PageImpl<>(result, pageRequest, totalRowCount))
                .build();
    }

    private String buildRowCountQuery(final Table schema, final Dataset dataset, final String filterQuery) {
        return "SELECT COUNT(*)"
                + " FROM " + schema.getPhysicalTableName()
                + " WHERE " + dataset.getPredicate()
                + filterQuery;
    }

    private String buildQuery(
            final List<QueryColumn> fieldsToQuery,
            final Table schema,
            final Dataset dataset,
            final Pageable pageRequest,
            final String filter) {

        String schemaFieldSelect = fieldsToQuery.stream()
                .map(QueryColumn::toSelectExpression)
                .collect(Collectors.joining(", "));

        String unPagedQuery = "SELECT " + schemaFieldSelect
                + " FROM " + schema.getPhysicalTableName()
                + " WHERE " + dataset.getPredicate()
                + filter
                + getOrderQuery(pageRequest);

        if (pageRequest.isUnpaged()) {
            return unPagedQuery;
        }

        return unPagedQuery + " " + QueryUtils.paginatedQueryFromPageable(pageRequest);
    }

    private String getFilterQuery(final Table schema, final FilterExpression searchFilter) {
        return getFilterQuery(schema, searchFilter, emptyMap());
    }

    private String getFilterQuery(
            final Table schema,
            final FilterExpression searchFilter,
            final Map<String, String> drillbackColumnMap,
            final String pipelineStepFilter) {

        return getFilterQuery(schema, searchFilter, drillbackColumnMap) + AND + pipelineStepFilter;
    }

    private String getFilterQuery(
            final Table schema, final FilterExpression searchFilter, final Map<String, String> drillbackColumnMap) {

        Option<FilterExpression> drillbackRowsOnlyFilter =
                getDrillbackRowsOnlyFilterExpression(schema, drillbackColumnMap);

        Set<SchemaField> schemaFields = ImmutableSet.<SchemaField>builder()
                .addAll(getSchemaFields(schema))
                .add(SchemaField.builder()
                        .name(ROW_KEY.name())
                        .type(FieldTypes.LONG)
                        .build())
                .build();

        if (drillbackRowsOnlyFilter.isDefined() && searchFilter != null) {
            CombinedFilter filterExpression = CombinedFilter.and(drillbackRowsOnlyFilter.get(), searchFilter);
            return AND + filterToSQLConverter.apply(filterExpression, schemaFields);
        }

        if (drillbackRowsOnlyFilter.isDefined()) {
            return AND + filterToSQLConverter.apply(drillbackRowsOnlyFilter.get(), schemaFields);
        }

        if (searchFilter != null) {
            return AND + filterToSQLConverter.apply(searchFilter, schemaFields);
        }

        return "";
    }

    private Set<SchemaField> getSchemaFields(final Table schema) {
        return schema.getFields().stream()
                .map(schemaField -> SchemaField.builder()
                        .name(schemaField.getName())
                        .type(schemaField.getFieldType())
                        .build())
                .collect(toSet());
    }

    private Option<FilterExpression> getDrillbackRowsOnlyFilterExpression(
            final Table schema, final Map<String, String> drillbackColumnMap) {

        if (drillbackColumnMap.isEmpty()) {
            return Option.none();
        }

        List<Filter> drillbackFilters = new ArrayList<>();
        for (Map.Entry<String, String> drillbackColumn : drillbackColumnMap.entrySet()) {
            String drillbackColumnName = drillbackColumn.getKey();
            String drillbackColumnValue = drillbackColumn.getValue();

            Filter drillbackFilter = toDrillbackFilter(schema, drillbackColumnName, drillbackColumnValue);
            drillbackFilters.add(drillbackFilter);
        }

        return Option.of(CombinedFilter.and(drillbackFilters));
    }

    private Filter toDrillbackFilter(
            final Table schema, final String drillbackColumnName, final String drillbackValue) {

        Field drillbackField = getShowDrillbackColumnField(schema, drillbackColumnName);
        String drillbackFieldType = drillbackField.getFieldType();

        Filter.FilterBuilder drillbackFilter = Filter.builder()
                .columnName(drillbackField.getName())
                .type(FilterOption.EQUALS)
                .filter(drillbackValue);

        switch (drillbackFieldType) {
            case FieldTypes.DATE:
                drillbackFilter.filterType(FilterType.DATE);
                drillbackFilter.dateFrom(drillbackValue);
                drillbackFilter.dateFormat(FilterToSQLConverter.DEFAULT_DATE_FORMAT);
                break;
            case FieldTypes.TIMESTAMP:
                drillbackFilter.filterType(FilterType.DATE);
                drillbackFilter.dateFrom(drillbackValue);
                drillbackFilter.dateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                break;
            case FieldTypes.STRING:
                drillbackFilter.filterType(FilterType.TEXT);
                break;
            default:
                drillbackFilter.filterType(FilterType.NUMBER);
        }

        return drillbackFilter.build();
    }

    private Field getShowDrillbackColumnField(final Table schema, final String showDrillbackColumnName) {
        List<Field> schemaFields = schema.getFields().stream()
                .filter(field -> field.getName().equalsIgnoreCase(showDrillbackColumnName))
                .collect(Collectors.toList());
        if (schemaFields.size() >= 1) {
            return schemaFields.get(0);
        }
        if (showDrillbackColumnName.equalsIgnoreCase("ROW_KEY")) {
            return LongField.builder()
                    .name("ROW_KEY")
                    .build();
        }
        return StringField.builder()
                .name(showDrillbackColumnName)
                .build();
    }

    private String getOrderQuery(final Pageable pageRequest) {
        Sort sort = pageRequest.getSort();
        String orderQuery = " ORDER BY " + ROW_KEY.name();
        if (sort != null && sort.isSorted()) {
            orderQuery = " ORDER BY " + sort.stream()
                    .map(order -> "\"" + order.getProperty() + "\"" + " " + order.getDirection() + " NULLS LAST")
                    .collect(Collectors.joining(","));
        }
        return orderQuery;
    }
}
