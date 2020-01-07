import {
  ProductItemGroupKey,
  ProductRowItem
} from "@/fcr/dashboard/products/product-schema.interface";

export const productSchema: ProductRowItem = {
  productId: 100,
  productName: "name",
  productVersion: "version",
  group: ProductItemGroupKey.SCHEMA,
  itemId: 101,
  version: 0,
  itemName: "display name for table1",
  startDate: "1970-01-01",
  endDate: null,
  createdTime: 0,
  importStatus: "SUCCESS"
};
