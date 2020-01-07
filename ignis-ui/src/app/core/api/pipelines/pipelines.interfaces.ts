import { Field } from '@/core/api/tables/tables.interfaces';
import { InputDagEdge } from '@/core/dag/dag-inputs.interface';

export enum StepType {
  MAP = "MAP",
  AGGREGATION = "AGGREGATION",
  JOIN = "JOIN",
  WINDOW = "WINDOW",
  UNION = "UNION",
  SCRIPTLET = "SCRIPTLET",
}

export interface Pipeline {
  id: number;
  name: string;
  steps: PipelineStep[];
}

export interface StepJoin {
  left: SchemaDetails;
  right: SchemaDetails;
  leftField: Field;
  rightField: Field;
}

export interface PipelineStep {
  id: number;
  name: string;
  description: string;
  type: StepType;
  schemaIn?: SchemaDetails;
  schemaOut: SchemaDetails;
  groupings?: string[];
  joins?: StepJoin[];
  unionInputSchemas: SchemaDetails[];
  jarFile: string;
  className: string;
}

export interface Select {
  select: string;
  outputFieldId: number;
  isWindow: boolean;
  window?: Window;
}

export interface Window {
  partitionBy: string[];
  orderBy: Order[];
}

export interface Order {
  fieldName: string;
  direction: OrderDirection;
  priority: number;
}

export enum OrderDirection {
  ASC = "ASC",
  DESC = "DESC"
}

export function findSchemas(pipelineStep: PipelineStep): SchemaDetails[] {
  switch (pipelineStep.type) {
    case StepType.MAP:
      return [pipelineStep.schemaIn, pipelineStep.schemaOut];
    case StepType.AGGREGATION:
      return [pipelineStep.schemaIn, pipelineStep.schemaOut];
    case StepType.WINDOW:
      return [pipelineStep.schemaIn, pipelineStep.schemaOut];
    case StepType.JOIN:
      const schemas = [];
      schemas.push(pipelineStep.schemaOut);
      for (const join of pipelineStep.joins) {
        schemas.push(join.left);
        schemas.push(join.right);
      }
      return schemas;
    case StepType.UNION:
      pipelineStep.unionInputSchemas.push(pipelineStep.schemaOut);
      return pipelineStep.unionInputSchemas;
    case StepType.SCRIPTLET:
      return [pipelineStep.schemaOut];
    default:
      throw Error("Step type not supported " + pipelineStep.type);
  }
}

export function findInputSchemas(pipelineStep: PipelineStep): SchemaDetails[] {
  switch (pipelineStep.type) {
    case StepType.MAP:
      return [pipelineStep.schemaIn];
    case StepType.AGGREGATION:
      return [pipelineStep.schemaIn];
    case StepType.WINDOW:
      return [pipelineStep.schemaIn];
    case StepType.JOIN:
      const schemas = [];
      for (const join of pipelineStep.joins) {
        if (schemas.filter(schema => schema.id === join.left.id).length === 0) {
          schemas.push(join.left);
        }
        if (schemas.filter(schema => schema.id === join.right.id).length === 0) {
          schemas.push(join.right);
        }
      }
      return schemas;
    case StepType.UNION:
      return pipelineStep.unionInputSchemas;
    default:
      throw Error('Step type not supported ' + pipelineStep.type);
  }
}

export enum PipelineStepStatus {
  PENDING = "PENDING",
  RUNNING = "RUNNING",
  SUCCESS = "SUCCESS",
  FAILED = 'FAILED',
  SKIPPED = 'SKIPPED'
}

export interface StartPipelineJobRequest {
  name: string;
  pipelineId: number;
  entityCode: string;
  referenceDate: string;
  stepId: number;
}

export interface SchemaDetails {
  id: number;
  displayName: string;
  physicalTableName: string;
  version: number;
}

export interface PipelineInvocation {
  id: number;
  name: string;
  createdTime: string;
  pipelineId: number;
  entityCode: string;
  referenceDate: string;
  invocationSteps: PipelineStepInvocation[];
}

export interface PipelineStepInvocation {
  id: number;
  datasetsIn: PipelineStepInvocationDataset[];
  inputPipelineStepIds: number[];
  datasetOutId: number;
  pipelineStep: PipelineStep;
  status: PipelineStepStatus;
}

export interface PipelineStepInvocationDataset {
  datasetId: number;
  datasetRunKey: number;
}

export interface PipelineEdge extends InputDagEdge {
  pipelineStep: PipelineStep;
}

export interface PipelineDownstream {
  pipelineId: number;
  pipelineName: string;
  requiredSchemas: SchemaDetails[];
}
