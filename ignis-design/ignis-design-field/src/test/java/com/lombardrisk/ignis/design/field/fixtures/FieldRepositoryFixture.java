package com.lombardrisk.ignis.design.field.fixtures;

import com.lombardrisk.ignis.data.common.fixtures.InMemoryRepository;
import com.lombardrisk.ignis.design.field.api.FieldRepository;
import com.lombardrisk.ignis.design.field.model.Field;
import io.vavr.control.Option;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

@AllArgsConstructor
public class FieldRepositoryFixture extends InMemoryRepository<Field> implements FieldRepository {

    private final Predicate<Long> schemaExists;

    @Override
    public Option<Field> findById(final long id) {
        return Option.ofOptional(super.findById(id));
    }

    @Override
    public List<Field> findAllById(final Iterable<Long> ids) {
        return super.findAllByIds(ids);
    }

    @Override
    public boolean existsSchema(final Long schemaId) {
        return schemaExists.test(schemaId);
    }

    @Override
    public Optional<Field> findByNameAndSchemaId(final String name, final Long schemaId) {
        return findAll().stream()
                .filter(field -> field.getSchemaId().equals(schemaId))
                .filter(field -> field.getName().equals(name))
                .findFirst();
    }

}
