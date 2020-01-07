import { Product } from "@/core/api/products/products.interfaces";
import { Table } from "@/core/api/tables/tables.interfaces";

export const table1: Table = {
  id: 1,
  physicalTableName: "table1",
  displayName: "table1 display name",
  createdTime: 0,
  version: 0,
  startDate: "1970-01-01",
  hasDatasets: true,
  validationRules: []
};
export const table2: Table = {
  id: 2,
  physicalTableName: "table2",
  displayName: "table2 display name",
  createdTime: 0,
  version: 0,
  startDate: "1970-01-01",
  hasDatasets: true,
  validationRules: []
};

export const product1: Product = {
  id: 100,
  name: "product1",
  version: "1.0",
  schemas: [table1],
  pipelines: [],
  importStatus: "SUCCESS"
};
export const product = product1;

export const product2: Product = {
  id: 200,
  name: "product2",
  version: "2.0",
  schemas: [table2, table1],
  pipelines: [],
  importStatus: "ERROR"
};

export const products: Product[] = [product1, product2];
