package com.lombardrisk.ignis.server.product.rule;

import com.lombardrisk.ignis.data.common.service.CRUDService;
import com.lombardrisk.ignis.server.product.rule.model.ValidationRule;
import io.vavr.control.Option;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@Slf4j
public class ValidationRuleService implements CRUDService<ValidationRule> {

    private final ValidationRuleRepository ruleRepository;

    public ValidationRuleService(final ValidationRuleRepository ruleRepository) {
        this.ruleRepository = ruleRepository;
    }

    @Override
    public String entityName() {
        return ValidationRule.class.getSimpleName();
    }

    @Override
    public Option<ValidationRule> findById(final long id) {
        return Option.ofOptional(ruleRepository.findById(id));
    }

    @Override
    public List<ValidationRule> findAllByIds(final Iterable<Long> ids) {
        return ruleRepository.findAllById(ids);
    }

    @Override
    public List<ValidationRule> findAll() {
        return ruleRepository.findAll();
    }

    @Override
    public ValidationRule delete(final ValidationRule nope) {
        throw new UnsupportedOperationException();
    }
}
