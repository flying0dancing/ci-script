package com.lombardrisk.ignis.design.server.productconfig.export.converter;

import com.lombardrisk.ignis.client.external.productconfig.export.FieldExport;
import com.lombardrisk.ignis.client.external.productconfig.export.ValidationRuleExport;
import com.lombardrisk.ignis.common.function.IsoMorphicFunction1;
import com.lombardrisk.ignis.design.field.model.Field;
import com.lombardrisk.ignis.design.server.productconfig.rule.model.ValidationRule;
import com.lombardrisk.ignis.design.server.productconfig.rule.model.ValidationRuleSeverity;
import com.lombardrisk.ignis.design.server.productconfig.rule.model.ValidationRuleType;
import io.vavr.Function1;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.lombardrisk.ignis.common.MapperUtils.map;
import static com.lombardrisk.ignis.common.MapperUtils.mapCollectionOrEmpty;

public class ValidationRuleExportConverter implements IsoMorphicFunction1<ValidationRule, ValidationRuleExport> {

    private static final long serialVersionUID = 2974956714634945093L;

    private final FieldExportConverter fieldConverter;

    public ValidationRuleExportConverter(final FieldExportConverter fieldConverter) {
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

    @Override
    public Function1<ValidationRuleExport, ValidationRule> inverse() {
        return validationRuleExport -> ValidationRule.builder()
                .id(validationRuleExport.getId())
                .name(validationRuleExport.getName())
                .ruleId(validationRuleExport.getRuleId())
                .version(validationRuleExport.getVersion())
                .description(validationRuleExport.getDescription())

                .validationRuleType(
                        ValidationRuleType.valueOf(validationRuleExport.getValidationRuleType().name()))
                .validationRuleSeverity(
                        ValidationRuleSeverity.valueOf(validationRuleExport.getValidationRuleSeverity().name()))

                .startDate(validationRuleExport.getStartDate())
                .endDate(validationRuleExport.getEndDate())

                .expression(validationRuleExport.getExpression())
                .contextFields(mapCollectionOrEmpty(
                        validationRuleExport.getContextFields(), fieldConverter.inverse(), HashSet::new))

                .build();
    }

    private List<FieldExport> toContextFieldViews(final Set<Field> contextFields) {
        return map(contextFields, fieldConverter);
    }
}
