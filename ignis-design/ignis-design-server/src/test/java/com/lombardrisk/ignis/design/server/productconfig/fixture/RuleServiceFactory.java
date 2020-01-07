package com.lombardrisk.ignis.design.server.productconfig.fixture;

import com.lombardrisk.ignis.common.jexl.JexlEngineFactory;
import com.lombardrisk.ignis.design.server.productconfig.schema.RuleService;
import com.lombardrisk.ignis.design.server.productconfig.api.ValidationRuleRepository;
import com.lombardrisk.ignis.design.server.productconfig.rule.test.RuleExpressionTester;
import lombok.experimental.UtilityClass;

@UtilityClass
public class RuleServiceFactory {

    public static RuleService create(final SchemaRepositoryFixture schemaRepository) {
        ValidationRuleRepository ruleRepositoryFixture = ValidationRuleRepositoryFixture.create(schemaRepository);

        RuleExpressionTester ruleExpressionTester = new RuleExpressionTester(JexlEngineFactory.jexlEngine().create());

        return new RuleService(schemaRepository, ruleRepositoryFixture, ruleExpressionTester);
    }
}
