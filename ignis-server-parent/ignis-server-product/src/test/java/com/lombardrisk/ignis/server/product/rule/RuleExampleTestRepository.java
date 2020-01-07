package com.lombardrisk.ignis.server.product.rule;

import com.lombardrisk.ignis.server.product.rule.model.ValidationRuleExample;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RuleExampleTestRepository extends JpaRepository<ValidationRuleExample, Long> {

}
