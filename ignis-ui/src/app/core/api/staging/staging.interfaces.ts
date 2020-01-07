import { Page } from '@/core/api/common/pageable.interface';
import { DatasetId } from '../datasets/datasets.types';
import { StagingJobId } from './staging.types';

export enum JobStatus {
  ABANDONED = "ABANDONED",
  COMPLETED = "COMPLETED",
  FAILED = "FAILED",
  STOPPED = "STOPPED",
  STOPPING = "STOPPING",
  STARTED = "STARTED",
  STARTING = "STARTING",
  UNKNOWN = "UNKNOWN"
}

export enum ExitStatus {
  EXECUTING = "EXECUTING",
  COMPLETED = "COMPLETED",
  NOOP = "NOOP",
  STOPPED = "STOPPED",
  FAILED = "FAILED",
  UNKNOWN = "UNKNOWN"
}

export const RunningJobStatuses = [
  JobStatus.STARTING,
  JobStatus.STARTED,
  JobStatus.STOPPING
];

export function isRunning(jobExecution: JobExecution): boolean {
  return (
    RunningJobStatuses.findIndex(status => status === jobExecution.status) !==
    -1
  );
}

export enum DatasetStatus {
  REGISTERED = "REGISTERED",
  VALIDATION_FAILED = "VALIDATION_FAILED",
  REGISTRATION_FAILED = "REGISTRATION_FAILED",
  UPLOADING = "UPLOADING",
  UPLOAD_FAILED = "UPLOAD_FAILED"
}

export enum JobType {
  STAGING = "STAGING",
  VALIDATION = "VALIDATION",
  IMPORT_PRODUCT = "IMPORT_PRODUCT",
  ROLLBACK_PRODUCT = "ROLLBACK_PRODUCT",
  PIPELINE = "PIPELINE"
}

export interface JobId {
  id: StagingJobId;
}

export interface GetJobsResponse {
  data: JobExecution[];
  page: Page;
}

export interface JobExecution {
  id: StagingJobId;
  name: string;
  serviceRequestType: JobType;
  exitCode: ExitStatus;
  status: JobStatus;
  startTime: number;
  endTime: number;
  yarnApplicationTrackingUrl?: string;
  createUser: string;
  requestMessage?: string;
  errors: string[];
}

export interface StagingJobsBody {
  name: string;
  metadata: DatasetMetadata;
  items: StagingItemRequest[];
  downstreamPipelines: string[];
}

export interface ValidationJobRequest {
  datasetId: number;
  name: string;
}

export interface StagingItemRequest {
  id?: number;
  source: CsvDataSource;
  schema: string;
  autoValidate: boolean;
  appendToDatasetId: number | null;
}

export interface CsvDataSource {
  filePath: string;
  header: boolean;
}

export interface DatasetMetadata {
  entityCode: string;
  referenceDate: string;
}

export interface EligiblePipeline {
  pipelineId: number;
  pipelineName: string;
  enabled: boolean;
  requiredSchemas: string[];
}

export interface Dataset {
  endTime: number;
  id: DatasetId;
  jobExecutionId: StagingJobId;
  lastUpdateTime: number;
  message: string;
  stagingFile: string;
  startTime: number;
  status: DatasetStatus;
  schema: StagingSchema;
  validationErrorFile?: string;
  validationErrorFileUrl?: string;
  datasetId?: number;
}

export interface StagingSchema {
  name: string;
  physicalName: string;
}

export type GetDatasetsByJobResponse = Dataset[];

export interface GetDatasetsParameters {
  jobExecutionId?: StagingJobId;
  datasetId?: DatasetId;
}
