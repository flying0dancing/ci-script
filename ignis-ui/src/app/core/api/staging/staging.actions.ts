import { Pageable } from '@/core/api/common/pageable.interface';
import { StartPipelineJobRequest } from '@/core/api/pipelines/pipelines.interfaces';
import { Action } from '@ngrx/store';

import * as Interfaces from './staging.interfaces';
import { JobExecution, JobId, ValidationJobRequest } from './staging.interfaces';
import * as Types from './staging.types';

export const GET = "staging/get";
export const GET_SILENT = "staging/get-silent";
export const GET_SUCCESS = "staging/get-success";
export const GET_FAIL = "staging/get-fail";
export const GET_BY_ID = "staging/get-by-id";
export const GET_BY_ID_SUCCESS = "staging/get-by-id-success";
export const GET_BY_ID_FAIL = "staging/get-by-id-fail";
export const GET_RUNNING_JOBS = "staging/get-running-jobs";
export const GET_RUNNING_JOBS_SUCCESS = "staging/get-running-jobs-success";
export const GET_RUNNING_JOBS_FAIL = "staging/get-running-jobs-fail";
export const START_STAGING_JOB = "staging/post";
export const START_STAGING_JOB_SUCCESS = "staging/post-success";
export const START_STAGING_JOB_FAIL = "staging/post-fail";
export const VALIDATE = "staging/validate";
export const VALIDATE_SUCCESS = "staging/validate-success";
export const VALIDATE_FAIL = "staging/validate-fail";
export const EMPTY = "staging/empty";
export const STOP_JOB = "staging/stop-job";
export const GET_JOB_DATASETS = "staging/get-job-datasets";
export const GET_JOB_DATASETS_SUCCESS = "staging/get-job-datasets-success";
export const GET_JOB_DATASETS_FAIL = "staging/get-job-datasets-fail";
export const GET_JOB_DATASETS_FOR_DATASET = "staging/get-job-datasets-for-dataset";
export const GET_JOB_DATASETS_FOR_DATASET_SUCCESS = "staging/get-job-datasets-for-dataset-success";
export const GET_JOB_DATASETS_FOR_DATASET_FAIL = "staging/get-job-datasets-for-dataset-fail";
export const START_PIPELINE_JOB = "pipelines/start-job";
export const START_PIPELINE_JOB_SUCCESS = "pipelines/start-job-success";
export const START_PIPELINE_JOB_FAIL = "pipelines/start-job-fail";

export class Get implements Action {
  readonly type = GET;

  constructor(public payload: { reducerMapKey: string, page?: Pageable }) {}
}

export class GetSilent implements Action {
  readonly type = GET_SILENT;

  constructor(public payload: { reducerMapKey: string }) {}
}

export class GetSuccess implements Action {
  readonly type = GET_SUCCESS;

  constructor(
    public payload: { reducerMapKey: string; jobs: Interfaces.JobExecution[] }
  ) {}
}

export class GetFail implements Action {
  readonly type = GET_FAIL;

  constructor(public payload: { reducerMapKey: string }) {}
}

export class GetById implements Action {
  readonly type = GET_BY_ID;

  constructor(public payload: { reducerMapKey: string, jobId: number }) {}
}

export class GetByIdSuccess implements Action {
  readonly type = GET_BY_ID_SUCCESS;

  constructor(
    public payload: { reducerMapKey: string; job: JobExecution }
  ) {}
}

export class GetByIdFail implements Action {
  readonly type = GET_BY_ID_FAIL;

  constructor(public payload: { reducerMapKey: string }) {}
}

export class GetRunningJobs implements Action {
  readonly type = GET_RUNNING_JOBS;

  constructor(public payload: { reducerMapKey: string }) {}
}

export class GetRunningJobsSuccess implements Action {
  readonly type = GET_RUNNING_JOBS_SUCCESS;

  constructor(
    public payload: { reducerMapKey: string; jobs: JobExecution[] }
  ) {}
}

export class GetRunningJobsFail implements Action {
  readonly type = GET_RUNNING_JOBS_FAIL;

  constructor(public payload: { reducerMapKey: string }) {}
}

export class StartStagingJob implements Action {
  readonly type = START_STAGING_JOB;

  constructor(
    public payload: { reducerMapKey: string; body: Interfaces.StagingJobsBody }
  ) {}
}

export class StartStagingJobSuccess implements Action {
  readonly type = START_STAGING_JOB_SUCCESS;

  constructor(public payload: { reducerMapKey: string }) {}
}

export class StartStagingJobFail implements Action {
  readonly type = START_STAGING_JOB_FAIL;

  constructor(public payload: { reducerMapKey: string }) {}
}

export class Validate implements Action {
  readonly type = VALIDATE;

  constructor(
    public payload: { reducerMapKey: string; body: ValidationJobRequest }
  ) {}
}

export class ValidateSuccess implements Action {
  readonly type = VALIDATE_SUCCESS;

  constructor(
    public payload: { reducerMapKey: string; body: JobId; datasetId: number }
  ) {}
}

export class ValidateFail implements Action {
  readonly type = VALIDATE_FAIL;

  constructor(public payload: { reducerMapKey: string }) {}
}

export class StartPipelineJob implements Action {
  readonly type = START_PIPELINE_JOB;

  constructor(
    public payload: { reducerMapKey: string; body: StartPipelineJobRequest }
  ) {}
}

export class StartPipelineJobSuccess implements Action {
  readonly type = START_PIPELINE_JOB_SUCCESS;

  constructor(public payload: { reducerMapKey: string }) {}
}

export class StartPipelineJobFail implements Action {
  readonly type = START_PIPELINE_JOB_FAIL;

  constructor(public payload: { reducerMapKey: string }) {}
}

export class Empty implements Action {
  readonly type = EMPTY;

  constructor(public payload: { reducerMapKey: string }) {}
}

export class StopJob implements Action {
  readonly type = STOP_JOB;

  constructor(
    public payload: {
      reducerMapKey: string;
      jobExecutionId: Types.StagingJobId;
    }
  ) {}
}

export class GetJobDatasets implements Action {
  readonly type = GET_JOB_DATASETS;

  constructor(
    public payload: {
      reducerMapKey: string;
      jobExecutionId: Types.StagingJobId;
    }
  ) {}
}

export class GetJobDatasetsSuccess implements Action {
  readonly type = GET_JOB_DATASETS_SUCCESS;

  constructor(
    public payload: { reducerMapKey: string; datasets: Interfaces.Dataset[] }
  ) {}
}

export class GetJobDatasetsFail implements Action {
  readonly type = GET_JOB_DATASETS_FAIL;

  constructor(public payload: { reducerMapKey: string }) {}
}

export class GetJobDatasetsForDataset implements Action {
  readonly type = GET_JOB_DATASETS_FOR_DATASET;

  constructor(
    public payload: {
      reducerMapKey: string;
      datasetId: Types.DatasetId;
    }
  ) {}
}

export class GetJobDatasetsForDatasetSuccess implements Action {
  readonly type = GET_JOB_DATASETS_FOR_DATASET_SUCCESS;

  constructor(
    public payload: { reducerMapKey: string; datasets: Interfaces.Dataset[] }
  ) {}
}

export class GetJobDatasetsForDatasetFail implements Action {
  readonly type = GET_JOB_DATASETS_FOR_DATASET_FAIL;

  constructor(public payload: { reducerMapKey: string }) {}
}

export type Types =
  | Get
  | GetSilent
  | GetSuccess
  | GetFail
  | GetById
  | GetByIdSuccess
  | GetByIdFail
  | GetRunningJobs
  | GetRunningJobsSuccess
  | GetRunningJobsFail
  | StartStagingJob
  | StartStagingJobSuccess
  | StartStagingJobFail
  | Validate
  | ValidateSuccess
  | ValidateFail
  | StartPipelineJob
  | StartPipelineJobSuccess
  | StartPipelineJobFail
  | Empty
  | StopJob
  | GetJobDatasets
  | GetJobDatasetsSuccess
  | GetJobDatasetsFail
  | GetJobDatasetsForDataset
  | GetJobDatasetsForDatasetSuccess
  | GetJobDatasetsForDatasetFail
  ;
