import { JobExecution } from '@/core/api/staging/staging.interfaces';
import { createFeatureSelector, createSelector, MemoizedSelector } from '@ngrx/store';
import { NAMESPACE } from './staging.constants';

import { initialStagingState, StagingState, State } from './staging.reducer';

export const getStagingState = createFeatureSelector<State>(NAMESPACE);

export const getStagingFactory = (key: string) =>
  createSelector(
    getStagingState,
    (state: State) => state[key]
  );

export const getStagingCollectionFactory = (key: string) =>
  createSelector(
    getStagingFactory(key),
    (state: StagingState) =>
      (state && state.collection) || initialStagingState.collection
  );

export function getStagingByIdFactory(
  jobExecutionId: number,
  key: string
): MemoizedSelector<object, JobExecution> {
  return createSelector(
    getStagingFactory(key),
    (state: StagingState) => {
      const jobs: JobExecution[] =
        (state && (state.collection || initialStagingState.collection)) || [];
      const runningJobs: JobExecution[] =
        (state && state.runningJobs) || initialStagingState.runningJobs;

      const allJobs = jobs.concat(runningJobs);
      for (let i = 0; i < allJobs.length; i++) {
        const job: JobExecution = allJobs[i];
        if (job.id === jobExecutionId) {
          return job;
        }
      }
      return null;
    }
  );
}

export const getStagingGetStateFactory = (key: string) =>
  createSelector(
    getStagingFactory(key),
    (state: StagingState) => (state && state.get) || initialStagingState.get
  );

export const getStagingJobStateFactory = (key: string) =>
  createSelector(
    getStagingFactory(key),
    (state: StagingState) =>
      (state && state.staging) || initialStagingState.staging
  );

export const getStagingValidateStateFactory = (key: string) =>
  createSelector(
    getStagingFactory(key),
    (state: StagingState) =>
      (state && state.validate) || initialStagingState.validate
  );

export const getPipelineJobStateFactory = (key: string) =>
  createSelector(
    getStagingFactory(key),
    (state: StagingState) =>
      (state && state.pipeline) || initialStagingState.pipeline
  );

export const getStagingGetLoadingStateFactory = (key: string) =>
  createSelector(
    getStagingFactory(key),
    (state: StagingState) =>
      (state && state.get.loading) || initialStagingState.get.loading
  );

export const getStagingGetDatasetsLoadingStateFactory = (key: string) =>
  createSelector(
    getStagingFactory(key),
    (state: StagingState) =>
      (state && state.getDetails.loading) ||
      initialStagingState.getDetails.loading
  );

export const getStagingJobDetailsFactory = (key: string) =>
  createSelector(
    getStagingFactory(key),
    (state: StagingState) =>
      (state && state.details) || initialStagingState.details
  );

export const getDatasetJobHistoryFactory = (key: string) =>
  createSelector(
    getStagingFactory(key),
    (state: StagingState) =>
      (state && state.datasetHistory) || initialStagingState.datasetHistory
  );

export const getStagingGetByIdLoadingFactory = (key: string) =>
  createSelector(
    getStagingFactory(key),
    (state: StagingState) =>
      (state && state.getById.loading) || initialStagingState.getById.loading
  );

export const getStagingGetByIdErrorFactory = (key: string) =>
  createSelector(
    getStagingFactory(key),
    (state: StagingState) =>
      (state && state.getById.error) || initialStagingState.getById.error
  );

export const getStagingRunningJobsFactory = (key: string) =>
  createSelector(
    getStagingFactory(key),
    (state: StagingState) =>
      (state && state.runningJobs) || initialStagingState.runningJobs
  );

export const getStagingRunningJobsLoadingFactory = (key: string) =>
  createSelector(
    getStagingFactory(key),
    (state: StagingState) =>
      (state && state.getRunningJobs.loading) || initialStagingState.getRunningJobs.loading
  );

export const getStagingRunningJobsErrorFactory = (key: string) =>
  createSelector(
    getStagingFactory(key),
    (state: StagingState) =>
      (state && state.getRunningJobs.error) || initialStagingState.getRunningJobs.error
  );
