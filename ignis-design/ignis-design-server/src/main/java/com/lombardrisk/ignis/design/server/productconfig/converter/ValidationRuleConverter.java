package com.lombardrisk.ignis.design.server.productconfig.converter;

import com.lombardrisk.ignis.client.design.rule.RuleDto;
import com.lombardrisk.ignis.client.design.schema.field.FieldDto;
import com.lombardrisk.ignis.common.MapperUtils;
import com.lombardrisk.ignis.design.field.FieldConverter;
import com.lombardrisk.ignis.design.field.model.Field;
import com.lombardrisk.ignis.design.server.productconfig.rule.model.ValidationRule;
import com.lombardrisk.ignis.design.server.productconfig.rule.model.ValidationRuleSeverity;
import com.lombardrisk.ignis.design.server.productconfig.rule.model.ValidationRuleType;
import io.vavr.Function1;

import java.util.List;
import java.util.Set;

public class ValidationRuleConverter implements Function1<ValidationRule, RuleDto> {

    private static final long serialVersionUID = 2974956714634945093L;

    private final FieldConverter fieldConverter;

    public ValidationRuleConverter(final FieldConverter fieldConverter) {
        this.fieldConverter = fieldConverter;
    }

    @Override
    public RuleDto apply(final ValidationRule validationRule) {
        ValidationRuleType validationRuleType = validationRule.getValidationRuleType();
        ValidationRuleSeverity validationRuleSeverity = validationRule.getValidationRuleSeverity();

        return RuleDto.builder()
                .id(validationRule.getId())
                .name(validationRule.getName())
                .ruleId(validationRule.getRuleId())
                .version(validationRule.getVersion())
                .description(validationRule.getDescription())

                .validationRuleType(RuleDto.Type.valueOf(validationRuleType.name()))
                .validationRuleSeverity(RuleDto.Severity.valueOf(validationRuleSeverity.name()))

                .startDate(validationRule.getStartDate())
                .endDate(validationRule.getEndDate())

                .expression(validationRule.getExpression())
                .contextFields(toContextFieldViews(validationRule.getContextFields()))
                .build();
    }

    private List<FieldDto> toContextFieldViews(final Set<Field> contextFields) {
        return MapperUtils.map(contextFields, fieldConverter);
    }
}
