import {
  Rule,
  ValidationRuleSeverity,
  ValidationRuleType
} from "@/core/api/tables/rule.interface";
import { GetTablesResponse, Table, UploadResponse } from "../tables.interfaces";

export const validationRule: Rule = {
  id: 1,
  ruleId: "1",
  name: "1",
  version: 1,
  description: "rule 1",
  startDate: "1970-01-01",
  endDate: "1970-01-01",
  expression: "TEST != null",
  validationRuleSeverity: ValidationRuleSeverity.Critical,
  validationRuleType: ValidationRuleType.Validity
};

export const table: Table = {
  id: 1,
  physicalTableName: "test",
  displayName: "test display name",
  createdTime: 123,
  startDate: "1970-01-01",
  version: 2,
  hasDatasets: true,
  validationRules: [validationRule]
};

export const tables: Table[] = [table, table];

export const getResponseSuccess: GetTablesResponse = {
  data: tables
};

export const uploadResponseSuccess: UploadResponse = { success: true };
