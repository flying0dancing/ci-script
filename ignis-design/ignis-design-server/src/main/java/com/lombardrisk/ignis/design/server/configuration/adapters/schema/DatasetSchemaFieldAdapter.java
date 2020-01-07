package com.lombardrisk.ignis.design.server.configuration.adapters.schema;

import com.lombardrisk.ignis.design.field.api.DatasetSchemaFieldRepository;
import com.lombardrisk.ignis.design.server.jpa.DatasetSchemaFieldJpaRepository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Transactional(propagation = Propagation.MANDATORY)
public class DatasetSchemaFieldAdapter implements DatasetSchemaFieldRepository {

    private final DatasetSchemaFieldJpaRepository datasetSchemaFieldRepository;

    public DatasetSchemaFieldAdapter(final DatasetSchemaFieldJpaRepository datasetSchemaFieldRepository) {
        this.datasetSchemaFieldRepository = datasetSchemaFieldRepository;
    }

    @Override
    public void updateDecimalField(
            final Long id, final String name, final Boolean nullable, final Integer precision, final Integer scale) {
        datasetSchemaFieldRepository.updateDecimalField(id, name, nullable, precision, scale);
    }

    @Override
    public void updateStringField(
            final Long id,
            final String name,
            final Boolean nullable,
            final Integer maxLength,
            final Integer minLength,
            final String regularExpression) {
        datasetSchemaFieldRepository.updateStringField(id, name, nullable, maxLength, minLength, regularExpression);
    }

    @Override
    public void updateDateField(
            final Long id, final String name, final Boolean nullable, final String fieldType, final String dateFormat) {
        datasetSchemaFieldRepository.updateDateField(id, name, nullable, fieldType, dateFormat);
    }

    @Override
    public void updateNumericField(
            final Long id, final String name, final Boolean nullable, final String fieldType) {
        datasetSchemaFieldRepository.updateNumericField(id, name, nullable, fieldType);
    }
}
