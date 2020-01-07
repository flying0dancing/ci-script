package com.lombardrisk.ignis.design.server.productconfig.converter;

import com.lombardrisk.ignis.client.design.RuleExample;
import com.lombardrisk.ignis.client.design.ValidationRuleRequest;
import com.lombardrisk.ignis.design.server.productconfig.rule.model.ValidationRule;
import com.lombardrisk.ignis.design.server.productconfig.rule.model.ValidationRuleExample;
import com.lombardrisk.ignis.design.server.productconfig.rule.model.ValidationRuleExampleField;
import com.lombardrisk.ignis.design.server.productconfig.rule.model.ValidationRuleSeverity;
import com.lombardrisk.ignis.design.server.productconfig.rule.model.ValidationRuleType;
import io.vavr.Function1;

import java.util.Set;

import static com.lombardrisk.ignis.design.server.productconfig.rule.model.TestResult.toTestResult;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

public class ValidationRuleRequestConverter implements Function1<ValidationRuleRequest, ValidationRule> {

    private static final long serialVersionUID = 1448714105223900377L;

    @Override
    public ValidationRule apply(final ValidationRuleRequest ruleRequest) {
        return ValidationRule.builder()
                .id(ruleRequest.getId())
                .ruleId(ruleRequest.getRuleId())
                .validationRuleType(ValidationRuleType.valueOf(ruleRequest.getValidationRuleType().name()))
                .validationRuleSeverity(ValidationRuleSeverity.valueOf(ruleRequest.getValidationRuleSeverity().name()))
                .version(ruleRequest.getVersion())
                .startDate(ruleRequest.getStartDate())
                .endDate(ruleRequest.getEndDate())
                .name(ruleRequest.getName())
                .description(ruleRequest.getDescription())
                .expression(ruleRequest.getExpression())
                .validationRuleExamples(toValidationRuleExamples(ruleRequest.getRuleExamples()))
                .build();
    }

    private static Set<ValidationRuleExample> toValidationRuleExamples(final Set<RuleExample> ruleExamples) {
        if (isNotEmpty(ruleExamples)) {
            return ruleExamples.stream()
                    .map(ruleExample -> ValidationRuleExample.builder()
                            .id(ruleExample.id)
                            .expectedResult(toTestResult(ruleExample.expectedResult.name()))
                            .validationRuleExampleFields(toRuleExampleFields(ruleExample))
                            .build())
                    .collect(toSet());
        } else {
            return emptySet();
        }
    }

    private static Set<ValidationRuleExampleField> toRuleExampleFields(final RuleExample ruleExample) {
        return ruleExample.exampleFields.entrySet()
                .stream()
                .map(entry -> ValidationRuleExampleField.builder()
                        .id(entry.getValue().id)
                        .name(entry.getKey())
                        .value(entry.getValue().value)
                        .build())
                .collect(toSet());
    }
}
