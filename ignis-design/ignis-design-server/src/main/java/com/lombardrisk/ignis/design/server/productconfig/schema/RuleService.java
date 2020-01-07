package com.lombardrisk.ignis.design.server.productconfig.schema;

import com.google.common.collect.ImmutableMap;
import com.lombardrisk.ignis.client.design.RuleExample;
import com.lombardrisk.ignis.client.external.rule.ExampleField;
import com.lombardrisk.ignis.data.common.error.ErrorResponse;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.data.common.service.CRUDService;
import com.lombardrisk.ignis.design.field.model.DoubleField;
import com.lombardrisk.ignis.design.field.model.Field;
import com.lombardrisk.ignis.design.field.model.FloatField;
import com.lombardrisk.ignis.design.server.productconfig.api.SchemaRepository;
import com.lombardrisk.ignis.design.server.productconfig.api.ValidationRuleRepository;
import com.lombardrisk.ignis.design.server.productconfig.rule.model.ValidationRule;
import com.lombardrisk.ignis.design.server.productconfig.rule.model.ValidationRuleExample;
import com.lombardrisk.ignis.design.server.productconfig.rule.model.ValidationRuleExampleField;
import com.lombardrisk.ignis.design.server.productconfig.rule.test.RuleExpressionTester;
import com.lombardrisk.ignis.design.server.productconfig.rule.test.TestedRuleExampleResult;
import com.lombardrisk.ignis.design.server.productconfig.rule.test.TestedRuleResult;
import io.vavr.Tuple2;
import io.vavr.control.Either;
import io.vavr.control.Option;
import io.vavr.control.Validation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.lombardrisk.ignis.client.external.rule.TestResultType.toTestResultType;
import static com.lombardrisk.ignis.design.server.productconfig.rule.test.RuleExpressionTester.ERRORS_ESTIMATION_COUNT;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

@Service
@Transactional
@Slf4j
public class RuleService implements CRUDService<ValidationRule> {

    private static final String RULE_NOT_UNIQUE_ERROR_CODE = "Rule is not unique";

    private final SchemaRepository schemaRepository;
    private final ValidationRuleRepository ruleRepository;
    private final RuleExpressionTester ruleExpressionTester;

    public RuleService(
            final SchemaRepository schemaRepository,
            final ValidationRuleRepository ruleRepository,
            final RuleExpressionTester ruleExpressionTester) {
        this.schemaRepository = schemaRepository;
        this.ruleRepository = ruleRepository;
        this.ruleExpressionTester = ruleExpressionTester;
    }

    @Override
    public String entityName() {
        return ValidationRule.class.getSimpleName();
    }

    @Override
    public Option<ValidationRule> findById(final long id) {
        return ruleRepository.findById(id);
    }

    @Override
    public List<ValidationRule> findAllByIds(final Iterable<Long> ids) {
        return ruleRepository.findAllById(ids);
    }

    @Override
    public List<ValidationRule> findAll() {
        throw new UnsupportedOperationException("Nah");
    }

    @Override
    public ValidationRule delete(final ValidationRule validationRule) {
        ruleRepository.delete(validationRule);

        return validationRule;
    }

    @Override
    public Validation<CRUDFailure, ValidationRule> deleteWithValidation(final long id) {
        return findWithValidation(id)
                .map(this::delete);
    }

    public Either<List<ErrorResponse>, ValidationRule> saveValidationRule(
            final Long tableId, final ValidationRule rule) {

        Option<Schema> optionalSchema = schemaRepository.findById(tableId);
        if (!optionalSchema.isDefined()) {
            return Either.left(singletonList(
                    CRUDFailure.notFoundIds(Schema.class.getSimpleName()).toErrorResponse()));
        }
        TestedRuleResult testedRuleResult = ruleExpressionTester.evaluateRuleExpression(rule, optionalSchema.get());

        List<ErrorResponse> errorResponses = checkRule(rule, optionalSchema.get(), testedRuleResult.getErrors());
        if (!errorResponses.isEmpty()) {
            return Either.left(errorResponses);
        }
        Optional<ValidationRule> optionalExistingRule = findExistingRule(rule.getId(), optionalSchema.get());
        Set<Field> contextFields = testedRuleResult.getContextFields();

        if (optionalExistingRule.isPresent()) {
            ValidationRule updatedRule =
                    updateRule(rule, optionalSchema.get(), optionalExistingRule.get(), contextFields);

            return Either.right(updatedRule);
        }
        return Either.right(createRule(rule, optionalSchema.get(), contextFields));
    }

    public List<RuleExample> testRuleExamples(final Long id) {
        Option<ValidationRule> optionalRule = ruleRepository.findById(id);

        return optionalRule.map(this::testRuleExample)
                .getOrElse(Collections::emptyList);
    }

    private List<RuleExample> testRuleExample(final ValidationRule rule) {
        return rule.getValidationRuleExamples().stream()
                .map(example -> toRuleExample(rule, example))
                .collect(toList());
    }

    private RuleExample toRuleExample(final ValidationRule rule, final ValidationRuleExample ruleExample) {
        TestedRuleExampleResult testedExampleResult = ruleExpressionTester.testExamples(ruleExample, rule);

        return RuleExample.builder()
                .id(ruleExample.getId())
                .expectedResult(toTestResultType(ruleExample.getExpectedResult().name()))
                .actualResult(testedExampleResult.getTestResultType())
                .exampleFields(toExampleFields(testedExampleResult.getExampleFieldsByField()))
                .unexpectedError(testedExampleResult.getUnexpectedError())
                .build();
    }

    private static ImmutableMap<String, ExampleField> toExampleFields(final Map<Field, ExampleField> fieldToExampleFieldMap) {
        return fieldToExampleFieldMap.entrySet().stream()
                .collect(toImmutableMap(
                        entry -> entry.getKey().getName(),
                        RuleService::toExampleField));
    }

    private static ExampleField toExampleField(final Map.Entry<Field, ExampleField> entry) {
        Field field = entry.getKey();
        ExampleField exampleField = entry.getValue();
        String value = exampleField.value;

        if (value != null
                && (field instanceof FloatField
                || field instanceof DoubleField)) {
            value = field.parse(value).toString();
        }
        return ExampleField.builder()
                .id(exampleField.id)
                .error(exampleField.error)
                .value(value)
                .build();
    }

    private List<ErrorResponse> checkRule(
            final ValidationRule rule,
            final Schema table,
            final List<Tuple2<String, String>> expressionErrors) {
        List<ErrorResponse> errorResponses = new ArrayList<>(ERRORS_ESTIMATION_COUNT);

        errorResponses.addAll(
                expressionErrors.stream()
                        .map(errTuple -> ErrorResponse.valueOf(errTuple._2, errTuple._1))
                        .collect(toList()));

        errorResponses.addAll(checkUniqueRule(rule, table));
        return errorResponses;
    }

    private static Optional<ValidationRule> findExistingRule(final Long id, final Schema table) {
        return table.getValidationRules().stream()
                .filter(existingRule -> existingRule.getId().equals(id))
                .findFirst();
    }

    private ValidationRule updateRule(
            final ValidationRule rule,
            final Schema table,
            final ValidationRule existingRule,
            final Set<Field> contextFields) {
        ValidationRule updatedRule = editRule(existingRule, rule, contextFields);

        ValidationRule savedRule = ruleRepository.save(updatedRule);

        log.info("Edited rule with id [{}] for table id [{}]", savedRule.getId(), table.getId());
        return savedRule;
    }

    private static ValidationRule editRule(
            final ValidationRule existingRule,
            final ValidationRule updatedRule,
            final Set<Field> contextFields) {
        existingRule.setRuleId(updatedRule.getRuleId());
        existingRule.setName(updatedRule.getName());
        existingRule.setVersion(updatedRule.getVersion());

        existingRule.setValidationRuleSeverity(updatedRule.getValidationRuleSeverity());
        existingRule.setValidationRuleType(updatedRule.getValidationRuleType());
        existingRule.setDescription(updatedRule.getDescription());

        existingRule.setStartDate(updatedRule.getStartDate());
        existingRule.setEndDate(updatedRule.getEndDate());
        existingRule.setExpression(updatedRule.getExpression());

        existingRule.setContextFields(contextFields);

        editRuleExamples(existingRule.getValidationRuleExamples(), updatedRule.getValidationRuleExamples());

        return existingRule;
    }

    private static void editRuleExamples(
            final Set<ValidationRuleExample> existingValidationRuleExamples,
            final Set<ValidationRuleExample> updatedValidationRuleExamples) {
        deleteRemovedExamples(existingValidationRuleExamples, updatedValidationRuleExamples);

        addNewExamples(existingValidationRuleExamples, updatedValidationRuleExamples);

        editExistingExamples(existingValidationRuleExamples, updatedValidationRuleExamples);
    }

    private static void deleteRemovedExamples(
            final Set<ValidationRuleExample> existingValidationRuleExamples,
            final Set<ValidationRuleExample> updatedValidationRuleExamples) {
        Set<Long> updatedRuleExampleIds =
                updatedValidationRuleExamples
                        .stream()
                        .map(ValidationRuleExample::getId)
                        .filter(Objects::nonNull)
                        .collect(toSet());

        existingValidationRuleExamples
                .removeIf(existingExample -> !updatedRuleExampleIds.contains(existingExample.getId()));
    }

    private static void addNewExamples(
            final Set<ValidationRuleExample> existingValidationRuleExamples,
            final Set<ValidationRuleExample> updatedValidationRuleExamples) {
        Set<ValidationRuleExample> newExamples =
                updatedValidationRuleExamples
                        .stream()
                        .filter(RuleService::isNewExample)
                        .collect(toSet());

        existingValidationRuleExamples.addAll(newExamples);
    }

    private static boolean isNewExample(final ValidationRuleExample updatedExample) {
        return updatedExample.getId() == null;
    }

    private static void editExistingExamples(
            final Set<ValidationRuleExample> existingRuleExamples,
            final Set<ValidationRuleExample> updatedRuleExamples) {

        for (ValidationRuleExample existingExample : existingRuleExamples) {
            if (isNotNewExample(existingExample)) {
                ValidationRuleExample updatedRuleExample =
                        updatedRuleExamples.stream()
                                .filter(RuleService::isNotNewExample)
                                .filter(updatedExample -> updatedExample.getId().equals(existingExample.getId()))
                                .findFirst()
                                .orElseThrow(RuntimeException::new);

                existingExample.setExpectedResult(updatedRuleExample.getExpectedResult());

                editExistingExampleFields(
                        existingExample.getValidationRuleExampleFields(),
                        updatedRuleExample.getValidationRuleExampleFields());
            }
        }
    }

    private static boolean isNotNewExample(final ValidationRuleExample existingRuleExample) {
        return existingRuleExample.getId() != null;
    }

    private static void editExistingExampleFields(
            final Set<ValidationRuleExampleField> existingRuleExampleFields,
            final Set<ValidationRuleExampleField> updatedRuleExampleFields) {

        addNewExampleFields(existingRuleExampleFields, updatedRuleExampleFields);

        updateExampleFields(existingRuleExampleFields, updatedRuleExampleFields);
    }

    private static void addNewExampleFields(
            final Set<ValidationRuleExampleField> existingRuleExampleFields,
            final Set<ValidationRuleExampleField> updatedRuleExampleFields) {
        updatedRuleExampleFields.stream()
                .filter(updatedExampleField -> updatedExampleField.getId() == null)
                .forEach(existingRuleExampleFields::add);
    }

    private static void updateExampleFields(
            final Set<ValidationRuleExampleField> existingRuleExampleFields,
            final Set<ValidationRuleExampleField> updatedRuleExampleFields) {

        Map<Long, ValidationRuleExampleField> ruleExampleFieldsById = updatedRuleExampleFields.stream()
                .filter(updatedExampleField -> updatedExampleField.getId() != null)
                .collect(toMap(
                        ValidationRuleExampleField::getId,
                        exampleField -> exampleField));

        for (ValidationRuleExampleField existingExampleField : existingRuleExampleFields) {
            ValidationRuleExampleField updatedExampleField = ruleExampleFieldsById.get(existingExampleField.getId());

            if (updatedExampleField != null) {
                existingExampleField.setName(updatedExampleField.getName());
                existingExampleField.setValue(updatedExampleField.getValue());
            }
        }
    }

    private ValidationRule createRule(
            final ValidationRule newRule,
            final Schema table,
            final Set<Field> contextFields) {
        newRule.setContextFields(contextFields);
        table.getValidationRules().add(newRule);
        Schema updatedSchema = schemaRepository.saveSchema(table);

        ValidationRule savedRule = updatedSchema.getValidationRules().stream()
                .filter(tableRule -> isSameRule(tableRule, newRule))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Cannot find rule with ruleId with " + newRule.getRuleId() + " after persist"));

        log.info("Added rule with ruleId [{}] to table [{}]", savedRule.getRuleId(), table.getId());
        return savedRule;
    }

    private List<ErrorResponse> checkUniqueRule(final ValidationRule rule, final Schema table) {
        if (isEdited(rule)) {
            return emptyList();
        }
        if (isNotUniqueRule(rule, table.getValidationRules())) {
            return Arrays.asList(
                    ErrorResponse.valueOf(RULE_NOT_UNIQUE_ERROR_CODE, "ruleId"),
                    ErrorResponse.valueOf(
                            "A new rule must have a unique Regulator Reference, Version, Start Date and End Date",
                            RULE_NOT_UNIQUE_ERROR_CODE)
            );
        }
        return emptyList();
    }

    private static boolean isEdited(final ValidationRule rule) {
        return rule.getId() != null;
    }

    private static boolean isNotUniqueRule(final ValidationRule newRule, final Set<ValidationRule> validationRules) {
        return validationRules.stream()
                .anyMatch(existingRule -> isSameRule(existingRule, newRule));
    }

    private static boolean isSameRule(final ValidationRule existingRule, final ValidationRule newRule) {
        return existingRule.getRuleId().equals(newRule.getRuleId())
                && existingRule.getVersion().equals(newRule.getVersion())
                && existingRule.getStartDate().equals(newRule.getStartDate())
                && existingRule.getEndDate().equals(newRule.getEndDate());
    }
}
