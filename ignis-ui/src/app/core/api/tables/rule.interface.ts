export enum ValidationRuleType {
  Syntax = "SYNTAX",
  Quality = "QUALITY",
  Validity = "VALIDITY",
  "Intra Series" = "INTRA_SERIES"
}

export enum ValidationRuleSeverity {
  Critical = "CRITICAL",
  Warning = "WARNING"
}

export interface Rule {
  id: number;
  name: string;
  version: number;
  ruleId: string;
  validationRuleType: ValidationRuleType;
  validationRuleSeverity: ValidationRuleSeverity;
  expression: string;
  description?: string;
  startDate: string;
  endDate: string;
}
