package com.lombardrisk.ignis.design.field.api;

import com.lombardrisk.ignis.design.field.model.Field;
import io.vavr.control.Option;

import java.util.List;
import java.util.Optional;

public interface FieldRepository {

    Option<Field> findById(long id);

    List<Field> findAllById(Iterable<Long> ids);

    void delete(Field field);

    <T extends Field> T save(T newField);

    boolean existsSchema(Long schemaId);

    Optional<Field> findByNameAndSchemaId(String name, Long schemaId);

    List<Field> findAll();
}
