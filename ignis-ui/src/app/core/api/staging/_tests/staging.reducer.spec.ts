import { GetJobsResponse, JobExecution, JobStatus } from '@/core/api/staging/staging.interfaces';
import { StagingState } from '@/core/api/staging/staging.reducer';
import * as deepFreeze from 'deep-freeze';
import * as StagingActions from '../staging.actions';
import { initialStagingState, reducer, stagingReducer } from '../staging.reducer';
import { StagingJobId } from '../staging.types';

import { dataset, job, jobId, jobs, stagingJob, validationJobRequest } from './staging.mocks';

const reducerMapKey = "test";
const jobExecutionId: StagingJobId = 1;
const defaultState: StagingState = {
  collection: [],
  runningJobs: [],
  details: null,
  datasetHistory: [],
  get: { loading: false, error: false },
  getById: { loading: false, error: false },
  getRunningJobs: { loading: false, error: false },
  staging: { loading: false, error: false },
  pipeline: { loading: false, error: false },
  validate: { loading: false, error: false },
  getDetails: { loading: false, error: false }
};
const jobsResponse: GetJobsResponse = {
  data: jobs,
  page: {number: 0, size: 1, totalPages: 1, totalElements: 1}
};

describe("Staging Reducer", () => {
  it("should not affect the state on an unknown action", () => {
    const state = defaultState;
    const action = { type: "unknown-action" } as any;

    deepFreeze(state);
    deepFreeze(action);

    expect(stagingReducer(state, action)).toEqual(state);
  });

  it("should return the initial state when no state is provided", () => {
    const state = undefined;
    const action = { type: "unknown-action" } as any;

    deepFreeze(action);

    expect(stagingReducer(state, action)).toEqual(initialStagingState);
  });

  describe("GET_SILENT action", () => {
    it("should not change the state of the application", () => {
      const state = defaultState;
      const action = new StagingActions.GetSilent({ reducerMapKey });

      deepFreeze(state);
      deepFreeze(action);

      expect(stagingReducer(state, action)).toEqual(defaultState);
    });
  });

  describe("GET action", () => {
    it("should set the loading state to true", () => {
      const state = defaultState;
      const action = new StagingActions.Get({ reducerMapKey });

      deepFreeze(state);
      deepFreeze(action);

      expect(stagingReducer(state, action)).toEqual({
        ...defaultState,
        get: { loading: true, error: false }
      });
    });
  });

  describe("GET_SUCCESS action", () => {
    it("should set the loading state to false and update the collection", () => {
      const state: StagingState = {
        collection: [],
        runningJobs: [],
        details: null,
        datasetHistory: [],
        get: { loading: true, error: false },
        getById: { loading: false, error: false },
        getRunningJobs: { loading: false, error: false },
        staging: { loading: false, error: false },
        pipeline: { loading: false, error: false },
        validate: { loading: false, error: false },
        getDetails: { loading: false, error: false }
      };
      const action = new StagingActions.GetSuccess({ reducerMapKey, jobs });

      deepFreeze(state);
      deepFreeze(action);

      expect(stagingReducer(state, action)).toEqual({
        ...defaultState,
        collection: jobs,
        get: { loading: false, error: false }
      });
    });

    it("should update collection with new jobs", () => {
      const job1: JobExecution = { ...job, id: 1, status: JobStatus.COMPLETED };
      const job2: JobExecution = { ...job, id: 2, status: JobStatus.STARTING };
      const job3: JobExecution = { ...job, id: 3, status: JobStatus.STOPPING };

      const state: StagingState = {
        collection: [job1, job2, job3],
        runningJobs: [],
        details: null,
        datasetHistory: [],
        get: { loading: true, error: false },
        getById: { loading: false, error: false },
        getRunningJobs: { loading: false, error: false },
        staging: { loading: false, error: false },
        pipeline: { loading: false, error: false },
        validate: { loading: false, error: false },
        getDetails: { loading: false, error: false }
      };

      const action = new StagingActions.GetSuccess({
        reducerMapKey,
        jobs: [
            { ...job3, status: JobStatus.COMPLETED },
            { ...job, id: 4, status: JobStatus.STARTING },
            { ...job, id: 5, status: JobStatus.FAILED }]
      });

      deepFreeze(state);
      deepFreeze(action);

      expect(stagingReducer(state, action)).toEqual({
        ...defaultState,
        collection: [
          job1,
          job2,
          { ...job3, status: JobStatus.COMPLETED },
          { ...job, id: 4, status: JobStatus.STARTING },
          { ...job, id: 5, status: JobStatus.FAILED }
        ],
        get: { loading: false, error: false },
      });
    });
  });

  describe("GET_FAIL action", () => {
    it("should set the loading state to false and error state to true", () => {
      const state: StagingState = {
        collection: [],
        runningJobs: [],
        details: null,
        datasetHistory: [],
        get: { loading: true, error: false },
        getById: { loading: false, error: false },
        getRunningJobs: { loading: false, error: false },
        staging: { loading: false, error: false },
        validate: { loading: false, error: false },
        pipeline: { loading: false, error: false },
        getDetails: { loading: false, error: false }
      };
      const action = new StagingActions.GetFail({ reducerMapKey });

      deepFreeze(state);
      deepFreeze(action);

      expect(stagingReducer(state, action)).toEqual({
        ...defaultState,
        get: { loading: false, error: true }
      });
    });
  });

  describe("GET_BY_ID action", () => {
    it("should set the loading state to true", () => {
      const state: StagingState = defaultState;
      const action = new StagingActions.GetById({
        reducerMapKey,
        jobId: 12345
      });

      deepFreeze(state);
      deepFreeze(action);

      expect(stagingReducer(state, action)).toEqual({
        ...defaultState,
        getById: { loading: true, error: false }
      });
    });
  });

  describe("GET_BY_ID_SUCCESS action", () => {
    it("should set the loading state to false and add to the collection", () => {
      const state: StagingState = {
        ...defaultState,
        collection: [],
        getById: { loading: true, error: false }
      };
      const action = new StagingActions.GetByIdSuccess({
        reducerMapKey,
        job: job
      });

      deepFreeze(state);
      deepFreeze(action);

      expect(stagingReducer(state, action)).toEqual({
        ...defaultState,
        collection: [job],
        getById: { loading: false, error: false }
      });
    });

    it("should set the loading state to false and update the collection", () => {
      const job1: JobExecution = { ...job, id: 1, status: JobStatus.FAILED };
      const job2: JobExecution = { ...job, id: 2, status: JobStatus.UNKNOWN };
      const job3: JobExecution = { ...job, id: 3, status: JobStatus.COMPLETED };

      const state: StagingState = {
        ...defaultState,
        collection: [job1, job2, job3],
        getById: { loading: true, error: false }
      };
      const action = new StagingActions.GetByIdSuccess({
        reducerMapKey,
        job: {...job2, status: JobStatus.COMPLETED }
      });

      deepFreeze(state);
      deepFreeze(action);

      expect(stagingReducer(state, action)).toEqual({
        ...defaultState,
        collection: [job1, { ...job2, status: JobStatus.COMPLETED }, job3],
        getById: { loading: false, error: false }
      });
    });
  });

  describe("GET_BY_ID_FAIL action", () => {
    it("should set the loading state to false and error to true", () => {
      const state: StagingState = {
        ...defaultState,
        getById: { loading: true, error: false }
      };
      const action = new StagingActions.GetByIdFail({
        reducerMapKey
      });

      deepFreeze(state);
      deepFreeze(action);

      expect(stagingReducer(state, action)).toEqual({
        ...defaultState,
        getById: { loading: false, error: true }
      });
    });
  });

  describe("GET_RUNNING_JOBS action", () => {
    it("should set the loading state to true", () => {
      const state: StagingState = defaultState;
      const action = new StagingActions.GetRunningJobs({
        reducerMapKey
      });

      deepFreeze(state);
      deepFreeze(action);

      expect(stagingReducer(state, action)).toEqual({
        ...defaultState,
        getRunningJobs: { loading: true, error: false }
      });
    });
  });

  describe("GET_RUNNING_JOBS_SUCCESS action", () => {
    it("should set the loading state to false and add to the collection", () => {
      const state: StagingState = {
        ...defaultState,
        collection: [],
        getRunningJobs: { loading: true, error: false }
      };
      const action = new StagingActions.GetRunningJobsSuccess({
        reducerMapKey,
        jobs: [job]
      });

      deepFreeze(state);
      deepFreeze(action);

      expect(stagingReducer(state, action)).toEqual({
        ...defaultState,
        runningJobs: [job],
        getRunningJobs: { loading: false, error: false }
      });
    });
  });

  describe("GET_RUNNING_JOBS_FAIL action", () => {
    it("should set the loading state to false and error to true", () => {
      const state: StagingState = {
        ...defaultState,
        getRunningJobs: { loading: true, error: false }
      };
      const action = new StagingActions.GetRunningJobsFail({
        reducerMapKey
      });

      deepFreeze(state);
      deepFreeze(action);

      expect(stagingReducer(state, action)).toEqual({
        ...defaultState,
        getRunningJobs: { loading: false, error: true }
      });
    });
  });

  describe("START_STAGING_JOB action", () => {
    it("should set the loading state to true", () => {
      const state: StagingState = defaultState;
      const action = new StagingActions.StartStagingJob({
        reducerMapKey,
        body: stagingJob
      });

      deepFreeze(state);
      deepFreeze(action);

      expect(stagingReducer(state, action)).toEqual({
        ...defaultState,
        staging: { loading: true, error: false }
      });
    });
  });

  describe("START_STAGING_JOB_SUCCESS action", () => {
    it("should set the loading state to false", () => {
      const state: StagingState = {
        collection: [],
        runningJobs: [],
        details: null,
        datasetHistory: [],
        get: { loading: false, error: false },
        getById: { loading: false, error: false },
        getRunningJobs: { loading: false, error: false },
        pipeline: { loading: false, error: false },
        staging: { loading: true, error: false },
        validate: { loading: false, error: false },
        getDetails: { loading: false, error: false }
      };
      const action = new StagingActions.StartStagingJobSuccess({
        reducerMapKey
      });

      deepFreeze(state);
      deepFreeze(action);

      expect(stagingReducer(state, action)).toEqual(defaultState);
    });
  });

  describe("START_STAGING_JOB_FAIL action", () => {
    it("should set the loading state to false and error state to true", () => {
      const state: StagingState = {
        collection: [],
        runningJobs: [],
        details: null,
        datasetHistory: [],
        get: { loading: false, error: false },
        getById: { loading: false, error: false },
        getRunningJobs: { loading: false, error: false },
        pipeline: { loading: false, error: false },
        staging: { loading: true, error: false },
        validate: { loading: false, error: false },
        getDetails: { loading: false, error: false }
      };
      const action = new StagingActions.StartStagingJobFail({ reducerMapKey });

      deepFreeze(state);
      deepFreeze(action);

      expect(stagingReducer(state, action)).toEqual({
        ...defaultState,
        staging: { loading: false, error: true }
      });
    });
  });

  describe("VALIDATE action", () => {
    it("should set the loading state to true", () => {
      const state = defaultState;
      const action = new StagingActions.Validate({
        reducerMapKey,
        body: validationJobRequest
      });

      deepFreeze(state);
      deepFreeze(action);

      expect(stagingReducer(state, action)).toEqual({
        ...defaultState,
        validate: { loading: true, error: false }
      });
    });
  });

  describe("VALIDATE_SUCCESS action", () => {
    it("should set the loading state to false", () => {
      const state: StagingState = {
        collection: [],
        runningJobs: [],
        details: null,
        datasetHistory: [],
        get: { loading: false, error: false },
        getById: { loading: false, error: false },
        getRunningJobs: { loading: false, error: false },
        pipeline: { loading: false, error: false },
        staging: { loading: false, error: false },
        validate: { loading: false, error: false },
        getDetails: { loading: false, error: false }
      };
      const action = new StagingActions.ValidateSuccess({
        reducerMapKey,
        body: jobId,
        datasetId: 1
      });

      deepFreeze(state);
      deepFreeze(action);

      expect(stagingReducer(state, action)).toEqual(defaultState);
    });
  });

  describe("VALIDATE_FAIL action", () => {
    it("should set the loading state to false and error state to true", () => {
      const state: StagingState = {
        collection: [],
        runningJobs: [],
        details: null,
        datasetHistory: [],
        get: { loading: false, error: false },
        getById: { loading: false, error: false },
        getRunningJobs: { loading: false, error: false },
        pipeline: { loading: false, error: false },
        staging: { loading: false, error: false },
        validate: { loading: true, error: false },
        getDetails: { loading: false, error: false }
      };
      const action = new StagingActions.ValidateFail({ reducerMapKey });

      deepFreeze(state);
      deepFreeze(action);

      expect(stagingReducer(state, action)).toEqual({
        ...defaultState,
        validate: { loading: false, error: true }
      });
    });
  });

  describe("GET_JOB_DATASETS action", () => {
    it("should set the loading state to true", () => {
      const state: StagingState = defaultState;
      const action = new StagingActions.GetJobDatasets({
        reducerMapKey,
        jobExecutionId
      });

      deepFreeze(state);
      deepFreeze(action);

      expect(stagingReducer(state, action)).toEqual({
        ...defaultState,
        getDetails: { loading: true, error: false }
      });
    });
  });

  describe("GET_JOB_DATASETS_SUCCESS action", () => {
    it("should set the loading state to false and update the details", () => {
      const state: StagingState = {
        collection: [],
        runningJobs: [],
        details: null,
        datasetHistory: [],
        get: { loading: false, error: false },
        getById: { loading: false, error: false },
        getRunningJobs: { loading: false, error: false },
        pipeline: { loading: false, error: false },
        staging: { loading: false, error: false },
        validate: { loading: false, error: false },
        getDetails: { loading: true, error: false }
      };
      const action = new StagingActions.GetJobDatasetsSuccess({
        reducerMapKey,
        datasets: [dataset]
      });

      deepFreeze(state);
      deepFreeze(action);

      expect(stagingReducer(state, action)).toEqual({
        ...defaultState,
        details: [dataset],
        getDetails: { loading: false, error: false }
      });
    });
  });

  describe("GET_JOB_DATASETS_FAIL action", () => {
    it("should set the loading state to false and error state to true", () => {
      const state: StagingState = {
        collection: [],
        runningJobs: [],
        details: null,
        datasetHistory: [],
        get: { loading: false, error: false },
        getById: { loading: false, error: false },
        getRunningJobs: { loading: false, error: false },
        pipeline: { loading: false, error: false },
        staging: { loading: false, error: false },
        validate: { loading: false, error: false },
        getDetails: { loading: true, error: false }
      };
      const action = new StagingActions.GetJobDatasetsFail({ reducerMapKey });

      deepFreeze(state);
      deepFreeze(action);

      expect(stagingReducer(state, action)).toEqual({
        ...defaultState,
        getDetails: { loading: false, error: true }
      });
    });
  });

  describe("GET_JOB_DATASETS_FOR_DATASET action", () => {
    it("should set the loading state to true", () => {
      const state: StagingState = defaultState;
      const action = new StagingActions.GetJobDatasetsForDataset({
        reducerMapKey,
        datasetId: dataset.id
      });

      deepFreeze(state);
      deepFreeze(action);

      expect(stagingReducer(state, action)).toEqual({
        ...defaultState,
        getDetails: { loading: true, error: false }
      });
    });
  });

  describe("GET_JOB_DATASETS_FOR_DATASET_SUCCESS action", () => {
    it("should set the loading state to false and update the dataset history", () => {
      const state: StagingState = {
        collection: [],
        runningJobs: [],
        details: null,
        datasetHistory: [],
        get: { loading: false, error: false },
        getById: { loading: false, error: false },
        getRunningJobs: { loading: false, error: false },
        pipeline: { loading: false, error: false },
        staging: { loading: false, error: false },
        validate: { loading: false, error: false },
        getDetails: { loading: true, error: false }
      };
      const action = new StagingActions.GetJobDatasetsForDatasetSuccess({
        reducerMapKey,
        datasets: [dataset]
      });

      deepFreeze(state);
      deepFreeze(action);

      expect(stagingReducer(state, action)).toEqual({
        ...defaultState,
        datasetHistory: [dataset],
        getDetails: { loading: false, error: false }
      });
    });
  });

  describe("GET_JOB_DATASETS_FOR_DATASET_FAIL action", () => {
    it("should set the loading state to false and error state to true", () => {
      const state: StagingState = {
        collection: [],
        runningJobs: [],
        details: null,
        datasetHistory: [],
        get: { loading: false, error: false },
        getById: { loading: false, error: false },
        getRunningJobs: { loading: false, error: false },
        pipeline: { loading: false, error: false },
        staging: { loading: false, error: false },
        validate: { loading: false, error: false },
        getDetails: { loading: true, error: false }
      };
      const action = new StagingActions.GetJobDatasetsFail({ reducerMapKey });

      deepFreeze(state);
      deepFreeze(action);

      expect(stagingReducer(state, action)).toEqual({
        ...defaultState,
        getDetails: { loading: false, error: true }
      });
    });
  });

  describe("STOP_JOB action", () => {
    it("should set the loading state to true", () => {
      const state = defaultState;
      const action = new StagingActions.StopJob({
        reducerMapKey,
        jobExecutionId
      });

      deepFreeze(state);
      deepFreeze(action);

      expect(stagingReducer(state, action)).toEqual(defaultState);
    });
  });

  describe("EMPTY action", () => {
    it("should return the initial state", () => {
      const state = defaultState;
      const action = new StagingActions.Empty({ reducerMapKey });

      deepFreeze(state);
      deepFreeze(action);

      expect(stagingReducer(state, action)).toEqual(initialStagingState);
    });
  });
});

describe("staging map reducer", () => {
  const initialState = {};
  deepFreeze(initialState);

  it("should not affect the state on an unknown action", () => {
    const action = { type: "unknown-action" } as any;

    deepFreeze(action);

    expect(reducer(initialState, action)).toEqual(initialState);
  });

  it("should return the initial state when no state is provided", () => {
    const state = undefined;
    const action = { type: "unknown-action" } as any;

    deepFreeze(action);

    expect(reducer(state, action)).toEqual(initialState);
  });

  describe("GET action", () => {
    it("should set the map key", () => {
      const action = new StagingActions.Get({ reducerMapKey });

      deepFreeze(action);

      expect(Object.keys(reducer(initialState, action))).toContain(
        reducerMapKey
      );
    });
  });

  describe("GET_SUCCESS action", () => {
    it("should set the map key", () => {
      const action = new StagingActions.GetSuccess({ reducerMapKey, jobs: jobs });

      deepFreeze(action);

      expect(Object.keys(reducer(initialState, action))).toContain(
        reducerMapKey
      );
    });
  });

  describe("GET_FAIL action", () => {
    it("should set the map key", () => {
      const action = new StagingActions.GetFail({ reducerMapKey });

      deepFreeze(action);

      expect(Object.keys(reducer(initialState, action))).toContain(
        reducerMapKey
      );
    });
  });

  describe("EMPTY action", () => {
    it("should set the map key", () => {
      const action = new StagingActions.Empty({ reducerMapKey });

      deepFreeze(action);

      expect(Object.keys(reducer(initialState, action))).toContain(
        reducerMapKey
      );
    });
  });
});
