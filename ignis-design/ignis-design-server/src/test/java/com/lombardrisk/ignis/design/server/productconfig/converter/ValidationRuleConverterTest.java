package com.lombardrisk.ignis.design.server.productconfig.converter;

import com.google.common.collect.ImmutableSet;
import com.lombardrisk.ignis.client.design.rule.RuleDto;
import com.lombardrisk.ignis.client.design.schema.field.FieldDto;
import com.lombardrisk.ignis.design.field.FieldConverter;
import com.lombardrisk.ignis.design.server.productconfig.rule.model.ValidationRule;
import com.lombardrisk.ignis.design.server.productconfig.rule.model.ValidationRuleSeverity;
import com.lombardrisk.ignis.design.server.productconfig.rule.model.ValidationRuleType;
import org.junit.Test;

import java.time.LocalDate;

import static com.lombardrisk.ignis.design.field.DesignField.Populated.decimalField;
import static com.lombardrisk.ignis.design.field.DesignField.Populated.stringField;
import static com.lombardrisk.ignis.design.server.fixtures.Design.Populated;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

public class ValidationRuleConverterTest {

    private final ValidationRuleConverter validationRuleConverter = new ValidationRuleConverter(new FieldConverter());

    @Test
    public void apply_SetsId() {
        ValidationRule validationRule = Populated.validationRule()
                .id(837L)
                .build();
        RuleDto ruleView = validationRuleConverter.apply(validationRule);

        assertThat(ruleView.getId())
                .isEqualTo(837L);
    }

    @Test
    public void apply_SetsName() {
        ValidationRule validationRule = Populated.validationRule()
                .name("Rule123")
                .build();
        RuleDto ruleView = validationRuleConverter.apply(validationRule);

        assertThat(ruleView.getName())
                .isEqualTo("Rule123");
    }

    @Test
    public void apply_SetsDescription() {
        ValidationRule validationRule = Populated.validationRule()
                .description("RuleDescription")
                .build();
        RuleDto ruleView = validationRuleConverter.apply(validationRule);

        assertThat(ruleView.getDescription())
                .isEqualTo("RuleDescription");
    }

    @Test
    public void apply_SetsRuleId() {
        ValidationRule validationRule = Populated.validationRule()
                .ruleId("RuleId")
                .build();
        RuleDto ruleView = validationRuleConverter.apply(validationRule);

        assertThat(ruleView.getRuleId())
                .isEqualTo("RuleId");
    }

    @Test
    public void apply_SetsVersion() {
        ValidationRule validationRule = Populated.validationRule()
                .version(900)
                .build();
        RuleDto ruleView = validationRuleConverter.apply(validationRule);

        assertThat(ruleView.getVersion())
                .isEqualTo(900);
    }

    @Test
    public void apply_SetsExpression() {
        ValidationRule validationRule = Populated.validationRule()
                .expression("expression")
                .build();
        RuleDto ruleView = validationRuleConverter.apply(validationRule);

        assertThat(ruleView.getExpression())
                .isEqualTo("expression");
    }

    @Test
    public void apply_Warning_SetsSeverity() {
        ValidationRule validationRule = Populated.validationRule()
                .validationRuleSeverity(ValidationRuleSeverity.WARNING)
                .build();
        RuleDto ruleView = validationRuleConverter.apply(validationRule);

        assertThat(ruleView.getValidationRuleSeverity())
                .isEqualTo(RuleDto.Severity.WARNING);
    }

    @Test
    public void apply_Critical_SetsSeverity() {
        ValidationRule validationRule = Populated.validationRule()
                .validationRuleSeverity(ValidationRuleSeverity.CRITICAL)
                .build();
        RuleDto ruleView = validationRuleConverter.apply(validationRule);

        assertThat(ruleView.getValidationRuleSeverity())
                .isEqualTo(RuleDto.Severity.CRITICAL);
    }

    @Test
    public void enums_AllTypesMapped() {
        for (ValidationRuleType validationRuleType : ValidationRuleType.values()) {
            RuleDto.Type type = RuleDto.Type.valueOf(validationRuleType.name());
            assertThat(type.name()).isEqualTo(validationRuleType.name());
        }
    }

    @Test
    public void enums_AllSeveritiesMapped() {
        for (ValidationRuleSeverity validationRuleSeverity : ValidationRuleSeverity.values()) {
            RuleDto.Severity severity = RuleDto.Severity.valueOf(validationRuleSeverity.name());
            assertThat(severity.name()).isEqualTo(validationRuleSeverity.name());
        }
    }

    @Test
    public void apply_Syntax_SetsType() {
        ValidationRule validationRule = Populated.validationRule()
                .validationRuleType(ValidationRuleType.SYNTAX)
                .build();
        RuleDto ruleView = validationRuleConverter.apply(validationRule);

        assertThat(ruleView.getValidationRuleType())
                .isEqualTo(RuleDto.Type.SYNTAX);
    }

    @Test
    public void apply_Quality_SetsType() {
        ValidationRule validationRule = Populated.validationRule()
                .validationRuleType(ValidationRuleType.QUALITY)
                .build();
        RuleDto ruleView = validationRuleConverter.apply(validationRule);

        assertThat(ruleView.getValidationRuleType())
                .isEqualTo(RuleDto.Type.QUALITY);
    }

    @Test
    public void apply_Validity_SetsType() {
        ValidationRule validationRule = Populated.validationRule()
                .validationRuleType(ValidationRuleType.VALIDITY)
                .build();
        RuleDto ruleView = validationRuleConverter.apply(validationRule);

        assertThat(ruleView.getValidationRuleType())
                .isEqualTo(RuleDto.Type.VALIDITY);
    }

    @Test
    public void apply_IntraSeries_SetsType() {
        ValidationRule validationRule = Populated.validationRule()
                .validationRuleType(ValidationRuleType.INTRA_SERIES)
                .build();
        RuleDto ruleView = validationRuleConverter.apply(validationRule);

        assertThat(ruleView.getValidationRuleType())
                .isEqualTo(RuleDto.Type.INTRA_SERIES);
    }

    @Test
    public void apply_SetsStartDate() {
        ValidationRule validationRule = Populated.validationRule()
                .startDate(LocalDate.of(2000, 1, 1))
                .build();
        RuleDto ruleView = validationRuleConverter.apply(validationRule);

        assertThat(ruleView.getStartDate())
                .isEqualTo(LocalDate.of(2000, 1, 1));
    }

    @Test
    public void apply_SetsEndDate() {
        ValidationRule validationRule = Populated.validationRule()
                .endDate(LocalDate.of(2005, 1, 1))
                .build();

        RuleDto ruleView = validationRuleConverter.apply(validationRule);

        assertThat(ruleView.getEndDate())
                .isEqualTo(LocalDate.of(2005, 1, 1));
    }

    @Test
    public void apply_SetsContextFields() {
        ValidationRule validationRule = Populated.validationRule()
                .contextFields(ImmutableSet.of(
                        decimalField("FF_XV").build(),
                        stringField("FF_XIII").build()))
                .build();

        RuleDto ruleView = validationRuleConverter.apply(validationRule);

        assertThat(ruleView.getContextFields())
                .extracting(Object::getClass, FieldDto::getName)
                .containsSequence(
                        tuple(FieldDto.DecimalFieldDto.class, "FF_XV"),
                        tuple(FieldDto.StringFieldDto.class, "FF_XIII"));
    }
}
