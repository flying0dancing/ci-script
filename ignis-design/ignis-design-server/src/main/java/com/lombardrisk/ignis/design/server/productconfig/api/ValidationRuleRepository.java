package com.lombardrisk.ignis.design.server.productconfig.api;

import com.lombardrisk.ignis.design.server.productconfig.rule.model.ValidationRule;
import io.vavr.control.Option;

import java.util.List;

public interface ValidationRuleRepository {

    Option<ValidationRule> findById(long id);

    List<ValidationRule> findAllById(Iterable<Long> ids);

    void delete(ValidationRule validationRule);

    ValidationRule save(ValidationRule updatedRule);
}
