package com.lombardrisk.ignis.design.server.productconfig.rule;

import com.lombardrisk.ignis.client.design.RuleExample;
import com.lombardrisk.ignis.client.design.schema.field.FieldDto;
import com.lombardrisk.ignis.client.external.rule.ExampleField;
import com.lombardrisk.ignis.client.external.rule.TestResultType;
import com.lombardrisk.ignis.common.assertions.VavrAssert;
import com.lombardrisk.ignis.data.common.error.ErrorResponse;
import com.lombardrisk.ignis.design.field.FieldService;
import com.lombardrisk.ignis.design.server.productconfig.ProductConfig;
import com.lombardrisk.ignis.design.server.productconfig.ProductConfigService;
import com.lombardrisk.ignis.design.server.productconfig.fixture.ProductServiceFixtureFactory;
import com.lombardrisk.ignis.design.server.productconfig.fixture.SchemaServiceFixtureFactory;
import com.lombardrisk.ignis.design.server.productconfig.rule.model.TestResult;
import com.lombardrisk.ignis.design.server.productconfig.rule.model.ValidationRule;
import com.lombardrisk.ignis.design.server.productconfig.rule.model.ValidationRuleExample;
import com.lombardrisk.ignis.design.server.productconfig.schema.RuleService;
import com.lombardrisk.ignis.design.server.productconfig.schema.Schema;
import io.vavr.control.Either;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Iterables.get;
import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Sets.newLinkedHashSet;
import static com.lombardrisk.ignis.design.field.DesignField.Populated.decimalFieldRequest;
import static com.lombardrisk.ignis.design.server.fixtures.Design.Populated;
import static org.assertj.core.api.Assertions.assertThat;

public class RuleServiceTest {

    private ProductConfigService productConfigService;
    private FieldService fieldService;
    private RuleService ruleService;

    @Rule
    public JUnitSoftAssertions soft = new JUnitSoftAssertions();

    @Before
    public void setUp() {

        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("user", ""));
        SchemaServiceFixtureFactory schemaServiceFactory
                = SchemaServiceFixtureFactory.create();
        ProductServiceFixtureFactory productServiceFactory =
                ProductServiceFixtureFactory.create(schemaServiceFactory, 100);

        productConfigService = productServiceFactory.getProductService();
        fieldService = schemaServiceFactory.getFieldService();
        ruleService = schemaServiceFactory.getRuleService();
    }

    @Test
    public void testRuleExamples_NonExistingRuleId_ReturnsNoRuleExamples() {
        assertThat(ruleService.testRuleExamples(-2342L))
                .isEmpty();
    }

    @Test
    public void testRuleExamples_ExistingRuleId_ReturnsRuleExamples() {
        ProductConfig productConfig = VavrAssert.assertValid(
                productConfigService.createProductConfig(Populated.newProductRequest().build()))
                .getResult();

        Schema existingTable = VavrAssert.assertValid(productConfigService.createNewSchemaOnProduct(
                productConfig.getId(), Populated.createSchemaRequest().build()))
                .getResult();

        fieldService.save(existingTable.getId(), decimalFieldRequest("DEC1").nullable(false).build())
                .get();

        Either<List<ErrorResponse>, ValidationRule> validationRuleResponse =
                ruleService.saveValidationRule(existingTable.getId(), Populated.validationRule()
                        .expression("DEC1 > 0")
                        .validationRuleExamples(newLinkedHashSet(Arrays.asList(
                                Populated.validationRuleExample()
                                        .expectedResult(TestResult.FAIL)
                                        .validationRuleExampleFields(newHashSet(
                                                Populated.validationRuleExampleField()
                                                        .name("DEC1").value("-22.433")
                                                        .build()))
                                        .build(),
                                Populated.validationRuleExample()
                                        .expectedResult(TestResult.FAIL)
                                        .build())))
                        .build());

        ValidationRule validationRule = validationRuleResponse.get();
        List<RuleExample> testedRuleExamples = ruleService.testRuleExamples(validationRule.getId());

        assertThat(testedRuleExamples).hasSize(2);

        RuleExample firstRuleExample = testedRuleExamples.get(0);
        Long firstValidationRuleExampleId = get(validationRule.getValidationRuleExamples(), 0).getId();
        soft.assertThat(firstRuleExample.id).isEqualTo(firstValidationRuleExampleId);
        soft.assertThat(firstRuleExample.expectedResult).isEqualTo(TestResultType.Fail);
        soft.assertThat(firstRuleExample.actualResult).isEqualTo(TestResultType.Fail);

        RuleExample lastRuleExample = get(testedRuleExamples, 1);
        Long lastValidationRuleExampleId = get(validationRule.getValidationRuleExamples(), 1).getId();
        soft.assertThat(lastRuleExample.id).isEqualTo(lastValidationRuleExampleId);
        soft.assertThat(lastRuleExample.expectedResult).isEqualTo(TestResultType.Fail);
        soft.assertThat(lastRuleExample.actualResult).isEqualTo(TestResultType.Error);
    }

    @Test
    public void testRuleExamples_RuleId_ReturnsRuleExamplesWithExampleFields() {
        ProductConfig productConfig = VavrAssert.assertValid(
                productConfigService.createProductConfig(Populated.newProductRequest().build()))
                .getResult();

        Schema existingTable = VavrAssert.assertValid(productConfigService.createNewSchemaOnProduct(
                productConfig.getId(), Populated.createSchemaRequest().build()))
                .getResult();

        FieldDto f1 = fieldService.save(existingTable.getId(), decimalFieldRequest("F1").nullable(false).build()).get();
        FieldDto f2 = fieldService.save(existingTable.getId(), decimalFieldRequest("F3").nullable(false).build()).get();

        Either<List<ErrorResponse>, ValidationRule> validationRuleResponse =
                ruleService.saveValidationRule(existingTable.getId(), Populated.validationRule()
                        .expression("F1 + F3 > 0")
                        .validationRuleExamples(newHashSet(
                                Populated.validationRuleExample()
                                        .validationRuleExampleFields(newLinkedHashSet(Arrays.asList(
                                                Populated.validationRuleExampleField()
                                                        .name("F1").value("-2.5")
                                                        .build(),
                                                Populated.validationRuleExampleField()
                                                        .name("F3").value(null)
                                                        .build())))
                                        .build()))
                        .build());

        ValidationRule validationRule = validationRuleResponse
                .getOrElseThrow(message -> new AssertionError(message.toString()));

        List<RuleExample> testedRuleExamples = ruleService.testRuleExamples(validationRule.getId());

        Map<String, ExampleField> exampleFields = testedRuleExamples.get(0).exampleFields;

        ExampleField firstExampleField = exampleFields.get("F1");
        assertThat(firstExampleField).isNotNull();

        ValidationRuleExample validationRuleExample = get(validationRule.getValidationRuleExamples(), 0);
        Long firstExampleFieldId = get(validationRuleExample.getValidationRuleExampleFields(), 0)
                .getId();

        soft.assertThat(firstExampleField.id).isEqualTo(firstExampleFieldId);
        soft.assertThat(firstExampleField.value).isEqualTo("-2.5");
        soft.assertThat(firstExampleField.error).isEqualTo(null);

        ExampleField lastExampleField = exampleFields.get("F3");
        assertThat(lastExampleField).isNotNull();
        Long lastExampleFieldId = get(validationRuleExample.getValidationRuleExampleFields(), 1).getId();

        soft.assertThat(lastExampleField.id).isEqualTo(lastExampleFieldId);
        soft.assertThat(lastExampleField.value).isEqualTo(null);
        soft.assertThat(lastExampleField.error).isNotBlank();
    }

    @Test
    public void testRuleExamples_ExistingRuleWithUnstableExpression_ReturnsRuleExampleWithUnexpectedError() {
        ProductConfig productConfig = VavrAssert.assertValid(
                productConfigService.createProductConfig(Populated.newProductRequest().build()))
                .getResult();

        Schema existingTable = VavrAssert.assertValid(productConfigService.createNewSchemaOnProduct(
                productConfig.getId(), Populated.createSchemaRequest().build()))
                .getResult();

        fieldService.save(existingTable.getId(), decimalFieldRequest("A").nullable(true).build()).get();

        Either<List<ErrorResponse>, ValidationRule> validationRuleResponse =
                ruleService.saveValidationRule(existingTable.getId(), Populated.validationRule()
                        .expression("A - 200 > 0")
                        .validationRuleExamples(newHashSet(
                                Populated.validationRuleExample()
                                        .validationRuleExampleFields(newHashSet(
                                                Populated.validationRuleExampleField().build()))
                                        .build()))
                        .build());

        ValidationRule validationRule = validationRuleResponse
                .getOrElseThrow(errors -> new AssertionError(errors.toString()));

        List<RuleExample> testedRuleExamples = ruleService.testRuleExamples(validationRule.getId());

        soft.assertThat(get(testedRuleExamples, 0).unexpectedError).isNotBlank();
    }
}
