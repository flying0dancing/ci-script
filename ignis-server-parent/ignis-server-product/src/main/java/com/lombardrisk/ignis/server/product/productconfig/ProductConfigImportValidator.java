package com.lombardrisk.ignis.server.product.productconfig;

import com.lombardrisk.ignis.common.MapperUtils;
import com.lombardrisk.ignis.data.common.error.ErrorResponse;
import com.lombardrisk.ignis.server.product.productconfig.model.ImportStatus;
import com.lombardrisk.ignis.server.product.productconfig.model.ProductConfig;
import com.lombardrisk.ignis.server.product.productconfig.model.ProductSchemaDetailsOnly;
import com.lombardrisk.ignis.server.product.table.model.Table;
import io.vavr.control.Validation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static com.lombardrisk.ignis.data.common.failure.CRUDFailure.Type.NOT_FOUND;
import static java.lang.Boolean.TRUE;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.SPACE;

@Slf4j
public class ProductConfigImportValidator {

    private final Validator validator;
    private final ProductConfigRepository productConfigRepository;

    public ProductConfigImportValidator(
            final Validator validator,
            final ProductConfigRepository productConfigRepository) {
        this.validator = validator;
        this.productConfigRepository = productConfigRepository;
    }

    public List<ErrorResponse> validateCanImport(final ProductConfig productConfig) {
        List<ErrorResponse> violationErrorResponses = validateBeans(productConfig);
        if (!violationErrorResponses.isEmpty()) {
            logErrorResponses(violationErrorResponses);

            return violationErrorResponses;
        }

        if (sameProductExists(productConfig)) {
            ErrorResponse existingProductErrorResponse = ErrorResponse.valueOf(
                    String.format(
                            "Product with name [%s] version [%s] already exists",
                            productConfig.getName(),
                            productConfig.getVersion()),
                    "VERSIONED_PRODUCT_CONFIG_EXISTS");
            logErrorResponse(existingProductErrorResponse);

            return singletonList(existingProductErrorResponse);
        }

        List<ErrorResponse> schemaErrorResponses = Stream.concat(
                validateNewSchemasDoNotExist(productConfig), validateProductHasPreviousSchemas(productConfig))
                .peek(ProductConfigImportValidator::logErrorResponse)
                .collect(toList());

        if (isNotEmpty(schemaErrorResponses)) {
            logErrorResponses(schemaErrorResponses);

            return schemaErrorResponses;
        }
        return emptyList();
    }

    @Transactional
    public Validation<ErrorResponse, ProductConfig> validateCanRollback(final Long productConfigId) {
        Optional<ProductConfig> optionalProductConfig = productConfigRepository.findById(productConfigId);

        if (!optionalProductConfig.isPresent()) {
            ErrorResponse missingProductConfigError = ErrorResponse.valueOf(
                    "Cannot find product with id [" + productConfigId + "]", NOT_FOUND.name());
            logErrorResponse(missingProductConfigError);

            return Validation.invalid(missingProductConfigError);
        }
        ProductConfig productConfig = optionalProductConfig.get();

        List<String> schemasWithStagedDatasets = findSchemasWithStagedDatasets(productConfig);
        if (isNotEmpty(schemasWithStagedDatasets)) {
            ErrorResponse productWithStagedDatasetsError = ErrorResponse.valueOf(
                    "Product [" + productConfig.getVersionedName() + "] "
                            + "has schemas referenced by staged datasets " + schemasWithStagedDatasets,
                    "REMOVE_STAGED_DATASET_SCHEMAS");
            logErrorResponse(productWithStagedDatasetsError);

            return Validation.invalid(productWithStagedDatasetsError);
        }
        if (productConfig.getImportStatus() == ImportStatus.IN_PROGRESS) {
            ErrorResponse productInProgressError = ErrorResponse.valueOf(
                    "Cannot delete product [" + productConfig.getVersionedName() + "] "
                            + "because it is being imported",
                    "DELETE_IN_PROGRESS_PRODUCT");
            logErrorResponse(productInProgressError);

            return Validation.invalid(productInProgressError);
        }
        return Validation.valid(productConfig);
    }

    private List<ErrorResponse> validateBeans(final ProductConfig productConfig) {
        Set<ConstraintViolation<ProductConfig>> beanValidationViolations = validator.validate(productConfig);

        List<ErrorResponse> violationErrors = MapperUtils.map(
                beanValidationViolations,
                ProductConfigErrorResponse::productConfigNotValid);

        violationErrors.addAll(
                validateSchemasHaveStartDate(productConfig.getTables()));

        return violationErrors;
    }

    private List<ErrorResponse> validateSchemasHaveStartDate(final Set<Table> schemas) {
        return schemas.stream()
                .filter(schema -> schema.getStartDate() == null)
                .map(ProductConfigImportValidator::toMissingStartDateError)
                .collect(toList());
    }

    private static ErrorResponse toMissingStartDateError(final Table schema) {
        return ErrorResponse.valueOf("Schema '" + schema.getVersionedName() + "' must have a start date", "startDate");
    }

    private boolean sameProductExists(final ProductConfig productConfig) {
        return productConfigRepository.existsByNameAndVersion(
                productConfig.getName(), productConfig.getVersion());
    }

    private Stream<ErrorResponse> validateNewSchemasDoNotExist(final ProductConfig productConfig) {

        Stream<ErrorResponse> physicalSchemaNameErrors = productConfig.getTables().stream()
                .map(schema -> productConfigRepository.findSchemasByPhysicalNameNotInProduct(
                        schema.getPhysicalTableName(), schema.getVersion(), productConfig.getName()))
                .filter(CollectionUtils::isNotEmpty)
                .flatMap(Collection::stream)
                .map(ProductConfigImportValidator::toPhysicalSchemaNameExistsError);

        Stream<ErrorResponse> displaySchemaNameErrors = productConfig.getTables().stream()
                .map(schema -> productConfigRepository.findSchemasByDisplayNameNotInProduct(
                        schema.getDisplayName(), schema.getVersion(), productConfig.getName()))
                .filter(CollectionUtils::isNotEmpty)
                .flatMap(Collection::stream)
                .map(ProductConfigImportValidator::toDisplaySchemaNameExistsError);

        return Stream.concat(physicalSchemaNameErrors, displaySchemaNameErrors);
    }

    private static ErrorResponse toPhysicalSchemaNameExistsError(final ProductSchemaDetailsOnly productSchemaDetails) {
        return ErrorResponse.valueOf(
                String.format(
                        "Schema with physical name [%s] version [%s] already exists in product [%s v.%s]",
                        productSchemaDetails.getSchemaPhysicalName(),
                        productSchemaDetails.getSchemaVersion(),
                        productSchemaDetails.getProductName(),
                        productSchemaDetails.getProductVersion()),
                "SCHEMA_PHYSICAL_NAME_EXISTS");
    }

    private static ErrorResponse toDisplaySchemaNameExistsError(final ProductSchemaDetailsOnly productSchemaDetails) {
        return ErrorResponse.valueOf(
                String.format(
                        "Schema with display name [%s] version [%s] already exists in product [%s v.%s]",
                        productSchemaDetails.getSchemaDisplayName(),
                        productSchemaDetails.getSchemaVersion(),
                        productSchemaDetails.getProductName(),
                        productSchemaDetails.getProductVersion()),
                "SCHEMA_DISPLAY_NAME_EXISTS");
    }

    private Stream<ErrorResponse> validateProductHasPreviousSchemas(final ProductConfig productConfig) {
        return productConfigRepository.findFirstByNameOrderByCreatedTimeDesc(productConfig.getName())
                .map(previousProduct ->
                        validateProductContainsPreviousSchemas(productConfig, previousProduct))
                .orElse(Stream.empty());
    }

    private Stream<ErrorResponse> validateProductContainsPreviousSchemas(
            final ProductConfig productConfig,
            final ProductConfig previousProductConfig) {

        return previousProductConfig.getTables().stream()
                .filter(previousSchema -> productConfig.getTables().stream()
                        .noneMatch(schema -> schema.isSameVersionAs(previousSchema)))
                .map(missingSchema -> ErrorResponse.valueOf(
                        String.format(
                                "Product [%s] must contain previous schema [%s]",
                                productConfig.getVersionedName(), missingSchema.getVersionedName()),
                        "MISSING_PREVIOUS_SCHEMA"));
    }

    private static void logErrorResponses(final List<ErrorResponse> errorResponses) {
        if (log.isDebugEnabled()) {
            log.debug("Failed validation:\n{}", errorResponses.stream()
                    .map(err -> SPACE + SPACE + err.getErrorCode() + " - " + err.getErrorMessage())
                    .collect(joining("\n")));
        }
    }

    private static List<String> findSchemasWithStagedDatasets(final ProductConfig productConfig) {
        return productConfig.getTables().stream()
                .filter(ProductConfigImportValidator::schemaHasDatasets)
                .map(Table::getVersionedName)
                .collect(toList());
    }

    private static boolean schemaHasDatasets(final Table table) {
        return TRUE.equals(table.getHasDatasets());
    }

    private static void logErrorResponse(final ErrorResponse errorResponse) {
        if (log.isDebugEnabled()) {
            log.debug("{} - {}", errorResponse.getErrorCode(), errorResponse.getErrorMessage());
        }
    }
}
