export type FieldTypes =
  | 'decimal'
  | 'string'
  | 'int'
  | 'long'
  | 'double'
  | 'float'
  | 'boolean'
  | 'date'
  | 'timestamp';

export interface Field {
  id: number;
  name: string;
  type: FieldTypes;
  format?: string;
  minLength?: number;
  maxLength?: number;
  regularExpression?: string;
  nullable?: boolean;
  precision?: number;
  scale?: number;
}
