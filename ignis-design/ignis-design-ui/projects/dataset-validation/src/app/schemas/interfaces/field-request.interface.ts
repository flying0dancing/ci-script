export class FieldRequest {
  id?: number;
  name: string;
  type: string;
  format?: string;
  maxLength?: number;
  minLength?: number;
  regularExpression: string;
  nullable?: boolean;
  precision?: number;
  scale?: number;

  constructor(type: string, name?: string) {
    this.name = name ? name.toUpperCase() : null;
    this.type = type;
  }
}
