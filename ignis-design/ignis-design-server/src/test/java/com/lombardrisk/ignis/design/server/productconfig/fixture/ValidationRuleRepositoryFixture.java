package com.lombardrisk.ignis.design.server.productconfig.fixture;

import com.google.common.collect.ImmutableList;
import com.lombardrisk.ignis.design.server.productconfig.api.SchemaRepository;
import com.lombardrisk.ignis.design.server.productconfig.api.ValidationRuleRepository;
import com.lombardrisk.ignis.design.server.productconfig.rule.model.ValidationRule;
import com.lombardrisk.ignis.design.server.productconfig.schema.Schema;
import io.vavr.control.Option;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.collect.Sets.newHashSet;

public class ValidationRuleRepositoryFixture implements ValidationRuleRepository {

    private final AtomicLong idSequence;
    private final SchemaRepository schemaRepository;

    private ValidationRuleRepositoryFixture(final SchemaRepositoryFixture schemaRepository) {
        this.schemaRepository = schemaRepository;
        this.idSequence = schemaRepository.getRuleIdSequence();
    }

    static ValidationRuleRepositoryFixture create(final SchemaRepositoryFixture schemaRepository) {
        return new ValidationRuleRepositoryFixture(schemaRepository);
    }

    @Override
    public Option<ValidationRule> findById(final long id) {
        Optional<ValidationRule> validationRule = findRules()
                .filter(rule -> rule.getId().equals(id))
                .findFirst();

        return Option.ofOptional(validationRule);
    }

    @Override
    public List<ValidationRule> findAllById(final Iterable<Long> ids) {
        ImmutableList<Long> idsCollection = ImmutableList.copyOf(ids);
        return findRules()
                .filter(rule -> idsCollection.contains(rule.getId()))
                .collect(Collectors.toList());
    }

    @Override
    public void delete(final ValidationRule validationRule) {
        schemaRepository.findAll()
                .forEach(schema -> schema.getValidationRules()
                        .removeIf(rule -> rule.getId().equals(validationRule.getId())));
    }

    @Override
    public ValidationRule save(final ValidationRule updatedRule) {
        if (updatedRule.getId() == null) {
            updatedRule.setId(idSequence.getAndIncrement());
            return updatedRule;
        }

        Optional<Schema> parentSchema = schemaRepository.findAll()
                .stream()
                .filter(schema -> schema.getValidationRules()
                        .stream()
                        .anyMatch(validationRule -> validationRule.getId().equals(updatedRule.getId())))
                .findFirst();

        if (parentSchema.isPresent()) {
            Schema schema = parentSchema.get();
            schema.getValidationRules().removeIf(validationRule -> validationRule.getId().equals(updatedRule.getId()));

            schema.getValidationRules().add(updatedRule);
            return updatedRule;
        }

        return updatedRule;
    }


    private Stream<ValidationRule> findRules() {
        return schemaRepository.findAll()
                .stream()
                .flatMap(schema -> Option.of(schema.getValidationRules())
                        .getOrElse(newHashSet())
                        .stream());
    }
}
