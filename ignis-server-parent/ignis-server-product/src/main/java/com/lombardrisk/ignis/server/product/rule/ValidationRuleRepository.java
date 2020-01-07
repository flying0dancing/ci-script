package com.lombardrisk.ignis.server.product.rule;

import com.lombardrisk.ignis.server.product.rule.model.ValidationRule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ValidationRuleRepository extends JpaRepository<ValidationRule, Long> {

}
