package com.lombardrisk.ignis.server.dataset.rule.view;

import com.lombardrisk.ignis.client.external.rule.ValidationRuleSummaryView;
import com.lombardrisk.ignis.server.dataset.model.Dataset;
import com.lombardrisk.ignis.server.dataset.rule.model.ValidationResultsSummary;
import com.lombardrisk.ignis.server.product.rule.view.ValidationRuleConverter;
import io.vavr.Function1;

public class ValidationResultsSummaryConverter
        implements Function1<ValidationResultsSummary, ValidationRuleSummaryView> {

    private static final long serialVersionUID = -360170990725340815L;

    private final ValidationRuleConverter validationRuleConverter;

    public ValidationResultsSummaryConverter(final ValidationRuleConverter validationRuleConverter) {
        this.validationRuleConverter = validationRuleConverter;
    }

    @Override
    public ValidationRuleSummaryView apply(final ValidationResultsSummary validationResultsSummary) {

        Dataset dataset = validationResultsSummary.getDataset();

        return ValidationRuleSummaryView.builder()
                .id(validationResultsSummary.getId())
                .validationRule(validationRuleConverter.apply(validationResultsSummary.getValidationRule()))
                .totalRecords(dataset.getRecordsCount())
                .numberOfFailures(validationResultsSummary.getNumberOfFailures())
                .numberOfErrors(validationResultsSummary.getNumberOfErrors())
                .datasetId(dataset.getId())
                .errorMessage(validationResultsSummary.getErrorMessage())
                .status(ValidationRuleSummaryView.SummaryStatus.valueOf(validationResultsSummary.getStatus().name()))
                .build();
    }
}
