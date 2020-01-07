package com.lombardrisk.ignis.server.product.productconfig;

import com.google.common.collect.ImmutableList;
import com.lombardrisk.ignis.common.fixtures.BeanValidationAssertions;
import com.lombardrisk.ignis.data.common.error.ErrorResponse;
import com.lombardrisk.ignis.server.product.fixture.ProductPopulated;
import com.lombardrisk.ignis.server.product.productconfig.model.ProductConfig;
import org.junit.Test;

import javax.validation.ConstraintViolation;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;

public class ProductConfigErrorResponseTest {

    @Test
    public void productZipEmpty_ReturnsErrorResponse() {
        ErrorResponse errorResponse = ProductConfigErrorResponse.productZipEmpty("my_empty_product.zip");

        assertThat(errorResponse.getErrorCode())
                .isEqualTo("PRODUCT_ZIP_EMPTY");

        assertThat(errorResponse.getErrorMessage())
                .isEqualTo("Product config zip [my_empty_product.zip] cannot be empty");
    }

    @Test
    public void productMissingManifest_ReturnsErrorResponse() {
        ErrorResponse errorResponse = ProductConfigErrorResponse.productMissingManifest("missing_manifest.zip");

        assertThat(errorResponse.getErrorCode())
                .isEqualTo("PRODUCT_MISSING_MANIFEST");

        assertThat(errorResponse.getErrorMessage())
                .isEqualTo("Product config zip [missing_manifest.zip] must have a manifest file");
    }

    @Test
    public void jsonFormatInvalid_ReturnsErrorResponse() {
        ErrorResponse errorResponse =
                ProductConfigErrorResponse.jsonFormatInvalid(asList("TABLE_ONE.json", "TABLE_TWO.json"));

        assertThat(errorResponse.getErrorCode())
                .isEqualTo("JSON_FORMAT_INVALID");

        assertThat(errorResponse.getErrorMessage())
                .isEqualTo("Files [TABLE_ONE.json, TABLE_TWO.json] are formatted incorrectly");
    }

    @Test
    public void productConfigExists_ReturnsErrorResponse() {
        ErrorResponse errorResponse = ProductConfigErrorResponse.productConfigExists("MY_PRODUCT_CONFIG");

        assertThat(errorResponse.getErrorCode())
                .isEqualTo("PRODUCT_CONFIG_EXISTS");

        assertThat(errorResponse.getErrorMessage())
                .contains("MY_PRODUCT_CONFIG", "already exists");
    }

    @Test
    public void productConfigNotValid_InvalidProductConfig_ReturnsErrorResponse() {
        ProductConfig invalidProduct = ProductPopulated.productConfig()
                .name(null)
                .build();

        Set<ConstraintViolation<ProductConfig>> constraintViolations =
                BeanValidationAssertions.VALIDATOR.validate(invalidProduct);

        assertThat(constraintViolations).hasSize(1);

        ErrorResponse errorResponse =
                ProductConfigErrorResponse.productConfigNotValid(ImmutableList.copyOf(constraintViolations).get(0));

        assertThat(errorResponse.getErrorCode())
                .isEqualTo("PRODUCT_CONFIG_NOT_VALID");

        assertThat(errorResponse.getErrorMessage())
                .isEqualTo("Property 'name' of ProductConfig 'null': must not be null");
    }

    @Test
    public void productConfigNotValid_InvalidTable_ReturnsErrorResponse() {
        ProductConfig invalidProduct = ProductPopulated.productConfig()
                .name("a product config name")
                .tables(singleton(ProductPopulated.table()
                        .physicalTableName("1NVALID_T4BLE_NAM£")
                        .build()))
                .build();

        Set<ConstraintViolation<ProductConfig>> constraintViolations =
                BeanValidationAssertions.VALIDATOR.validate(invalidProduct);

        assertThat(constraintViolations).hasSize(1);

        ErrorResponse errorResponse =
                ProductConfigErrorResponse.productConfigNotValid(ImmutableList.copyOf(constraintViolations).get(0));

        assertThat(errorResponse.getErrorCode())
                .isEqualTo("PRODUCT_CONFIG_NOT_VALID");

        assertThat(errorResponse.getErrorMessage())
                .startsWith("Property 'physicalTableName' of Table '1NVALID_T4BLE_NAM£': PhysicalTableName has to start with");
    }

    @Test
    public void productConfigNotValid_InvalidTableField_ReturnsErrorResponse() {
        ProductConfig invalidProduct = ProductPopulated.productConfig()
                .name("a product config name")
                .tables(singleton(ProductPopulated.table()
                        .fields(singleton(ProductPopulated.decimalField()
                                .name(null)
                                .build()))
                        .build()))
                .build();

        Set<ConstraintViolation<ProductConfig>> constraintViolations =
                BeanValidationAssertions.VALIDATOR.validate(invalidProduct);

        assertThat(constraintViolations).hasSize(1);

        ErrorResponse errorResponse =
                ProductConfigErrorResponse.productConfigNotValid(ImmutableList.copyOf(constraintViolations).get(0));

        assertThat(errorResponse.getErrorCode())
                .isEqualTo("PRODUCT_CONFIG_NOT_VALID");

        assertThat(errorResponse.getErrorMessage())
                .isEqualTo("Property 'name' of DecimalField 'null': must not be null");
    }

    @Test
    public void productConfigNotValid_InvalidRule_ReturnsErrorResponse() {
        ProductConfig invalidProduct = ProductPopulated.productConfig()
                .name("a product config name")
                .tables(singleton(ProductPopulated.table()
                        .validationRules(singleton(ProductPopulated.validationRule()
                                .name("the rule name")
                                .expression(null)
                                .build()))
                        .build()))
                .build();

        Set<ConstraintViolation<ProductConfig>> constraintViolations =
                BeanValidationAssertions.VALIDATOR.validate(invalidProduct);

        assertThat(constraintViolations).hasSize(1);

        ErrorResponse errorResponse =
                ProductConfigErrorResponse.productConfigNotValid(ImmutableList.copyOf(constraintViolations).get(0));

        assertThat(errorResponse.getErrorCode())
                .isEqualTo("PRODUCT_CONFIG_NOT_VALID");

        assertThat(errorResponse.getErrorMessage())
                .isEqualTo("Property 'expression' of ValidationRule 'the rule name': must not be blank");
    }
}
