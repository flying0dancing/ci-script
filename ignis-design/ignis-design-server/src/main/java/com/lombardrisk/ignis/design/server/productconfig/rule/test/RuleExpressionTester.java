package com.lombardrisk.ignis.design.server.productconfig.rule.test;

import com.lombardrisk.ignis.client.external.rule.ExampleField;
import com.lombardrisk.ignis.client.external.rule.TestResultType;
import com.lombardrisk.ignis.design.field.model.Field;
import com.lombardrisk.ignis.design.server.productconfig.rule.model.ValidationRule;
import com.lombardrisk.ignis.design.server.productconfig.rule.model.ValidationRuleExample;
import com.lombardrisk.ignis.design.server.productconfig.rule.model.ValidationRuleExampleField;
import com.lombardrisk.ignis.design.server.productconfig.schema.Schema;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.control.Try;
import io.vavr.control.Validation;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlException;
import org.apache.commons.jexl3.JexlInfo;
import org.apache.commons.jexl3.JexlScript;
import org.apache.commons.jexl3.MapContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.lombardrisk.ignis.design.server.productconfig.rule.test.RuleExpressionContext.EMPTY_VALUES_BY_FIELD_CLASS;
import static com.lombardrisk.ignis.design.server.productconfig.rule.test.RuleExpressionContext.LOWER_VALUES_BY_FIELD_CLASS;
import static com.lombardrisk.ignis.design.server.productconfig.rule.test.RuleExpressionContext.SMALL_STRING_VALUE;
import static com.lombardrisk.ignis.design.server.productconfig.rule.test.RuleExpressionContext.UPPER_VALUES_BY_FIELD_CLASS;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.ObjectUtils.notEqual;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.apache.commons.lang3.StringUtils.endsWith;

public class RuleExpressionTester {

    private static final Logger LOGGER = LoggerFactory.getLogger(RuleExpressionTester.class);

    private static final String EXPRESSION_ERROR_CODE = "expression";
    private static final String UNDEFINED_FIELDS_ERROR_CODE = "Undefined fields";
    private static final String PARSE_ERROR_CODE = "Parse error";
    private static final String NON_BOOLEAN_ERROR_CODE = "Non-boolean expression";

    private static final String EXPRESSION_SUFFIX = " expression";
    private static final String EXCEPTION_SUFFIX = " exception";
    private static final String NO_FIELDS_ERROR_CODE = "No fields referenced";
    private static final String NUMBER_FORMAT_MSG = "Cannot interpret variables as numbers";

    public static final int ERRORS_ESTIMATION_COUNT = 6;

    private final JexlEngine jexl;

    public RuleExpressionTester(final JexlEngine jexl) {
        this.jexl = jexl;
    }

    public TestedRuleExampleResult testExamples(
            final ValidationRuleExample validationRuleExample, final ValidationRule rule) {

        Map<Field, ExampleField> exampleFieldsByField = createFieldToExampleField(
                rule.getContextFields(), validationRuleExample.getValidationRuleExampleFields());

        Tuple2<TestResultType, String> expressionResult =
                evaluateExpression(exampleFieldsByField, rule.getExpression());

        return TestedRuleExampleResult.builder()
                .exampleFieldsByField(exampleFieldsByField)
                .testResultType(expressionResult._1)
                .unexpectedError(expressionResult._2)
                .build();
    }

    private Map<Field, ExampleField> createFieldToExampleField(
            final Set<Field> contextFields, final Set<ValidationRuleExampleField> validationRuleExampleFields) {

        Map<String, Tuple2<Long, String>> exampleIdAndValueByFieldName =
                validationRuleExampleFields
                        .stream()
                        .collect(toMap(
                                ValidationRuleExampleField::getName,
                                exampleField -> Tuple.of(exampleField.getId(), exampleField.getValue())));
        Map<Field, ExampleField> fieldToExampleField = new HashMap<>();

        for (Field contextField : contextFields) {
            Tuple2<Long, String> fieldIdAndValue = exampleIdAndValueByFieldName.get(contextField.getName());
            String value = fieldIdAndValue != null ? fieldIdAndValue._2 : null;
            @SuppressWarnings("unchecked")
            Validation<String, String> valueValidation = contextField.validate(value);

            fieldToExampleField.put(
                    contextField,
                    ExampleField.builder()
                            .id(fieldIdAndValue != null ? fieldIdAndValue._1 : null)
                            .value(value)
                            .error(valueValidation.isInvalid() ? valueValidation.getError() : null)
                            .build());
        }
        return fieldToExampleField;
    }

    private Tuple2<TestResultType, String> evaluateExpression(
            final Map<Field, ExampleField> fieldToExampleField, final String expression) {
        Map<String, Object> scriptContext = new HashMap<>();

        for (Entry<Field, ExampleField> fieldToExampleFieldEntry : fieldToExampleField.entrySet()) {
            ExampleField exampleField = fieldToExampleFieldEntry.getValue();
            if (exampleField.error != null) {
                LOGGER.debug(
                        "Cannot evaluate expression because at least 1 example field contains an error: {}",
                        exampleField.error);
                return Tuple.of(TestResultType.Error, null);
            }
            Field field = fieldToExampleFieldEntry.getKey();
            Object value = field.parse(exampleField.value);

            scriptContext.put(field.getName(), value);
        }
        try {
            JexlScript script = jexl.createScript(expression);
            logContext(expression, scriptContext);

            Object execute = script.execute(new MapContext(scriptContext));

            TestResultType testResultType = (Boolean) execute ? TestResultType.Pass : TestResultType.Fail;
            return Tuple.of(testResultType, null);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);

            TestedRuleResult testedRuleResult = toParseErrorResult(e);
            String errorMessage = testedRuleResult.getErrors()
                    .stream()
                    .filter(error -> notEqual(error._1, EXPRESSION_ERROR_CODE))
                    .map(Tuple2::_2)
                    .findFirst()
                    .orElse(null);

            return Tuple.of(TestResultType.Error, errorMessage);
        }
    }

    private static void logContext(final String expression, final Map<String, Object> scriptContext) {
        if (LOGGER.isTraceEnabled()) {

            Map<String, Tuple2<Object, String>> context =
                    scriptContext.entrySet()
                            .stream()
                            .collect(toMap(
                                    Entry::getKey,
                                    entry -> Tuple.of(
                                            entry.getValue(),
                                            entry.getValue() == null
                                                    ? null
                                                    : entry.getValue().getClass().getSimpleName())));
            LOGGER.trace("Evaluate {} into expression \n{}", context, expression);
        }
    }

    public TestedRuleResult evaluateRuleExpression(final ValidationRule rule, final Schema table) {
        LOGGER.debug("Test rule with ruleId [{}] with expression / script => \n{}",
                rule.getRuleId(), rule.getExpression());

        TestedRuleResult testedRuleResult =
                Try.of(() -> jexl.createScript(rule.getExpression()))
                        .map(jexlScript -> toTestedRuleContext(jexlScript, table))
                        .getOrElseGet(RuleExpressionTester::toParseErrorResult);

        logErrors(testedRuleResult);
        return testedRuleResult;
    }

    private static TestedRuleResult toTestedRuleContext(final JexlScript script, final Schema table) {
        List<Tuple2<String, String>> expressionErrors = validateExpression(script, table);

        if (expressionErrors.isEmpty()) {
            return TestedRuleResult.builder()
                    .errors(emptyList())
                    .contextFields(toContextFields(script, table))
                    .build();
        }
        return TestedRuleResult.builder()
                .contextFields(emptySet())
                .errors(expressionErrors).build();
    }

    private static List<Tuple2<String, String>> validateExpression(final JexlScript script, final Schema table) {
        List<Tuple2<String, String>> errors = new ArrayList<>(ERRORS_ESTIMATION_COUNT);

        errors.addAll(
                validateNoFieldsReferenced(script, table.getPhysicalTableName()));
        errors.addAll(
                validateUndefinedFields(script, table));

        if (errors.isEmpty()) {
            errors.addAll(
                    validateExpressionReturnsBoolean(script, table));
        }
        return errors;
    }

    private static Set<Field> toContextFields(final JexlScript script, final Schema schema) {
        Set<String> fieldVariables = findVariables(script).collect(toSet());

        return schema.getFields().stream()
                .filter(field -> fieldVariables.contains(field.getName()))
                .collect(toSet());
    }

    private static List<Tuple2<String, String>> validateNoFieldsReferenced(
            final JexlScript script, final String schemaName) {

        if (script.getVariables().isEmpty()) {
            return Arrays.asList(
                    Tuple.of(EXPRESSION_ERROR_CODE, NO_FIELDS_ERROR_CODE),
                    Tuple.of(
                            NO_FIELDS_ERROR_CODE,
                            "Must reference at least one field from schema " + schemaName));
        }
        return emptyList();
    }

    private static List<Tuple2<String, String>> validateUndefinedFields(final JexlScript script, final Schema table) {
        Set<String> schemaFieldNames =
                table.getFields().stream()
                        .map(Field::getName)
                        .collect(toSet());

        Set<String> undefinedFields =
                findVariables(script)
                        .filter(fieldNameVar -> !schemaFieldNames.contains(fieldNameVar))
                        .collect(toSet());

        if (!undefinedFields.isEmpty()) {
            String errorMessage = String.format("Fields: %s must be defined in schema %s",
                    String.join(", ", undefinedFields), table.getPhysicalTableName());

            return Arrays.asList(
                    Tuple.of(EXPRESSION_ERROR_CODE, UNDEFINED_FIELDS_ERROR_CODE),
                    Tuple.of(UNDEFINED_FIELDS_ERROR_CODE, errorMessage));
        }
        return emptyList();
    }

    private static Stream<String> findVariables(final JexlScript script) {
        Function<List<String>, String> toVarsWithoutDots = antStyleVar -> antStyleVar.get(0);

        return script.getVariables().stream()
                .map(toVarsWithoutDots);
    }

    private static List<Tuple2<String, String>> validateExpressionReturnsBoolean(
            final JexlScript script, final Schema table) {
        Object result = executeScript(script, table.getFields());

        if (!(result instanceof Boolean)) {
            return Arrays.asList(
                    Tuple.of(EXPRESSION_ERROR_CODE, NON_BOOLEAN_ERROR_CODE),
                    Tuple.of(
                            NON_BOOLEAN_ERROR_CODE,
                            "Expression must evaluate to Boolean (true/false), but was "
                                    + result.getClass().getSimpleName()));
        }
        return emptyList();
    }

    private static Object executeScript(final JexlScript script, final Set<Field> fields) {
        checkExpression(script, fields, RuleExpressionTester::toFieldNameAndLowerValue);

        checkExpression(script, fields, RuleExpressionTester::toFieldNameAndUpperValue);

        return checkExpression(script, fields, RuleExpressionTester::toFieldNameAndEmptyValue);
    }

    private static Object checkExpression(
            final JexlScript script,
            final Set<Field> fields,
            final Function<? super Tuple2<String, Class<? extends Field>>, Tuple2<String, Object>> byValueMapper) {
        Map<String, Object> valuesContext =
                fields.stream()
                        .map(RuleExpressionTester::toFieldNameAndClass)
                        .map(byValueMapper)
                        .collect(toMap(Tuple2::_1, Tuple2::_2));

        return script.execute(new MapContext(valuesContext));
    }

    private static Tuple2<String, Class<? extends Field>> toFieldNameAndClass(final Field field) {
        return Tuple.of(field.getName(), field.getClass());
    }

    private static Tuple2<String, Object> toFieldNameAndLowerValue(
            final Tuple2<String, Class<? extends Field>> nameAndClass) {
        Object lowerValue = LOWER_VALUES_BY_FIELD_CLASS.get(nameAndClass._2);

        if (lowerValue == null) {
            throwUnsupportedTypeException(nameAndClass);
        }
        return Tuple.of(nameAndClass._1, lowerValue);
    }

    private static Tuple2<String, Object> toFieldNameAndUpperValue(
            final Tuple2<String, Class<? extends Field>> nameAndClass) {
        Object upperValue = UPPER_VALUES_BY_FIELD_CLASS.get(nameAndClass._2);

        if (upperValue == null) {
            throwUnsupportedTypeException(nameAndClass);
        }
        return Tuple.of(nameAndClass._1, upperValue);
    }

    private static Tuple2<String, Object> toFieldNameAndEmptyValue(
            final Tuple2<String, Class<? extends Field>> nameAndClass) {
        Object emptyValue = EMPTY_VALUES_BY_FIELD_CLASS.get(nameAndClass._2);

        if (emptyValue == null) {
            throwUnsupportedTypeException(nameAndClass);
        }
        return Tuple.of(nameAndClass._1, emptyValue);
    }

    private static void throwUnsupportedTypeException(final Tuple2<String, Class<? extends Field>> nameAndClass) {
        throw new IllegalStateException(
                "Field '" + nameAndClass._1 + "' has the unsupported type " + nameAndClass._2.getSimpleName());
    }

    private static TestedRuleResult toParseErrorResult(final Throwable cause) {
        if (cause instanceof JexlException) {
            JexlException jexlException = (JexlException) cause;
            JexlInfo info = jexlException.getInfo();

            String[] detailedMessageParts = jexlException.getMessage().split(":\\d+\\s");
            String detailedMessage = detailedMessageParts[detailedMessageParts.length - 1];

            String message = String.format(
                    "%s (at line %s, column %s)",
                    detailedMessage, info.getLine(), info.getColumn());

            String errorCode;
            if (jexlException.getCause() instanceof ArithmeticException) {
                errorCode = "Arithmetic exception";
            } else {
                errorCode = createJexlExceptionErrorCode(jexlException.getClass().getSimpleName());
            }
            return TestedRuleResult.builder()
                    .errors(Arrays.asList(
                            Tuple.of(EXPRESSION_ERROR_CODE, errorCode),
                            Tuple.of(errorCode, message)))
                    .build();
        } else if (isNumberFormatExceptionFromDefaultContext(cause)) {
            return TestedRuleResult.builder()
                    .errors(Arrays.asList(
                            Tuple.of(EXPRESSION_ERROR_CODE, PARSE_ERROR_CODE),
                            Tuple.of(PARSE_ERROR_CODE, NUMBER_FORMAT_MSG)))
                    .build();
        }
        return TestedRuleResult.builder()
                .errors(Arrays.asList(
                        Tuple.of(EXPRESSION_ERROR_CODE, PARSE_ERROR_CODE),
                        Tuple.of(PARSE_ERROR_CODE, defaultIfBlank(cause.getMessage(), EMPTY))))
                .build();
    }

    private static boolean isNumberFormatExceptionFromDefaultContext(final Throwable cause) {
        return cause instanceof NumberFormatException
                && cause.getMessage().contains(SMALL_STRING_VALUE);
    }

    private static String createJexlExceptionErrorCode(final String jexlExceptionName) {
        if ("Ambiguous".equals(jexlExceptionName)) {
            return jexlExceptionName + EXPRESSION_SUFFIX;
        }
        return endsWith(jexlExceptionName, "xception")
                ? jexlExceptionName
                : jexlExceptionName + EXCEPTION_SUFFIX;
    }

    private static void logErrors(final TestedRuleResult context) {
        if (!context.getErrors().isEmpty() && LOGGER.isDebugEnabled()) {
            LOGGER.debug(
                    "Errors: {}",
                    context.getErrors().stream()
                            .map(Objects::toString)
                            .collect(joining(", ")));
        }
    }
}
