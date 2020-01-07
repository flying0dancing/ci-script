package com.lombardrisk.ignis.client.design;

import com.lombardrisk.ignis.client.design.fixtures.Populated;
import org.junit.Test;

import java.time.LocalDate;

import static com.lombardrisk.ignis.common.fixtures.BeanValidationAssertions.assertThat;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.repeat;

public class ValidationRuleRequestTest {

    @Test
    public void validate_Correct_ReturnsNoConstraintViolations() {
        assertThat(Populated.validationRuleRequest().build())
                .hasNoViolations();
    }

    @Test
    public void validate_BlankName_ReturnsConstraintViolation() {
        assertThat(
                Populated.validationRuleRequest()
                        .name(null)
                        .build())
                .containsViolation("name", "must not be blank");

        assertThat(
                Populated.validationRuleRequest()
                        .name(EMPTY)
                        .build())
                .containsViolation("name", "must not be blank");

        assertThat(
                Populated.validationRuleRequest()
                        .name("  \n")
                        .build())
                .containsViolation("name", "must not be blank");
    }

    @Test
    public void validate_GreaterThan100Name_ReturnsConstraintViolation() {
        assertThat(
                Populated.validationRuleRequest()
                        .name(repeat("a", 101))
                        .build())
                .containsViolation("name", "length must be between 1 and 100");
    }

    @Test
    public void validate_BlankRuleId_ReturnsConstraintViolation() {
        assertThat(
                Populated.validationRuleRequest()
                        .ruleId(null)
                        .build())
                .containsViolation("ruleId", "must not be blank");

        assertThat(
                Populated.validationRuleRequest()
                        .ruleId(EMPTY)
                        .build())
                .containsViolation("ruleId", "must not be blank");

        assertThat(
                Populated.validationRuleRequest()
                        .ruleId(" ")
                        .build())
                .containsViolation("ruleId", "must not be blank");
    }

    @Test
    public void validate_GreaterThan100RuleId_ReturnsConstraintViolation() {
        assertThat(
                Populated.validationRuleRequest()
                        .ruleId(repeat("b", 101))
                        .build())
                .containsViolation("ruleId", "length must be between 1 and 100");
    }

    @Test
    public void validate_NullVersion_ReturnsConstraintViolation() {
        assertThat(
                Populated.validationRuleRequest()
                        .version(null)
                        .build())
                .containsViolation("version", "must not be null");
    }

    @Test
    public void validate_LessThan1Version_ReturnsConstraintViolation() {
        assertThat(
                Populated.validationRuleRequest()
                        .version(0)
                        .build())
                .containsViolation("version", "must be greater than or equal to 1");
    }

    @Test
    public void validate_NullValidationRuleType_ReturnsConstraintViolation() {
        assertThat(
                Populated.validationRuleRequest()
                        .validationRuleType(null)
                        .build())
                .containsViolation("validationRuleType", "must not be null");
    }

    @Test
    public void validate_NullValidationRuleSeverity_ReturnsConstraintViolation() {
        assertThat(
                Populated.validationRuleRequest()
                        .validationRuleSeverity(null)
                        .build())
                .containsViolation("validationRuleSeverity", "must not be null");
    }

    @Test
    public void validate_GreaterThan1000Description_ReturnsConstraintViolation() {
        assertThat(
                Populated.validationRuleRequest()
                        .description(repeat("d", 2001))
                        .build())
                .containsViolation("description", "length must be between 0 and 2000");
    }

    @Test
    public void validate_BlankExpression_ReturnsConstraintViolation() {
        assertThat(
                Populated.validationRuleRequest()
                        .expression(EMPTY)
                        .build())
                .containsViolation("expression", "must not be blank");

        assertThat(
                Populated.validationRuleRequest()
                        .expression(null)
                        .build())
                .containsViolation("expression", "must not be blank");

        assertThat(
                Populated.validationRuleRequest()
                        .expression("  ")
                        .build())
                .containsViolation("expression", "must not be blank");
    }

    @Test
    public void validate_GreaterThan1000Expression_ReturnsConstraintViolation() {
        assertThat(
                Populated.validationRuleRequest()
                        .expression(repeat("d", 1001))
                        .build())
                .containsViolation("expression", "length must be between 0 and 1000");
    }

    @Test
    public void validate_NullStartDate_ReturnsConstraintViolation() {
        assertThat(
                Populated.validationRuleRequest()
                        .startDate(null)
                        .build())
                .containsViolation("startDate", "must not be null");
    }

    @Test
    public void validate_NullEndDate_ReturnsConstraintViolation() {
        assertThat(
                Populated.validationRuleRequest()
                        .endDate(null)
                        .build())
                .containsViolation("endDate", "must not be null");
    }

    @Test
    public void validate_StartDateAfterEndDate_ReturnsConstraintViolation() {
        assertThat(
                Populated.validationRuleRequest()
                        .endDate(LocalDate.of(2000, 1, 1))
                        .startDate(LocalDate.of(2000, 1, 2))
                        .build())
                .containsViolation("datesOrdered", "Start Date must not be after End Date");
    }
}