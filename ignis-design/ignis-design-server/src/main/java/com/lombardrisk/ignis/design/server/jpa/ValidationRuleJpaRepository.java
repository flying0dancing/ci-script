package com.lombardrisk.ignis.design.server.jpa;

import com.lombardrisk.ignis.design.server.productconfig.rule.model.ValidationRule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ValidationRuleJpaRepository extends JpaRepository<ValidationRule, Long> {

}
