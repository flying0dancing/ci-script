package com.lombardrisk.ignis.server.job.product;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.lombardrisk.ignis.client.external.job.JobStatus;
import com.lombardrisk.ignis.client.external.productconfig.export.SchemaPeriod;
import com.lombardrisk.ignis.common.stream.CollectorUtils;
import com.lombardrisk.ignis.data.common.Identifiable;
import com.lombardrisk.ignis.data.common.error.ErrorResponse;
import com.lombardrisk.ignis.pipeline.step.api.DrillbackColumnLink;
import com.lombardrisk.ignis.server.job.exception.JobStartException;
import com.lombardrisk.ignis.server.job.servicerequest.ServiceRequest;
import com.lombardrisk.ignis.server.job.servicerequest.ServiceRequestRepository;
import com.lombardrisk.ignis.server.job.staging.JobStarter;
import com.lombardrisk.ignis.server.product.pipeline.PipelineService;
import com.lombardrisk.ignis.server.product.pipeline.PipelineStepService;
import com.lombardrisk.ignis.server.product.pipeline.model.Pipeline;
import com.lombardrisk.ignis.server.product.productconfig.ProductConfigImportDiffer;
import com.lombardrisk.ignis.server.product.productconfig.ProductConfigImportEntityService;
import com.lombardrisk.ignis.server.product.productconfig.ProductConfigImportValidator;
import com.lombardrisk.ignis.server.product.productconfig.ProductConfigRepository;
import com.lombardrisk.ignis.server.product.productconfig.model.ProductConfig;
import com.lombardrisk.ignis.server.product.productconfig.model.ProductConfigFileContents;
import com.lombardrisk.ignis.server.product.productconfig.model.ProductImportContext;
import com.lombardrisk.ignis.server.product.productconfig.model.ProductImportDiff;
import com.lombardrisk.ignis.server.product.productconfig.view.ProductConfigExportConverter;
import com.lombardrisk.ignis.server.product.table.TableService;
import com.lombardrisk.ignis.server.product.table.model.Field;
import com.lombardrisk.ignis.server.product.table.model.Table;
import com.lombardrisk.ignis.web.common.exception.GlobalExceptionHandler;
import com.lombardrisk.ignis.web.common.exception.RollbackTransactionException;
import io.vavr.control.Either;
import io.vavr.control.Validation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;

import static com.google.common.collect.Maps.newHashMap;
import static com.lombardrisk.ignis.common.json.MapperWrapper.MAPPER;
import static com.lombardrisk.ignis.server.job.servicerequest.ServiceRequest.PRODUCT_CONFIG_ID;
import static com.lombardrisk.ignis.server.job.servicerequest.ServiceRequestType.IMPORT_PRODUCT;
import static com.lombardrisk.ignis.server.job.servicerequest.ServiceRequestType.ROLLBACK_PRODUCT;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.SPACE;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Slf4j
public class ProductConfigImportJobService {

    private static final String SPACED_SEP = ", ";
    private static final String COLON_SEP = " : ";

    private static final ErrorResponse IMPORT_PRODUCT_ERROR =
            ErrorResponse.valueOf("Unexpected error starting import product job", "IMPORT_PRODUCT_ERROR");
    private static final ErrorResponse ROLLBACK_PRODUCT_ERROR =
            ErrorResponse.valueOf("Unexpected error starting rollback product job", "ROLLBACK_PRODUCT_ERROR");

    private final ProductConfigRepository productConfigRepository;
    private final ProductConfigImportValidator productConfigImportValidator;
    private final ProductConfigImportDiffer productConfigImportDiffer;
    private final ProductConfigExportConverter productConfigExportConverter;
    private final TableService tableService;
    private final PipelineService pipelineService;
    private final PipelineStepService pipelineStepService;
    private final ProductConfigImportEntityService productConfigImportEntityService;
    private final ServiceRequestRepository serviceRequestRepository;
    private final JobStarter jobOperator;

    public ProductConfigImportJobService(
            final ProductConfigRepository productConfigRepository,
            final ProductConfigImportValidator productConfigImportValidator,
            final ProductConfigImportDiffer productConfigImportDiffer,
            final ProductConfigExportConverter productConfigExportConverter,
            final TableService tableService,
            final PipelineService pipelineService,
            final PipelineStepService pipelineStepService,
            final ProductConfigImportEntityService productConfigImportEntityService,
            final ServiceRequestRepository serviceRequestRepository,
            final JobStarter jobOperator) {
        this.productConfigRepository = productConfigRepository;
        this.productConfigImportValidator = productConfigImportValidator;
        this.productConfigImportDiffer = productConfigImportDiffer;
        this.productConfigExportConverter = productConfigExportConverter;
        this.tableService = tableService;
        this.pipelineService = pipelineService;
        this.pipelineStepService = pipelineStepService;
        this.productConfigImportEntityService = productConfigImportEntityService;
        this.serviceRequestRepository = serviceRequestRepository;
        this.jobOperator = jobOperator;
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public Validation<List<ErrorResponse>, Identifiable> importProductConfig(
            final ProductConfigFileContents productConfigFileContents,
            final String userName) {

        ProductConfig newProductConfig = productConfigExportConverter.apply(productConfigFileContents);
        List<ErrorResponse> importProductValidations = productConfigImportValidator.validateCanImport(
                newProductConfig);

        if (isNotEmpty(importProductValidations)) {
            return Validation.invalid(importProductValidations);
        }
        Long newProductConfigId = null;
        Long newServiceRequestId = null;
        try {
            ProductImportDiff productImportDiff = productConfigImportDiffer.calculateDiff(newProductConfig);

            if (productImportDiff == null) {
                ErrorResponse errorResponse = ErrorResponse.valueOf(
                        String.format(
                                "Product with name [%s] version [%s] has no schemas",
                                newProductConfig.getName(),
                                newProductConfig.getVersion()),
                        "PRODUCT_HAS_NO_SCHEMAS");
                return Validation.invalid(singletonList(errorResponse));
            }

            newProductConfigId = productConfigImportEntityService.saveProductConfig(
                    productImportDiff, productConfigFileContents);

            Either<ErrorResponse, ServiceRequest> serviceRequestValidation = saveServiceRequest(
                    newProductConfigId, productImportDiff, userName);

            if (serviceRequestValidation.isLeft()) {
                return Validation.invalid(singletonList(serviceRequestValidation.getLeft()));
            }
            newServiceRequestId = serviceRequestValidation.get().getId();

            List<ErrorResponse> importJobErrors = startImportJob(
                    newProductConfigId, serviceRequestValidation.get());

            if (isNotEmpty(importJobErrors)) {
                return Validation.invalid(importJobErrors);
            }
            return Validation.valid(Identifiable.fromId(newProductConfigId));
        } catch (final RollbackTransactionException e) {
            updateImportJobAndServiceRequestStatus(newProductConfigId, newServiceRequestId);

            log.error("Could not import product configuration", e);
            return e.toValidation();
        } catch (IllegalArgumentException e) {
            updateImportJobAndServiceRequestStatus(newProductConfigId, newServiceRequestId);
            throw e;
        } catch (final Exception e) {
            updateImportJobAndServiceRequestStatus(newProductConfigId, newServiceRequestId);

            log.error("Could not import product configuration", e);
            return Validation.invalid(Collections.singletonList(GlobalExceptionHandler.UNEXPECTED_ERROR_RESPONSE));
        }
    }

    private void updateImportJobAndServiceRequestStatus(final Long newProductConfigId, final Long newServiceRequestId) {
        boolean canUpdateStatus = newProductConfigId != null;
        if (newServiceRequestId != null) {
            canUpdateStatus = updateServiceRequestToAbandoned(newServiceRequestId);
        }

        if (canUpdateStatus) {
            productConfigImportEntityService.updateImportStatusError(newProductConfigId);
        }
    }

    private boolean updateServiceRequestToAbandoned(final Long newServiceRequestId) {
        final ServiceRequest serviceRequest = serviceRequestRepository.findFirstById(newServiceRequestId);

        if (serviceRequest == null) {
            return true;
        }

        if (serviceRequest.getJobExecutionId() == null) {
            serviceRequest.setStatus(JobStatus.ABANDONED);
            serviceRequestRepository.save(serviceRequest);

            return true;
        }

        return false;
    }

    private List<ErrorResponse> startImportJob(
            final Long productConfigId,
            final ServiceRequest serviceRequest) {
        try {
            Properties properties = new Properties();
            properties.setProperty(PRODUCT_CONFIG_ID, productConfigId.toString());

            jobOperator.startJob(IMPORT_PRODUCT.name(), serviceRequest, properties);

            return emptyList();
        } catch (JobStartException e) {
            log.error(e.getMessage(), e);

            return singletonList(IMPORT_PRODUCT_ERROR);
        }
    }

    private Either<ErrorResponse, ServiceRequest> saveServiceRequest(
            final Long productConfigId,
            final ProductImportDiff productImportDiff,
            final String userName) {
        try {
            String requestMessage = MAPPER.writeValueAsString(
                    toImportContext(productImportDiff, pipelineService.findByProductId(productConfigId)));

            String requestName = IMPORT_PRODUCT.getDisplayName() + SPACE + productImportDiff.getVersionedName();

            ServiceRequest serviceRequest = ServiceRequest.builder()
                    .name(requestName)
                    .serviceRequestType(IMPORT_PRODUCT)
                    .requestMessage(requestMessage)
                    .createdBy(userName)
                    .build();
            serviceRequest = serviceRequestRepository.save(serviceRequest);

            productConfigRepository.updateImportRequestId(productConfigId, serviceRequest.getId());

            return Either.right(serviceRequest);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage(), e);

            return Either.left(IMPORT_PRODUCT_ERROR);
        }
    }

    private ProductImportContext toImportContext(
            final ProductImportDiff productImportDiff,
            final List<Pipeline> pipelines) {

        Map<String, Map<String, Field>> physicalTableNameToFields =
                productImportDiff.physicalTableNameToFieldNameToField();

        Map<String, Set<DrillbackColumnLink>> drillbackLinksBySchemaName = pipelines.stream()
                .flatMap(pipeline -> pipeline.getSteps().stream())
                .flatMap(step -> pipelineStepService
                        .findAdditionalPhoenixDrillbackColumns(step, productImportDiff).stream())
                .collect(CollectorUtils.toMultimap(
                        DrillbackColumnLink::getOutputSchema, Function.identity()));

        return ProductImportContext.builder()
                .newSchemaNameToId(
                        productImportDiff.getNewSchemas().stream()
                                .collect(toMap(Table::getPhysicalTableName, Table::getId)))
                .schemaNameToNewFields(
                        toSchemaNameToNewFields(productImportDiff))
                .existingSchemaIdToNewPeriod(
                        toNewPeriods(productImportDiff.getExistingSchemaToNewPeriod()))
                .existingSchemaIdToOldPeriod(
                        toOldPeriods(productImportDiff.getExistingSchemaToNewPeriod()))
                .schemaNameToDrillBackColumns(drillbackLinksBySchemaName)
                .build();
    }

    private static Map<Long, SchemaPeriod> toNewPeriods(final Map<Table, SchemaPeriod> existingSchemaToNewPeriod) {
        return existingSchemaToNewPeriod.entrySet()
                .stream()
                .collect(toMap(
                        entry -> entry.getKey().getId(),
                        Entry::getValue));
    }

    private static Map<Long, SchemaPeriod> toOldPeriods(final Map<Table, SchemaPeriod> existingSchemaToNewPeriod) {
        return existingSchemaToNewPeriod.keySet()
                .stream()
                .collect(toMap(Table::getId, Table::getSchemaPeriod));
    }

    private static Map<String, Map<String, Long>> toSchemaNameToNewFields(
            final ProductImportDiff productImportDiff) {
        Map<String, String> fieldAndSchemaNames = new HashMap<>(
                toFieldAndSchemaNamesMap(productImportDiff.getExistingSchemas()));

        fieldAndSchemaNames.putAll(
                toFieldAndSchemaNamesMap(productImportDiff.getNewSchemas()));

        Map<String, Map<String, Long>> schemaNameToNewFields = new HashMap<>();

        for (Table newVersionedSchema : productImportDiff.getNewVersionedSchemas()) {

            for (Field newVersionedField : newVersionedSchema.getFields()) {

                String existingSchemaName = fieldAndSchemaNames.get(
                        newVersionedSchema.getPhysicalTableName() + newVersionedField.getName());

                if (isBlank(existingSchemaName)) {
                    schemaNameToNewFields.putIfAbsent(newVersionedSchema.getPhysicalTableName(), newHashMap());

                    Map<String, Long> newFields = schemaNameToNewFields.get(newVersionedSchema.getPhysicalTableName());
                    newFields.put(newVersionedField.getName(), newVersionedField.getId());
                }
            }
        }
        return schemaNameToNewFields;
    }

    private static Map<String, String> toFieldAndSchemaNamesMap(final Set<Table> schemas) {
        Map<String, String> fieldNameBySchemaName = new HashMap<>();

        for (Table existingSchema : schemas) {
            for (Field existingField : existingSchema.getFields()) {
                fieldNameBySchemaName.put(
                        existingSchema.getPhysicalTableName() + existingField.getName(),
                        existingSchema.getPhysicalTableName());
            }
        }
        return fieldNameBySchemaName;
    }

    public Validation<ErrorResponse, Identifiable> rollbackProductConfig(
            final Long productConfigId,
            final String userName) {
        Validation<ErrorResponse, ProductConfig> productConfigValidation =
                productConfigImportValidator.validateCanRollback(productConfigId);

        if (productConfigValidation.isInvalid()) {
            return Validation.invalid(productConfigValidation.getError());
        }
        ProductConfig productConfig = productConfigValidation.get();

        Optional<ErrorResponse> optionalError = deleteProductConfig(productConfig);
        if (optionalError.isPresent()) {
            return Validation.invalid(optionalError.get());
        }
        return startRollbackJob(userName, productConfig);
    }

    @Transactional
    public Optional<ErrorResponse> deleteProductConfig(final ProductConfig productConfig) {
        List<Table> schemasToDelete =
                productConfig.getTables()
                        .stream()
                        .filter(table -> isLinkedToOneProductOnly(table.getId()))
                        .collect(toList());

        pipelineService.deletePipelinesForProducts(singletonList(productConfig.getId()));

        schemasToDelete.forEach(tableService::delete);

        productConfigRepository.deleteById(productConfig.getId());

        Validation<ErrorResponse, Map<Long, SchemaPeriod>> schemaIdToOldPeriod =
                revertAnySchemaPeriods(productConfig.getImportRequestId());

        logDeletedProductConfig(productConfig, schemasToDelete, schemaIdToOldPeriod);
        return schemaIdToOldPeriod
                .fold(Optional::of, noop -> Optional.empty());
    }

    private Validation<ErrorResponse, Map<Long, SchemaPeriod>> revertAnySchemaPeriods(final Long importRequestId) {
        Optional<ServiceRequest> optionalImportRequest = serviceRequestRepository.findById(importRequestId);

        if (optionalImportRequest.isPresent()) {
            return updateOldSchemaPeriods(optionalImportRequest.get());
        }
        return Validation.invalid(ErrorResponse.valueOf(
                "Cannot find import request with id" + importRequestId,
                "MISSING_IMPORT_REQUEST"));
    }

    private Validation<ErrorResponse, Map<Long, SchemaPeriod>> updateOldSchemaPeriods(
            final ServiceRequest importRequest) {
        try {
            ProductImportContext productImportContext =
                    MAPPER.readValue(importRequest.getRequestMessage(), ProductImportContext.class);
            Map<Long, SchemaPeriod> existingSchemaIdToOldPeriod = productImportContext.getExistingSchemaIdToOldPeriod();

            for (Entry<Long, SchemaPeriod> existingSchemaIdToNewPeriod
                    : productImportContext.getExistingSchemaIdToOldPeriod().entrySet()) {
                Long existingSchemaId = existingSchemaIdToNewPeriod.getKey();
                SchemaPeriod oldSchemaPeriod = existingSchemaIdToNewPeriod.getValue();

                tableService.updatePeriod(existingSchemaId, oldSchemaPeriod);
            }

            return Validation.valid(existingSchemaIdToOldPeriod);
        } catch (IOException e) {
            log.error(e.getMessage(), e);

            return Validation.invalid(ROLLBACK_PRODUCT_ERROR);
        }
    }

    private static void logDeletedProductConfig(
            final ProductConfig productConfig,
            final List<Table> deletedTables,
            final Validation<ErrorResponse, Map<Long, SchemaPeriod>> schemaIdToOldPeriod) {
        String schemaPeriods = schemaIdToOldPeriod.getOrElse(emptyMap())
                .entrySet().stream()
                .map(entry -> entry.getKey() + COLON_SEP + entry.getValue())
                .collect(joining(SPACED_SEP));

        log.info("Reverted schema periods by ID [{}]", schemaPeriods);

        List<String> deletedSchemaNames =
                deletedTables.stream()
                        .map(Table::getVersionedName)
                        .collect(toList());
        log.info(
                "Removed product [{}] and associated schemas {}",
                productConfig.getVersionedName(),
                deletedSchemaNames);
    }

    private boolean isLinkedToOneProductOnly(final Long tableId) {
        return productConfigRepository.countByTablesId(tableId) <= 1;
    }

    private Validation<ErrorResponse, Identifiable> startRollbackJob(
            final String userName,
            final ProductConfig productConfig) {
        try {
            String requestName = ROLLBACK_PRODUCT.getDisplayName() + SPACE + productConfig.getVersionedName();

            ServiceRequest serviceRequest = serviceRequestRepository.save(
                    ServiceRequest.builder()
                            .name(requestName)
                            .serviceRequestType(ROLLBACK_PRODUCT)
                            .requestMessage(Objects.toString(productConfig.getImportRequestId()))
                            .createdBy(userName)
                            .build());
            jobOperator.startJob(ROLLBACK_PRODUCT.name(), serviceRequest, new Properties());

            return Validation.valid(productConfig);
        } catch (JobStartException e) {
            log.error(e.getMessage(), e);

            return Validation.invalid(ROLLBACK_PRODUCT_ERROR);
        }
    }
}
