package com.lombardrisk.ignis.design.server.productconfig.rule.test;

import com.lombardrisk.ignis.design.server.productconfig.rule.model.ValidationRuleExample;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RuleExampleTestRepository extends JpaRepository<ValidationRuleExample, Long> {

}
