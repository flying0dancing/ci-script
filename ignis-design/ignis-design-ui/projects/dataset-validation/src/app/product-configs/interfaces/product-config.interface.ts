import { Schema } from '../../schemas';

export interface ProductConfig {
  id: number;
  name: string;
  version: string;
  schemas: Schema[];
}
