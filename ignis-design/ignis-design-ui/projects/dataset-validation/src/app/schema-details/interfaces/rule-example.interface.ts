import { TestResult } from '../../schemas';

export interface ExampleField {
  id: number;
  value: string;
  error?: string;
}

export interface RuleExample {
  id: number;
  expectedResult: TestResult;
  actualResult: TestResult;
  exampleFields: {
    [fieldName: string]: ExampleField;
  };
  unexpectedError?: string;
}
