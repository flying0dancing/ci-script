import { RuleExample } from '../../schema-details/interfaces/rule-example.interface';
import { Field } from './field.interface';

export enum ValidationRuleType {
  Syntax = 'SYNTAX',
  Quality = 'QUALITY',
  Validity = 'VALIDITY',
  'Intra Series' = 'INTRA_SERIES'
}

export enum ValidationRuleSeverity {
  Critical = 'CRITICAL',
  Warning = 'WARNING'
}

export type TestResult = 'Pass' | 'Fail' | 'Error';

export interface ValidationRuleExampleField {
  id: number;
  name: string;
  value: string;
}

export interface ValidationRuleExample {
  id: number;
  expectedResult: string;
  validationRuleExampleFields: ValidationRuleExampleField[];
}

interface Rule {
  id: number;
  name: string;
  version: number;
  ruleId: string;
  validationRuleType: ValidationRuleType;
  validationRuleSeverity: ValidationRuleSeverity;
  description?: string;
  startDate: string;
  endDate: string;
  expression: string;
  contextFields: Field[];
}

export interface RuleResponse extends Rule {
  validationRuleExamples: ValidationRuleExample[];
}

export interface RuleRequest extends Rule {
  ruleExamples: RuleExample[];
}
