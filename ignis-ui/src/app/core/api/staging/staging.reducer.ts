import * as StagingActions from './staging.actions';
import { Dataset, JobExecution } from './staging.interfaces';

export interface StagingState {
  collection: JobExecution[];
  runningJobs: JobExecution[];
  details: Dataset[];
  datasetHistory: Dataset[];
  get: {
    loading: boolean;
    error: boolean;
  };
  getById: {
    loading: boolean;
    error: boolean;
  }
  getRunningJobs: {
    loading: boolean;
    error: boolean;
  };
  staging: {
    loading: boolean;
    error: boolean;
  };
  validate: {
    loading: boolean;
    error: boolean;
  };
  pipeline: {
    loading: boolean;
    error: boolean;
  };
  getDetails: {
    loading: boolean;
    error: boolean;
  };
}

export interface State {
  [key: string]: StagingState;
}

export const initialStagingState = {
  collection: [],
  runningJobs: [],
  details: null,
  datasetHistory: [],
  get: {
    loading: false,
    error: false
  },
  getById: {
    loading: false,
    error: false
  },
  getRunningJobs: {
    loading: false,
    error: false
  },
  staging: {
    loading: false,
    error: false
  },
  validate: {
    loading: false,
    error: false
  },
  pipeline: {
    loading: false,
    error: false
  },
  getDetails: {
    loading: false,
    error: false
  }
};

export function stagingReducer(
  state: StagingState = initialStagingState,
  action: StagingActions.Types
) {
  switch (action.type) {
    case StagingActions.GET:
      return { ...state, get: { ...state.get, loading: true, error: false } };

    case StagingActions.GET_SUCCESS:
      return {
        ...state,
        get: { ...state.get, loading: false },
        collection: updateCollection(state.collection, action.payload.jobs)
      };

    case StagingActions.GET_FAIL:
      return { ...state, get: { loading: false, error: true } };

    case StagingActions.GET_BY_ID:
      return { ...state, getById: { ...state.getById, loading: true, error: false } };

    case StagingActions.GET_BY_ID_SUCCESS:
      return {
        ...state,
        getById: { ...state.getById, loading: false },
        collection: updateCollection(state.collection, [action.payload.job])
      };

    case StagingActions.GET_BY_ID_FAIL:
      return { ...state, getById: { loading: false, error: true } };

    case StagingActions.GET_RUNNING_JOBS:
      return { ...state, getRunningJobs: { ...state.getRunningJobs, loading: true, error: false } };

    case StagingActions.GET_RUNNING_JOBS_SUCCESS:
      return {
        ...state,
        getRunningJobs: { ...state.getRunningJobs, loading: false },
        runningJobs: action.payload.jobs
      };

    case StagingActions.GET_RUNNING_JOBS_FAIL:
      return { ...state, getRunningJobs: { loading: false, error: true } };

    case StagingActions.START_STAGING_JOB:
      return {
        ...state,
        staging: { ...state.staging, loading: true, error: false }
      };

    case StagingActions.START_STAGING_JOB_SUCCESS:
      return { ...state, staging: { ...state.staging, loading: false } };

    case StagingActions.START_STAGING_JOB_FAIL:
      return { ...state, staging: { loading: false, error: true } };

    case StagingActions.VALIDATE:
      return { ...state, validate: { loading: true, error: false } };

    case StagingActions.VALIDATE_SUCCESS:
      return { ...state, validate: { loading: false, error: false } };

    case StagingActions.VALIDATE_FAIL:
      return { ...state, validate: { loading: false, error: true } };

    case StagingActions.START_PIPELINE_JOB:
      return { ...state, pipeline: { loading: true, error: false } };

    case StagingActions.START_PIPELINE_JOB_SUCCESS:
      return { ...state, pipeline: { loading: false, error: false } };

    case StagingActions.START_PIPELINE_JOB_FAIL:
      return { ...state, pipeline: { loading: false, error: true } };

    case StagingActions.EMPTY:
      return { ...initialStagingState };

    case StagingActions.GET_JOB_DATASETS:
      return {
        ...state,
        getDetails: { ...state.getDetails, loading: true },
        details: initialStagingState.details
      };

    case StagingActions.GET_JOB_DATASETS_SUCCESS:
      return {
        ...state,
        getDetails: { ...state.getDetails, loading: false },
        details: action.payload.datasets
      };

    case StagingActions.GET_JOB_DATASETS_FAIL:
      return { ...state, getDetails: { loading: false, error: true } };

    case StagingActions.GET_JOB_DATASETS_FOR_DATASET:
      return {
        ...state,
        getDetails: { ...state.getDetails, loading: true },
        datasetHistory: initialStagingState.datasetHistory
      };

    case StagingActions.GET_JOB_DATASETS_FOR_DATASET_SUCCESS:
      return {
        ...state,
        getDetails: { ...state.getDetails, loading: false },
        datasetHistory: action.payload.datasets
      };

    case StagingActions.GET_JOB_DATASETS_FOR_DATASET_FAIL:
      return { ...state, getDetails: { loading: false, error: true } };

    default:
      return state;
  }
}

export function reducer(state: State = {}, action: any) {
  switch (action.type) {
    case StagingActions.GET:
    case StagingActions.GET_SILENT:
    case StagingActions.GET_SUCCESS:
    case StagingActions.GET_FAIL:
    case StagingActions.GET_BY_ID:
    case StagingActions.GET_BY_ID_SUCCESS:
    case StagingActions.GET_BY_ID_FAIL:
    case StagingActions.GET_RUNNING_JOBS:
    case StagingActions.GET_RUNNING_JOBS_SUCCESS:
    case StagingActions.GET_RUNNING_JOBS_FAIL:
    case StagingActions.START_STAGING_JOB:
    case StagingActions.START_STAGING_JOB_SUCCESS:
    case StagingActions.START_STAGING_JOB_FAIL:
    case StagingActions.VALIDATE:
    case StagingActions.VALIDATE_SUCCESS:
    case StagingActions.VALIDATE_FAIL:
    case StagingActions.START_PIPELINE_JOB:
    case StagingActions.START_PIPELINE_JOB_SUCCESS:
    case StagingActions.START_PIPELINE_JOB_FAIL:
    case StagingActions.GET_JOB_DATASETS:
    case StagingActions.GET_JOB_DATASETS_SUCCESS:
    case StagingActions.GET_JOB_DATASETS_FAIL:
    case StagingActions.GET_JOB_DATASETS_FOR_DATASET:
    case StagingActions.GET_JOB_DATASETS_FOR_DATASET_SUCCESS:
    case StagingActions.GET_JOB_DATASETS_FOR_DATASET_FAIL:
    case StagingActions.EMPTY:
      return {
        ...state,
        [action.payload.reducerMapKey]: stagingReducer(
          state[action.payload.reducerMapKey],
          action
        )
      };
    default:
      return state;
  }
}

function updateCollection(currentState: JobExecution[], newState: JobExecution[]): JobExecution[] {
  const nextState = new Map();

  currentState.forEach(job => nextState.set(job.id, job));
  newState.forEach(job => nextState.set(job.id, job));

  return Array.from(nextState.values());
}
