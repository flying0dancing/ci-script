package com.lombardrisk.ignis.common.fixtures;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Objects;
import java.util.Set;

import static java.util.stream.Collectors.joining;

public final class BeanValidationAssertions<T> extends AbstractAssert<BeanValidationAssertions<T>, T> {

    public static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    private BeanValidationAssertions(final T actual) {
        super(actual, BeanValidationAssertions.class);
    }

    public static <T> BeanValidationAssertions<T> assertThat(final T bean) {
        return new BeanValidationAssertions<>(bean);
    }

    public BeanValidationAssertions<T> hasNoViolations() {
        isNotNull();

        Set<ConstraintViolation<T>> constraintViolations = validate();
        if (!constraintViolations.isEmpty()) {
            failWithMessage(
                    "Expected 0 constraint violations after bean validation\nbut found:\n  %s",
                    constraintViolations.stream()
                            .map(Objects::toString)
                            .collect(joining("\n  ")));
        }
        return myself;
    }

    public BeanValidationAssertions<T> containsViolation(final String fieldName, final String message) {
        isNotNull();

        Set<ConstraintViolation<T>> constraintViolations = validate();

        Assertions.assertThat(constraintViolations)
                .filteredOn(violation -> fieldName.contains(violation.getPropertyPath().toString()))
                .extracting(ConstraintViolation::getMessage)
                .contains(message);

        return myself;
    }

    public void hasViolations(final String fieldName) {
        Assertions.assertThat(VALIDATOR.validate(actual))
                .filteredOn(violation -> violation.getPropertyPath().toString().contains(fieldName))
                .isNotEmpty();
    }

    private Set<ConstraintViolation<T>> validate() {
        return VALIDATOR.validate(actual);
    }
}
