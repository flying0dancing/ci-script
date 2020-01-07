package com.lombardrisk.ignis.design.field.api;

public interface DatasetSchemaFieldRepository {
    void updateDecimalField(
            final Long id,
            final String name,
            final Boolean nullable,
            final Integer precision,
            final Integer scale);

    void updateStringField(
            final Long id,
            final String name,
            final Boolean nullable,
            final Integer maxLength,
            final Integer minLength,
            final String regularExpression);

    void updateDateField(
            final Long id,
            final String name,
            final Boolean nullable,
            final String fieldType,
            final String dateFormat);

    void updateNumericField(
            final Long id,
            final String name,
            final Boolean nullable,
            final String fieldType);
}
