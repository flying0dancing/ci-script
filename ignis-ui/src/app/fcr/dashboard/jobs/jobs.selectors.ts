import * as DatasetSelectors from '@/core/api/datasets/datasets.selectors';
import * as PipelineSelectors from '@/core/api/pipelines/pipelines.selectors';
import * as StagingSelectors from '@/core/api/staging/staging.selectors';
import * as TablesSelectors from '@/core/api/tables/tables.selectors';
import { NAMESPACE } from './jobs.constants';

export const getStagingCollection = StagingSelectors.getStagingCollectionFactory(
  NAMESPACE
);

export const getStagingGetState = StagingSelectors.getStagingGetStateFactory(
  NAMESPACE
);

export const getStagingJobState = StagingSelectors.getStagingJobStateFactory(
  NAMESPACE
);

export const getPipelineJobsState = StagingSelectors.getPipelineJobStateFactory(
  NAMESPACE
);

export const getStagingValidateState = StagingSelectors.getStagingValidateStateFactory(
  NAMESPACE
);

export const getStagingLoading = StagingSelectors.getStagingGetLoadingStateFactory(
  NAMESPACE
);

export const getStagingDatasetsLoading = StagingSelectors.getStagingGetDatasetsLoadingStateFactory(
  NAMESPACE
);

export const getStagingDatasetsDetails = StagingSelectors.getStagingJobDetailsFactory(
  NAMESPACE
);

export const getDatasetJobHistory = StagingSelectors.getDatasetJobHistoryFactory(
  NAMESPACE
);

export function getStagingJobDetailsById(id: number) {
  return StagingSelectors.getStagingByIdFactory(id, NAMESPACE);
}

export const getDatasetCollection = DatasetSelectors.getDatasetsCollectionFactory(
  NAMESPACE
);

export const getDatasetGetState = DatasetSelectors.getDatasetsGetStateFactory(
  NAMESPACE
);

export const getDatasetLoading = DatasetSelectors.getDatasetsGetLoadingStateFactory(
  NAMESPACE
);

export const getSourceFiles = DatasetSelectors.getSourceFilesFactory(NAMESPACE);

export const getSchemas = TablesSelectors.getTablesCollectionFactory(NAMESPACE);

export const getSchemasLoading = TablesSelectors.getTablesGetLoadingStateFactory(
  NAMESPACE
);

export const getPipelineInvocations = PipelineSelectors.getPipelineInvocationsCollectionFactory(
  NAMESPACE
);

export const getStagingGetByIdLoading = StagingSelectors.getStagingGetByIdLoadingFactory(
  NAMESPACE
);

export const getStagingGetByIdError = StagingSelectors.getStagingGetByIdErrorFactory(
  NAMESPACE
);

export const getStagingRunningJobs = StagingSelectors.getStagingRunningJobsFactory(
  NAMESPACE
);

export const getStagingRunningJobsLoading = StagingSelectors.getStagingRunningJobsLoadingFactory(
  NAMESPACE
);

export const getStagingRunningJobsError = StagingSelectors.getStagingRunningJobsErrorFactory(
  NAMESPACE
);
