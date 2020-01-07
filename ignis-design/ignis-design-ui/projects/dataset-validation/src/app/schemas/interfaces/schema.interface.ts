import { Field } from './field.interface';
import { RuleResponse } from './rule.interface';

export interface Schema {
  id: number;
  physicalTableName: string;
  displayName: string;
  majorVersion: number;
  latest: boolean;
  createdBy: string;
  createdTime: number;
  startDate: string;
  endDate: string;
  fields: Field[];
  validationRules: RuleResponse[];
}

export interface SchemaMetadata {
  physicalTableName: string;
  joinSchemaType: string;
  aggregationSchemaType: string;
  aggregationGroupingFieldNames: string[];
  fieldNames: string[];
}

export function schemaVersionDisplay(schema: Schema) {
  return `${schema.displayName} v${schema.majorVersion}`;
}
