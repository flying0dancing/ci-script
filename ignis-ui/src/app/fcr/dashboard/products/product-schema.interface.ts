import { Pipeline } from "@/core/api/pipelines/pipelines.interfaces";
import { ImportStatus, Product } from "@/core/api/products/products.interfaces";
import { Table } from "@/core/api/tables/tables.interfaces";
import { flatten } from "@/shared/utilities/array.utilities";

export enum ProductItemGroupKey {
  SCHEMA = "Schemas",
  PIPELINE = "Pipelines"
}

export interface ProductRowItem {
  importStatus: ImportStatus;
  productId: number;
  productName: string;
  productVersion: string;
  group: ProductItemGroupKey;
  itemId: number;
  version: number;
  itemName: string;
  createdTime: number;
  startDate: string;
  endDate: string;
}

export function productToProductSchemaAndPipelines(
  product: Product
): ProductRowItem[] {
  const tables: Table[] = product.schemas == null ? [] : product.schemas;
  const pipelines: Pipeline[] =
    product.pipelines == null ? [] : product.pipelines;

  const productRowItems = [];
  productRowItems.push(
    tables.map(table => toProductSchemaRowItem(product, table))
  );
  productRowItems.push(
    pipelines.map(pipeline => toProductPipelineRowItem(product, pipeline))
  );

  return flatten(productRowItems);
}

function toProductSchemaRowItem(
  product: Product,
  table: Table
): ProductRowItem {
  return {
    importStatus: product.importStatus,
    productId: product.id,
    productName: product.name,
    productVersion: product.version,
    group: ProductItemGroupKey.SCHEMA,
    itemId: table.id,
    version: table.version,
    itemName: table.displayName,
    createdTime: table.createdTime,
    startDate: table.startDate,
    endDate: table.endDate
  };
}

function toProductPipelineRowItem(
  product: Product,
  pipeline: Pipeline
): ProductRowItem {
  return {
    importStatus: product.importStatus,
    productId: product.id,
    productName: product.name,
    productVersion: product.version,
    group: ProductItemGroupKey.PIPELINE,
    itemId: pipeline.id,
    version: null,
    itemName: pipeline.name,
    createdTime: null,
    startDate: null,
    endDate: null
  };
}
