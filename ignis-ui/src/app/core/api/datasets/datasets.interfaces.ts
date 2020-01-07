import { DatasetId, ValidationJobId } from './datasets.types';

export enum ValidationStatus {
  NOT_VALIDATED = "NOT_VALIDATED",
  VALIDATING = "VALIDATING",
  VALIDATED = "VALIDATED",
  QUEUED = "QUEUED",
  VALIDATION_FAILED = "VALIDATION_FAILED"
}

export interface Dataset {
  id: number;
  table: string;
  tableId: number;
  name: string;
  datasetType: string;
  validationStatus: ValidationStatus;
  pipelineJobId: number;
  pipelineInvocationId: number;
  pipelineStepInvocationId: number;
  validationJobId: ValidationJobId;
  createdTime: string;
  lastUpdated: string;
  metadataKey: string;
  metadata: string;
  recordsCount: number;
  rowKeySeed: number;
  predicate: string;
  entityCode: string;
  referenceDate: string;
  localReferenceDate: string;
  hasRules: boolean;
  runKey: number;
}

export interface GetDatasetsResponse {
  _embedded: {
    datasetList: Dataset[];
  };
}

export interface PutDatasetBody {
  datasetId: DatasetId;
  state: string;
}
