package com.lombardrisk.ignis.design.server.configuration.adapters.schema;

import com.lombardrisk.ignis.design.field.api.FieldRepository;
import com.lombardrisk.ignis.design.field.model.Field;
import com.lombardrisk.ignis.design.server.jpa.FieldJpaRepository;
import com.lombardrisk.ignis.design.server.jpa.SchemaJpaRepository;
import io.vavr.control.Option;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public class FieldAdapter implements FieldRepository {

    private final FieldJpaRepository fieldRepository;
    private final SchemaJpaRepository schemaJpaRepository;

    public FieldAdapter(
            final FieldJpaRepository fieldRepository,
            final SchemaJpaRepository schemaJpaRepository) {
        this.fieldRepository = fieldRepository;
        this.schemaJpaRepository = schemaJpaRepository;
    }

    @Override
    public Option<Field> findById(final long id) {
        return Option.ofOptional(fieldRepository.findById(id));
    }

    @Override
    public List<Field> findAllById(final Iterable<Long> ids) {
        return fieldRepository.findAllById(ids);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void delete(final Field field) {
        fieldRepository.delete(field);
    }

    @Override
    public <T extends Field> T save(final T newField) {
        return fieldRepository.save(newField);
    }

    @Override
    public boolean existsSchema(final Long schemaId) {
        return schemaJpaRepository.existsById(schemaId);
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public Optional<Field> findByNameAndSchemaId(final String name, final Long schemaId) {
        return fieldRepository.findByNameAndSchemaId(name, schemaId);
    }

    @Override
    public List<Field> findAll() {
        return fieldRepository.findAll();
    }
}
