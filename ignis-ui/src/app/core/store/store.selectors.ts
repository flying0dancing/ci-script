import * as fromRouter from "@ngrx/router-store";
import { createFeatureSelector, createSelector } from "@ngrx/store";

export const getRouterState = createFeatureSelector<
  fromRouter.RouterReducerState
>("router");

export const getRouterQueryParams = createSelector(
  getRouterState,
  (state: fromRouter.RouterReducerState) =>
    state ? state.state.root.queryParams : []
);
