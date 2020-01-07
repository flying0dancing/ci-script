package com.lombardrisk.ignis.design.server.productconfig.rule.test;

import com.google.common.collect.ImmutableSet;
import com.lombardrisk.ignis.api.table.validation.ValidatableIntField;
import com.lombardrisk.ignis.data.common.Identifiable;
import com.lombardrisk.ignis.design.field.model.Field;
import com.lombardrisk.ignis.design.server.productconfig.rule.model.ValidationRule;
import com.lombardrisk.ignis.design.server.productconfig.schema.Schema;
import io.vavr.CheckedFunction1;
import io.vavr.Tuple2;
import org.junit.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static com.lombardrisk.ignis.common.jexl.JexlEngineFactory.jexlEngine;
import static com.lombardrisk.ignis.design.field.DesignField.Populated.decimalField;
import static com.lombardrisk.ignis.design.field.DesignField.Populated.intField;
import static com.lombardrisk.ignis.design.field.DesignField.Populated.longField;
import static com.lombardrisk.ignis.design.field.DesignField.Populated.stringField;
import static com.lombardrisk.ignis.design.server.productconfig.rule.test.RuleExpressionContext.EMPTY_VALUES_BY_FIELD_CLASS;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

public class RuleExpressionTesterTest {

    private RuleExpressionTester tester = new RuleExpressionTester(jexlEngine().create());

    @Test
    public void evaluateRuleExpression_ExpressionEvaluates_ReturnsNoError() {
        Schema table = Schema.builder()
                .fields(ImmutableSet.of(decimalField("A").build(), decimalField("B").build()))
                .build();

        ValidationRule rule = ValidationRule.builder()
                .expression("A == B")
                .build();

        List<Tuple2<String, String>> errors = tester.evaluateRuleExpression(rule, table).getErrors();

        assertThat(errors.isEmpty()).isTrue();
    }

    @Test
    public void evaluateRuleExpression_ExpressionWithoutAnyFields_ReturnsError() {
        Schema table = Schema.builder()
                .physicalTableName("T2")
                .fields(ImmutableSet.of(decimalField("F1").build(), decimalField("F2").build()))
                .build();

        ValidationRule rule = ValidationRule.builder()
                .expression("var x = 1; x == 1")
                .build();

        List<Tuple2<String, String>> errors = tester.evaluateRuleExpression(rule, table).getErrors();

        assertThat(errors)
                .extracting(Tuple2::_1, Tuple2::_2)
                .containsSequence(
                        tuple("expression", "No fields referenced"),
                        tuple("No fields referenced", "Must reference at least one field from schema T2"));
    }

    @Test
    public void evaluateRuleExpression_ExpressionWithUnsupportedFields_ReturnsError() {
        Field field = new UnsupportedField("F1");

        Schema table = Schema.builder()
                .physicalTableName("T2")
                .fields(ImmutableSet.of(field)).build();

        ValidationRule rule = ValidationRule.builder()
                .expression("F1 != null")
                .build();

        List<Tuple2<String, String>> errors = tester.evaluateRuleExpression(rule, table).getErrors();

        assertThat(errors)
                .extracting(Tuple2::_1, Tuple2::_2)
                .containsSequence(
                        tuple("expression", "Parse error"),
                        tuple("Parse error", "Field 'F1' has the unsupported type UnsupportedField"));
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @Test
    public void defaultsByFieldClass_AllFieldSubClassHaveDefaultValues() {
        ClassPathScanningCandidateComponentProvider provider =
                new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AssignableTypeFilter(Field.class));
        Set<BeanDefinition> components = provider.findCandidateComponents(Identifiable.class.getPackage().getName());

        CheckedFunction1<String, Class<?>> toSubClass = Class::forName;

        assertThat(
                components.stream()
                        .map(BeanDefinition::getBeanClassName)
                        .map(toSubClass.unchecked())
                        .filter(subClass -> EMPTY_VALUES_BY_FIELD_CLASS.get(subClass) == null)
        ).isEmpty();
    }

    @Test
    public void evaluateRuleExpression_ExpressionDoesNotEvaluate_ReturnsError() {
        Schema table = Schema.builder().build();

        ValidationRule rule = ValidationRule.builder()
                .ruleId("parse failure")
                .expression("blah blah blah")
                .build();

        List<Tuple2<String, String>> errors = tester.evaluateRuleExpression(rule, table).getErrors();

        assertThat(errors)
                .extracting(Tuple2::_1, Tuple2::_2)
                .containsSequence(
                        tuple("expression", "Ambiguous expression"),
                        tuple(
                                "Ambiguous expression",
                                "ambiguous statement error in 'blah blah blah' (at line 1, column 6)"));
    }

    @Test
    public void evaluateRuleExpression_ArithmeticExceptionExpression_ReturnsError() {
        Schema table = Schema.builder()
                .fields(singleton(decimalField("F").build()))
                .build();

        ValidationRule rule = ValidationRule.builder()
                .expression("F / 0")
                .build();

        List<Tuple2<String, String>> errors = tester.evaluateRuleExpression(rule, table).getErrors();

        assertThat(errors)
                .extracting(Tuple2::_1, Tuple2::_2)
                .containsSequence(
                        tuple("expression", "Arithmetic exception"),
                        tuple("Arithmetic exception", "/ error (at line 1, column 5)"));
    }

    @Test
    public void evaluateRuleExpression_ExpressionVariableNotPartOfSchema_ReturnsError() {
        Schema table = Schema.builder()
                .physicalTableName("T1")
                .fields(ImmutableSet.of(decimalField("A").build(), decimalField("B").build()))
                .build();

        ValidationRule rule = ValidationRule.builder()
                .expression("A + B == C || D")
                .build();

        List<Tuple2<String, String>> errors = tester.evaluateRuleExpression(rule, table).getErrors();

        assertThat(errors)
                .extracting(Tuple2::_1, Tuple2::_2)
                .containsSequence(
                        tuple("expression", "Undefined fields"),
                        tuple("Undefined fields", "Fields: C, D must be defined in schema T1"));
    }

    @Test
    public void evaluateRuleExpression_NonBooleanExpression_ReturnsError() {
        Schema table = Schema.builder()
                .fields(ImmutableSet.of(decimalField("COL1").build()))
                .build();

        ValidationRule rule = ValidationRule.builder()
                .expression("COL1")
                .build();

        List<Tuple2<String, String>> errors = tester.evaluateRuleExpression(rule, table).getErrors();

        assertThat(errors)
                .extracting(Tuple2::_1, Tuple2::_2)
                .containsSequence(
                        tuple("expression", "Non-boolean expression"),
                        tuple(
                                "Non-boolean expression",
                                "Expression must evaluate to Boolean (true/false), but was BigDecimal"));
    }

    @Test
    public void evaluateRuleExpression_CorrectExpression_ReturnsContextFields() {
        Schema table = Schema.builder()
                .fields(new LinkedHashSet<>(asList(
                        decimalField("COL1").build(),
                        decimalField("COL3").build(),
                        decimalField("COL5").build(),
                        decimalField("COL7").build())))
                .build();

        ValidationRule rule = ValidationRule.builder()
                .expression("COL1 + COL3 > COL7")
                .build();

        Set<Field> contextFields = tester.evaluateRuleExpression(rule, table).getContextFields();

        assertThat(contextFields)
                .extracting(Field::getName)
                .containsExactlyInAnyOrder("COL1", "COL3", "COL7");
    }

    @Test
    public void evaluateRuleExpression_InvalidOrExpression_ReturnsError() {
        Schema table = Schema.builder()
                .fields(new LinkedHashSet<>(asList(
                        stringField("COL1").build(),
                        decimalField("COL3").build())))
                .build();

        ValidationRule rule = ValidationRule.builder()
                .expression("isNull( COL1 ) || COL3 == 0 || invalidFunction( COL1 )")
                .build();

        List<Tuple2<String, String>> errors = tester.evaluateRuleExpression(rule, table).getErrors();

        assertThat(errors)
                .extracting(Tuple2::_1, Tuple2::_2)
                .containsExactlyInAnyOrder(
                        tuple(
                                "Method exception",
                                "unsolvable function/method 'invalidFunction' (at line 1, column 47)"),
                        tuple("expression", "Method exception"));
    }

    @Test
    public void evaluateRuleExpression_FieldsWithMixedCase_ReturnsContextFields() {
        Schema table = Schema.builder()
                .fields(new LinkedHashSet<>(asList(
                        longField("MY_ID").build(),
                        stringField("MyFirstName").build(),
                        longField("MYLASTNAME").build(),
                        intField("_age_").build(),
                        stringField("My_Address").build())))
                .build();

        ValidationRule rule = ValidationRule.builder()
                .expression("MY_ID != null "
                        + "&& MyFirstName != null "
                        + "&& MYLASTNAME != null "
                        + "&& _age_ != null"
                        + "&& My_Address != null")
                .build();

        Set<Field> contextFields = tester.evaluateRuleExpression(rule, table).getContextFields();

        assertThat(contextFields)
                .extracting(Field::getName)
                .containsExactlyInAnyOrder("MY_ID", "MyFirstName", "MYLASTNAME", "_age_", "My_Address");
    }

    @Test
    public void evaluateRuleExpression_FieldsWithMixedCase_ReturnsNoErrors() {
        Schema table = Schema.builder()
                .fields(new LinkedHashSet<>(asList(
                        longField("MY_ID").build(),
                        stringField("MyFirstName").build(),
                        longField("MYLASTNAME").build(),
                        intField("_age_").build(),
                        stringField("My_Address").build())))
                .build();

        ValidationRule rule = ValidationRule.builder()
                .expression("MY_ID != null "
                        + "&& MyFirstName != null "
                        + "&& MYLASTNAME != null "
                        + "&& _age_ != null"
                        + "&& My_Address != null")
                .build();

        List<Tuple2<String, String>> errors = tester.evaluateRuleExpression(rule, table).getErrors();

        assertThat(errors).isEmpty();
    }

    private static class UnsupportedField extends Field<Integer> implements ValidatableIntField {

        private static final long serialVersionUID = -7862883981887049849L;

        UnsupportedField(final String name) {
            setName(name);
        }

        @Override
        public Field<Integer> copy() {
            return null;
        }

        @Override
        public Integer generateData() {
            return null;
        }
    }
}
