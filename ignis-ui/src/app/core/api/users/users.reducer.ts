import * as UsersActions from "./users.actions";
import { User } from "./users.interfaces";

export interface UsersState {
  collection: User[];
  currentUser: User;
  get: {
    loading: boolean;
    error: boolean;
  };
  post: {
    loading: boolean;
    error: boolean;
  };
  getCurrentUser: {
    loading: boolean;
    error: boolean;
  };
  changePassword: {
    loading: boolean;
    error: boolean;
  };
}

export interface State {
  [key: string]: UsersState;
}

export const initialUsersState = {
  collection: [],
  currentUser: null,
  get: {
    loading: false,
    error: false
  },
  post: {
    loading: false,
    error: false
  },
  getCurrentUser: {
    loading: false,
    error: false
  },
  changePassword: {
    loading: false,
    error: false
  }
};

export function usersReducer(
  state: UsersState = initialUsersState,
  action: UsersActions.Types
) {
  switch (action.type) {
    case UsersActions.GET:
      return { ...state, get: { ...state.get, loading: true, error: false } };

    case UsersActions.GET_SUCCESS:
      return {
        ...state,
        get: { ...state.get, loading: false },
        collection: action.payload.collection
      };

    case UsersActions.GET_FAIL:
      return { ...state, get: { loading: false, error: true } };

    case UsersActions.GET_CURRENT_USER:
      return {
        ...state,
        getCurrentUser: { ...state.getCurrentUser, loading: true, error: false }
      };

    case UsersActions.GET_CURRENT_USER_SUCCESS:
      return {
        ...state,
        getCurrentUser: { ...state.getCurrentUser, loading: false },
        currentUser: action.payload.currentUser
      };

    case UsersActions.GET_CURRENT_USER_FAIL:
      return { ...state, getCurrentUser: { loading: false, error: true } };

    case UsersActions.POST:
      return { ...state, post: { ...state.post, loading: true, error: false } };

    case UsersActions.POST_SUCCESS:
      return { ...state, post: { ...state.post, loading: false } };

    case UsersActions.POST_FAIL:
      return { ...state, post: { loading: false, error: true } };

    case UsersActions.CHANGE_PASSWORD:
      return {
        ...state,
        changePassword: { ...state.changePassword, loading: true, error: false }
      };

    case UsersActions.CHANGE_PASSWORD_SUCCESS:
      return {
        ...state,
        changePassword: { ...state.changePassword, loading: false }
      };

    case UsersActions.CHANGE_PASSWORD_FAIL:
      return {
        ...state,
        changePassword: {
          loading: false,
          error: {
            statusCode: action.payload.statusCode
          }
        }
      };
    case UsersActions.CLEAR_PASSWORD_ERRORS:
      return {
        ...state,
        changePassword: {
          loading: false,
          error: null
        }
      };

    case UsersActions.EMPTY:
      return { ...initialUsersState };

    default:
      return state;
  }
}

export function reducer(state: State = {}, action: any) {
  switch (action.type) {
    case UsersActions.GET:
    case UsersActions.GET_SUCCESS:
    case UsersActions.GET_FAIL:
    case UsersActions.GET_CURRENT_USER:
    case UsersActions.GET_CURRENT_USER_SUCCESS:
    case UsersActions.GET_CURRENT_USER_FAIL:
    case UsersActions.POST:
    case UsersActions.POST_SUCCESS:
    case UsersActions.POST_FAIL:
    case UsersActions.CHANGE_PASSWORD:
    case UsersActions.CHANGE_PASSWORD_SUCCESS:
    case UsersActions.CHANGE_PASSWORD_FAIL:
    case UsersActions.CLEAR_PASSWORD_ERRORS:
    case UsersActions.EMPTY:
      return {
        ...state,
        [action.payload.reducerMapKey]: usersReducer(
          state[action.payload.reducerMapKey],
          action
        )
      };
    default:
      return state;
  }
}
