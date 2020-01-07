import { CalendarHoliday } from "@/core/api/calendar/calendar.interface";
import * as CalendarActions from "./calendar.actions";
import { GetSuccess } from "./calendar.actions";

export interface CalendarState {
  collection: { [key: string]: CalendarHoliday[] };
  get: {
    loading: boolean;
    error: boolean;
  };
  post: {
    loading: boolean;
    error: boolean;
  };
  put: {
    loading: boolean;
    error: boolean;
  };
  delete: {
    loading: boolean;
    error: boolean;
  };
}

export interface State {
  [key: string]: CalendarState;
}

export const initialCalendarState: CalendarState = {
  collection: {},
  get: {
    loading: false,
    error: false
  },
  post: {
    loading: false,
    error: false
  },
  put: {
    loading: false,
    error: false
  },
  delete: {
    loading: false,
    error: false
  }
};

function keyedCalendars(action: GetSuccess) {
  const calendarMap = {};

  action.payload.calendars.forEach(value => {
    const existing = !!calendarMap[value.product]
      ? calendarMap[value.product]
      : [];
    existing.push(value);
    calendarMap[value.product] = existing;
  });
  return calendarMap;
}

function addNewHoliday(state: CalendarState, newCalendar) {
  const newState = { ...state.collection };
  const existing = !newState[newCalendar.product]
    ? []
    : newState[newCalendar.product];
  existing.push(newCalendar);
  newState[newCalendar.product] = existing;
  return newState;
}

function removeHoliday(
  state: CalendarState,
  calendarId: number,
  productName: string
): { [key: string]: CalendarHoliday[] } {
  const withItemRemoved = state.collection[productName].filter(
    value => value.id !== calendarId
  );
  const newCollection = { ...state.collection };
  newCollection[productName] = withItemRemoved;

  return newCollection;
}

function updateHoliday(
  state: CalendarState,
  calendar: CalendarHoliday
): { [key: string]: CalendarHoliday[] } {
  const indexToUpdate = state.collection[calendar.product]
    .map(hol => hol.id)
    .indexOf(calendar.id);

  const newCollection = { ...state.collection };
  newCollection[calendar.product][indexToUpdate] = calendar;

  return newCollection;
}

export function calendarsReducer(
  state: CalendarState = initialCalendarState,
  action: CalendarActions.Types
): CalendarState {
  switch (action.type) {
    case CalendarActions.GET:
      return { ...state, get: { ...state.get, loading: true } };
    case CalendarActions.GET_SUCCESS:
      return {
        ...state,
        get: { ...state.get, loading: false },
        collection: keyedCalendars(action)
      };
    case CalendarActions.GET_FAIL:
      return { ...state, get: { loading: false, error: true } };

    case CalendarActions.CREATE:
      return { ...state, post: { ...state.post, loading: true } };

    case CalendarActions.CREATE_SUCCESS:
      const newCalendar = action.payload.calendar;
      return {
        ...state,
        post: { ...state.post, loading: false },
        collection: addNewHoliday(state, newCalendar)
      };

    case CalendarActions.CREATE_FAIL:
      return { ...state, post: { loading: false, error: true } };

    case CalendarActions.UPDATE:
      return { ...state, put: { ...state.put, loading: true } };

    case CalendarActions.UPDATE_SUCCESS:
      const calendarToUpdate = action.payload.calendar;
      return {
        ...state,
        put: { ...state.put, loading: false },
        collection: updateHoliday(state, calendarToUpdate)
      };

    case CalendarActions.UPDATE_FAIL:
      return { ...state, put: { loading: false, error: true } };

    case CalendarActions.DELETE:
      return { ...state, delete: { ...state.delete, loading: true } };

    case CalendarActions.DELETE_SUCCESS:
      return {
        ...state,
        delete: { ...state.delete, loading: false },
        collection: removeHoliday(
          state,
          action.payload.calendarId,
          action.payload.productName
        )
      };

    case CalendarActions.DELETE_FAIL:
      return { ...state, delete: { loading: false, error: true } };

    case CalendarActions.EMPTY:
      return { ...initialCalendarState };

    default:
      return state;
  }
}

export function reducer(state: State = {}, action: any) {
  switch (action.type) {
    case CalendarActions.GET:
    case CalendarActions.GET_SUCCESS:
    case CalendarActions.GET_FAIL:
    case CalendarActions.CREATE:
    case CalendarActions.CREATE_SUCCESS:
    case CalendarActions.CREATE_FAIL:
    case CalendarActions.UPDATE:
    case CalendarActions.UPDATE_SUCCESS:
    case CalendarActions.UPDATE_FAIL:
    case CalendarActions.DELETE:
    case CalendarActions.DELETE_SUCCESS:
    case CalendarActions.DELETE_FAIL:
    case CalendarActions.EMPTY:
      return {
        ...state,
        [action.payload.reducerMapKey]: calendarsReducer(
          state[action.payload.reducerMapKey],
          action
        )
      };
    default:
      return state;
  }
}
