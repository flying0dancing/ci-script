import * as deepFreeze from "deep-freeze";
import * as AuthActions from "../auth.actions";
import { authReducer, initialAuthState, reducer } from "../auth.reducer";

import { password, username } from "./auth.mocks";

const reducerMapKey = "test";

describe("Auth Reducer", () => {
  it("should not affect the state on an unknown action", () => {
    const state = {
      login: { loading: false, error: null },
      logout: { loading: false, error: false }
    };
    const action = { type: "unknown-action" } as any;

    deepFreeze(state);
    deepFreeze(action);

    expect(authReducer(state, action)).toEqual(state);
  });

  it("should return the initial state when no state is provided", () => {
    const state = undefined;
    const action = { type: "unknown-action" } as any;

    deepFreeze(action);

    expect(authReducer(state, action)).toEqual(initialAuthState);
  });

  describe("LOGIN action", () => {
    it("should set the loading state to true", () => {
      const state = initialAuthState;
      const action = new AuthActions.Login({
        reducerMapKey,
        username,
        password,
        refererUrl: null
      });

      deepFreeze(state);
      deepFreeze(action);

      expect(authReducer(state, action)).toEqual({
        login: { loading: true, error: null },
        logout: { loading: false, error: false }
      });
    });
  });

  describe("LOGIN_SUCCESS action", () => {
    it("should set the loading state to false and update the collection", () => {
      const state = {
        login: { loading: true, error: null },
        logout: { loading: false, error: false }
      };
      const action = new AuthActions.LoginSuccess({ reducerMapKey });

      deepFreeze(state);
      deepFreeze(action);

      expect(authReducer(state, action)).toEqual({
        login: { loading: false, error: null },
        logout: { loading: false, error: false }
      });
    });
  });

  describe("LOGIN_FAIL action", () => {
    it("should set the loading state to false and error state to error code and message", () => {
      const state = {
        login: { loading: true, error: null },
        logout: { loading: false, error: false }
      };
      const action = new AuthActions.LoginFail({
        reducerMapKey,
        error: { status: "401", error: { message: "error" } }
      });

      deepFreeze(state);
      deepFreeze(action);

      expect(authReducer(state, action)).toEqual({
        login: {
          loading: false,
          error: { statusCode: "401", message: "error" }
        },
        logout: { loading: false, error: false }
      });
    });
  });

  describe("LOGOUT action", () => {
    it("should set the loading state to true", () => {
      const state = initialAuthState;
      const action = new AuthActions.Logout({ reducerMapKey });

      deepFreeze(state);
      deepFreeze(action);

      expect(authReducer(state, action)).toEqual({
        login: { loading: false, error: null },
        logout: { loading: true, error: false }
      });
    });
  });

  describe("LOGOUT_SUCCESS action", () => {
    it("should set the loading state to false", () => {
      const state = {
        login: { loading: false, error: null },
        logout: { loading: true, error: false }
      };
      const action = new AuthActions.LogoutSuccess({ reducerMapKey });

      deepFreeze(state);
      deepFreeze(action);

      expect(authReducer(state, action)).toEqual({
        login: { loading: false, error: null },
        logout: { loading: false, error: false }
      });
    });
  });

  describe("LOGOUT_FAIL action", () => {
    it("should set the loading state to false and error state to true", () => {
      const state = {
        login: { loading: false, error: null },
        logout: { loading: true, error: false }
      };
      const action = new AuthActions.LogoutFail({ reducerMapKey });

      deepFreeze(state);
      deepFreeze(action);

      expect(authReducer(state, action)).toEqual({
        login: { loading: false, error: null },
        logout: { loading: false, error: true }
      });
    });
  });

  describe("EMPTY action", () => {
    it("should return the initial state", () => {
      const state = {
        login: { loading: false, error: null },
        logout: { loading: false, error: false }
      };
      const action = new AuthActions.Empty({ reducerMapKey });

      deepFreeze(state);
      deepFreeze(action);

      expect(authReducer(state, action)).toEqual(initialAuthState);
    });
  });
});

describe("auth map reducer", () => {
  const initialState = {};
  deepFreeze(initialState);

  it("should not affect the state on an unknown action", () => {
    const action = { type: "unknown-action" } as any;

    deepFreeze(action);

    expect(reducer(initialState, action)).toEqual(initialState);
  });

  it("should return the initial state when no state is provided", () => {
    const state = undefined;
    const action = { type: "unknown-action" } as any;

    deepFreeze(action);

    expect(reducer(state, action)).toEqual(initialState);
  });

  describe("LOGIN action", () => {
    it("should set the map key", () => {
      const action = new AuthActions.Login({
        reducerMapKey,
        username,
        password,
        refererUrl: null
      });

      deepFreeze(action);

      expect(Object.keys(reducer(initialState, action))).toContain(
        reducerMapKey
      );
    });
  });

  describe("LOGIN_SUCCESS action", () => {
    it("should set the map key", () => {
      const action = new AuthActions.LoginSuccess({ reducerMapKey });

      deepFreeze(action);

      expect(Object.keys(reducer(initialState, action))).toContain(
        reducerMapKey
      );
    });
  });

  describe("LOGIN_FAIL action", () => {
    it("should set the map key", () => {
      const action = new AuthActions.LoginFail({
        reducerMapKey,
        error: {
          status: 401,
          error: {
            message: "error"
          }
        }
      });

      deepFreeze(action);

      expect(Object.keys(reducer(initialState, action))).toContain(
        reducerMapKey
      );
    });
  });

  describe("LOGOUT action", () => {
    it("should set the map key", () => {
      const action = new AuthActions.Logout({ reducerMapKey });

      deepFreeze(action);

      expect(Object.keys(reducer(initialState, action))).toContain(
        reducerMapKey
      );
    });
  });

  describe("LOGOUT_SUCCESS action", () => {
    it("should set the map key", () => {
      const action = new AuthActions.LogoutSuccess({ reducerMapKey });

      deepFreeze(action);

      expect(Object.keys(reducer(initialState, action))).toContain(
        reducerMapKey
      );
    });
  });

  describe("LOGOUT_FAIL action", () => {
    it("should set the map key", () => {
      const action = new AuthActions.LogoutFail({ reducerMapKey });

      deepFreeze(action);

      expect(Object.keys(reducer(initialState, action))).toContain(
        reducerMapKey
      );
    });
  });

  describe("EMPTY action", () => {
    it("should set the map key", () => {
      const action = new AuthActions.Empty({ reducerMapKey });

      deepFreeze(action);

      expect(Object.keys(reducer(initialState, action))).toContain(
        reducerMapKey
      );
    });
  });
});
