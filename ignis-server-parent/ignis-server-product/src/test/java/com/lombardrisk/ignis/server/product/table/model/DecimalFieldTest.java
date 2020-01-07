package com.lombardrisk.ignis.server.product.table.model;

import com.lombardrisk.ignis.common.fixtures.BeanValidationAssertions;
import com.lombardrisk.ignis.server.product.fixture.ProductPopulated;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import static com.lombardrisk.ignis.common.fixtures.BeanValidationAssertions.VALIDATOR;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Enclosed.class)
public class DecimalFieldTest {

    public static class ColumnDefTest {

        @Test
        public void toColumnDef_ReturnsDecimalColumnDef() {
            DecimalField decimalField = new DecimalField();
            decimalField.setName("DECIMAL_FIELD");

            assertThat(
                    ProductPopulated.decimalField("DECIMAL_FIELD")
                            .precision(22)
                            .scale(11)
                            .build()
                            .toColumnDef()
            ).isEqualTo("\"DECIMAL_FIELD\" DECIMAL(22,11)");
        }
    }

    public static class BeanValidationTest {

        @Test
        public void validate_Correct_ReturnsNoConstraintViolations() {
            assertThat(VALIDATOR.validate(ProductPopulated.decimalField().build()))
                    .isEmpty();
        }

        @Test
        public void validate_NullPrecision_ReturnsConstraintViolation() {
            BeanValidationAssertions.assertThat(
                    ProductPopulated.decimalField()
                            .precision(null)
                            .build())
                    .containsViolation("precision", "must not be null");
        }

        @Test
        public void validate_LessThan1Precision_ReturnsConstraintViolation() {
            BeanValidationAssertions.assertThat(
                    ProductPopulated.decimalField()
                            .precision(0)
                            .build())
                    .containsViolation("precision", "must be greater than or equal to 1");
        }

        @Test
        public void validate_GreaterThan38Precision_ReturnsConstraintViolation() {
            BeanValidationAssertions.assertThat(
                    ProductPopulated.decimalField()
                            .precision(39)
                            .build())
                    .containsViolation("precision", "must be less than or equal to 38");
        }

        @Test
        public void validate_ScaleGreaterThanPrecision_ReturnsConstraintViolation() {
            BeanValidationAssertions.assertThat(
                    ProductPopulated.decimalField()
                            .precision(3)
                            .scale(4)
                            .build())
                    .containsViolation(
                            "scaleLessThanOrEqualToPrecision",
                            "scale needs to be less than or equal to precision");
        }

        @Test
        public void validate_NullScale_ReturnsConstraintViolation() {
            BeanValidationAssertions.assertThat(
                    ProductPopulated.decimalField()
                            .scale(null)
                            .build())
                    .containsViolation("scale", "must not be null");
        }
    }

    public static class ValidationAndParsingAndConversion {

        @Test
        public void doValidate_NumberValue_ReturnsValidValidationResult() {
            assertThat(
                    ProductPopulated.decimalField().build()
                            .doValidate("2").isValid())
                    .isTrue();
        }

        @Test
        public void doValidate_LettersAndNumbersValue_ReturnsInvalidValidationResult() {
            assertThat(
                    ProductPopulated.decimalField("DEC").build()
                            .doValidate("23X.11").getError())
                    .isEqualTo("\"Field [DEC] expected to be have a decimal type, but was [23X.11]\"");
        }

        @Test
        public void doValidate_CanApplyPrecisionAfterScaleRounding_ReturnsValidValidationResult() {
            DecimalField decimalField =
                    ProductPopulated.decimalField()
                            .precision(3)
                            .scale(1)
                            .build();
            assertThat(
                    decimalField.doValidate("23.11").isValid())
                    .isTrue();
        }

        @Test
        public void doValidate_ValueWithFractionalPartCanNotApplyPrecisionAndScale_ReturnsValidValidationResult() {
            DecimalField decimalField =
                    ProductPopulated.decimalField()
                            .name("rebel")
                            .precision(5)
                            .scale(3)
                            .build();

            assertThat(
                    decimalField.doValidate("123.45").getError())
                    .isEqualTo(
                            "\"Field [rebel] cannot be converted to decimal type: "
                                    + "precision [5] and scale [3] cannot be applied for value [123.45], "
                                    + "because the resulting number (123.xxx) will not fit the precision (5 < 6)\"");
        }

        @Test
        public void doValidate_ValueWithWholePartOnlyCanNotApplyPrecisionAndScale_ReturnsValidValidationResult() {
            DecimalField decimalField =
                    ProductPopulated.decimalField()
                            .precision(2)
                            .scale(2)
                            .build();
            assertThat(
                    decimalField.doValidate("5").isInvalid())
                    .isTrue();
        }

        @Test
        public void doParse_NumericValue_ReturnsBigDecimal() {
            assertThat(
                    ProductPopulated.decimalField().build()
                            .doParse("23.1").doubleValue())
                    .isEqualTo(23.1d);
        }
    }
}
