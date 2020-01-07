package com.lombardrisk.ignis.server.product.rule;

import com.lombardrisk.ignis.server.product.fixture.ProductPopulated;
import com.lombardrisk.ignis.server.product.rule.model.ValidationRule;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.time.LocalDate;

import static com.lombardrisk.ignis.common.fixtures.BeanValidationAssertions.assertThat;

public class ValidationRuleTest {

    @Test
    public void validate_Correct_ReturnsNoConstraintViolations() {
        assertThat(ProductPopulated.validationRule().build())
                .hasNoViolations();
    }

    @Test
    public void validate_NullRuleId_ReturnsConstraintViolation() {
        assertThat(
                ProductPopulated.validationRule()
                        .ruleId(null)
                        .build())
                .containsViolation("ruleId", "must not be blank");
    }

    @Test
    public void validate_EmptyRuleId_ReturnsConstraintViolation() {
        assertThat(
                ProductPopulated.validationRule()
                        .ruleId("")
                        .build())
                .containsViolation("ruleId", "must not be blank");
    }

    @Test
    public void validate_NullExpression_ReturnsConstraintViolation() {
        assertThat(
                ProductPopulated.validationRule()
                        .expression(null)
                        .build())
                .containsViolation("expression", "must not be blank");
    }

    @Test
    public void validate_EmptyExpression_ReturnsConstraintViolation() {
        assertThat(
                ProductPopulated.validationRule()
                        .expression("     ")
                        .build())
                .containsViolation("expression", "must not be blank");
    }

    @Test
    public void isValidFor_dateBetweenStartAndEnd_returnsTrue() {
        ValidationRule validationRule = ProductPopulated.validationRule()
                .startDate(LocalDate.of(2018, 1, 1))
                .endDate(LocalDate.of(2018, 1, 3))
                .build();

        LocalDate date = LocalDate.of(2018, 1, 2);

        Assertions.assertThat(validationRule.isValidFor(date))
                .isTrue();
    }

    @Test
    public void isValidFor_dateBeforeStart_returnsFalse() {
        ValidationRule validationRule = ProductPopulated.validationRule()
                .startDate(LocalDate.of(2018, 1, 10))
                .endDate(LocalDate.of(2018, 1, 15))
                .build();

        LocalDate date = LocalDate.of(2018, 1, 9);

        Assertions.assertThat(validationRule.isValidFor(date))
                .isFalse();
    }

    @Test
    public void isValidFor_dateAfterEnd_returnsFalse() {
        ValidationRule validationRule = ProductPopulated.validationRule()
                .startDate(LocalDate.of(2018, 1, 10))
                .endDate(LocalDate.of(2018, 1, 15))
                .build();

        LocalDate date = LocalDate.of(2018, 1, 16);

        Assertions.assertThat(validationRule.isValidFor(date))
                .isFalse();
    }

    @Test
    public void isValidFor_startEqualsDate_returnsTrue() {
        ValidationRule validationRule = ProductPopulated.validationRule()
                .startDate(LocalDate.of(2018, 1, 10))
                .endDate(LocalDate.of(2018, 1, 15))
                .build();

        LocalDate date = LocalDate.of(2018, 1, 10);

        Assertions.assertThat(validationRule.isValidFor(date))
                .isTrue();
    }

    @Test
    public void isValidFor_endEqualsDate_returnsTrue() {
        ValidationRule validationRule = ProductPopulated.validationRule()
                .startDate(LocalDate.of(2018, 1, 10))
                .endDate(LocalDate.of(2018, 1, 15))
                .build();

        LocalDate date = LocalDate.of(2018, 1, 15);

        Assertions.assertThat(validationRule.isValidFor(date))
                .isTrue();
    }
}
