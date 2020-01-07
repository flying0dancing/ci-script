package com.lombardrisk.ignis.spark.staging.execution;

import com.lombardrisk.ignis.spark.api.staging.field.FieldValidation;
import com.lombardrisk.ignis.spark.api.staging.StagingSchemaValidation;
import com.lombardrisk.ignis.spark.staging.datafields.DataRow;
import com.lombardrisk.ignis.spark.staging.datafields.IndexedDataField;
import io.vavr.control.Validation;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.lombardrisk.ignis.common.MapperUtils.mapListToIndexedMap;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Slf4j
@Getter
public class DatasetSchemaValidator implements org.apache.spark.api.java.function.Function<DataRow, Optional<String>> {

    private static final long serialVersionUID = 212957252754688838L;

    private final StagingSchemaValidation stagingSchemaValidation;
    private final Map<Integer, FieldValidation> indexedFields;

    DatasetSchemaValidator(final StagingSchemaValidation stagingSchemaValidation) {
        this.stagingSchemaValidation = stagingSchemaValidation;
        this.indexedFields = mapListToIndexedMap(new ArrayList<>(stagingSchemaValidation.getFields()));
    }

    @Override
    public Optional<String> call(final DataRow dataRow) {
        Validation<String, DataRow> result = validateRow(dataRow)
                .flatMap(this::validateFields)
                .mapError(error -> buildValidationMessage(dataRow, error));

        if (result.isInvalid()) {
            return Optional.of(result.getError());
        }

        return Optional.empty();
    }

    Object parseField(final IndexedDataField dataField) {
        return findFieldInfo(dataField)
                .map(field -> field.parse(dataField.getValue()))
                .orElse(null);
    }

    private Validation<String, DataRow> validateRow(final DataRow row) {
        int dataFieldCount = row.size();
        int schemaFieldCount = stagingSchemaValidation.getFields().size();

        if (dataFieldCount != schemaFieldCount) {
            return Validation.invalid(
                    String.format("Expected %d fields - actual: %d", schemaFieldCount, dataFieldCount));
        }

        return Validation.valid(row);
    }

    private Validation<String, DataRow> validateFields(final DataRow row) {
        List<String> validationFailures = row.getFields().stream()
                .map(this::validateField)
                .filter(Validation::isInvalid)
                .map(Validation::getError)
                .collect(toList());

        if (validationFailures.isEmpty()) {
            return Validation.valid(row);
        }

        return Validation.invalid(String.join(";", validationFailures));
    }

    private Validation<String, IndexedDataField> validateField(final IndexedDataField dataField) {
        Validation<String, FieldValidation<?>> result = findField(dataField);

        if (result.isValid()) {
            return result.get()
                    .validate(dataField.getValue())
                    .map(string -> dataField);
        }

        return result.map(field -> null);
    }

    private Validation<String, FieldValidation<?>> findField(final IndexedDataField dataField) {
        int index = dataField.getIndex();

        if (!indexedFields.containsKey(index)) {
            return Validation.invalid("The field with index " + index + " is undefined, please check schema.");
        }

        FieldValidation<?> field = indexedFields.get(index);
        return Validation.valid(field);
    }

    private Optional<FieldValidation<?>> findFieldInfo(final IndexedDataField dataField) {
        FieldValidation<?> value = indexedFields.get(dataField.getIndex());
        return Optional.ofNullable(value);
    }

    private static String buildValidationMessage(final DataRow dataRow, final String validationError) {
        String row = dataRow.getFields().stream()
                .map(DatasetSchemaValidator::ifNullMapToNullString)
                .collect(joining(","));

        String error = isBlank(validationError) ? EMPTY : validationError;
        return row + "," + error;
    }

    private static String ifNullMapToNullString(final IndexedDataField field) {
        return isBlank(field.getValue()) ? "<NULL>" : field.getValue();
    }
}