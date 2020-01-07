package com.lombardrisk.ignis.server.product.rule.view;

import com.lombardrisk.ignis.api.rule.ValidationRuleSeverity;
import com.lombardrisk.ignis.api.rule.ValidationRuleType;
import com.lombardrisk.ignis.client.external.productconfig.export.FieldExport;
import com.lombardrisk.ignis.client.external.productconfig.export.ValidationRuleExport;
import com.lombardrisk.ignis.common.MapperUtils;
import com.lombardrisk.ignis.server.product.rule.model.ValidationRule;
import com.lombardrisk.ignis.server.product.table.model.Field;
import com.lombardrisk.ignis.server.product.table.view.FieldConverter;
import io.vavr.Function1;

import java.util.List;
import java.util.Set;

public class ValidationRuleConverter implements Function1<ValidationRule, ValidationRuleExport> {

    private static final long serialVersionUID = 2974956714634945093L;

    private final FieldConverter fieldConverter;

    public ValidationRuleConverter(final FieldConverter fieldConverter) {
        this.fieldConverter = fieldConverter;
    }

    @Override
    public ValidationRuleExport apply(final ValidationRule validationRule) {
        ValidationRuleType validationRuleType = validationRule.getValidationRuleType();
        ValidationRuleSeverity validationRuleSeverity = validationRule.getValidationRuleSeverity();

        return ValidationRuleExport.builder()
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
                .contextFields(toContextFieldViews(validationRule.getContextFields()))
                .build();
    }

    private List<FieldExport> toContextFieldViews(final Set<Field> contextFields) {
        return MapperUtils.map(contextFields, fieldConverter);
    }
}
