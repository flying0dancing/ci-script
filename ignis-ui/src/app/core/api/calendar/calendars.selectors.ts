import { NAMESPACE } from "@/core/api/calendar/calendar.actions";
import { CalendarHoliday } from "@/core/api/calendar/calendar.interface";
import { flatten } from "@/shared/utilities/array.utilities";
import {
  createFeatureSelector,
  createSelector,
  MemoizedSelector
} from "@ngrx/store";

import { CalendarState, initialCalendarState, State } from "./calendar.reducer";

export const getCalendarsState = createFeatureSelector<State>(NAMESPACE);

export const getCalendarsFactory = (key: string) =>
  createSelector(
    getCalendarsState,
    (state: State) => state[key]
  );

export const getCalendarsCollectionFactory = (key: string) =>
  createSelector(
    getCalendarsFactory(key),
    (state: CalendarState) =>
      (state && state.collection) || initialCalendarState.collection
  );

export function getCalendarByIdFactory(
  id: number,
  key: string
): MemoizedSelector<object, CalendarHoliday> {
  return createSelector(
    getCalendarsFactory(key),
    (state: CalendarState) => {
      return flatten(Object.values(state.collection)).find(
        calendar => calendar.id === id
      );
    }
  );
}

export const getCalendarsGetStateFactory = (key: string) =>
  createSelector(
    getCalendarsFactory(key),
    (state: CalendarState) => (state && state.get) || initialCalendarState.get
  );

export const getCalendarsGetLoadingStateFactory = (key: string) =>
  createSelector(
    getCalendarsFactory(key),
    (state: CalendarState) =>
      (state && state.get.loading) || initialCalendarState.get.loading
  );
export const getCalendarsCreateStateFactory = (key: string) =>
  createSelector(
    getCalendarsFactory(key),
    (state: CalendarState) => (state && state.post) || initialCalendarState.post
  );

export const getCalendarsCreateLoadingStateFactory = (key: string) =>
  createSelector(
    getCalendarsFactory(key),
    (state: CalendarState) =>
      (state && state.post.loading) || initialCalendarState.post.loading
  );

export const getCalendarsUpdateLoadingStateFactory = (key: string) =>
  createSelector(
    getCalendarsFactory(key),
    (state: CalendarState) =>
      (state && state.put.loading) || initialCalendarState.put.loading
  );

export const getCalendarsDeleteLoadingStateFactory = (key: string) =>
  createSelector(
    getCalendarsFactory(key),
    (state: CalendarState) =>
      (state && state.delete.loading) || initialCalendarState.delete.loading
  );

export function getCalendars(): MemoizedSelector<
  object,
  { [key: number]: CalendarHoliday[] }
> {
  return createSelector(
    getCalendarsFactory(NAMESPACE),
    (state: CalendarState) => {
      return state.collection;
    }
  );
}

export function getCalendarsForProductId(
  productId: number
): MemoizedSelector<object, CalendarHoliday[]> {
  return createSelector(
    getCalendarsFactory(NAMESPACE),
    (state: CalendarState) => {
      return state.collection[productId];
    }
  );
}
