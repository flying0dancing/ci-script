package com.lombardrisk.ignis.design.server.jpa;

import com.lombardrisk.ignis.design.field.model.DatasetSchemaField;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DatasetSchemaFieldJpaRepository extends JpaRepository<DatasetSchemaField, Long> {

    @SuppressWarnings("squid:S00107")
    @Modifying(flushAutomatically = true)
    @Query("update DatasetSchemaField"
            + " set type = :fieldType,"
            + "     name = :name,"
            + "     nullable = :nullable,"
            //decimalField
            + "     precision = :decimalPrecision,"
            + "     scale = :decimalScale,"
            //stringField
            + "     maxLength = :maxLength,"
            + "     minLength = :minLength,"
            + "     regularExpression = :regularExpression,"
            //date/timestampField
            + "     format = :dateFormat"
            + " where id = :id")
    void updateField(
            @Param("id") final Long id,
            @Param("fieldType") final String fieldType,
            @Param("name") final String name,
            @Param("nullable") final Boolean nullable,
            @Param("decimalPrecision") final Integer precision,
            @Param("decimalScale") final Integer scale,
            @Param("maxLength") final Integer maxLength,
            @Param("minLength") final Integer minLength,
            @Param("regularExpression") final String regularExpression,
            @Param("dateFormat") final String dateFormat);

    default void updateDecimalField(
            final Long id,
            final String name,
            final Boolean nullable,
            final Integer precision,
            final Integer scale) {
        updateField(id, "decimal", name, nullable, precision, scale, null, null, null, null);
    }

    default void updateStringField(
            final Long id,
            final String name,
            final Boolean nullable,
            final Integer maxLength,
            final Integer minLength,
            final String regularExpression) {
        updateField(id, "string", name, nullable, null, null, maxLength, minLength, regularExpression, null);
    }

    default void updateDateField(
            final Long id,
            final String name,
            final Boolean nullable,
            final String fieldType,
            final String dateFormat) {
        updateField(id, fieldType, name, nullable, null, null, null, null, null, dateFormat);
    }

    default void updateNumericField(
            final Long id,
            final String name,
            final Boolean nullable,
            final String fieldType) {
        updateField(id, fieldType, name, nullable, null, null, null, null, null, null);
    }
}
