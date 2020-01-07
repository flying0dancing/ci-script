package com.lombardrisk.ignis.design.server.productconfig.fixture;

import com.google.common.collect.ImmutableList;
import com.lombardrisk.ignis.data.common.fixtures.InMemoryRepository;
import com.lombardrisk.ignis.design.field.FieldService;
import com.lombardrisk.ignis.design.field.model.Field;
import com.lombardrisk.ignis.design.server.productconfig.api.SchemaRepository;
import com.lombardrisk.ignis.design.server.productconfig.rule.model.ValidationRule;
import com.lombardrisk.ignis.design.server.productconfig.schema.Schema;
import io.vavr.control.Option;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@AllArgsConstructor
public class SchemaRepositoryFixture extends InMemoryRepository<Schema> implements SchemaRepository {

    private final AtomicLong schemaIdSequence = new AtomicLong(1);
    @Getter
    private final AtomicLong ruleIdSequence = new AtomicLong(1);

    private final FieldService fieldService;

    @SuppressWarnings("ConstantConditions")
    @Override
    public Optional<Schema> findByIdAndProductId(final long productId, final long schemaId) {
        return findById(schemaId).toJavaOptional()
                .filter(schema -> schema.getProductId().equals(productId));
    }

    @Override
    public Optional<Schema> findSecondLatestVersion(final String schemaDisplayName) {
        Optional<Integer> secondHighestVersion = findSchemas()
                .filter(schema -> schema.getDisplayName().equals(schemaDisplayName))
                .filter(schema -> !schema.getLatest())
                .map(Schema::getMajorVersion)
                .max(Comparator.naturalOrder());

        if (!secondHighestVersion.isPresent()) {
            return Optional.empty();
        }

        return findSchemas()
                .filter(schema -> schema.getMajorVersion().equals(secondHighestVersion.get()))
                .findFirst();
    }

    @Override
    public Optional<Schema> findPreviousVersion(final String displayName, final int majorVersion) {
        Optional<Integer> previousVersion = findSchemas()
                .filter(schema -> schema.getDisplayName().equals(displayName))
                .filter(schema -> schema.getMajorVersion() < majorVersion)
                .map(Schema::getMajorVersion)
                .max(Comparator.naturalOrder());

        if (!previousVersion.isPresent()) {
            return Optional.empty();
        }

        return findSchemas()
                .filter(schema -> schema.getDisplayName().equals(displayName) && schema.getMajorVersion()
                        .equals(previousVersion.get()))
                .findFirst();
    }

    @Override
    public Optional<Schema> findNextVersion(final String displayName, final int majorVersion) {
        Optional<Integer> nextVersion = findSchemas()
                .filter(schema -> schema.getDisplayName().equals(displayName))
                .filter(schema -> schema.getMajorVersion() > majorVersion)
                .map(Schema::getMajorVersion)
                .min(Comparator.naturalOrder());

        if (!nextVersion.isPresent()) {
            return Optional.empty();
        }

        return findSchemas()
                .filter(schema -> schema.getDisplayName().equals(displayName) && schema.getMajorVersion()
                        .equals(nextVersion.get()))
                .findFirst();
    }

    @Override
    public Optional<Schema> findByPhysicalTableNameAndVersion(
            final String physicalTableName,
            final Integer majorVersion) {
        return filtered(schema -> schema.getPhysicalTableName().equals(physicalTableName) && schema.getMajorVersion()
                .equals(majorVersion))
                .findFirst();
    }

    @Override
    public Optional<Schema> findByDisplayNameAndVersion(final String displayName, final Integer majorVersion) {
        return filtered(schema -> schema.getDisplayName().equals(displayName) && schema.getMajorVersion()
                .equals(majorVersion))
                .findFirst();
    }

    @Override
    public List<Schema> findAllByIds(final Iterable<Long> ids) {
        List<Long> idList = ImmutableList.copyOf(ids);

        return findSchemas()
                .filter(schema -> idList.contains(schema.getId()))
                .collect(toList());
    }

    @Override
    public Option<Schema> findById(final long id) {
        Optional<Schema> first = findSchemas()
                .filter(schema -> schema.getId().equals(id))
                .findFirst();

        return Option.ofOptional(first);
    }

    @Override
    public Schema saveSchema(final Schema schema) {
        if (schema.getId() == null) {
            schema.setId(schemaIdSequence.getAndIncrement());
        }

        if (schema.getValidationRules() != null) {
            for (ValidationRule rule : schema.getValidationRules()) {
                if (rule.getId() == null) {
                    rule.setId(ruleIdSequence.getAndIncrement());
                }
            }
        }
        return save(schema);
    }

    @Override
    public Schema deleteSchema(final Schema schema) {
        super.delete(schema);
        return schema;
    }

    @Override
    public Optional<Schema> findMaxVersion(final String physicalName) {
        return findSchemas()
                .filter(schema -> schema.getPhysicalTableName().equals(physicalName))
                .max(Comparator.comparing(Schema::getMajorVersion));
    }

    public Stream<Schema> findSchemas() {
        return findAll().stream()
                .map(this::addRelatedEntities);
    }

    protected Stream<Schema> filtered(final Predicate<Schema> schemaPredicate) {
        return findSchemas().filter(schemaPredicate);
    }

    private Schema addRelatedEntities(final Schema schema) {
        schema.setFields(fieldService.findAll().stream()
                .filter(field -> field.getSchemaId().equals(schema.getId()))
                .sorted(Comparator.comparing(Field::getId))
                .collect(Collectors.toCollection(LinkedHashSet::new)));
        return schema;
    }
}
