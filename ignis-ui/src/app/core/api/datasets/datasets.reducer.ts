import * as DatasetsActions from "./datasets.actions";
import { Dataset } from "./datasets.interfaces";
import { SourceFiles } from "./datasets.types";

// @todo consider separate reducer for files as they are 2 different entities
export interface DatasetsState {
  collection: Dataset[];
  sourceFiles: SourceFiles;
  get: {
    loading: boolean;
    error: boolean;
  };
}

export interface State {
  [key: string]: DatasetsState;
}

export const initialDatasetsState = {
  collection: [],
  sourceFiles: [],
  get: {
    loading: false,
    error: false
  }
};

export function datasetsReducer(
  state: DatasetsState = initialDatasetsState,
  action: DatasetsActions.Types
) {
  switch (action.type) {
    case DatasetsActions.GET:
      return { ...state, get: { ...state.get, loading: true } };

    case DatasetsActions.GET_SUCCESS:
      return {
        ...state,
        get: { ...state.get, loading: false },
        collection: action.payload.datasets
      };

    case DatasetsActions.GET_FAIL:
      return { ...state, get: { loading: false, error: true } };

    case DatasetsActions.GET_SILENT_SUCCESS:
      return { ...state, collection: action.payload.datasets };

    case DatasetsActions.GET_SOURCE_FILES:
      return { ...state, get: { ...state.get, loading: true } };

    case DatasetsActions.GET_SOURCE_FILES_SUCCESS:
      return {
        ...state,
        get: { ...state.get, loading: false },
        sourceFiles: action.payload.sourceFiles
      };

    case DatasetsActions.GET_SOURCE_FILES_FAIL:
      return { ...state, get: { loading: false, error: true } };

    case DatasetsActions.EMPTY:
      return { ...initialDatasetsState };

    default:
      return state;
  }
}

export function reducer(state: State = {}, action: any) {
  switch (action.type) {
    case DatasetsActions.GET:
    case DatasetsActions.GET_SILENT:
    case DatasetsActions.GET_SILENT_SUCCESS:
    case DatasetsActions.GET_SUCCESS:
    case DatasetsActions.GET_FAIL:
    case DatasetsActions.GET_SOURCE_FILES:
    case DatasetsActions.GET_SOURCE_FILES_SUCCESS:
    case DatasetsActions.GET_SOURCE_FILES_FAIL:
    case DatasetsActions.EMPTY:
      return {
        ...state,
        [action.payload.reducerMapKey]: datasetsReducer(
          state[action.payload.reducerMapKey],
          action
        )
      };
    default:
      return state;
  }
}
