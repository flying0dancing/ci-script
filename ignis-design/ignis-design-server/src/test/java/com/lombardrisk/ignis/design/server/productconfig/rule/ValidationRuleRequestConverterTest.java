package com.lombardrisk.ignis.design.server.productconfig.rule;

import com.google.common.collect.ImmutableMap;
import com.lombardrisk.ignis.client.design.RuleExample;
import com.lombardrisk.ignis.client.design.ValidationRuleRequest;
import com.lombardrisk.ignis.client.design.ValidationRuleSeverity;
import com.lombardrisk.ignis.client.design.ValidationRuleType;
import com.lombardrisk.ignis.client.design.fixtures.Populated;
import com.lombardrisk.ignis.client.external.rule.ExampleField;
import com.lombardrisk.ignis.client.external.rule.TestResultType;
import com.lombardrisk.ignis.design.server.productconfig.converter.ValidationRuleRequestConverter;
import com.lombardrisk.ignis.design.server.productconfig.rule.model.TestResult;
import com.lombardrisk.ignis.design.server.productconfig.rule.model.ValidationRule;
import com.lombardrisk.ignis.design.server.productconfig.rule.model.ValidationRuleExample;
import com.lombardrisk.ignis.design.server.productconfig.rule.model.ValidationRuleExampleField;
import org.junit.Test;

import java.time.LocalDate;

import static com.google.common.collect.Sets.newHashSet;
import static com.lombardrisk.ignis.design.server.productconfig.rule.model.ValidationRuleSeverity.CRITICAL;
import static com.lombardrisk.ignis.design.server.productconfig.rule.model.ValidationRuleType.QUALITY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

public class ValidationRuleRequestConverterTest {

    private final ValidationRuleRequestConverter converter = new ValidationRuleRequestConverter();

    @Test
    public void apply_WithId_ReturnsValidationRuleWithId() {
        ValidationRuleRequest ruleRequest = Populated.validationRuleRequest()
                .id(233L)
                .build();

        ValidationRule rule = converter.apply(ruleRequest);

        assertThat(rule.getId()).isEqualTo(233L);
    }

    @Test
    public void apply_WithRuleId_ReturnsValidationRuleWithRuleId() {
        ValidationRuleRequest ruleRequest = Populated.validationRuleRequest()
                .ruleId("my rule id")
                .build();

        ValidationRule rule = converter.apply(ruleRequest);

        assertThat(rule.getRuleId()).isEqualTo("my rule id");
    }

    @Test
    public void apply_WithExpression_ReturnsValidationRuleWithExpression() {
        ValidationRuleRequest ruleRequest = Populated.validationRuleRequest()
                .expression("my expression")
                .build();

        ValidationRule rule = converter.apply(ruleRequest);

        assertThat(rule.getExpression()).isEqualTo("my expression");
    }

    @Test
    public void apply_WithName_ReturnsValidationRuleWithName() {
        ValidationRuleRequest ruleRequest = Populated.validationRuleRequest()
                .name("my name")
                .build();

        ValidationRule rule = converter.apply(ruleRequest);

        assertThat(rule.getName()).isEqualTo("my name");
    }

    @Test
    public void apply_WithDescription_ReturnsValidationRuleWithDescription() {
        ValidationRuleRequest ruleRequest = Populated.validationRuleRequest()
                .description("my description")
                .build();

        ValidationRule rule = converter.apply(ruleRequest);

        assertThat(rule.getDescription()).isEqualTo("my description");
    }

    @Test
    public void apply_WithEndDate_ReturnsValidationRuleWithEndDate() {
        LocalDate endDate = LocalDate.of(9201,1,1);
        ValidationRuleRequest ruleRequest = Populated.validationRuleRequest()
                .endDate(endDate)
                .build();

        ValidationRule rule = converter.apply(ruleRequest);

        assertThat(rule.getEndDate()).isEqualTo(endDate);
    }

    @Test
    public void apply_WithStartDate_ReturnsValidationRuleWithStartDate() {
        LocalDate startDate = LocalDate.of(1991,1,1);
        ValidationRuleRequest ruleRequest = Populated.validationRuleRequest()
                .startDate(startDate)
                .build();

        ValidationRule rule = converter.apply(ruleRequest);

        assertThat(rule.getStartDate()).isEqualTo(startDate);
    }

    @Test
    public void apply_WithValidationRuleSeverity_ReturnsValidationRuleWithValidationRuleSeverity() {
        ValidationRuleRequest ruleRequest = Populated.validationRuleRequest()
                .validationRuleSeverity(ValidationRuleSeverity.CRITICAL)
                .build();

        ValidationRule rule = converter.apply(ruleRequest);

        assertThat(rule.getValidationRuleSeverity()).isEqualTo(CRITICAL);
    }

    @Test
    public void apply_WithValidationRuleType_ReturnsValidationRuleWithValidationRuleType() {
        ValidationRuleRequest ruleRequest = Populated.validationRuleRequest()
                .validationRuleType(ValidationRuleType.QUALITY)
                .build();

        ValidationRule rule = converter.apply(ruleRequest);

        assertThat(rule.getValidationRuleType()).isEqualTo(QUALITY);
    }

    @Test
    public void apply_WithVersion_ReturnsValidationRuleWithVersion() {
        ValidationRuleRequest ruleRequest = Populated.validationRuleRequest()
                .version(5)
                .build();

        ValidationRule rule = converter.apply(ruleRequest);

        assertThat(rule.getVersion()).isEqualTo(5);
    }

    @Test
    public void apply_WithRuleExamples_ReturnsValidationRuleWithValidationRuleExample() {
        ValidationRuleRequest ruleRequest = Populated.validationRuleRequest()
                .version(5)
                .ruleExamples(newHashSet(
                        RuleExample.builder()
                                .id(22L)
                                .expectedResult(TestResultType.Pass)
                                .exampleFields(ImmutableMap.of(
                                        "fieldName",
                                        ExampleField.builder()
                                                .id(33L)
                                                .value("abc")
                                                .build()))
                                .build()))
                .build();

        ValidationRule rule = converter.apply(ruleRequest);

        ValidationRuleExample validationRuleExample = rule.getValidationRuleExamples().iterator().next();
        assertThat(validationRuleExample.getExpectedResult())
                .isEqualTo(TestResult.PASS);
        assertThat(validationRuleExample.getValidationRuleExampleFields())
                .extracting(
                        ValidationRuleExampleField::getId,
                        ValidationRuleExampleField::getName,
                        ValidationRuleExampleField::getValue)
                .containsSequence(tuple(33L, "fieldName", "abc"));
    }
}