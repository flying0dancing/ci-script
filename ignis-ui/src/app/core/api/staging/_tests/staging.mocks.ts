import {
  Dataset,
  DatasetStatus,
  ExitStatus,
  GetDatasetsByJobResponse,
  GetJobsResponse,
  JobExecution,
  JobId,
  JobStatus,
  JobType,
  StagingJobsBody,
  ValidationJobRequest
} from '../staging.interfaces';

export const stagingJob: StagingJobsBody = {
  name: "test",
  metadata: {
    entityCode: "ENTITY123",
    referenceDate: "01/01/2001",
  },
  items: [
    {
      id: 1,
      source: {
        filePath: "path/to/file",
        header: true
      },
      schema: "schema",
      autoValidate: true,
      appendToDatasetId: null
    }
  ],
  downstreamPipelines: []
};

export const validationJobRequest: ValidationJobRequest = {
  datasetId: 123,
  name: "test"
};

export const jobId: JobId = {
  id: 453
};

export const dataset: Dataset = {
  id: 1,
  startTime: 1518517732000,
  endTime: 1518517732000,
  stagingFile: "/staging/file",
  validationErrorFile: "/validation/file",
  schema: {
    name: "schema",
    physicalName: "physicalSchema"
  },
  status: DatasetStatus.REGISTERED,
  lastUpdateTime: 1518517732000,
  jobExecutionId: 1,
  message: "message"
};

export const job: JobExecution = {
  id: 1,
  name: "test",
  serviceRequestType: JobType.STAGING,
  exitCode: ExitStatus.COMPLETED,
  status: JobStatus.COMPLETED,
  startTime: 1,
  endTime: 1,
  yarnApplicationTrackingUrl: "/yarn",
  createUser: "admin",
  requestMessage: "",
  errors: []
};

export const validationJob: JobExecution = {
  id: 2,
  name: "validate rules",
  serviceRequestType: JobType.VALIDATION,
  exitCode: ExitStatus.COMPLETED,
  status: JobStatus.COMPLETED,
  startTime: 1,
  endTime: 1,
  yarnApplicationTrackingUrl: "/yarn",
  createUser: "admin",
  requestMessage: "12345",
  errors: []
};

export const jobRunning: JobExecution = {
  ...job,
  exitCode: ExitStatus.EXECUTING,
  status: JobStatus.STARTED,
  endTime: null
};

export const jobs: JobExecution[] = [job];

export const getJobsResponse: GetJobsResponse = {
  data: jobs,
  page: { number: 0, size: 1, totalElements: 1, totalPages: 1 }
};

export const getDatasetsByJobResponse: GetDatasetsByJobResponse = [
  dataset,
  dataset
];
