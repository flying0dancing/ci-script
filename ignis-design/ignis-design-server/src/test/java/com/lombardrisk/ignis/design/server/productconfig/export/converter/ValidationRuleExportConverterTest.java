package com.lombardrisk.ignis.design.server.productconfig.export.converter;

import com.google.common.collect.ImmutableSet;
import com.lombardrisk.ignis.client.external.productconfig.export.FieldExport;
import com.lombardrisk.ignis.client.external.productconfig.export.FieldExport.DecimalFieldExport;
import com.lombardrisk.ignis.client.external.productconfig.export.FieldExport.StringFieldExport;
import com.lombardrisk.ignis.client.external.productconfig.export.ValidationRuleExport;
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

public class ValidationRuleExportConverterTest {

    private final ValidationRuleExportConverter validationRuleConverter =
            new ValidationRuleExportConverter(new FieldExportConverter());

    @Test
    public void apply_SetsId() {
        ValidationRule validationRule = Populated.validationRule()
                .id(837L)
                .build();
        ValidationRuleExport ruleView = validationRuleConverter.apply(validationRule);

        assertThat(ruleView.getId())
                .isEqualTo(837L);
    }

    @Test
    public void apply_SetsName() {
        ValidationRule validationRule = Populated.validationRule()
                .name("Rule123")
                .build();
        ValidationRuleExport ruleView = validationRuleConverter.apply(validationRule);

        assertThat(ruleView.getName())
                .isEqualTo("Rule123");
    }

    @Test
    public void apply_SetsDescription() {
        ValidationRule validationRule = Populated.validationRule()
                .description("RuleDescription")
                .build();
        ValidationRuleExport ruleView = validationRuleConverter.apply(validationRule);

        assertThat(ruleView.getDescription())
                .isEqualTo("RuleDescription");
    }

    @Test
    public void apply_SetsRuleId() {
        ValidationRule validationRule = Populated.validationRule()
                .ruleId("RuleId")
                .build();
        ValidationRuleExport ruleView = validationRuleConverter.apply(validationRule);

        assertThat(ruleView.getRuleId())
                .isEqualTo("RuleId");
    }

    @Test
    public void apply_SetsVersion() {
        ValidationRule validationRule = Populated.validationRule()
                .version(900)
                .build();
        ValidationRuleExport ruleView = validationRuleConverter.apply(validationRule);

        assertThat(ruleView.getVersion())
                .isEqualTo(900);
    }

    @Test
    public void apply_SetsExpression() {
        ValidationRule validationRule = Populated.validationRule()
                .expression("expression")
                .build();
        ValidationRuleExport ruleView = validationRuleConverter.apply(validationRule);

        assertThat(ruleView.getExpression())
                .isEqualTo("expression");
    }

    @Test
    public void apply_Warning_SetsSeverity() {
        ValidationRule validationRule = Populated.validationRule()
                .validationRuleSeverity(ValidationRuleSeverity.WARNING)
                .build();
        ValidationRuleExport ruleView = validationRuleConverter.apply(validationRule);

        assertThat(ruleView.getValidationRuleSeverity())
                .isEqualTo(ValidationRuleExport.Severity.WARNING);
    }

    @Test
    public void apply_Critical_SetsSeverity() {
        ValidationRule validationRule = Populated.validationRule()
                .validationRuleSeverity(ValidationRuleSeverity.CRITICAL)
                .build();
        ValidationRuleExport ruleView = validationRuleConverter.apply(validationRule);

        assertThat(ruleView.getValidationRuleSeverity())
                .isEqualTo(ValidationRuleExport.Severity.CRITICAL);
    }

    @Test
    public void enums_AllTypesMapped() {
        for (ValidationRuleType validationRuleType : ValidationRuleType.values()) {
            ValidationRuleExport.Type type = ValidationRuleExport.Type.valueOf(validationRuleType.name());
            assertThat(type.name()).isEqualTo(validationRuleType.name());
        }
    }

    @Test
    public void enums_AllSeveritiesMapped() {
        for (ValidationRuleSeverity validationRuleSeverity : ValidationRuleSeverity.values()) {
            ValidationRuleExport.Severity severity =
                    ValidationRuleExport.Severity.valueOf(validationRuleSeverity.name());
            assertThat(severity.name()).isEqualTo(validationRuleSeverity.name());
        }
    }

    @Test
    public void apply_Syntax_SetsType() {
        ValidationRule validationRule = Populated.validationRule()
                .validationRuleType(ValidationRuleType.SYNTAX)
                .build();
        ValidationRuleExport ruleView = validationRuleConverter.apply(validationRule);

        assertThat(ruleView.getValidationRuleType())
                .isEqualTo(ValidationRuleExport.Type.SYNTAX);
    }

    @Test
    public void apply_Quality_SetsType() {
        ValidationRule validationRule = Populated.validationRule()
                .validationRuleType(ValidationRuleType.QUALITY)
                .build();
        ValidationRuleExport ruleView = validationRuleConverter.apply(validationRule);

        assertThat(ruleView.getValidationRuleType())
                .isEqualTo(ValidationRuleExport.Type.QUALITY);
    }

    @Test
    public void apply_Validity_SetsType() {
        ValidationRule validationRule = Populated.validationRule()
                .validationRuleType(ValidationRuleType.VALIDITY)
                .build();
        ValidationRuleExport ruleView = validationRuleConverter.apply(validationRule);

        assertThat(ruleView.getValidationRuleType())
                .isEqualTo(ValidationRuleExport.Type.VALIDITY);
    }

    @Test
    public void apply_IntraSeries_SetsType() {
        ValidationRule validationRule = Populated.validationRule()
                .validationRuleType(ValidationRuleType.INTRA_SERIES)
                .build();
        ValidationRuleExport ruleView = validationRuleConverter.apply(validationRule);

        assertThat(ruleView.getValidationRuleType())
                .isEqualTo(ValidationRuleExport.Type.INTRA_SERIES);
    }

    @Test
    public void apply_SetsStartDate() {
        ValidationRule validationRule = Populated.validationRule()
                .startDate(LocalDate.of(2000, 1, 1))
                .build();
        ValidationRuleExport ruleView = validationRuleConverter.apply(validationRule);

        assertThat(ruleView.getStartDate())
                .isEqualTo(LocalDate.of(2000, 1, 1));
    }

    @Test
    public void apply_SetsEndDate() {
        ValidationRule validationRule = Populated.validationRule()
                .endDate(LocalDate.of(2005, 1, 1))
                .build();

        ValidationRuleExport ruleView = validationRuleConverter.apply(validationRule);

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

        ValidationRuleExport ruleView = validationRuleConverter.apply(validationRule);

        assertThat(ruleView.getContextFields())
                .extracting(Object::getClass, FieldExport::getName)
                .containsSequence(
                        tuple(DecimalFieldExport.class, "FF_XV"),
                        tuple(StringFieldExport.class, "FF_XIII"));
    }

    @Test
    public void inverse_ConvertsBackToOriginal() {
        ValidationRule original = Populated.validationRule().build();

        ValidationRuleExport export = validationRuleConverter.apply(original);
        ValidationRule reConverted = validationRuleConverter.inverse().apply(export);

        assertThat(reConverted)
                .isEqualToIgnoringGivenFields(original, "id", "validationRuleExamples");
    }
}
