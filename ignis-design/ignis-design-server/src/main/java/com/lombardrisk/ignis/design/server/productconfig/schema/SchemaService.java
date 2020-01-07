package com.lombardrisk.ignis.design.server.productconfig.schema;

import com.lombardrisk.ignis.client.design.schema.CopySchemaRequest;
import com.lombardrisk.ignis.client.design.schema.NewSchemaVersionRequest;
import com.lombardrisk.ignis.client.design.schema.UpdateSchema;
import com.lombardrisk.ignis.data.common.Identifiable;
import com.lombardrisk.ignis.data.common.error.ErrorResponse;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.data.common.service.CRUDService;
import com.lombardrisk.ignis.design.field.FieldService;
import com.lombardrisk.ignis.design.field.model.Field;
import com.lombardrisk.ignis.design.server.productconfig.ProductConfig;
import com.lombardrisk.ignis.design.server.productconfig.api.SchemaRepository;
import com.lombardrisk.ignis.design.server.productconfig.rule.model.ValidationRule;
import com.lombardrisk.ignis.design.server.productconfig.rule.model.ValidationRuleExample;
import com.lombardrisk.ignis.design.server.productconfig.schema.model.SchemaConstraints;
import com.lombardrisk.ignis.design.server.productconfig.schema.validation.CreateNewVersionValidator;
import com.lombardrisk.ignis.design.server.productconfig.schema.validation.SchemaConstraintsValidator;
import com.lombardrisk.ignis.design.server.productconfig.schema.validation.UpdateValidator;
import io.vavr.control.Option;
import io.vavr.control.Validation;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.lombardrisk.ignis.common.MapperUtils.mapSet;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toSet;

@Slf4j
public class SchemaService implements CRUDService<Schema> {

    private final SchemaRepository schemaRepository;
    private final FieldService fieldService;
    private final UpdateValidator updateValidator;
    private final SchemaConstraintsValidator createValidator;
    private final CreateNewVersionValidator createNewVersionValidator = new CreateNewVersionValidator();

    public SchemaService(
            final SchemaRepository schemaRepository,
            final FieldService fieldService, final UpdateValidator updateValidator,
            final SchemaConstraintsValidator createValidator) {
        this.schemaRepository = schemaRepository;
        this.fieldService = fieldService;
        this.updateValidator = updateValidator;
        this.createValidator = createValidator;
    }

    @Override
    public String entityName() {
        return Schema.class.getSimpleName();
    }

    @Override
    public Option<Schema> findById(final long id) {
        return schemaRepository.findById(id);
    }

    @Override
    public List<Schema> findAllByIds(final Iterable<Long> ids) {
        return schemaRepository.findAllByIds(ids);
    }

    @Override
    public List<Schema> findAll() {
        return schemaRepository.findAll();
    }

    public Validation<CRUDFailure, Schema> findByProductIdAndId(final long productId, final long schemaId) {
        return findWithValidation(schemaId);
    }

    @Transactional
    public Validation<CRUDFailure, Identifiable> deleteByProductIdAndId(final long productId, final long schemaId) {
        Validation<CRUDFailure, Schema> schemaSearch = findWithValidation(schemaId);
        if (schemaSearch.isInvalid()) {
            return Validation.invalid(schemaSearch.getError());
        }
        Schema schemaToRemove = schemaSearch.get();

        Optional<Schema> findSecondVersion = schemaRepository.findSecondLatestVersion(schemaToRemove.getDisplayName());
        if (schemaToRemove.getLatest() && findSecondVersion.isPresent()) {
            Schema secondLatestVersion = findSecondVersion.get();
            secondLatestVersion.setLatest(true);
            schemaRepository.saveSchema(secondLatestVersion);
        }

        delete(schemaToRemove);

        return schemaSearch.map(Identifiable::toIdentifiable);
    }

    @Transactional
    public Validation<List<CRUDFailure>, Schema> validateAndCreateNew(final Schema request) {
        List<CRUDFailure> validationFailures = createValidator.validateCreate(request.getSchemaConstraints());

        if (!validationFailures.isEmpty()) {
            return Validation.invalid(validationFailures);
        }

        Schema created = createNew(request);
        return Validation.valid(created);
    }

    @Transactional
    public Schema createNew(final Schema request) {
        Schema schema = new Schema();
        schema.setProductId(request.getProductId());
        schema.setPhysicalTableName(request.getPhysicalTableName());
        schema.setDisplayName(request.getDisplayName());
        schema.setStartDate(request.getStartDate());
        schema.setEndDate(request.getEndDate());
        schema.setLatest(Option.of(request.getLatest()).getOrElse(true));
        schema.setMajorVersion(Option.of(request.getMajorVersion()).getOrElse(1));
        schema.setCreatedTime(new Date());
        schema.setCreatedBy(SecurityContextHolder.getContext().getAuthentication().getName());
        schema.setFields(request.getFields());

        return schemaRepository.saveSchema(schema);
    }

    public <T extends OutputStream> Validation<ErrorResponse, SchemaCsvOutputStream<T>> createExampleCsv(
            final long schemaId, final T outputStream) {

        return findWithValidation(schemaId)
                .mapError(CRUDFailure::toErrorResponse)
                .flatMap(schema -> outputSchema(outputStream, schema));
    }

    private <T extends OutputStream>  Validation<ErrorResponse, SchemaCsvOutputStream<T>> outputSchema(
            final T outputStream,
            final Schema schema) {

        String csvHeader = schema.getFields().stream()
                .map(Field::getName)
                .collect(Collectors.joining(","));
        try {
            outputStream.write(csvHeader.getBytes(Charset.forName("UTF-8")));
            return Validation.valid(
                    new SchemaCsvOutputStream<>(schema, outputStream));
        } catch (IOException e) {
            log.error("Could not write csv header to output stream", e);
            return Validation.invalid(ErrorResponse.valueOf(
                    "Could not write csv header to output stream", "FAILED_WRITE"));
        }
    }

    @Transactional
    public Validation<CRUDFailure, Schema> updateWithoutFields(
            final long productId, final long schemaId, final UpdateSchema updateSchemaRequest) {

        Validation<CRUDFailure, Schema> schemaValidation = findByProductIdAndId(productId, schemaId);
        if (schemaValidation.isInvalid()) {
            return schemaValidation;
        }

        Schema schema = schemaValidation.get();

        if (schema.getLatest().equals(false) && updateSchemaRequest.getEndDate() == null) {
            return Validation.invalid(CRUDFailure.invalidRequestParameter(
                    "endDate",
                    "End date cannot be blank if schema is not the latest version"));
        }

        Validation<CRUDFailure, Schema> validationResult = validateUpdate(updateSchemaRequest, schema);
        if (validationResult.isInvalid()) {
            return validationResult;
        }


        schema.setDisplayName(updateSchemaRequest.getDisplayName());
        schema.setPhysicalTableName(updateSchemaRequest.getPhysicalTableName());
        schema.setStartDate(updateSchemaRequest.getStartDate());
        schema.setEndDate(updateSchemaRequest.getEndDate());

        Schema updatedSchema = schemaRepository.saveSchema(schema);
        return Validation.valid(updatedSchema);
    }

    public Validation<CRUDFailure, Schema> validateUpdate(final UpdateSchema updateSchemaRequest, final Schema schema) {
        Schema schemaBeforeChanges = schema.copy();
        Schema schemaAfterChanges = schema.copy();

        schemaAfterChanges.setDisplayName(updateSchemaRequest.getDisplayName());
        schemaAfterChanges.setPhysicalTableName(updateSchemaRequest.getPhysicalTableName());
        schemaAfterChanges.setStartDate(updateSchemaRequest.getStartDate());
        schemaAfterChanges.setEndDate(updateSchemaRequest.getEndDate());

        return updateValidator.validateUpdateRequest(schemaBeforeChanges, schemaAfterChanges);
    }

    @Transactional
    public Validation<CRUDFailure, Schema> createNextVersion(
            final ProductConfig productConfig,
            final Long schemaId,
            final NewSchemaVersionRequest newSchemaVersionRequest) {
        Optional<Schema> findSchemaResult = schemaRepository.findByIdAndProductId(productConfig.getId(), schemaId);
        if (!findSchemaResult.isPresent()) {
            return Validation.invalid(notFound(schemaId));
        }

        Schema existingSchemaToCopy = findSchemaResult.get();
        Schema maxVersion = schemaRepository.findMaxVersion(existingSchemaToCopy.getPhysicalTableName())
                .orElse(existingSchemaToCopy);

        Option<CRUDFailure> invalidRequest = createNewVersionValidator
                .validateNewVersion(maxVersion, newSchemaVersionRequest);

        if (invalidRequest.isDefined()) {
            return invalidRequest.toInvalid(() -> null);
        }

        Schema nextVersion = createNewVersion(
                newSchemaVersionRequest,
                existingSchemaToCopy,
                maxVersion.getMajorVersion() + 1);

        Schema nextVersionWithRules = copyOverRulesToNewVersion(existingSchemaToCopy, nextVersion);
        schemaRepository.saveSchema(nextVersionWithRules);

        updatePreviousVersion(newSchemaVersionRequest, maxVersion);

        return Validation.valid(nextVersionWithRules);
    }

    @Transactional
    public Validation<List<CRUDFailure>, Schema> copySchema(
            final Long productConfigId,
            final Long schemaId,
            final CopySchemaRequest copySchemaRequest) {

        Optional<Schema> findSchemaResult = schemaRepository.findByIdAndProductId(productConfigId, schemaId);
        if (!findSchemaResult.isPresent()) {
            return Validation.invalid(singletonList(notFound(schemaId)));
        }

        Schema existingSchemaToCopy = findSchemaResult.get();

        List<CRUDFailure> invalidRequest = createValidator.validateCreate(SchemaConstraints.builder()
                .displayName(copySchemaRequest.getDisplayName())
                .physicalTableName(copySchemaRequest.getPhysicalTableName())
                .startDate(copySchemaRequest.getStartDate())
                .endDate(copySchemaRequest.getEndDate())
                .majorVersion(1)
                .build());

        if (!invalidRequest.isEmpty()) {
            return Validation.invalid(invalidRequest);
        }

        Schema copiedSchema = createCopiedSchema(copySchemaRequest, existingSchemaToCopy);

        copiedSchema = copyOverRulesToNewVersion(existingSchemaToCopy, copiedSchema);
        copiedSchema = schemaRepository.saveSchema(copiedSchema);

        return Validation.valid(copiedSchema);
    }

    private Schema createNewVersion(
            final NewSchemaVersionRequest newSchemaVersionRequest,
            final Schema existingSchema,
            final int newVersionNumber) {

        Schema newVersion = existingSchema.copy();
        Set<Field> fields = newVersion.getFields();

        newVersion.setFields(new HashSet<>());
        newVersion.setProductId(existingSchema.getProductId());
        newVersion.setMajorVersion(newVersionNumber);
        newVersion.setLatest(true);
        newVersion.setStartDate(newSchemaVersionRequest.getStartDate());
        newVersion.setEndDate(null);
        newVersion.setCreatedTime(new Date());
        newVersion.setCreatedBy(SecurityContextHolder.getContext().getAuthentication().getName());

        Schema schema = schemaRepository.saveSchema(newVersion);
        for (Field field : fields) {
            schema.getFields().add(
                    fieldService.createNewField(field, schema.getId()));
        }

        return schema;
    }

    private Schema createCopiedSchema(
            final CopySchemaRequest copySchemaRequest,
            final Schema existingSchema) {

        Schema newVersion = existingSchema.copy();
        Set<Field> fields = newVersion.getFields();

        newVersion.setFields(new HashSet<>());
        newVersion.setDisplayName(copySchemaRequest.getDisplayName());
        newVersion.setPhysicalTableName(copySchemaRequest.getPhysicalTableName());
        newVersion.setProductId(existingSchema.getProductId());
        newVersion.setMajorVersion(1);
        newVersion.setLatest(true);
        newVersion.setStartDate(copySchemaRequest.getStartDate());
        newVersion.setEndDate(copySchemaRequest.getEndDate());
        newVersion.setCreatedTime(new Date());
        newVersion.setCreatedBy(SecurityContextHolder.getContext().getAuthentication().getName());

        Schema schema = schemaRepository.saveSchema(newVersion);
        for (Field field : fields) {
            schema.getFields().add(
                    fieldService.createNewField(field, schema.getId()));
        }

        return schema;
    }

    private Schema copyOverRulesToNewVersion(final Schema existingSchema, final Schema nextVersion) {
        Map<String, Field> fieldMap = nextVersion.getFields().stream()
                .collect(Collectors.toMap(Field::getName, Function.identity()));

        Set<ValidationRule> rules = existingSchema.getValidationRules()
                .stream()
                .filter(validationRule -> validationRule.getEndDate()
                        .isAfter(nextVersion.getStartDate()))
                .map(validationRule -> createNewRule(fieldMap, validationRule))
                .collect(Collectors.toSet());

        nextVersion.setValidationRules(rules);

        return nextVersion;
    }

    private ValidationRule createNewRule(final Map<String, Field> fieldMap, final ValidationRule validationRule) {
        return ValidationRule.builder()
                .ruleId(validationRule.getRuleId())
                .name(validationRule.getName())
                .version(validationRule.getVersion())
                .expression(validationRule.getExpression())
                .description(validationRule.getDescription())
                .startDate(validationRule.getStartDate())
                .endDate(validationRule.getEndDate())
                .validationRuleType(validationRule.getValidationRuleType())
                .validationRuleSeverity(validationRule.getValidationRuleSeverity())
                .validationRuleExamples(mapSet(validationRule.getValidationRuleExamples(), ValidationRuleExample::copy))
                .contextFields(validationRule.getContextFields().stream()
                        .map(field -> fieldMap.get(field.getName()))
                        .collect(toSet()))
                .build();
    }

    private void updatePreviousVersion(
            final NewSchemaVersionRequest newSchemaVersionRequest,
            final Schema existingSchema) {
        existingSchema.setLatest(false);
        existingSchema.setEndDate(newSchemaVersionRequest.getStartDate().minusDays(1));
        schemaRepository.saveSchema(existingSchema);
    }

    @Override
    public Schema delete(final Schema schema) {
        return schemaRepository.deleteSchema(schema);
    }


    @Data
    public static final class SchemaCsvOutputStream<T extends OutputStream> {
        private final String filename;
        private final T outputStream;

        public SchemaCsvOutputStream(final Schema schema, final T outputStream) {
            this.filename = schema.getDisplayName() + "-example.csv";
            this.outputStream = outputStream;
        }
    }
}
