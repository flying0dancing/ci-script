import { ActionReducer } from '@ngrx/store';
import { ErrorResponse } from '../utilities/interfaces/errors.interface';
import { GetAllIdentifiableSuccessAction } from '../utilities/interfaces/get-all-identifiable-action.interface';
import { GetOneIdentifiableSuccessAction } from '../utilities/interfaces/get-one-identifiable-action.interface';
import { IdentifiableAction } from '../utilities/interfaces/identifiable-action.interface';

export interface LoadingState {
  loading: boolean;
  loaded: boolean;
  errors: ErrorResponse;
}

export class LoadingUtility {
  static getInitialState(): LoadingState {
    return {
      loading: false,
      loaded: false,
      errors: ErrorResponse.empty()
    };
  }

  static toLoadingState(): LoadingState {
    return {
      loading: true,
      loaded: false,
      errors: ErrorResponse.empty()
    };
  }

  static toLoadedState(): LoadingState {
    return {
      loading: false,
      loaded: true,
      errors: ErrorResponse.empty()
    };
  }

  static toErrorState(errorResponse: ErrorResponse): LoadingState {
    return {
      loading: false,
      loaded: false,
      errors: new ErrorResponse(errorResponse.errors)
    };
  }

  static selectLoading(state: LoadingState): boolean {
    return state.loading;
  }

  static selectLoaded(state: LoadingState): boolean {
    return state.loaded;
  }

  static selectErrors(state: LoadingState): ErrorResponse {
    return state.errors;
  }
}

export interface IdState<State> {
  [key: number]: State;
}

export class IdStateUtility<State> {
  constructor(public initialState: State) {}

  createReducer(
    composedReducer: ActionReducer<State, IdentifiableAction>,
    actions: string[]
  ) {
    return function reducer(
      state: IdState<State> = {},
      action: IdentifiableAction
    ) {
      const matchedAction = actions.find(a => a === action.type);

      return matchedAction
        ? {
            ...state,
            [action.id]: composedReducer(state[action.id], action)
          }
        : state;
    };
  }

  selectState(state: IdState<State>, id: number): State {
    return state[id] || Object.assign({}, state, this.initialState);
  }
}

export interface ResourceState {
  ids: number[];
  loading: boolean;
  loaded: boolean;
  error: boolean;
}

export const INITIAL_STATE: ResourceState = {
  ids: null,
  loading: false,
  loaded: false,
  error: false
};

export function loadingTrue(state: ResourceState): ResourceState {
  return {
    ...state,
    loading: true,
    loaded: false,
    error: false
  };
}

export function errorOccurred(state: ResourceState): ResourceState {
  return {
    ...state,
    loading: false,
    loaded: false,
    error: true
  };
}

export function successfulGetAllResponse(
  state = INITIAL_STATE,
  action: GetAllIdentifiableSuccessAction
): ResourceState {
  return {
    ...state,
    ids: action.ids,
    loading: false,
    loaded: true,
    error: false
  };
}

export function successfulGetOneResponse(
  state = INITIAL_STATE,
  action: GetOneIdentifiableSuccessAction
): ResourceState {
  return {
    ...state,
    ids: addId(state.ids, action.id),
    loading: false,
    loaded: true,
    error: false
  };
}

export function successfulResponseNoUpdate(state = INITIAL_STATE) {
  return {
    ...state,
    loading: false,
    error: false
  };
}

function addId(ids: number[], newId: number): number[] | null {
  if (ids === null) {
    return null;
  }
  const idSet = new Set(ids.slice()) as any;
  idSet.add(newId);
  return [...idSet];
}

export const getIds = (state: ResourceState) => state.ids;

export const getLoading = (state: ResourceState) => state.loading;

export const getLoaded = (state: ResourceState) => state.loaded;

export const getError = (state: ResourceState) => state.error;

export interface ResponseState {
  ids: number[];
  loading: boolean;
  loaded: boolean;
  creating: boolean;
  createdIds: number[];
  errorResponse: ErrorResponse;
}

export const INITIAL_RESPONSE_STATE: ResponseState = {
  ids: [],
  loading: false,
  loaded: false,
  creating: false,
  createdIds: [],
  errorResponse: ErrorResponse.empty()
};

export function toLoadingState(state = INITIAL_RESPONSE_STATE): ResponseState {
  return {
    ...state,
    loading: true,
    loaded: false,
    errorResponse: ErrorResponse.empty()
  };
}

export function toCreatingState(state = INITIAL_RESPONSE_STATE): ResponseState {
  return {
    ...state,
    creating: true,
    createdIds: [],
    errorResponse: ErrorResponse.empty()
  };
}

export function toCreateSuccessState(
  state = INITIAL_RESPONSE_STATE,
  ids: number[]
): ResponseState {
  return {
    ...state,
    creating: false,
    createdIds: ids,
    errorResponse: ErrorResponse.empty()
  };
}

export function toCreateFailedState(
  state = INITIAL_RESPONSE_STATE,
  errorResponse: ErrorResponse
): ResponseState {
  return {
    ...toErrorState(state, errorResponse),
    creating: false,
    createdIds: []
  };
}

export function toSuccessState(state = INITIAL_RESPONSE_STATE): ResponseState {
  return {
    ...state,
    loading: false,
    loaded: true,
    errorResponse: ErrorResponse.empty()
  };
}

export function toSuccessfulGetAllState(
  state = INITIAL_RESPONSE_STATE,
  action: GetAllIdentifiableSuccessAction
): ResponseState {
  return toSuccessState({ ...state, ids: action.ids });
}

export function toErrorState(
  state = INITIAL_RESPONSE_STATE,
  errorResponse: ErrorResponse
): ResponseState {
  return {
    ...state,
    loading: false,
    loaded: true,
    errorResponse: new ErrorResponse(errorResponse.errors)
  };
}

export const selectIds = (state: ResponseState) => state.ids;

export const selectLoading = (state: ResponseState) => state.loading;
export const selectLoaded = (state: ResponseState) => state.loaded;
export const selectErrorResponse = (state: ResponseState) =>
  state.errorResponse;

export const selectCreating = (state: ResponseState) => state.creating;
export const selectCreated = (state: ResponseState) => state.createdIds;
