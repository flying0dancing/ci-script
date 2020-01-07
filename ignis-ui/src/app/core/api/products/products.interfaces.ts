import { Pipeline } from "@/core/api/pipelines/pipelines.interfaces";
import { Table } from "@/core/api/tables/tables.interfaces";

export type ImportStatus = "SUCCESS" | "IN_PROGRESS" | "ERROR";

export interface Product {
  id: number;
  name: string;
  version: string;
  schemas: Table[];
  pipelines: Pipeline[];
  importStatus: ImportStatus;
}

export interface UploadResponse {
  id: number;
}
