import { FieldRequest } from './field-request.interface';
import { RuleResponse } from './rule.interface';

export const CREATE_REQUEST = 'create';

export class CreateSchemaRequest {
  readonly type = CREATE_REQUEST;
  physicalTableName: string;
  displayName: string;
  fields: FieldRequest[];
  startDate: string;
  endDate: string;
  majorVersion: number;
  validationRules: RuleResponse[];

  constructor(
    startDate: string,
    physicalTableName?: string,
    displayName?: string,
    endDate?: string
  ) {
    this.physicalTableName = physicalTableName
      ? physicalTableName.toUpperCase()
      : null;
    this.displayName = displayName ? displayName.toUpperCase() : null;
    this.startDate = startDate;
    this.endDate = endDate;
    this.fields = [];
    this.validationRules = [];
  }
}
