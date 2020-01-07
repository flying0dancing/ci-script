package com.lombardrisk.ignis.design.field;

import com.lombardrisk.ignis.client.design.schema.field.FieldDto;
import com.lombardrisk.ignis.common.reservedWord.FieldValidator;
import com.lombardrisk.ignis.data.common.error.ErrorResponse;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.data.common.failure.NotFoundFailure;
import com.lombardrisk.ignis.design.field.api.DatasetSchemaFieldRepository;
import com.lombardrisk.ignis.design.field.api.FieldDependencyRepository;
import com.lombardrisk.ignis.design.field.api.FieldRepository;
import com.lombardrisk.ignis.design.field.model.BooleanField;
import com.lombardrisk.ignis.design.field.model.DateField;
import com.lombardrisk.ignis.design.field.model.DecimalField;
import com.lombardrisk.ignis.design.field.model.DoubleField;
import com.lombardrisk.ignis.design.field.model.Field;
import com.lombardrisk.ignis.design.field.model.FloatField;
import com.lombardrisk.ignis.design.field.model.IntField;
import com.lombardrisk.ignis.design.field.model.LongField;
import com.lombardrisk.ignis.design.field.model.StringField;
import com.lombardrisk.ignis.design.field.model.TimestampField;
import com.lombardrisk.ignis.design.field.request.FieldRequest;
import io.vavr.control.Either;
import io.vavr.control.Option;
import io.vavr.control.Validation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.apache.commons.collections.CollectionUtils.isEmpty;

@Service
@Transactional
@Slf4j
public class FieldService {

    private static final String FIELD_NOT_UNIQUE_ERROR_CODE = "Field is not unique";

    private final FieldRepository fieldRepository;
    private final DatasetSchemaFieldRepository updateFieldRepository;
    private final FieldDependencyRepository fieldDependencyRepository;
    private final FieldConverter fieldConverter = new FieldConverter();
    private final FieldValidator fieldValidator = new FieldValidator();

    public FieldService(
            final FieldRepository fieldRepository,
            final DatasetSchemaFieldRepository updateFieldRepository,
            final FieldDependencyRepository fieldDependencyRepository) {
        this.fieldRepository = fieldRepository;
        this.updateFieldRepository = updateFieldRepository;
        this.fieldDependencyRepository = fieldDependencyRepository;
    }

    public Validation<CRUDFailure, Field> findWithValidation(final long id) {
        return findById(id)
                .toValidation(notFound(id));
    }

    public Option<Field> findById(final long id) {
        return fieldRepository.findById(id);
    }

    public Option<FieldDto> findOne(final long id) {
        return fieldRepository.findById(id)
                .map(fieldConverter);
    }

    public List<Field> findAllByIds(final Iterable<Long> ids) {
        return fieldRepository.findAllById(ids);
    }

    public List<Field> findAll() {
        return fieldRepository.findAll();
    }

    @Transactional
    public Validation<CRUDFailure, Field> deleteWithValidation(final long id) {
        Validation<CRUDFailure, Field> fieldResult = findWithValidation(id);
        if (fieldResult.isInvalid()) {
            return fieldResult;
        }
        return Validation.valid(delete(fieldResult.get()));
    }

    @Transactional
    public Either<ErrorResponse, FieldDto> save(final Long schemaId, final FieldRequest fieldRequest) {
        Field newField = fieldRequest.convert();

        boolean schemaExists = fieldRepository.existsSchema(schemaId);
        if (!schemaExists) {
            return Either.left(
                    CRUDFailure.notFoundIds("Schema", schemaId).toErrorResponse());
        }

        Either<ErrorResponse, FieldDto> invalidNameCapture = checkFieldNameSyntax(fieldRequest.getName());
        if (invalidNameCapture != null) return invalidNameCapture;

        boolean fieldNameExists = fieldRepository.findByNameAndSchemaId(fieldRequest.getName(), schemaId).isPresent();
        if (fieldNameExists) {
            return Either.left(ErrorResponse.valueOf(
                    "A field with name " + fieldRequest.getName() + " already exists",
                    FIELD_NOT_UNIQUE_ERROR_CODE));
        }

        return Either.right(createField(newField, schemaId));
    }

    public Either<ErrorResponse, FieldDto> update(final Long fieldId, final FieldRequest fieldRequest) {

        Option<Field> fieldOptional = fieldRepository.findById(fieldId);
        if (!fieldOptional.isDefined()) {
            return Either.left(CRUDFailure.notFoundIds("Field", fieldId).toErrorResponse());
        }

        Either<ErrorResponse, FieldDto> invalidNameCapture = checkFieldNameSyntax(fieldRequest.getName());
        if (invalidNameCapture != null) return invalidNameCapture;

        Field existingField = fieldOptional.get();

        boolean otherFieldWithNameExists =
                fieldRepository.findByNameAndSchemaId(fieldRequest.getName(), existingField.getSchemaId())
                        .filter(field -> !field.getId().equals(fieldId))
                        .isPresent();

        if (otherFieldWithNameExists) {
            return Either.left(ErrorResponse.valueOf(
                    "A field with name " + fieldRequest.getName() + " already exists",
                    FIELD_NOT_UNIQUE_ERROR_CODE));
        }

        return Either.right(updateField(existingField, fieldRequest.convert(), existingField.getSchemaId()));
    }

    private Either<ErrorResponse, FieldDto> checkFieldNameSyntax(final String fieldName) {
        List<String> capturedReservedWords = fieldValidator.catchReservedWords(fieldName);
        if (!isEmpty(capturedReservedWords)) {
            return Either.left(
                    CRUDFailure.constraintFailure(capturedReservedWords.get(0).toUpperCase()
                            + " is a reserved word not allowed in a field name").toErrorResponse());
        }
        return null;
    }

    @Transactional
    public FieldDto createField(final Field newField, final Long schemaId) {
        Field savedField = createNewField(newField, schemaId);

        log.debug("Added field with id [{}] to schema with id [{}]", savedField.getId(), schemaId);
        return fieldConverter.apply(savedField);
    }

    public Field createNewField(final Field newField, final Long schemaId) {
        newField.setSchemaId(schemaId);
        return fieldRepository.save(newField);
    }

    private NotFoundFailure notFound(final long id) {
        return CRUDFailure.notFoundIds("Field", id);
    }

    private Field delete(final Field field) {
        fieldDependencyRepository.deleteForField(field);
        fieldRepository.delete(field);
        return field;
    }

    private static Optional<Field> findExistingField(final Long id, final Set<Field> existingFields) {
        if (id == null) {
            return Optional.empty();
        }

        return existingFields.stream()
                .filter(existingField -> existingField.getId().equals(id))
                .findFirst();
    }

    private FieldDto updateField(final Field existingField, final Field newField, final Long schemaId) {

        if (existingField.getClass().isAssignableFrom(newField.getClass())) {
            updateExistingFieldProperties(existingField, newField);
        } else {
            updateFieldType(newField);
        }

        Option<Field> optionalUpdatedField = fieldRepository.findById(newField.getId());

        Field updatedField = optionalUpdatedField.getOrElseThrow(
                () -> new IllegalStateException("Cannot find field with id " + newField.getId() + " after persist"));
        log.debug("Edited field with id [{}] for schema with id [{}]", updatedField.getId(), schemaId);
        return fieldConverter.apply(updatedField);
    }

    private static void updateExistingFieldProperties(final Field existingField, final Field updatedField) {
        existingField.setName(updatedField.getName());
        existingField.setNullable(updatedField.isNullable());

        if (existingField instanceof DecimalField) {
            updateDecimalField((DecimalField) existingField, (DecimalField) updatedField);
        } else if (existingField instanceof StringField) {
            updateStringField((StringField) existingField, (StringField) updatedField);
        } else if (existingField instanceof DateField) {
            updateDateField((DateField) existingField, (DateField) updatedField);
        } else if (existingField instanceof TimestampField) {
            updateTimestampField((TimestampField) existingField, (TimestampField) updatedField);
        }
    }

    private static void updateDecimalField(final DecimalField existingField, final DecimalField updatedField) {
        existingField.setPrecision(updatedField.getPrecision());
        existingField.setScale(updatedField.getScale());
    }

    private static void updateDateField(final DateField existingField, final DateField updatedField) {
        existingField.setFormat(updatedField.getFormat());
    }

    private static void updateTimestampField(final TimestampField existingField, final TimestampField updatedField) {
        existingField.setFormat(updatedField.getFormat());
    }

    private static void updateStringField(final StringField existingField, final StringField updatedField) {
        existingField.setMaxLength(updatedField.getMaxLength());
        existingField.setMinLength(updatedField.getMinLength());
        existingField.setRegularExpression(updatedField.getRegularExpression());
    }

    private void updateFieldType(final Field updatedField) {
        if (updatedField instanceof DecimalField) {
            updateDecimalField((DecimalField) updatedField);
        } else if (updatedField instanceof DateField) {
            updateDateField((DateField) updatedField);
        } else if (updatedField instanceof TimestampField) {
            updateTimestampField((TimestampField) updatedField);
        } else if (updatedField instanceof StringField) {
            updateStringField((StringField) updatedField);
        } else if (updatedField instanceof IntField) {
            updateFieldRepository.updateNumericField(
                    updatedField.getId(), updatedField.getName(), updatedField.isNullable(), "integer");
        } else if (updatedField instanceof LongField) {
            updateFieldRepository.updateNumericField(
                    updatedField.getId(), updatedField.getName(), updatedField.isNullable(), "long");
        } else if (updatedField instanceof FloatField) {
            updateFieldRepository.updateNumericField(
                    updatedField.getId(), updatedField.getName(), updatedField.isNullable(), "float");
        } else if (updatedField instanceof DoubleField) {
            updateFieldRepository.updateNumericField(
                    updatedField.getId(), updatedField.getName(), updatedField.isNullable(), "double");
        } else if (updatedField instanceof BooleanField) {
            updateFieldRepository.updateNumericField(
                    updatedField.getId(), updatedField.getName(), updatedField.isNullable(), "boolean");
        }
    }

    private void updateDecimalField(final DecimalField decimalField) {
        updateFieldRepository.updateDecimalField(
                decimalField.getId(),
                decimalField.getName(),
                decimalField.isNullable(),
                decimalField.getPrecision(),
                decimalField.getScale());
    }

    private void updateDateField(final DateField dateField) {
        updateFieldRepository.updateDateField(
                dateField.getId(),
                dateField.getName(),
                dateField.isNullable(),
                "date",
                dateField.getFormat());
    }

    private void updateTimestampField(final TimestampField dateField) {
        updateFieldRepository.updateDateField(
                dateField.getId(),
                dateField.getName(),
                dateField.isNullable(),
                "timestamp",
                dateField.getFormat());
    }

    private void updateStringField(final StringField stringField) {
        updateFieldRepository.updateStringField(
                stringField.getId(),
                stringField.getName(),
                stringField.isNullable(),
                stringField.getMaxLength(),
                stringField.getMinLength(),
                stringField.getRegularExpression());
    }
}
