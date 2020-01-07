package com.lombardrisk.ignis.server.product.table.model;

import com.lombardrisk.ignis.server.product.table.model.IntField;
import org.junit.Test;

import static com.lombardrisk.ignis.common.fixtures.BeanValidationAssertions.assertThat;

public class FieldTest {

    @Test
    public void validate_NameWithCorrectFormat_ReturnsNoConstraintViolations() {
        assertThat(field("C0RRECT_NAM3"))
                .hasNoViolations();
    }

    @Test
    public void validate_NameWithSpace_ReturnsConstraintViolation() {
        assertThat(field("IN C0RRECT NAM3"))
                .containsViolation(
                        "name",
                        "Name must not contain any special characters or spaces");
    }

    @Test
    public void validate_NameWithPipe_ReturnsConstraintViolation() {
        assertThat(field("IN|C0RRECT_NAM3"))
                .containsViolation(
                        "name",
                        "Name must not contain any special characters or spaces");
    }

    @Test
    public void validate_NameStartWithNumber_PassesValidation() {
        assertThat(field("1_C0RRECT_NAM3"))
                .hasNoViolations();
    }

    private static IntField field(final String name) {
        IntField testField = new IntField();
        testField.setName(name);
        return testField;
    }
}
