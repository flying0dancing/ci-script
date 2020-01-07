package com.lombardrisk.ignis.server.dataset.rule.detail;

import com.google.common.collect.ImmutableList;
import com.lombardrisk.ignis.server.dataset.model.Dataset;
import com.lombardrisk.ignis.server.dataset.phoenix.QueryUtils;
import com.lombardrisk.ignis.server.product.rule.model.ValidationRule;
import com.lombardrisk.ignis.server.product.table.model.Field;
import com.lombardrisk.ignis.server.product.table.model.StringField;
import io.vavr.control.Validation;
import lombok.Getter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.stream.Collectors;

import static com.lombardrisk.ignis.api.rule.ValidationOutput.DATASET_ID;
import static com.lombardrisk.ignis.api.rule.ValidationOutput.DATASET_ROW_KEY;
import static com.lombardrisk.ignis.api.rule.ValidationOutput.VALIDATION_RESULT_TYPE;
import static com.lombardrisk.ignis.api.rule.ValidationOutput.VALIDATION_RULE_ID;
import static com.lombardrisk.ignis.api.rule.ValidationOutput.VALIDATION_RULE_RESULTS;
import static com.lombardrisk.ignis.pipeline.step.api.DatasetFieldName.ROW_KEY;
import static java.util.stream.Collectors.toList;

public final class ValidationDetailQuery {

    private final Long datasetId;
    private final String datasetTableName;
    private final ValidationRule validationRule;
    @Getter
    private final long recordsCount;
    @Getter
    private final Pageable pageRequest;
    @Getter
    private final List<Field> validationDetailsColumns;

    private ValidationDetailQuery(
            final Dataset dataset,
            final long recordsCount,
            final ValidationRule validationRule,
            final Pageable pageRequest) {
        this.datasetId = dataset.getId();
        this.recordsCount = recordsCount;
        this.datasetTableName = dataset.getName();
        this.pageRequest = pageRequest;
        this.validationRule = validationRule;
        this.validationDetailsColumns = validationDetailsColumns(dataset);
    }

    public static ValidationDetailQuery allRules(final Dataset dataset, final Pageable pageRequest) {
        int numberOfValidationRules = dataset.getSchema()
                .getValidationRules()
                .size();

        long recordsCount = dataset.getRecordsCount() * numberOfValidationRules;
        return new ValidationDetailQuery(dataset, recordsCount, null, pageRequest);
    }

    public static ValidationDetailQuery byRule(
            final Dataset dataset, final ValidationRule validationRule, final Pageable pageRequest) {
        return new ValidationDetailQuery(dataset, dataset.getRecordsCount(), validationRule, pageRequest);
    }

    /**
     * Validates created query.
     *
     * Avoids SQL Injection by checking user specified sort params are in the set of fields on the schema.
     *
     * @return Either a list of invalid parameters or the page of results.
     */
    public Validation<List<String>, ValidationDetailQuery> validate() {
        List<String> invalidParameters = validatePageRequest(validationDetailsColumns, pageRequest);
        if (!invalidParameters.isEmpty()) {
            return Validation.invalid(invalidParameters);
        }

        return Validation.valid(this);
    }

    public String getSqlQuery() {
        String selectSql = joinSql(selectColumns(validationDetailsColumns));

        return selectSql + sortedQuery() + paginatedQuery();
    }

    public static List<Field> validationDetailsColumns(final Dataset dataset) {
        StringField stringField = new StringField();
        stringField.setName(VALIDATION_RESULT_TYPE);

        return ImmutableList.<Field>builder()
                .addAll(dataset.getSchema().getFields())
                .add(stringField)
                .build();
    }

    private String joinSql(final String select) {
        String selectWithoutRuleId = "SELECT " + select
                + " FROM " + datasetTableName + " dataset"
                + " JOIN " + VALIDATION_RULE_RESULTS + " v"
                + " ON dataset." + ROW_KEY.name() + " = v." + DATASET_ROW_KEY
                + " WHERE v." + DATASET_ID + " = " + datasetId;

        if (validationRule == null) {
            return selectWithoutRuleId;
        }

        return selectWithoutRuleId
                + " AND v." + VALIDATION_RULE_ID + " = " + validationRule.getId();
    }

    private String selectColumns(final List<Field> columns) {
        return columns.stream()
                .map(field -> "\"" + field.getName() + "\"")
                .collect(Collectors.joining(","));
    }

    private String sortedQuery() {
        Sort sort = pageRequest.getSort();
        String orderSql = sort.stream()
                .map(this::toSql)
                .collect(Collectors.joining(","));

        return sort.isSorted()
                ? " ORDER BY " + orderSql
                : "";
    }

    private String paginatedQuery() {
        return QueryUtils.paginatedQueryFromPageable(pageRequest);
    }

    private String toSql(final Sort.Order order) {
        return "\"" + order.getProperty() + "\" " + order.getDirection().toString();
    }

    private static List<String> validatePageRequest(
            final List<Field> validationDetailsColumns, final Pageable pageRequest) {
        List<String> sortableColumns = validationDetailsColumns.stream()
                .map(Field::getName)
                .collect(toList());

        return pageRequest.getSort().stream()
                .map(Sort.Order::getProperty)
                .filter(property -> !sortableColumns.contains(property))
                .collect(Collectors.toList());
    }
}
