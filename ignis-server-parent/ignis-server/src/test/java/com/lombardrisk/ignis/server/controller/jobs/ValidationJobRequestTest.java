package com.lombardrisk.ignis.server.controller.jobs;

import com.lombardrisk.ignis.server.fixtures.Populated;
import org.junit.Test;

import static com.lombardrisk.ignis.common.fixtures.BeanValidationAssertions.assertThat;

public class ValidationJobRequestTest {

    @Test
    public void validate_Correct_ReturnsNoConstraintViolations() {
        assertThat(Populated.validationJobRequest().build())
                .hasNoViolations();
    }

    @Test
    public void validate_NullDatasetId_ReturnsConstraintViolation() {
        assertThat(
                Populated.validationJobRequest()
                        .datasetId(null)
                        .build())
                .containsViolation("datasetId", "must not be null");
    }

    @Test
    public void validate_NullName_ReturnsConstraintViolation() {
        assertThat(
                Populated.validationJobRequest()
                        .name(null)
                        .build())
                .containsViolation("name", "must not be blank");
    }

    @Test
    public void validate_EmptyName_ReturnsConstraintViolation() {
        assertThat(
                Populated.validationJobRequest()
                        .name("  ")
                        .build())
                .containsViolation("name", "must not be blank");
    }
}