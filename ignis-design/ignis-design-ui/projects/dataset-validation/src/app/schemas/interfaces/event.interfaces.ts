import { Schema } from '../index';

export interface EditSchemaEvent {
  schema: Schema;
}

export interface OpenNewSchemaVersionDialog {
  schemaId: number;
  schemaDisplayName: string;
  previousStartDate: string;
}

export interface NewSchemaVersion {
  id: number;
  startDate: number;
}
