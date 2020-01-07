package com.lombardrisk.ignis.design.server.productconfig.fixture;

import com.lombardrisk.ignis.design.field.fixtures.FieldServiceFactory;
import com.lombardrisk.ignis.design.server.fixtures.Design;
import com.lombardrisk.ignis.design.server.productconfig.api.ProductConfigRepository;
import com.lombardrisk.ignis.design.server.productconfig.rule.model.ValidationRule;
import com.lombardrisk.ignis.design.server.productconfig.schema.Schema;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static org.assertj.core.api.Assertions.assertThat;

public class ValidationRuleRepositoryFixtureTest {

    private ProductConfigRepository productConfigRepository;
    private SchemaRepositoryFixture schemaRepository;
    private ValidationRuleRepositoryFixture ruleRepositoryFixture;

    @Before
    public void setUp() {
        productConfigRepository = ProductConfigRepositoryFixture.empty();
        schemaRepository = new SchemaRepositoryFixture(FieldServiceFactory.create(x -> true).getFieldService());
        ruleRepositoryFixture = ValidationRuleRepositoryFixture.create(schemaRepository);
    }

    @Test
    public void save_RuleIsNewRule_AddsId() {
        ValidationRule rule1 = ruleRepositoryFixture.save(Design.Populated.validationRule().build());
        ValidationRule rule2 = ruleRepositoryFixture.save(Design.Populated.validationRule().build());
        ValidationRule rule3 = ruleRepositoryFixture.save(Design.Populated.validationRule().build());
        ValidationRule rule4 = ruleRepositoryFixture.save(Design.Populated.validationRule().build());

        assertThat(rule1.getId()).isEqualTo(1L);
        assertThat(rule2.getId()).isEqualTo(2L);
        assertThat(rule3.getId()).isEqualTo(3L);
        assertThat(rule4.getId()).isEqualTo(4L);
    }

    @Test
    public void findById_RuleExists_ReturnsValue() {
        ValidationRule rule1 = ruleRepositoryFixture.save(Design.Populated.validationRule().name("rule1").build());
        ValidationRule rule2 = ruleRepositoryFixture.save(Design.Populated.validationRule().name("rule2").build());

        Schema schema = schemaRepository.saveSchema(Design.Populated.schema()
                .validationRules(newHashSet(rule1, rule2))
                .build());

        productConfigRepository.save(Design.Populated.productConfig().tables(newHashSet(schema)).build());

        assertThat(ruleRepositoryFixture.findById(rule1.getId()).get())
                .isEqualTo(rule1);
        assertThat(ruleRepositoryFixture.findById(rule2.getId()).get())
                .isEqualTo(rule2);
    }

    @Test
    public void findById_WrongId_ReturnsEmpty() {
        ValidationRule rule = ruleRepositoryFixture.save(Design.Populated.validationRule().name("rule").build());

        Schema schema = schemaRepository.saveSchema(Design.Populated.schema()
                .validationRules(newHashSet(rule))
                .build());

        productConfigRepository.save(Design.Populated.productConfig().tables(newHashSet(schema)).build());

        assertThat(ruleRepositoryFixture.findById(10921).isDefined())
                .isFalse();
    }

    @Test
    public void save_ExistingRule_UpdatesRuleOnSchema() {
        ValidationRule rule = ruleRepositoryFixture.save(Design.Populated.validationRule().name("rule").build());

        Schema schema = schemaRepository.saveSchema(Design.Populated.schema()
                .validationRules(newHashSet(rule))
                .build());

        productConfigRepository.save(Design.Populated.productConfig().tables(newHashSet(schema)).build());

        rule.setName("Hello");
        ruleRepositoryFixture.save(rule);

        Set<ValidationRule> schemaRules = schemaRepository.findById(schema.getId()).get().getValidationRules();
        assertThat(schemaRules.iterator().next().getName()).isEqualTo("Hello");
    }
}
