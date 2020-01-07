import * as deepFreeze from "deep-freeze";
import * as UsersActions from "../users.actions";
import { initialUsersState, reducer, usersReducer } from "../users.reducer";

import {
  getUsersResponse,
  newPassword,
  oldPassword,
  user,
  username
} from "./users.mocks";

const reducerMapKey = "test";
const defaultState = {
  collection: [],
  currentUser: null,
  get: { loading: false, error: false },
  post: { loading: false, error: false },
  getCurrentUser: { loading: false, error: false },
  changePassword: { loading: false, error: false }
};

describe("Users Reducer", () => {
  it("should not affect the state on an unknown action", () => {
    const state = defaultState;
    const action = { type: "unknown-action" } as any;

    deepFreeze(state);
    deepFreeze(action);

    expect(usersReducer(state, action)).toEqual(state);
  });

  it("should return the initial state when no state is provided", () => {
    const state = undefined;
    const action = { type: "unknown-action" } as any;

    deepFreeze(action);

    expect(usersReducer(state, action)).toEqual(initialUsersState);
  });

  describe("GET action", () => {
    it("should set the loading state to true", () => {
      const state = initialUsersState;
      const action = new UsersActions.Get({ reducerMapKey });

      deepFreeze(state);
      deepFreeze(action);

      expect(usersReducer(state, action)).toEqual({
        collection: [],
        currentUser: null,
        get: { loading: true, error: false },
        post: { loading: false, error: false },
        getCurrentUser: { loading: false, error: false },
        changePassword: { loading: false, error: false }
      });
    });
  });

  describe("GET_SUCCESS action", () => {
    it("should set the loading state to false and update the collection", () => {
      const state = {
        collection: [],
        currentUser: null,
        get: { loading: true, error: false },
        post: { loading: false, error: false },
        getCurrentUser: { loading: false, error: false },
        changePassword: { loading: false, error: false }
      };
      const action = new UsersActions.GetSuccess({
        reducerMapKey,
        collection: getUsersResponse
      });

      deepFreeze(state);
      deepFreeze(action);

      expect(usersReducer(state, action)).toEqual({
        collection: getUsersResponse,
        currentUser: null,
        get: { loading: false, error: false },
        post: { loading: false, error: false },
        getCurrentUser: { loading: false, error: false },
        changePassword: { loading: false, error: false }
      });
    });
  });

  describe("GET_FAIL action", () => {
    it("should set the loading state to false and error state to true", () => {
      const state = {
        collection: [],
        currentUser: null,
        get: { loading: true, error: false },
        post: { loading: false, error: false },
        getCurrentUser: { loading: false, error: false },
        changePassword: { loading: false, error: false }
      };
      const action = new UsersActions.GetFail({ reducerMapKey });

      deepFreeze(state);
      deepFreeze(action);

      expect(usersReducer(state, action)).toEqual({
        collection: [],
        currentUser: null,
        get: { loading: false, error: true },
        post: { loading: false, error: false },
        getCurrentUser: { loading: false, error: false },
        changePassword: { loading: false, error: false }
      });
    });
  });

  describe("GET_CURRENT_USER action", () => {
    it("should set the loading state to true", () => {
      const state = initialUsersState;
      const action = new UsersActions.GetCurrentUser({ reducerMapKey });

      deepFreeze(state);
      deepFreeze(action);

      expect(usersReducer(state, action)).toEqual({
        collection: [],
        currentUser: null,
        get: { loading: false, error: false },
        post: { loading: false, error: false },
        getCurrentUser: { loading: true, error: false },
        changePassword: { loading: false, error: false }
      });
    });
  });

  describe("GET_CURRENT_USER_SUCCESS action", () => {
    it("should set the loading state to false and update the collection", () => {
      const state = {
        collection: [],
        currentUser: null,
        get: { loading: false, error: false },
        post: { loading: false, error: false },
        getCurrentUser: { loading: true, error: false },
        changePassword: { loading: false, error: false }
      };
      const action = new UsersActions.GetCurrentUserSuccess({
        reducerMapKey,
        currentUser: user
      });

      deepFreeze(state);
      deepFreeze(action);

      expect(usersReducer(state, action)).toEqual({
        collection: [],
        currentUser: user,
        get: { loading: false, error: false },
        post: { loading: false, error: false },
        getCurrentUser: { loading: false, error: false },
        changePassword: { loading: false, error: false }
      });
    });
  });

  describe("GET_CURRENT_USER_FAIL action", () => {
    it("should set the loading state to false and error state to true", () => {
      const state = {
        collection: [],
        currentUser: null,
        get: { loading: false, error: false },
        post: { loading: false, error: false },
        getCurrentUser: { loading: true, error: false },
        changePassword: { loading: false, error: false }
      };
      const action = new UsersActions.GetCurrentUserFail({ reducerMapKey });

      deepFreeze(state);
      deepFreeze(action);

      expect(usersReducer(state, action)).toEqual({
        collection: [],
        currentUser: null,
        get: { loading: false, error: false },
        post: { loading: false, error: false },
        getCurrentUser: { loading: false, error: true },
        changePassword: { loading: false, error: false }
      });
    });
  });

  describe("POST action", () => {
    it("should set the loading state to true", () => {
      const state = initialUsersState;
      const action = new UsersActions.Post({
        reducerMapKey,
        username,
        password: newPassword
      });

      deepFreeze(state);
      deepFreeze(action);

      expect(usersReducer(state, action)).toEqual({
        collection: [],
        currentUser: null,
        get: { loading: false, error: false },
        post: { loading: true, error: false },
        getCurrentUser: { loading: false, error: false },
        changePassword: { loading: false, error: false }
      });
    });
  });

  describe("POST_SUCCESS action", () => {
    it("should set the loading state to false and update the collection", () => {
      const state = {
        collection: [],
        currentUser: null,
        get: { loading: false, error: false },
        post: { loading: true, error: false },
        getCurrentUser: { loading: false, error: false },
        changePassword: { loading: false, error: false }
      };
      const action = new UsersActions.PostSuccess({ reducerMapKey });

      deepFreeze(state);
      deepFreeze(action);

      expect(usersReducer(state, action)).toEqual(defaultState);
    });
  });

  describe("POST_FAIL action", () => {
    it("should set the loading state to false and error state to true", () => {
      const state = {
        collection: [],
        currentUser: null,
        get: { loading: false, error: false },
        post: { loading: true, error: false },
        getCurrentUser: { loading: false, error: false },
        changePassword: { loading: false, error: false }
      };
      const action = new UsersActions.PostFail({ reducerMapKey });

      deepFreeze(state);
      deepFreeze(action);

      expect(usersReducer(state, action)).toEqual({
        collection: [],
        currentUser: null,
        get: { loading: false, error: false },
        post: { loading: false, error: true },
        getCurrentUser: { loading: false, error: false },
        changePassword: { loading: false, error: false }
      });
    });
  });

  describe("CHANGE_PASSWORD action", () => {
    it("should set the loading state to true", () => {
      const state = initialUsersState;
      const action = new UsersActions.ChangePassword({
        reducerMapKey,
        oldPassword,
        newPassword
      });

      deepFreeze(state);
      deepFreeze(action);

      expect(usersReducer(state, action)).toEqual({
        collection: [],
        currentUser: null,
        get: { loading: false, error: false },
        post: { loading: false, error: false },
        getCurrentUser: { loading: false, error: false },
        changePassword: { loading: true, error: false }
      });
    });
  });

  describe("CHANGE_PASSWORD_SUCCESS action", () => {
    it("should set the loading state to false and update the collection", () => {
      const state = {
        collection: [],
        currentUser: null,
        get: { loading: false, error: false },
        post: { loading: false, error: false },
        getCurrentUser: { loading: false, error: false },
        changePassword: { loading: true, error: false }
      };
      const action = new UsersActions.ChangePasswordSuccess({ reducerMapKey });

      deepFreeze(state);
      deepFreeze(action);

      expect(usersReducer(state, action)).toEqual(defaultState);
    });
  });

  describe("CHANGE_PASSWORD_FAIL action", () => {
    it("should set the loading state to false and error state to true", () => {
      const state = {
        collection: [],
        currentUser: null,
        get: { loading: false, error: false },
        post: { loading: false, error: false },
        getCurrentUser: { loading: false, error: false },
        changePassword: { loading: true, error: false }
      };
      const action = new UsersActions.ChangePasswordFail({
        reducerMapKey,
        statusCode: "401"
      });

      deepFreeze(state);
      deepFreeze(action);

      expect(usersReducer(state, action)).toEqual({
        collection: [],
        currentUser: null,
        get: { loading: false, error: false },
        post: { loading: false, error: false },
        getCurrentUser: { loading: false, error: false },
        changePassword: { loading: false, error: { statusCode: "401" } }
      });
    });
  });

  describe("EMPTY action", () => {
    it("should return the initial state", () => {
      const state = defaultState;
      const action = new UsersActions.Empty({ reducerMapKey });

      deepFreeze(state);
      deepFreeze(action);

      expect(usersReducer(state, action)).toEqual(initialUsersState);
    });
  });
});

describe("users map reducer", () => {
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

  describe("GET action", () => {
    it("should set the map key", () => {
      const action = new UsersActions.Get({ reducerMapKey });

      deepFreeze(action);

      expect(Object.keys(reducer(initialState, action))).toContain(
        reducerMapKey
      );
    });
  });

  describe("GET_SUCCESS action", () => {
    it("should set the map key", () => {
      const action = new UsersActions.GetSuccess({
        reducerMapKey,
        collection: getUsersResponse
      });

      deepFreeze(action);

      expect(Object.keys(reducer(initialState, action))).toContain(
        reducerMapKey
      );
    });
  });

  describe("GET_FAIL action", () => {
    it("should set the map key", () => {
      const action = new UsersActions.GetFail({ reducerMapKey });

      deepFreeze(action);

      expect(Object.keys(reducer(initialState, action))).toContain(
        reducerMapKey
      );
    });
  });

  describe("GET_CURRENT_USER action", () => {
    it("should set the map key", () => {
      const action = new UsersActions.GetCurrentUser({ reducerMapKey });

      deepFreeze(action);

      expect(Object.keys(reducer(initialState, action))).toContain(
        reducerMapKey
      );
    });
  });

  describe("GET_CURRENT_USER_SUCCESS action", () => {
    it("should set the map key", () => {
      const action = new UsersActions.GetCurrentUserSuccess({
        reducerMapKey,
        currentUser: user
      });

      deepFreeze(action);

      expect(Object.keys(reducer(initialState, action))).toContain(
        reducerMapKey
      );
    });
  });

  describe("GET_CURRENT_USER_FAIL action", () => {
    it("should set the map key", () => {
      const action = new UsersActions.GetCurrentUserFail({ reducerMapKey });

      deepFreeze(action);

      expect(Object.keys(reducer(initialState, action))).toContain(
        reducerMapKey
      );
    });
  });

  describe("POST action", () => {
    it("should set the map key", () => {
      const action = new UsersActions.Post({
        reducerMapKey,
        username,
        password: newPassword
      });

      deepFreeze(action);

      expect(Object.keys(reducer(initialState, action))).toContain(
        reducerMapKey
      );
    });
  });

  describe("POST_SUCCESS action", () => {
    it("should set the map key", () => {
      const action = new UsersActions.PostSuccess({ reducerMapKey });

      deepFreeze(action);

      expect(Object.keys(reducer(initialState, action))).toContain(
        reducerMapKey
      );
    });
  });

  describe("POST_FAIL action", () => {
    it("should set the map key", () => {
      const action = new UsersActions.PostFail({ reducerMapKey });

      deepFreeze(action);

      expect(Object.keys(reducer(initialState, action))).toContain(
        reducerMapKey
      );
    });
  });

  describe("CHANGE_PASSWORD action", () => {
    it("should set the map key", () => {
      const action = new UsersActions.ChangePassword({
        reducerMapKey,
        oldPassword,
        newPassword
      });

      deepFreeze(action);

      expect(Object.keys(reducer(initialState, action))).toContain(
        reducerMapKey
      );
    });
  });

  describe("CHANGE_PASSWORD_SUCCESS action", () => {
    it("should set the map key", () => {
      const action = new UsersActions.ChangePasswordSuccess({ reducerMapKey });

      deepFreeze(action);

      expect(Object.keys(reducer(initialState, action))).toContain(
        reducerMapKey
      );
    });
  });

  describe("CHANGE_PASSWORD_FAIL action", () => {
    it("should set the map key", () => {
      const action = new UsersActions.ChangePasswordFail({
        reducerMapKey,
        statusCode: "401"
      });

      deepFreeze(action);

      expect(Object.keys(reducer(initialState, action))).toContain(
        reducerMapKey
      );
    });
  });

  describe("EMPTY action", () => {
    it("should set the map key", () => {
      const action = new UsersActions.Empty({ reducerMapKey });

      deepFreeze(action);

      expect(Object.keys(reducer(initialState, action))).toContain(
        reducerMapKey
      );
    });
  });
});
