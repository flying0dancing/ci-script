import {
  RuleResponse,
  ValidationRuleSeverity,
  ValidationRuleType
} from '../app/schemas';

export function rule(): RuleResponse {
  return {
    id: 1,
    ruleId: 'r1',
    name: 'rule-1',
    version: 1,
    validationRuleSeverity: ValidationRuleSeverity.Critical,
    validationRuleType: ValidationRuleType.Syntax,
    startDate: '2018-01-01',
    endDate: '2019-01-01',
    description: 'Rule 1 description',
    expression: 'true',
    contextFields: [{ id: 1231, name: 'F1', type: 'int' }],
    validationRuleExamples: [
      {
        id: 3,
        expectedResult: 'Pass',
        validationRuleExampleFields: [{ id: 34, name: 'F1', value: 'abc' }]
      }
    ]
  };
}
