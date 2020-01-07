import { Schema } from '../..';

export interface ProductContextInterface {
  productId: number;
  productName?: string;
  schemas?: Schema[];
}
