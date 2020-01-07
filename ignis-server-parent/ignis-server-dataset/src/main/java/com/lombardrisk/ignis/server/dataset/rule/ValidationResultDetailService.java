package com.lombardrisk.ignis.server.dataset.rule;

import com.lombardrisk.ignis.client.external.productconfig.export.FieldExport;
import com.lombardrisk.ignis.client.external.rule.ValidationResultsDetailView;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.data.common.failure.InvalidRequestParamsFailure;
import com.lombardrisk.ignis.server.dataset.DatasetService;
import com.lombardrisk.ignis.server.dataset.model.Dataset;
import com.lombardrisk.ignis.server.dataset.rule.detail.ValidationDetailQuery;
import com.lombardrisk.ignis.server.dataset.rule.detail.ValidationDetailRepository;
import com.lombardrisk.ignis.server.dataset.rule.detail.ValidationDetailRow;
import com.lombardrisk.ignis.server.product.rule.ValidationRuleService;
import com.lombardrisk.ignis.server.product.rule.model.ValidationRule;
import com.lombardrisk.ignis.server.product.table.model.Field;
import com.lombardrisk.ignis.server.product.table.view.FieldConverter;
import com.lombardrisk.ignis.server.product.util.PageConverter;
import io.vavr.Tuple;
import io.vavr.collection.Seq;
import io.vavr.control.Option;
import io.vavr.control.Validation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

public class ValidationResultDetailService {

    private final PageConverter pageConverter;
    private final FieldConverter fieldConverter;
    private final DatasetService datasetService;
    private final ValidationRuleService validationRuleService;
    private final ValidationDetailRepository validationDetailRepository;

    public ValidationResultDetailService(
            final PageConverter pageConverter,
            final FieldConverter fieldConverter,
            final DatasetService datasetService,
            final ValidationRuleService validationRuleService,
            final ValidationDetailRepository validationDetailRepository) {
        this.pageConverter = pageConverter;
        this.fieldConverter = fieldConverter;
        this.datasetService = datasetService;
        this.validationRuleService = validationRuleService;
        this.validationDetailRepository = validationDetailRepository;
    }

    public Validation<List<CRUDFailure>, ValidationResultsDetailView> findValidationDetails(
            final long datasetId, final long ruleId, final Pageable pageable) {

        Validation<CRUDFailure, Dataset> datasetValidation = datasetService.findWithValidation(datasetId);
        Validation<CRUDFailure, ValidationRule> ruleValidation = validationRuleService.findWithValidation(ruleId);
        Validation<CRUDFailure, Pageable> pageValidation = validatePage(pageable).toInvalid(pageable);

        Validation<List<CRUDFailure>, ValidationDetailQuery> validatedParameters = datasetValidation
                .combine(ruleValidation)
                .combine(pageValidation)
                .ap(ValidationDetailQuery::byRule)
                .mapError(Seq::asJava);

        if (validatedParameters.isInvalid()) {
            return Validation.invalid(validatedParameters.getError());
        }

        return findDetailsWithQuery(validatedParameters.get());
    }

    @Transactional
    public Validation<List<CRUDFailure>, ValidationResultsDetailView> findValidationDetails(
            final long datasetId,
            final Pageable pageable) {

        Validation<CRUDFailure, Dataset> datasetValidation = datasetService.findWithValidation(datasetId);
        Validation<CRUDFailure, Pageable> pageValidation = validatePage(pageable).toInvalid(pageable);

        Validation<List<CRUDFailure>, ValidationDetailQuery> validatedParameters = datasetValidation
                .combine(pageValidation)
                .ap(ValidationDetailQuery::allRules)
                .mapError(Seq::asJava);

        if (validatedParameters.isInvalid()) {
            return Validation.invalid(validatedParameters.getError());
        }

        return findDetailsWithQuery(validatedParameters.get());
    }

    private Validation<List<CRUDFailure>, ValidationResultsDetailView> findDetailsWithQuery(
            final ValidationDetailQuery validationDetailQuery) {

        List<Field> validationDetailsColumns = validationDetailQuery.getValidationDetailsColumns();

        return validationDetailQuery.validate()
                .bimap(
                        this::handleValidationFailure,
                        validationDetailRepository::findValidationDetails)
                .map(rows -> convert(validationDetailsColumns, rows));
    }

    private Option<CRUDFailure> validatePage(final Pageable pageable) {
        InvalidRequestParamsFailure<String> page =
                CRUDFailure.invalidRequestParameters(Arrays.asList(
                        Tuple.of("page", "null"),
                        Tuple.of("size", "null")
                ));

        return Option.when(pageable.isUnpaged(), page);
    }

    private List<CRUDFailure> handleValidationFailure(final List<String> invalidParameters) {

        InvalidRequestParamsFailure<String> failure = CRUDFailure.invalidRequestParameters(
                invalidParameters.stream()
                        .map(param -> Tuple.of("sortParameter", param))
                        .collect(toList()));

        return Collections.singletonList(failure);
    }

    private ValidationResultsDetailView convert(
            final List<Field> fields,
            final Page<ValidationDetailRow> validationDetailRows) {

        List<FieldExport> schemaViews = fields.stream()
                .map(fieldConverter)
                .collect(toList());

        List<Map<String, Object>> rowViews = validationDetailRows.getContent()
                .stream()
                .map(ValidationDetailRow::getValues)
                .collect(toList());

        return new ValidationResultsDetailView(schemaViews, rowViews, pageConverter.apply(validationDetailRows));
    }
}
