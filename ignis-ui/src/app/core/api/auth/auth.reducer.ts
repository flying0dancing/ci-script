import * as AuthActions from "./auth.actions";

export interface AuthState {
  login: {
    loading: boolean;
    error: {
      statusCode: number;
      message: string;
    };
  };
  logout: {
    loading: boolean;
    error: boolean;
  };
}

export interface State {
  [key: string]: AuthState;
}

export const initialAuthState = {
  login: {
    loading: false,
    error: null
  },
  logout: {
    loading: false,
    error: false
  }
};

export function authReducer(
  state: AuthState = initialAuthState,
  action: AuthActions.Types
) {
  switch (action.type) {
    case AuthActions.LOGIN:
      return {
        ...state,
        login: { ...state.login, loading: true, error: null }
      };

    case AuthActions.LOGIN_SUCCESS:
      return { ...state, login: { ...state.login, loading: false } };

    case AuthActions.LOGIN_FAIL:
      return {
        ...state,
        login: {
          loading: false,
          error: {
            statusCode: action.payload.error.status,
            message: action.payload.error.error.message
          }
        }
      };

    case AuthActions.LOGOUT:
      return { ...state, logout: { ...state.logout, loading: true } };

    case AuthActions.LOGOUT_SUCCESS:
      return { ...state, logout: { ...state.logout, loading: false } };

    case AuthActions.LOGOUT_FAIL:
      return { ...state, logout: { loading: false, error: true } };

    case AuthActions.EMPTY:
      return { ...initialAuthState };

    default:
      return state;
  }
}

export function reducer(state: State = {}, action: any) {
  switch (action.type) {
    case AuthActions.LOGIN:
    case AuthActions.LOGIN_SUCCESS:
    case AuthActions.LOGIN_FAIL:
    case AuthActions.LOGOUT:
    case AuthActions.LOGOUT_SUCCESS:
    case AuthActions.LOGOUT_FAIL:
    case AuthActions.EMPTY:
      return {
        ...state,
        [action.payload.reducerMapKey]: authReducer(
          state[action.payload.reducerMapKey],
          action
        )
      };
    default:
      return state;
  }
}
