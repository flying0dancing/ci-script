import * as deepFreeze from "deep-freeze";
import * as TablesActions from "../tables.actions";
import { initialTablesState, reducer, tablesReducer } from "../tables.reducer";
import { tables } from "./tables.mocks";

const reducerMapKey = "test";
const tableId = 1;
const defaultState = {
  collection: [],
  get: { loading: false, error: false }
};

describe("Tables Reducer", () => {
  it("should not affect the state on an unknown action", () => {
    const state = defaultState;
    const action = { type: "unknown-action" } as any;

    deepFreeze(state);
    deepFreeze(action);

    expect(tablesReducer(state, action)).toEqual(state);
  });

  it("should return the initial state when no state is provided", () => {
    const state = undefined;
    const action = { type: "unknown-action" } as any;

    deepFreeze(action);

    expect(tablesReducer(state, action)).toEqual(initialTablesState);
  });

  describe("GET action", () => {
    it("should set the loading state to true", () => {
      const state = initialTablesState;
      const action = new TablesActions.Get({ reducerMapKey });

      deepFreeze(state);
      deepFreeze(action);

      expect(tablesReducer(state, action)).toEqual({
        collection: [],
        get: { loading: true, error: false }
      });
    });
  });

  describe("GET_SUCCESS action", () => {
    it("should set the loading state to false and update the collection", () => {
      const state = {
        collection: [],
        get: { loading: true, error: false }
      };
      const action = new TablesActions.GetSuccess({ reducerMapKey, tables });

      deepFreeze(state);
      deepFreeze(action);

      expect(tablesReducer(state, action)).toEqual({
        collection: tables,
        get: { loading: false, error: false }
      });
    });
  });

  describe("GET_FAIL action", () => {
    it("should set the loading state to false and error state to true", () => {
      const state = {
        collection: [],
        get: { loading: true, error: false }
      };
      const action = new TablesActions.GetFail({ reducerMapKey });

      deepFreeze(state);
      deepFreeze(action);

      expect(tablesReducer(state, action)).toEqual({
        collection: [],
        get: { loading: false, error: true }
      });
    });
  });

  describe("EMPTY action", () => {
    it("should return the initial state", () => {
      const state = defaultState;
      const action = new TablesActions.Empty({ reducerMapKey });

      deepFreeze(state);
      deepFreeze(action);

      expect(tablesReducer(state, action)).toEqual(initialTablesState);
    });
  });
});

describe("tables map reducer", () => {
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
      const action = new TablesActions.Get({ reducerMapKey });

      deepFreeze(action);

      expect(Object.keys(reducer(initialState, action))).toContain(
        reducerMapKey
      );
    });
  });

  describe("GET_SUCCESS action", () => {
    it("should set the map key", () => {
      const action = new TablesActions.GetSuccess({ reducerMapKey, tables });

      deepFreeze(action);

      expect(Object.keys(reducer(initialState, action))).toContain(
        reducerMapKey
      );
    });
  });

  describe("GET_FAIL action", () => {
    it("should set the map key", () => {
      const action = new TablesActions.GetFail({ reducerMapKey });

      deepFreeze(action);

      expect(Object.keys(reducer(initialState, action))).toContain(
        reducerMapKey
      );
    });
  });

  describe("EMPTY action", () => {
    it("should set the map key", () => {
      const action = new TablesActions.Empty({ reducerMapKey });

      deepFreeze(action);

      expect(Object.keys(reducer(initialState, action))).toContain(
        reducerMapKey
      );
    });
  });
});
