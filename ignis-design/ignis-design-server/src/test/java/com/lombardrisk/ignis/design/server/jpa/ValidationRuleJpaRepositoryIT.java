package com.lombardrisk.ignis.design.server.jpa;

import com.lombardrisk.ignis.design.field.model.DecimalField;
import com.lombardrisk.ignis.design.field.model.Field;
import com.lombardrisk.ignis.design.server.DesignStudioTestConfig;
import com.lombardrisk.ignis.design.server.productconfig.ProductConfig;
import com.lombardrisk.ignis.design.server.productconfig.rule.model.ValidationRule;
import com.lombardrisk.ignis.design.server.productconfig.rule.model.ValidationRuleExample;
import com.lombardrisk.ignis.design.server.productconfig.rule.model.ValidationRuleExampleField;
import com.lombardrisk.ignis.design.server.productconfig.rule.test.RuleExampleFieldTestRepository;
import com.lombardrisk.ignis.design.server.productconfig.rule.test.RuleExampleTestRepository;
import com.lombardrisk.ignis.design.server.productconfig.schema.Schema;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Objects;
import java.util.Set;

import static com.google.common.collect.Iterables.get;
import static com.google.common.collect.Sets.newHashSet;
import static com.lombardrisk.ignis.design.field.DesignField.Populated.decimalField;
import static com.lombardrisk.ignis.design.server.fixtures.Design.Populated;
import static com.lombardrisk.ignis.design.server.productconfig.rule.model.TestResult.FAIL;
import static com.lombardrisk.ignis.design.server.productconfig.rule.model.TestResult.PASS;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SuppressWarnings({ "ConstantConditions", "OptionalGetWithoutIsPresent" })
@RunWith(SpringRunner.class)
@DesignStudioTestConfig
public class ValidationRuleJpaRepositoryIT {

    @Rule
    public JUnitSoftAssertions soft = new JUnitSoftAssertions();

    @Autowired
    private ProductConfigJpaRepository productRepository;
    @Autowired
    private ValidationRuleJpaRepository validationRuleRepository;
    @Autowired
    private SchemaJpaRepository tableRepository;
    @Autowired
    private FieldJpaRepository fieldJpaRepository;
    @Autowired
    private RuleExampleTestRepository ruleExampleTestRepository;
    @Autowired
    private RuleExampleFieldTestRepository ruleExampleFieldTestRepository;

    private ProductConfig product;

    @Before
    public void setUp() {
        product = productRepository.saveAndFlush(Populated.productConfig().build());
    }

    @Test
    public void findById_RuleId_ReturnsRule() {
        ValidationRule validationRule = ValidationRule.builder()
                .name("validationRule1")
                .description("description")
                .expression("EXPRESSION")
                .ruleId("regId")
                .build();

        Schema savedTable = tableRepository.saveAndFlush(Populated.schema()
                .productId(product.getId())
                .fields(emptySet())
                .validationRules(singleton(validationRule))
                .build());

        Long validationRuleId = savedTable.getValidationRules().iterator().next().getId();
        ValidationRule retrievedValidationRule = validationRuleRepository.findById(validationRuleId).get();

        soft.assertThat(retrievedValidationRule.getName())
                .isEqualTo("validationRule1");
    }

    @Test
    public void save_UpdatedRule_PersistsUpdates() {
        ValidationRule validationRule = ValidationRule.builder()
                .name("validationRule1")
                .description("description")
                .expression("EXPRESSION")
                .ruleId("regId")
                .build();

        Schema savedTable = tableRepository.saveAndFlush(Populated.schema()
                .productId(product.getId())
                .fields(emptySet())
                .validationRules(singleton(validationRule))
                .build());

        ValidationRule savedRule = savedTable.getValidationRules().iterator().next();
        savedRule.setName("newName");

        validationRuleRepository.saveAndFlush(savedRule);

        ValidationRule updatedRule = validationRuleRepository.findById(savedRule.getId()).get();

        assertThat(updatedRule.getName())
                .isEqualTo("newName");
    }

    @Test
    public void delete_Rule_DeletesRule() {
        ValidationRule validationRule = ValidationRule.builder()
                .name("validationRule1")
                .description("description")
                .expression("EXPRESSION")
                .ruleId("regId")
                .build();

        Schema savedTable = tableRepository.saveAndFlush(Populated.schema()
                .productId(product.getId())
                .fields(emptySet())
                .validationRules(singleton(validationRule))
                .build());

        ValidationRule savedRule = savedTable.getValidationRules().iterator().next();

        Long validationRuleId = savedRule.getId();
        validationRuleRepository.deleteById(validationRuleId);

        assertThat(validationRuleRepository.findById(savedRule.getId()))
                .isEmpty();
    }

    @Test
    public void save_RuleWithExistingFields_PersistsFieldsRelationship() {
        Schema savedSchema = tableRepository.saveAndFlush(
                Populated.schema()
                        .productId(product.getId())
                        .fields(emptySet())
                        .build());

        DecimalField f1 = fieldJpaRepository.save(decimalField("F1").schemaId(savedSchema.getId()).build());
        DecimalField f2 = fieldJpaRepository.save(decimalField("F2").schemaId(savedSchema.getId()).build());
        fieldJpaRepository.save(decimalField("F3").schemaId(savedSchema.getId()).build());

        Set<Field> contextFields = newHashSet(f1, f2);
        ValidationRule rule = ValidationRule.builder()
                .name("some name")
                .ruleId("some rule ID")
                .expression("F1 > F2")
                .contextFields(contextFields)
                .build();

        savedSchema.getValidationRules().clear();
        savedSchema.getValidationRules().add(rule);
        savedSchema = tableRepository.saveAndFlush(savedSchema);

        ValidationRule savedRule = savedSchema.getValidationRules().iterator().next();

        soft.assertThat(savedRule.getId())
                .isGreaterThan(0);

        soft.assertThat(savedRule.getContextFields())
                .extracting(Field::getName)
                .containsExactlyInAnyOrder("F1", "F2");
        soft.assertThat(savedRule.getContextFields())
                .extracting(Field::getId)
                .filteredOn(Objects::isNull)
                .isEmpty();
    }

    @Test
    public void save_RuleWithNonExistentFields_ThrowsException() {
        Schema savedSchema = tableRepository.saveAndFlush(
                Populated.schema()
                        .productId(product.getId())
                        .fields(emptySet())
                        .build());

        DecimalField decimalField = decimalField("F11").build();
        decimalField.setId(12312312312L);

        ValidationRule rule = ValidationRule.builder()
                .name("some name")
                .ruleId("some rule ID")
                .expression("F11 > 0")
                .contextFields(newHashSet(decimalField))
                .build();

        savedSchema.getValidationRules().add(rule);

        assertThatThrownBy(() -> tableRepository.saveAndFlush(savedSchema))
                .isInstanceOf(JpaObjectRetrievalFailureException.class);
    }

    @Test
    public void save_RuleWithExamples_PersistsExamples() {
        Schema schema = Populated.schema()
                .productId(product.getId())
//                .fields(newHashSet(decimalField("Fld1").build()))
                .fields(emptySet())
                .validationRules(newHashSet(
                        Populated.validationRule()
                                .expression("Fld1 == 22")
                                .validationRuleExamples(newHashSet(
                                        Populated.validationRuleExample()
                                                .expectedResult(PASS)
                                                .validationRuleExampleFields(newHashSet(
                                                        Populated.validationRuleExampleField()
                                                                .name("Fld1")
                                                                .value("22")
                                                                .build()))
                                                .build()))
                                .build()))
                .build();

        Schema savedSchema = tableRepository.saveAndFlush(schema);

        ValidationRuleExample validationRuleExample =
                savedSchema.getValidationRules().iterator().next()
                        .getValidationRuleExamples().iterator().next();
        ValidationRuleExampleField validationRuleExampleField =
                validationRuleExample.getValidationRuleExampleFields().iterator().next();

        soft.assertThat(validationRuleExample.getId())
                .isGreaterThan(0L);
        soft.assertThat(validationRuleExample.getExpectedResult())
                .isEqualTo(PASS);
        soft.assertThat(validationRuleExampleField.getId())
                .isGreaterThan(0L);
        soft.assertThat(validationRuleExampleField)
                .extracting(ValidationRuleExampleField::getName, ValidationRuleExampleField::getValue)
                .containsSequence("Fld1", "22");
    }

    @Test
    public void delete_RuleWithExamples_DeletesExamples() {
        Schema schema = Populated.schema()
                .productId(product.getId())
//                .fields(newHashSet(decimalField("F1").build()))
                .fields(emptySet())
                .validationRules(newHashSet(
                        Populated.validationRule()
                                .validationRuleExamples(newHashSet(
                                        Populated.validationRuleExample().expectedResult(PASS).build(),
                                        Populated.validationRuleExample().expectedResult(PASS).build(),
                                        Populated.validationRuleExample().expectedResult(FAIL).build()))
                                .build()))
                .build();

        Schema savedSchema = tableRepository.saveAndFlush(schema);

        ValidationRule validationRule = savedSchema.getValidationRules().iterator().next();
        soft.assertThat(validationRule.getValidationRuleExamples())
                .hasSize(2);

        ValidationRuleExample firstRuleExample = get(validationRule.getValidationRuleExamples(), 0);
        Long firstRuleExampleId = firstRuleExample.getId();
        soft.assertThat(ruleExampleTestRepository.findById(firstRuleExampleId))
                .isPresent();
        Long firstRuleExampleFieldId = get(firstRuleExample.getValidationRuleExampleFields(), 0).getId();
        soft.assertThat(ruleExampleFieldTestRepository.findById(firstRuleExampleFieldId))
                .isPresent();
        Long secondRuleExampleId = get(validationRule.getValidationRuleExamples(), 1).getId();
        soft.assertThat(ruleExampleTestRepository.findById(secondRuleExampleId))
                .isPresent();

        Long validationRuleId = validationRule.getId();
        validationRuleRepository.deleteById(validationRuleId);

        soft.assertThat(ruleExampleTestRepository.findById(firstRuleExampleId))
                .isEmpty();
        soft.assertThat(ruleExampleFieldTestRepository.findById(firstRuleExampleFieldId))
                .isEmpty();
        soft.assertThat(ruleExampleTestRepository.findById(secondRuleExampleId))
                .isEmpty();
    }
}
