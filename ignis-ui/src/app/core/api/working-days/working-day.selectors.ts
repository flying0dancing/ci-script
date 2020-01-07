import { NAMESPACE } from "@/core/api/working-days/working-days.actions";
import { ProductWorkingDay } from "@/core/api/working-days/working-days.interface";
import {
  createFeatureSelector,
  createSelector,
  MemoizedSelector
} from "@ngrx/store";

import {
  initialWorkingDaysState,
  State,
  WorkingDaysState
} from "./working-days.reducer";

export const getWorkingDaysState = createFeatureSelector<State>(NAMESPACE);

export const getWorkingDaysFactory = (key: string) =>
  createSelector(
    getWorkingDaysState,
    (state: State) => state[key]
  );

export const getWorkingDaysCollectionFactory = (key: string) =>
  createSelector(
    getWorkingDaysFactory(key),
    (state: WorkingDaysState) =>
      (state && state.workingDays) || initialWorkingDaysState.workingDays
  );

export const getWorkingDaysGetStateFactory = (key: string) =>
  createSelector(
    getWorkingDaysFactory(key),
    (state: WorkingDaysState) =>
      (state && state.get) || initialWorkingDaysState.get
  );

export const getWorkingDaysGetLoadingStateFactory = (key: string) =>
  createSelector(
    getWorkingDaysFactory(key),
    (state: WorkingDaysState) =>
      (state && state.get.loading) || initialWorkingDaysState.get.loading
  );
export const getWorkingDaysUpdateLoadingStateFactory = (key: string) =>
  createSelector(
    getWorkingDaysFactory(key),
    (state: WorkingDaysState) =>
      (state && state.put.loading) || initialWorkingDaysState.put.loading
  );

export function getWorkingDays(): MemoizedSelector<
  object,
  { [key: number]: ProductWorkingDay[] }
> {
  return createSelector(
    getWorkingDaysFactory(NAMESPACE),
    (state: WorkingDaysState) => {
      return state.workingDays;
    }
  );
}
