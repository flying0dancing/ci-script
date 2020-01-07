package com.lombardrisk.ignis.server.jpa;

import com.lombardrisk.ignis.server.config.IntegrationTestConfig;
import com.lombardrisk.ignis.server.product.fixture.ProductPopulated;
import com.lombardrisk.ignis.server.product.productconfig.ProductConfigRepository;
import com.lombardrisk.ignis.server.product.productconfig.model.ProductConfig;
import com.lombardrisk.ignis.server.product.rule.RuleExampleFieldTestRepository;
import com.lombardrisk.ignis.server.product.rule.RuleExampleTestRepository;
import com.lombardrisk.ignis.server.product.rule.ValidationRuleRepository;
import com.lombardrisk.ignis.server.product.rule.model.ValidationRule;
import com.lombardrisk.ignis.server.product.rule.model.ValidationRuleExample;
import com.lombardrisk.ignis.server.product.rule.model.ValidationRuleExampleField;
import com.lombardrisk.ignis.server.product.table.TableRepository;
import com.lombardrisk.ignis.server.product.table.model.DecimalField;
import com.lombardrisk.ignis.server.product.table.model.Field;
import com.lombardrisk.ignis.server.product.table.model.Table;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import static com.google.common.collect.Iterables.get;
import static com.google.common.collect.Sets.newHashSet;
import static com.lombardrisk.ignis.api.rule.TestResult.FAIL;
import static com.lombardrisk.ignis.api.rule.TestResult.PASS;
import static com.lombardrisk.ignis.server.product.fixture.ProductPopulated.decimalField;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SuppressWarnings({ "ConstantConditions", "OptionalGetWithoutIsPresent" })
@RunWith(SpringRunner.class)
@IntegrationTestConfig
public class ValidationRuleRepositoryIT {

    @Rule
    public JUnitSoftAssertions soft = new JUnitSoftAssertions();

    @Autowired
    private ValidationRuleRepository validationRuleRepository;
    @Autowired
    private ProductConfigRepository productConfigRepository;
    @Autowired
    private TableRepository tableRepository;
    @Autowired
    private RuleExampleTestRepository ruleExampleTestRepository;
    @Autowired
    private RuleExampleFieldTestRepository ruleExampleFieldTestRepository;

    @Test
    public void findById_RuleId_ReturnsRule() {
        ProductConfig productConfig = productConfigRepository.save(ProductPopulated.productConfig().build());

        ValidationRule validationRule = ValidationRule.builder()
                .name("validationRule1")
                .description("description")
                .expression("EXPRESSION")
                .ruleId("regId")
                .build();

        Table savedTable = tableRepository.saveAndFlush(ProductPopulated.table()
                .productId(productConfig.getId())
                .validationRules(singleton(validationRule))
                .build());

        Long validationRuleId = savedTable.getValidationRules().iterator().next().getId();
        ValidationRule retrievedValidationRule = validationRuleRepository.findById(validationRuleId).get();

        soft.assertThat(retrievedValidationRule.getName())
                .isEqualTo("validationRule1");
    }

    @Test
    public void save_UpdatedRule_PersistsUpdates() {
        ProductConfig productConfig = productConfigRepository.save(ProductPopulated.productConfig().build());

        ValidationRule validationRule = ValidationRule.builder()
                .name("validationRule1")
                .description("description")
                .expression("EXPRESSION")
                .ruleId("regId")
                .build();

        Table savedTable = tableRepository.saveAndFlush(ProductPopulated.table()
                .productId(productConfig.getId())
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
        ProductConfig productConfig = productConfigRepository.save(ProductPopulated.productConfig().build());

        ValidationRule validationRule = ValidationRule.builder()
                .name("validationRule1")
                .description("description")
                .expression("EXPRESSION")
                .ruleId("regId")
                .build();

        Table savedTable = tableRepository.saveAndFlush(ProductPopulated.table()
                .productId(productConfig.getId())
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
        ProductConfig productConfig = productConfigRepository.save(ProductPopulated.productConfig().build());

        Table savedSchema = tableRepository.saveAndFlush(
                ProductPopulated.table()
                        .productId(productConfig.getId())
                        .fields(new LinkedHashSet<>(asList(
                                decimalField("F1").build(),
                                decimalField("F2").build(),
                                decimalField("F3").build())))
                        .build());

        Set<Field> contextFields =
                savedSchema.getFields().stream()
                        .limit(2)
                        .collect(toSet());
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
        ProductConfig productConfig = productConfigRepository.save(ProductPopulated.productConfig().build());

        Table savedSchema = tableRepository.saveAndFlush(
                ProductPopulated.table()
                        .productId(productConfig.getId())
                        .fields(newHashSet(
                                decimalField("F1").build(),
                                decimalField("F2").build(),
                                decimalField("F3").build()))
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
        ProductConfig productConfig = productConfigRepository.save(ProductPopulated.productConfig().build());

        Table schema = ProductPopulated.table()
                .productId(productConfig.getId())
                .fields(newHashSet(decimalField("Fld1").build()))
                .validationRules(newHashSet(

                        ProductPopulated.validationRule()
                                .expression("Fld1 == 22")
                                .validationRuleExamples(newHashSet(
                                        ProductPopulated.validationRuleExample()
                                                .expectedResult(PASS)
                                                .validationRuleExampleFields(newHashSet(
                                                        ProductPopulated.validationRuleExampleField()
                                                                .name("Fld1")
                                                                .value("22")
                                                                .build()))
                                                .build()))
                                .build()))

                .build();

        Table savedSchema = tableRepository.saveAndFlush(schema);

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
        ProductConfig productConfig = productConfigRepository.save(ProductPopulated.productConfig().build());

        Table schema = ProductPopulated.table()
                .productId(productConfig.getId())
                .fields(newHashSet(decimalField("F1").build()))
                .validationRules(newHashSet(
                        ProductPopulated.validationRule()
                                .validationRuleExamples(newHashSet(
                                        ProductPopulated.validationRuleExample().expectedResult(PASS).build(),
                                        ProductPopulated.validationRuleExample().expectedResult(PASS).build(),
                                        ProductPopulated.validationRuleExample().expectedResult(FAIL).build()))
                                .build()))
                .build();

        Table savedSchema = tableRepository.saveAndFlush(schema);

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
