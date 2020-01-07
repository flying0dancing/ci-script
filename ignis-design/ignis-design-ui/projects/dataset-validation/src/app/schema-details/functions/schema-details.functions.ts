import { Field } from 'projects/dataset-validation/src/app/schemas/interfaces/field.interface';
import { areEqualLocalDates } from '../../core/utilities';
import {
  RuleRequest,
  RuleResponse,
  TestResult,
  ValidationRuleExample
} from '../../schemas';
import {
  ExampleField,
  RuleExample
} from '../interfaces/rule-example.interface';

function hasChanged(foundRule: RuleResponse, rule: RuleRequest): boolean {
  return (
    foundRule.id !== rule.id ||
    foundRule.ruleId !== rule.ruleId ||
    foundRule.name !== rule.name ||
    foundRule.version !== rule.version ||
    foundRule.expression !== rule.expression ||
    !areEqualLocalDates(foundRule.startDate, rule.startDate) ||
    !areEqualLocalDates(foundRule.endDate, rule.endDate) ||
    foundRule.validationRuleType !== rule.validationRuleType ||
    foundRule.validationRuleSeverity !== rule.validationRuleSeverity ||
    foundRule.description !== rule.description
  );
}

export function isNewOrEdited(
  ruleRequest: RuleRequest,
  currentRules: RuleResponse[]
): boolean {
  const foundRule = currentRules.find(
    (currentRule: RuleResponse) => currentRule.id === ruleRequest.id
  );

  return !foundRule || hasChanged(foundRule, ruleRequest);
}

function hasFieldChanged(foundField: Field, field: Field): boolean {
  return (
    foundField.id !== field.id ||
    foundField.name !== field.name ||
    foundField.type !== field.type ||
    foundField.format !== field.format ||
    foundField.minLength !== field.minLength ||
    foundField.maxLength !== field.maxLength ||
    foundField.regularExpression !== field.regularExpression ||
    foundField.nullable !== field.nullable ||
    foundField.precision !== field.precision ||
    foundField.scale !== field.scale
  );
}

export function isFieldNew(field: Field, currentFields: Field[]): boolean {
  const foundField = currentFields.find(
    currentField => currentField.id === field.id
  );

  return !foundField;
}

export function isFieldEdited(field: Field, currentFields: Field[]): boolean {
  const foundField = currentFields.find(
    currentField => currentField.id === field.id
  );

  return hasFieldChanged(foundField, field);
}

export function emptyExampleFieldIfNull(
  currentExampleField: ExampleField
): ExampleField {
  if (currentExampleField) {
    return { ...currentExampleField } as ExampleField;
  } else {
    return { id: null, value: null, error: null };
  }
}

export const NOT_NULLABLE_ERROR_MSG = 'must not be null';

export function createFieldNotNullableError(fieldName: string): string {
  return `Field [${fieldName}] is not nullable`;
}

function toExpectedResult(expectedResult: string): TestResult {
  switch (expectedResult.toUpperCase()) {
    case 'PASS':
      return 'Pass';
    case 'FAIL':
      return 'Fail';
    case 'ERROR':
      return 'Error';
  }
}

export function toRuleExamples(
  validationRuleExamples: ValidationRuleExample[]
): RuleExample[] {
  return (validationRuleExamples ? validationRuleExamples : []).map(
    validationExample => {
      const exampleFields = validationExample.validationRuleExampleFields.reduce(
        (exampleFieldsMap, validationExampleField) => {
          const fieldName = validationExampleField.name;

          exampleFieldsMap[fieldName] = {};
          exampleFieldsMap[fieldName].id = validationExampleField.id;
          exampleFieldsMap[fieldName].value = validationExampleField.value;

          return exampleFieldsMap;
        },
        {}
      );
      return {
        id: validationExample.id,
        expectedResult: toExpectedResult(validationExample.expectedResult),
        actualResult: null,
        exampleFields: exampleFields
      };
    }
  );
}
