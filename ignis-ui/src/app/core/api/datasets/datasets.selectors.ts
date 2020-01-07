import { Dataset } from "@/core/api/datasets/datasets.interfaces";
import {
  createFeatureSelector,
  createSelector,
  MemoizedSelector
} from "@ngrx/store";
import { NAMESPACE } from "./datasets.constants";

import { DatasetsState, initialDatasetsState, State } from "./datasets.reducer";

export const getDatasetsState = createFeatureSelector<State>(NAMESPACE);

export const getDatasetsFactory = (key: string) =>
  createSelector(
    getDatasetsState,
    (state: State) => state[key]
  );

export const getDatasetsCollectionFactory = (key: string) =>
  createSelector(
    getDatasetsFactory(key),
    (state: DatasetsState) =>
      (state && state.collection) || initialDatasetsState.collection
  );

export function getDatasetByIdFactory(
  id: number,
  key: string
): MemoizedSelector<object, Dataset> {
  return createSelector(
    getDatasetsFactory(key),
    (state: DatasetsState) => {
      const datasets: Dataset[] =
        (state && (state.collection || initialDatasetsState.collection)) || [];
      return datasets.find(dataset => dataset.id === id);
    }
  );
}
export function getDatasetByIdsFactory(
  ids: number[],
  key: string
): MemoizedSelector<object, Dataset[]> {
  return createSelector(
    getDatasetsFactory(key),
    (state: DatasetsState) => {
      const datasets: Dataset[] =
        (state && (state.collection || initialDatasetsState.collection)) || [];
      return datasets.filter(dataset => ids.indexOf(dataset.id) !== -1);
    }
  );
}

export const getDatasetsGetStateFactory = (key: string) =>
  createSelector(
    getDatasetsFactory(key),
    (state: DatasetsState) => (state && state.get) || initialDatasetsState.get
  );

export const getDatasetsGetLoadingStateFactory = (key: string) =>
  createSelector(
    getDatasetsFactory(key),
    (state: DatasetsState) =>
      (state && state.get.loading) || initialDatasetsState.get.loading
  );

export const getSourceFilesFactory = (key: string) =>
  createSelector(
    getDatasetsFactory(key),
    (state: DatasetsState) =>
      (state && state.sourceFiles) || initialDatasetsState.sourceFiles
  );

export function getDatasetsForSchemaIdsEntityCodeAndReferenceDate(
  schemaIds: number[],
  entityCode: string,
  refDate: string
): MemoizedSelector<object, Dataset[]> {
  return createSelector(
    getDatasetsFactory(NAMESPACE),
    (state: DatasetsState) => {
      const datasets: Dataset[] =
        (state && (state.collection || initialDatasetsState.collection)) || [];

      return datasets
        .filter(dataset => dataset.localReferenceDate === refDate)
        .filter(dataset => dataset.entityCode === entityCode)
        .filter(dataset => schemaIds.indexOf(dataset.tableId) !== -1);
    }
  );
}
