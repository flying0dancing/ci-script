import { Rule } from "@/core/api/tables/rule.interface";

export interface GetTablesResponse {
  data: Table[];
}

export interface Table {
  id: number;
  physicalTableName: string;
  displayName: string;
  version: number;
  hasDatasets: boolean;
  createdTime: number;
  startDate: string;
  endDate?: string;
  validationRules: Rule[];
}

export interface Field {
  id: number;
  name: string;
  nullable: boolean;
  precision: number;
  scale: number;
  type: string;
  fieldType: string;
}

export interface UploadResponse {
  success: boolean;
}
