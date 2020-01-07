import {
  ActionReducerMap,
  createFeatureSelector,
  createSelector
} from "@ngrx/store";

import { reducer } from "./http-request-count.reducer";

export interface State {
  httpRequestCount: number;
}

export const reducers: ActionReducerMap<State> = {
  httpRequestCount: reducer
};

export const selectLoaders = createFeatureSelector<State>("loaders");

export const selectHttpRequestCount = createSelector(
  selectLoaders,
  (state: State) => state.httpRequestCount
);

export const selectHasPendingHttpRequests = createSelector(
  selectHttpRequestCount,
  (state: number) => state > 0
);
