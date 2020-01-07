package com.lombardrisk.ignis.design.server.configuration.adapters.schema;

import com.lombardrisk.ignis.design.server.jpa.ValidationRuleJpaRepository;
import com.lombardrisk.ignis.design.server.productconfig.api.ValidationRuleRepository;
import com.lombardrisk.ignis.design.server.productconfig.rule.model.ValidationRule;
import io.vavr.control.Option;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public class ValidationRuleAdapter implements ValidationRuleRepository {

    private final ValidationRuleJpaRepository validationRuleRepository;

    public ValidationRuleAdapter(final ValidationRuleJpaRepository validationRuleRepository) {
        this.validationRuleRepository = validationRuleRepository;
    }

    @Override
    public Option<ValidationRule> findById(final long id) {
        return Option.ofOptional(validationRuleRepository.findById(id));
    }

    @Override
    public List<ValidationRule> findAllById(final Iterable<Long> ids) {
        return validationRuleRepository.findAllById(ids);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void delete(final ValidationRule validationRule) {
        validationRuleRepository.delete(validationRule);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public ValidationRule save(final ValidationRule validationRule) {
        return validationRuleRepository.save(validationRule);
    }
}
