import { Product } from "@/core/api/products/products.interfaces";
import { Table } from "@/core/api/tables/tables.interfaces";
import {
  ProductItemGroupKey,
  ProductRowItem,
  productToProductSchemaAndPipelines
} from "@/fcr/dashboard/products/product-schema.interface";

describe("productToProductSchema", () => {
  it("should map null product tables to empty list", () => {
    const product: Product = {
      id: 100,
      name: "name",
      version: "version",
      schemas: null,
      pipelines: [],
      importStatus: "SUCCESS"
    };

    const schemas: ProductRowItem[] = productToProductSchemaAndPipelines(
      product
    );

    expect(schemas).toEqual([]);
  });

  it("should map empty product tables to empty list", () => {
    const product: Product = {
      id: 100,
      name: "name",
      version: "version",
      schemas: [],
      importStatus: "SUCCESS",
      pipelines: []
    };

    const schemas: ProductRowItem[] = productToProductSchemaAndPipelines(
      product
    );

    expect(schemas).toEqual([]);
  });

  it("should map product tables to new objects", () => {
    const table1: Table = {
      id: 101,
      displayName: "table1",
      physicalTableName: "tbl1",
      version: 1,
      startDate: "1",
      endDate: "2",
      createdTime: 0,
      validationRules: [],
      hasDatasets: false
    };
    const table2: Table = {
      id: 102,
      displayName: "table2",
      physicalTableName: "tbl2",
      version: 0,
      startDate: "10",
      createdTime: 0,
      validationRules: [],
      hasDatasets: false
    };
    const product: Product = {
      id: 100,
      name: "name",
      version: "version",
      schemas: [table1, table2],
      importStatus: "SUCCESS",
      pipelines: []
    };

    const schemas: ProductRowItem[] = productToProductSchemaAndPipelines(
      product
    );

    expect(schemas).toEqual([
      {
        productId: 100,
        productName: "name",
        productVersion: "version",
        itemId: 101,
        itemName: "table1",
        group: ProductItemGroupKey.SCHEMA,
        version: 1,
        startDate: "1",
        endDate: "2",
        createdTime: 0,
        importStatus: "SUCCESS"
      },
      {
        productId: 100,
        productName: "name",
        productVersion: "version",
        itemId: 102,
        itemName: "table2",
        group: ProductItemGroupKey.SCHEMA,
        version: 0,
        startDate: "10",
        createdTime: 0,
        importStatus: "SUCCESS"
      }
    ]);
  });
});
