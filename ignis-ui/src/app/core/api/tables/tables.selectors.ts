import { createFeatureSelector, createSelector } from "@ngrx/store";
import { NAMESPACE } from "./tables.constants";
import { initialTablesState, State, TablesState } from "./tables.reducer";

export const getTablesState = createFeatureSelector<State>(NAMESPACE);

export const getTablesFactory = (key: string) =>
  createSelector(
    getTablesState,
    (state: State) => state[key]
  );

export const getTablesCollectionFactory = (key: string) =>
  createSelector(
    getTablesFactory(key),
    (state: TablesState) =>
      (state && state.collection) || initialTablesState.collection
  );

export const getTablesGetStateFactory = (key: string) =>
  createSelector(
    getTablesFactory(key),
    (state: TablesState) => (state && state.get) || initialTablesState.get
  );

export const getTablesGetLoadingStateFactory = (key: string) =>
  createSelector(
    getTablesFactory(key),
    (state: TablesState) =>
      (state && state.get.loading) || initialTablesState.get.loading
  );
