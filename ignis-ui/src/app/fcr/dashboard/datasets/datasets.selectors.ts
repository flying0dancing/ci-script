import * as DatasetSelectors from "@/core/api/datasets/datasets.selectors";
import * as TablesSelectors from "@/core/api/tables/tables.selectors";
import { NAMESPACE } from "./datasets.constants";

export const getDatasetCollection = DatasetSelectors.getDatasetsCollectionFactory(
  NAMESPACE
);

export const getDatasetGetState = DatasetSelectors.getDatasetsGetStateFactory(
  NAMESPACE
);

export function getDatasetById(id: number) {
  return DatasetSelectors.getDatasetByIdFactory(id, NAMESPACE);
}

export function getDatasetByIds(ids: number[]) {
  return DatasetSelectors.getDatasetByIdsFactory(ids, NAMESPACE);
}

export const getDatasetLoading = DatasetSelectors.getDatasetsGetLoadingStateFactory(
  NAMESPACE
);

export const getSourceFiles = DatasetSelectors.getSourceFilesFactory(NAMESPACE);

export const getSchemas = TablesSelectors.getTablesCollectionFactory(NAMESPACE);
