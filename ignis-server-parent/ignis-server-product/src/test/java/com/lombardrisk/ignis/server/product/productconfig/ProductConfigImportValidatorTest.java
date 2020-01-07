package com.lombardrisk.ignis.server.product.productconfig;

import com.lombardrisk.ignis.server.product.fixture.ProductPopulated;
import com.lombardrisk.ignis.server.product.productconfig.model.ImportStatus;
import com.lombardrisk.ignis.server.product.productconfig.model.ProductConfig;
import com.lombardrisk.ignis.data.common.error.ErrorResponse;
import io.vavr.control.Validation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Strict.class)
public class ProductConfigImportValidatorTest {

    @Mock
    private ProductConfigRepository productConfigRepository;
    @Mock
    private Validator validator;

    @InjectMocks
    private ProductConfigImportValidator productConfigImportValidator;

    @Before
    public void setup() {
        when(validator.validate(any(ProductConfig.class)))
                .thenReturn(emptySet());
    }

    @Test
    public void validateCanImport_ImportableProductConfig_ReturnNoErrors() {
        when(validator.validate(any()))
                .thenReturn(emptySet());
        when(productConfigRepository.existsByNameAndVersion(any(), any()))
                .thenReturn(false);
        when(productConfigRepository.findFirstByNameOrderByCreatedTimeDesc(any()))
                .thenReturn(Optional.empty());

        assertThat(productConfigImportValidator.validateCanImport(ProductPopulated.productConfig().build()))
                .isEmpty();
    }

    @Test
    public void validateCanImport_ProductConfigWithBeanValidationError_ReturnErrors() {
        Set<ConstraintViolation<ProductConfig>> violations = newHashSet(
                new DummyConstraintViolation(ProductPopulated.productConfig().build(), "name cannot be null"));
        when(validator.validate(any(ProductConfig.class)))
                .thenReturn(violations);

        assertThat(productConfigImportValidator.validateCanImport(ProductPopulated.productConfig().build()))
                .extracting(ErrorResponse::getErrorCode)
                .contains("PRODUCT_CONFIG_NOT_VALID");
    }

    @Test
    public void validateCanImport_ProductConfigExists_ReturnErrors() {
        when(productConfigRepository.existsByNameAndVersion(any(), any()))
                .thenReturn(true);

        assertThat(productConfigImportValidator.validateCanImport(ProductPopulated.productConfig().build()))
                .extracting(ErrorResponse::getErrorCode)
                .contains("VERSIONED_PRODUCT_CONFIG_EXISTS");
    }

    @Test
    public void validateCanImport_ProductConfigWithSchemasExistingInAnotherProduct_ReturnErrors() {
        when(productConfigRepository.findSchemasByPhysicalNameNotInProduct(any(), anyInt(), any()))
                .thenReturn(newArrayList(
                        ProductPopulated.productSchemaDetailsOnly()
                                .productName("P1").productVersion("T3")
                                .schemaPhysicalName("S1").schemaVersion(321)
                                .build()));
        when(productConfigRepository.findSchemasByDisplayNameNotInProduct(any(), anyInt(), any()))
                .thenReturn(newArrayList(
                        ProductPopulated.productSchemaDetailsOnly()
                                .productName("P2").productVersion("T1")
                                .schemaDisplayName("S O S").schemaVersion(321)
                                .build()));

        List<ErrorResponse> errorResponses = productConfigImportValidator.validateCanImport(
                ProductPopulated.productConfig()
                        .tables(newHashSet(ProductPopulated.table().build()))
                        .build());

        assertThat(errorResponses)
                .extracting(ErrorResponse::getErrorCode)
                .containsSequence("SCHEMA_PHYSICAL_NAME_EXISTS", "SCHEMA_DISPLAY_NAME_EXISTS");

        assertThat(errorResponses.get(0).getErrorMessage())
                .contains("P1", "T3", "S1", "321");
        assertThat(errorResponses.get(1).getErrorMessage())
                .contains("P2", "T1", "S O S", "321");
    }

    @Test
    public void validateCanImport_ProductConfigIsMissingPreviousSchemas_ReturnErrors() {
        when(productConfigRepository.findFirstByNameOrderByCreatedTimeDesc(any()))
                .thenReturn(Optional.of(
                        ProductPopulated.productConfig()
                                .tables(newHashSet(
                                        ProductPopulated.table().build()))
                                .build()));

        List<ErrorResponse> errorResponses = productConfigImportValidator.validateCanImport(
                ProductPopulated.productConfig().tables(newHashSet()).build());

        assertThat(errorResponses)
                .extracting(ErrorResponse::getErrorCode)
                .contains("MISSING_PREVIOUS_SCHEMA");
    }

    @Test
    public void validateCanRollback_RollbackableProductConfig_ReturnsNoErrors() {
        ProductConfig productConfig = ProductPopulated.productConfig().build();

        when(productConfigRepository.findById(any()))
                .thenReturn(Optional.of(productConfig));

        Validation<ErrorResponse, ProductConfig> productConfigValidation =
                productConfigImportValidator.validateCanRollback(1L);

        assertThat(productConfigValidation.get())
                .isSameAs(productConfig);
    }

    @Test
    public void validateCanRollback_ProductConfigDoesNotExist_ReturnsErrors() {
        when(productConfigRepository.findById(any()))
                .thenReturn(Optional.empty());

        Validation<ErrorResponse, ProductConfig> productConfigValidation =
                productConfigImportValidator.validateCanRollback(0L);

        assertThat(productConfigValidation.getError())
                .extracting(ErrorResponse::getErrorCode)
                .isEqualTo("NOT_FOUND");
    }

    @Test
    public void validateCanRollback_ProductConfigHasStagedDatasets_ReturnsErrors() {
        when(productConfigRepository.findById(any()))
                .thenReturn(Optional.of(ProductPopulated.productConfig()
                        .tables(newHashSet(
                                ProductPopulated.table()
                                        .hasDatasets(true)
                                        .build()))
                        .build()));

        Validation<ErrorResponse, ProductConfig> productConfigValidation =
                productConfigImportValidator.validateCanRollback(0L);

        assertThat(productConfigValidation.getError())
                .extracting(ErrorResponse::getErrorCode)
                .isEqualTo("REMOVE_STAGED_DATASET_SCHEMAS");
    }

    @Test
    public void validateCanRollback_ProductConfigIsStillBeingImported_ReturnsErrors() {
        when(productConfigRepository.findById(any()))
                .thenReturn(Optional.of(ProductPopulated.productConfig()
                        .importStatus(ImportStatus.IN_PROGRESS)
                        .build()));

        Validation<ErrorResponse, ProductConfig> productConfigValidation =
                productConfigImportValidator.validateCanRollback(0L);

        assertThat(productConfigValidation.getError())
                .extracting(ErrorResponse::getErrorCode)
                .isEqualTo("DELETE_IN_PROGRESS_PRODUCT");
    }
}
