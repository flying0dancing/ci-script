import { ProductWorkingDay } from "@/core/api/working-days/working-days.interface";
import * as WorkingDaysActions from "./working-days.actions";

export interface WorkingDaysState {
  workingDays: { [key: string]: ProductWorkingDay[] };
  get: {
    loading: boolean;
    error: boolean;
  };
  put: {
    loading: boolean;
    error: boolean;
  };
}

export interface State {
  [key: string]: WorkingDaysState;
}

export const initialWorkingDaysState: WorkingDaysState = {
  workingDays: {},
  get: {
    loading: false,
    error: false
  },
  put: {
    loading: false,
    error: false
  }
};

function keyedWorkingDays(action: WorkingDaysActions.GetSuccess) {
  const workingDayMap = {};

  action.payload.workingDays.forEach(value => {
    const existing = !!workingDayMap[value.product]
      ? workingDayMap[value.product]
      : [];
    existing.push(value);
    workingDayMap[value.product] = existing;
  });
  return workingDayMap;
}

function updateWorkingDays(
  state: WorkingDaysState,
  productName: string,
  workingDays: ProductWorkingDay[]
): { [key: string]: ProductWorkingDay[] } {
  const newCollection = { ...state.workingDays };
  newCollection[productName] = workingDays;

  return newCollection;
}

export function workingDaysReducer(
  state: WorkingDaysState = initialWorkingDaysState,
  action: WorkingDaysActions.Types
): WorkingDaysState {
  switch (action.type) {
    case WorkingDaysActions.GET_SUCCESS:
      return {
        ...state,
        get: { ...state.get, loading: false },
        workingDays: keyedWorkingDays(action)
      };
    case WorkingDaysActions.GET_FAIL:
      return { ...state, get: { loading: false, error: true } };

    case WorkingDaysActions.UPDATE:
      return { ...state, put: { ...state.put, loading: true } };

    case WorkingDaysActions.UPDATE_SUCCESS:
      return {
        ...state,
        put: { ...state.put, loading: false },
        workingDays: updateWorkingDays(
          state,
          action.payload.productName,
          action.payload.newWorkingDays
        )
      };

    case WorkingDaysActions.UPDATE_FAIL:
      return { ...state, put: { loading: false, error: true } };

    case WorkingDaysActions.EMPTY:
      return { ...initialWorkingDaysState };

    default:
      return state;
  }
}

export function reducer(state: State = {}, action: any) {
  switch (action.type) {
    case WorkingDaysActions.GET:
    case WorkingDaysActions.GET_FAIL:
    case WorkingDaysActions.GET_SUCCESS:
    case WorkingDaysActions.UPDATE:
    case WorkingDaysActions.UPDATE_SUCCESS:
    case WorkingDaysActions.UPDATE_FAIL:
    case WorkingDaysActions.EMPTY:
      return {
        ...state,
        [action.payload.reducerMapKey]: workingDaysReducer(
          state[action.payload.reducerMapKey],
          action
        )
      };
    default:
      return state;
  }
}
