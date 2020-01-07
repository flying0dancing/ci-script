package com.lombardrisk.ignis.server.product.rule.view;

import com.lombardrisk.ignis.api.rule.ValidationRuleSeverity;
import com.lombardrisk.ignis.api.rule.ValidationRuleType;
import com.lombardrisk.ignis.client.external.productconfig.export.ValidationRuleExport;
import com.lombardrisk.ignis.server.product.rule.model.ValidationRule;
import io.vavr.Function1;

public class ValidationRuleExportConverter implements Function1<ValidationRuleExport, ValidationRule> {

    private static final long serialVersionUID = -5685327955265730713L;

    @Override
    public ValidationRule apply(final ValidationRuleExport validationRuleExport) {
        return ValidationRule.builder()
                .name(validationRuleExport.getName())
                .ruleId(validationRuleExport.getRuleId())
                .version(validationRuleExport.getVersion())
                .description(validationRuleExport.getDescription())
                .startDate(validationRuleExport.getStartDate())
                .endDate(validationRuleExport.getEndDate())
                .expression(validationRuleExport.getExpression())
                .validationRuleType(ValidationRuleType.valueOf(validationRuleExport.getValidationRuleType().name()))
                .validationRuleSeverity(ValidationRuleSeverity.valueOf(validationRuleExport.getValidationRuleSeverity().name()))
                .build();
    }
}
