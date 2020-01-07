package com.lombardrisk.ignis.server.product.rule.view;

import com.lombardrisk.ignis.api.rule.ValidationRuleSeverity;
import com.lombardrisk.ignis.api.rule.ValidationRuleType;
import com.lombardrisk.ignis.client.external.productconfig.export.ValidationRuleExport;
import com.lombardrisk.ignis.client.external.productconfig.view.TestResult;
import com.lombardrisk.ignis.client.external.productconfig.view.ValidationRuleExampleFieldView;
import com.lombardrisk.ignis.client.external.productconfig.view.ValidationRuleExampleView;
import com.lombardrisk.ignis.client.external.productconfig.view.ValidationRuleView;
import com.lombardrisk.ignis.server.product.rule.model.ValidationRule;
import com.lombardrisk.ignis.server.product.rule.model.ValidationRuleExample;
import com.lombardrisk.ignis.server.product.rule.model.ValidationRuleExampleField;
import com.lombardrisk.ignis.server.product.table.view.FieldToFieldView;

import java.util.function.Function;
import java.util.stream.Collectors;

public class ValidationRuleToValidationRuleView implements Function<ValidationRule, ValidationRuleView> {

    private final FieldToFieldView fieldToFieldView = new FieldToFieldView();

    @Override
    public ValidationRuleView apply(final ValidationRule validationRule) {
        ValidationRuleType validationRuleType = validationRule.getValidationRuleType();
        ValidationRuleSeverity validationRuleSeverity = validationRule.getValidationRuleSeverity();

        return ValidationRuleView.builder()
                .id(validationRule.getId())
                .name(validationRule.getName())
                .ruleId(validationRule.getRuleId())
                .version(validationRule.getVersion())
                .description(validationRule.getDescription())

                .validationRuleType(ValidationRuleExport.Type.valueOf(validationRuleType.name()))
                .validationRuleSeverity(ValidationRuleExport.Severity.valueOf(validationRuleSeverity.name()))

                .startDate(validationRule.getStartDate())
                .endDate(validationRule.getEndDate())

                .expression(validationRule.getExpression())

                .contextFields(validationRule.getContextFields().stream()
                        .map(contextField -> fieldToFieldView.apply(contextField))
                        .collect(Collectors.toSet()))

                .validationRuleExamples(validationRule.getValidationRuleExamples().stream()
                        .map(validationRuleExample ->
                            toValidationRuleExampleView(validationRuleExample))
                        .collect(Collectors.toSet()))

                .build();
    }

    private ValidationRuleExampleView toValidationRuleExampleView(final ValidationRuleExample validationRuleExample) {
        return ValidationRuleExampleView.builder()
                .id(validationRuleExample.getId())
                .expectedResult(toTestResult(validationRuleExample.getExpectedResult()))
                .validationRuleExampleFields(validationRuleExample.getValidationRuleExampleFields().stream()
                        .map(validationRuleExampleField -> toValidationRuleExampleFieldView(validationRuleExampleField))
                        .collect(Collectors.toSet()))
                .build();
    }

    private ValidationRuleExampleFieldView toValidationRuleExampleFieldView(
            final ValidationRuleExampleField validationRuleExampleField) {
        return ValidationRuleExampleFieldView.builder()
                .id(validationRuleExampleField.getId())
                .name(validationRuleExampleField.getName())
                .value(validationRuleExampleField.getValue())
                .build();
    }

    private TestResult toTestResult(com.lombardrisk.ignis.api.rule.TestResult testResult) {
        switch (testResult) {
            case PASS:
                return TestResult.PASS;

            case FAIL:
                return TestResult.FAIL;

            case ERROR:
                return TestResult.ERROR;
        }
        return null;
    }
}
