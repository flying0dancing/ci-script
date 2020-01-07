import * as TablesActions from "./tables.actions";

export interface TablesState {
  collection: any;
  get: {
    loading: boolean;
    error: boolean;
  };
}

export interface State {
  [key: string]: TablesState;
}

export const initialTablesState = {
  collection: [],
  get: {
    loading: false,
    error: false
  }
};

export function tablesReducer(
  state: TablesState = initialTablesState,
  action: TablesActions.Types
) {
  switch (action.type) {
    case TablesActions.GET:
      return { ...state, get: { ...state.get, loading: true } };

    case TablesActions.GET_SUCCESS:
      return {
        ...state,
        get: { ...state.get, loading: false },
        collection: action.payload.tables
      };

    case TablesActions.GET_FAIL:
      return { ...state, get: { loading: false, error: true } };

    case TablesActions.EMPTY:
      return { ...initialTablesState };

    default:
      return state;
  }
}

export function reducer(state: State = {}, action: any) {
  switch (action.type) {
    case TablesActions.GET:
    case TablesActions.GET_SUCCESS:
    case TablesActions.GET_FAIL:
    case TablesActions.EMPTY:
      return {
        ...state,
        [action.payload.reducerMapKey]: tablesReducer(
          state[action.payload.reducerMapKey],
          action
        )
      };
    default:
      return state;
  }
}
