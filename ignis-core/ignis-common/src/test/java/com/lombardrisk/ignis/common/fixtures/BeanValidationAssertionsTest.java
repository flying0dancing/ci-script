package com.lombardrisk.ignis.common.fixtures;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.Test;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class BeanValidationAssertionsTest {

    @Test
    public void hasNoViolations_BeanWithNoConstraintViolations_Passes() {
        BeanValidationAssertions.assertThat(new BeanClass(0L, 0))
                .hasNoViolations();
    }

    @Test
    public void hasNoViolations_BeanWithConstraintViolations_Fails() {
        assertThatThrownBy(
                () ->
                        BeanValidationAssertions.assertThat(new BeanClass(null, 0))
                                .hasNoViolations())
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining(
                        "Expected 0 constraint violations after bean validation\nbut found:\n  ConstraintViolationImpl{");
    }

    @Test
    public void containsViolation_BeanWithConstraintViolation_Passes() {
        BeanValidationAssertions.assertThat(new BeanClass(null, -1))
                .containsViolation("id", "must not be null")
                .containsViolation("age", "must be greater than or equal to 0");
    }

    @Test
    public void containsViolation_BeanWithConstraintViolation_FailsWithWrongExpectedMessage() {
        assertThatThrownBy(
                () ->
                        BeanValidationAssertions.assertThat(new BeanClass(null, 1))
                                .containsViolation("id", "must be positive"))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("<[\"must not be null\"]>")
                .hasMessageContaining("<[\"must be positive\"]>");
    }

    @Test
    public void hasViolations_BeanWithConstraintViolations_Passes() {
        BeanValidationAssertions.assertThat(new BeanClass(null, 0))
                .hasViolations("id");
    }

    @Test
    public void hasViolations_BeanWithNoConstraintViolations_Fails() {
        assertThatThrownBy(
                () ->
                        BeanValidationAssertions.assertThat(new BeanClass(0L, 1))
                                .hasViolations("id"))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("Expecting actual not to be empty");
    }

    @Data
    @AllArgsConstructor
    private static class BeanClass {

        @NotNull
        private Long id;
        @Min(0)
        private Integer age;
    }
}
