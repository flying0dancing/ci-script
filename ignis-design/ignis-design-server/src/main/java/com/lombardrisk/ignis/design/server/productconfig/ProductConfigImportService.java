package com.lombardrisk.ignis.design.server.productconfig;

import com.lombardrisk.ignis.client.design.pipeline.CreatePipelineRequest;
import com.lombardrisk.ignis.client.design.pipeline.PipelineStepRequest;
import com.lombardrisk.ignis.client.design.pipeline.PipelineView;
import com.lombardrisk.ignis.client.design.productconfig.NewProductConfigRequest;
import com.lombardrisk.ignis.client.external.pipeline.export.PipelineExport;
import com.lombardrisk.ignis.data.common.Identifiable;
import com.lombardrisk.ignis.data.common.error.ErrorResponse;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.design.field.FieldService;
import com.lombardrisk.ignis.design.field.model.Field;
import com.lombardrisk.ignis.design.server.pipeline.PipelineService;
import com.lombardrisk.ignis.design.server.pipeline.PipelineStepService;
import com.lombardrisk.ignis.design.server.productconfig.api.ProductConfigRepository;
import com.lombardrisk.ignis.design.server.productconfig.export.ImportProductRequest;
import com.lombardrisk.ignis.design.server.productconfig.export.ProductConfigImportFileService;
import com.lombardrisk.ignis.design.server.productconfig.export.converter.PipelineExportToPipelineConverter;
import com.lombardrisk.ignis.design.server.productconfig.export.converter.PipelineStepExportToRequestConverter;
import com.lombardrisk.ignis.design.server.productconfig.export.converter.SchemaExportConverter;
import com.lombardrisk.ignis.design.server.productconfig.export.validator.ProductImportValidator;
import com.lombardrisk.ignis.design.server.productconfig.rule.model.ValidationRule;
import com.lombardrisk.ignis.design.server.productconfig.schema.RuleService;
import com.lombardrisk.ignis.design.server.productconfig.schema.Schema;
import com.lombardrisk.ignis.design.server.productconfig.schema.SchemaService;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.control.Option;
import io.vavr.control.Validation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.lombardrisk.ignis.common.MapperUtils.mapCollection;
import static com.lombardrisk.ignis.common.MapperUtils.mapCollectionOrEmpty;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@Slf4j
public class ProductConfigImportService {

    private final ProductConfigImportFileService productConfigImportFileService;
    private final ProductImportValidator productImportValidator;
    private final SchemaExportConverter schemaExportConverter;
    private final ProductConfigService productConfigService;
    private final SchemaService schemaService;
    private final FieldService fieldService;
    private final RuleService ruleService;
    private final PipelineService pipelineService;
    private final PipelineStepService pipelineStepService;
    private final ProductConfigRepository productConfigRepository;

    public ProductConfigImportService(
            final ProductConfigImportFileService productConfigImportFileService,
            final ProductImportValidator productImportValidator,
            final SchemaExportConverter schemaExportConverter,
            final ProductConfigService productConfigService,
            final SchemaService schemaService,
            final FieldService fieldService,
            final RuleService ruleService,
            final PipelineService pipelineService,
            final PipelineStepService pipelineStepService,
            final ProductConfigRepository productConfigRepository) {
        this.productConfigImportFileService = productConfigImportFileService;
        this.productImportValidator = productImportValidator;
        this.schemaExportConverter = schemaExportConverter;
        this.productConfigService = productConfigService;
        this.schemaService = schemaService;
        this.fieldService = fieldService;
        this.ruleService = ruleService;
        this.pipelineService = pipelineService;
        this.pipelineStepService = pipelineStepService;
        this.productConfigRepository = productConfigRepository;
    }

    @Transactional
    public Validation<List<ErrorResponse>, Identifiable> importProductConfig(
            final String productFileName, final InputStream fileStream) throws IOException {
        log.info("Upload product config file [{}]", productFileName);

        Validation<ErrorResponse, ImportProductRequest> productConfigView = productConfigImportFileService
                .readProductConfig(productFileName, fileStream);

        if (productConfigView.isInvalid()) {
            return Validation.invalid(singletonList(productConfigView.getError()));
        }

        ImportProductRequest result = productConfigView.get();
        List<ErrorResponse> failures = productImportValidator.validate(result)
                .stream()
                .map(CRUDFailure::toErrorResponse)
                .collect(toList());

        if (!failures.isEmpty()) {
            return Validation.invalid(failures);
        }

        Set<Schema> schemas = mapCollection(
                result.getSchemaExports(), schemaExportConverter.inverse(), LinkedHashSet::new);

        Validation<List<ErrorResponse>, ProductConfig> productConfig =
                importProductConfig(result.getNewProductConfigRequest(), schemas);

        if (productConfig.isValid()) {
            createNewPipelines(result.getPipelineExports(), productConfig.get());
        }

        return productConfig.map(Identifiable::toIdentifiable);
    }

    private Validation<List<ErrorResponse>, ProductConfig> importProductConfig(
            final NewProductConfigRequest productConfig,
            final Set<Schema> schemasToImport) {

        Map<String, List<Schema>> schemasByDisplayName = schemasToImport.stream()
                .collect(groupingBy(Schema::getDisplayName));

        List<ErrorResponse> schemaImportErrors = prepareSchemasForImport(schemasByDisplayName)
                .stream()
                .map(CRUDFailure::toErrorResponse)
                .collect(toList());

        if (!schemaImportErrors.isEmpty()) {
            return Validation.invalid(schemaImportErrors);
        }

        ProductConfig newProduct = productConfigService.saveProductConfig(productConfig);
        List<Tuple2<Schema, Set<ValidationRule>>> result = saveSchemasWithoutRules(schemasToImport, newProduct);

        List<ErrorResponse> errors = new ArrayList<>();
        for (Tuple2<Schema, Set<ValidationRule>> schemaAndRules : result) {
            List<ErrorResponse> ruleErrors = saveRules(schemaAndRules._2(), schemaAndRules._1());
            errors.addAll(ruleErrors);
        }

        if (!errors.isEmpty()) {
            updateImportStatusError(newProduct);
            return Validation.invalid(errors);
        }

        updateImportStatusSuccess(newProduct);
        return Validation.valid(newProduct);
    }

    private void updateImportStatusError(final ProductConfig productConfig) {
        productConfig.setImportStatus(ProductConfig.ImportStatus.ERROR);
        productConfigRepository.save(productConfig);
    }

    private void updateImportStatusSuccess(final ProductConfig productConfig) {
        productConfig.setImportStatus(ProductConfig.ImportStatus.SUCCESS);
        productConfigRepository.save(productConfig);
    }

    private List<CRUDFailure> prepareSchemasForImport(final Map<String, List<Schema>> schemasByDisplayName) {

        List<CRUDFailure> schemaImportErrors = new ArrayList<>();

        for (Map.Entry<String, List<Schema>> entry : schemasByDisplayName.entrySet()) {
            String displayName = entry.getKey();
            List<Schema> schemasForDisplayName = entry.getValue();

            Option<CRUDFailure> validationFailure = validateDuplicates(displayName, schemasForDisplayName);
            if (validationFailure.isDefined()) {
                schemaImportErrors.add(validationFailure.get());
            } else {
                setLatestSchemaVersionToLatest(displayName, schemasForDisplayName);
            }
        }
        return schemaImportErrors;
    }

    private List<Tuple2<Schema, Set<ValidationRule>>> saveSchemasWithoutRules(
            final Set<Schema> schemas, final ProductConfig newProduct) {

        List<Schema> createdSchemas = new ArrayList<>();
        List<Tuple2<Schema, Set<ValidationRule>>> result = new ArrayList<>();

        for (Schema schema : schemas) {
            Set<ValidationRule> rulesToBeSaved = Option.of(schema.getValidationRules())
                    .getOrElse(HashSet::new);
            schema.setProductId(newProduct.getId());
            schema.setValidationRules(new HashSet<>());

            Schema createSchemaResult = schemaService.createNew(schema);

            for (Field field : schema.getFields()) {
                fieldService.createField(field, createSchemaResult.getId());
            }

            productConfigRepository.save(newProduct);
            createdSchemas.add(createSchemaResult);
            result.add(Tuple.of(createSchemaResult, rulesToBeSaved));
        }

        if (newProduct.getTables() == null) {
            newProduct.setTables(new HashSet<>());
        }

        newProduct.getTables().addAll(createdSchemas);

        return result;
    }

    private List<ErrorResponse> saveRules(final Set<ValidationRule> validationRules, final Schema schema) {
        List<ErrorResponse> errorResponses = new ArrayList<>();

        for (ValidationRule validationRule : validationRules) {
            ruleService.saveValidationRule(schema.getId(), validationRule)
                    .peekLeft(errorResponses::addAll);
        }

        return errorResponses;
    }

    private void setLatestSchemaVersionToLatest(final String displayName, final List<Schema> schemas) {
        Optional<Schema> findLatestVersion = schemas.stream()
                .max(Comparator.comparing(Schema::getMajorVersion));

        if (!findLatestVersion.isPresent()) {
            throw new IllegalStateException("Schemas for display name " + displayName + " have no max version");
        }

        Schema latestVersion = findLatestVersion.get();
        latestVersion.setLatest(true);
        schemas.stream()
                .filter(schema -> !schema.equals(latestVersion))
                .forEach(schema -> schema.setLatest(false));
    }

    private Option<CRUDFailure> validateDuplicates(
            final String displayName, final List<Schema> schemasByDisplayName) {
        Map<Integer, List<Schema>> versionMap = schemasByDisplayName.stream()
                .collect(groupingBy(Schema::getMajorVersion));

        List<Integer> containsDuplicates = versionMap.entrySet()
                .stream()
                .filter(entry -> entry.getValue().size() > 1)
                .map(Map.Entry::getKey)
                .collect(toList());

        if (containsDuplicates.size() > 0) {
            return Option.of(CRUDFailure.constraintFailure(
                    "Schema " + displayName + " has duplicates for versions " + containsDuplicates));
        }

        return Option.none();
    }

    private void createNewPipelines(
            final List<PipelineExport> pipelineExports, final ProductConfig productConfig) {

        PipelineExportToPipelineConverter pipelineExportConverter = new PipelineExportToPipelineConverter();
        PipelineStepExportToRequestConverter pipelineStepConverter = new PipelineStepExportToRequestConverter();

        for (PipelineExport pipelineExport : pipelineExports) {
            CreatePipelineRequest pipelineRequest = pipelineExportConverter.apply(pipelineExport, productConfig);
            LinkedHashSet<PipelineStepRequest> pipelineStepRequests = mapCollectionOrEmpty(
                    pipelineExport.getSteps(),
                    stepExport -> pipelineStepConverter.apply(stepExport, productConfig),
                    LinkedHashSet::new);

            Validation<ErrorResponse, PipelineView> createdPipeline = pipelineService.saveNewPipeline(pipelineRequest);
            if (createdPipeline.isValid()) {
                pipelineStepService.importPipelineSteps(createdPipeline.get().getId(), pipelineStepRequests);
            }
        }
    }
}
