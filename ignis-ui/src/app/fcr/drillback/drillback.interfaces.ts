import { Page } from '@/core/api/common/pageable.interface';
import { PipelineInvocation, PipelineStepInvocation } from '@/core/api/pipelines/pipelines.interfaces';
import { Field } from '@/core/api/tables/tables.interfaces';

export type DatasetSchema = Field[];

export type Data = Map<String, Object>;
export type InputFieldToOutputField = Map<string, string>;

export interface DrillbackStepDetails {
  schemasIn: Map<number, DatasetSchema>;
  schemasOut: Map<number, DatasetSchema>;
  schemaToInputFieldToOutputFieldMapping: Map<string, InputFieldToOutputField>;
}

export interface DatasetRowData {
  datasetId: number;
  data: Data[];
  page: Page;
}

export interface PipelineInvocationSelectEvent {
  pipelineInvocation: PipelineInvocation;
  pipelineStepInvocation: PipelineStepInvocation;
}
