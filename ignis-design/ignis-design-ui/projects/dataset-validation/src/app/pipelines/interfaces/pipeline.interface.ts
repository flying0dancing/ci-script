import { InputDagEdge } from '../../core/dag/dag-inputs.interface';
import { ApiError } from '../../core/utilities/interfaces/errors.interface';
import { PipelineStep } from './pipeline-step.interface';

export interface Pipeline {
  id: number;
  name: string;
  productId: number;
  steps: PipelineStep[];
  error: PipelineError;
}

export interface GraphNotConnected {
  connectedSets: number[][];
}

export interface PipelineError {
  errors: ApiError[];
  graphNotConnected: GraphNotConnected;
}

export interface PipelineEdge extends InputDagEdge {
  pipelineStep: PipelineStep;
}

export enum PipelineDisplayReposonseType {
  SUCCESS,
  ERROR
}

export interface PipelineConnectedSets {
  connectedSets: PipelineEdge[][];
  type: PipelineDisplayReposonseType.SUCCESS;
}

export interface PipelineCycleError {
  parentChain: number[];
  cyclicalStep: number;
}

export interface PipelineDisplayError {
  errorCode: string;
  errorMessage: string;
  cycleError: PipelineCycleError;
  type: PipelineDisplayReposonseType.ERROR;
}
