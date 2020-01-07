import * as deepFreeze from "deep-freeze";
import * as DatasetsActions from "../datasets.actions";
import {
  datasetsReducer,
  initialDatasetsState,
  reducer
} from "../datasets.reducer";

import { datasets, sourceFiles } from "./datasets.mocks";

const reducerMapKey = "test";
const datasetId = 1;

describe("Datasets Reducer", () => {
  it("should not affect the state on an unknown action", () => {
    const state = {
      collection: [],
      sourceFiles: [],
      get: { loading: false, error: false }
    };
    const action = { type: "unknown-action" } as any;

    deepFreeze(state);
    deepFreeze(action);

    expect(datasetsReducer(state, action)).toEqual(state);
  });

  it("should return the initial state when no state is provided", () => {
    const state = undefined;
    const action = { type: "unknown-action" } as any;

    deepFreeze(action);

    expect(datasetsReducer(state, action)).toEqual(initialDatasetsState);
  });

  describe("GET action", () => {
    it("should set the loading state to true", () => {
      const state = initialDatasetsState;
      const action = new DatasetsActions.Get({ reducerMapKey });

      deepFreeze(state);
      deepFreeze(action);

      expect(datasetsReducer(state, action)).toEqual({
        collection: [],
        sourceFiles: [],
        get: { loading: true, error: false }
      });
    });
  });

  describe("GET_SILENT action", () => {
    it("should return the initial state", () => {
      const state = initialDatasetsState;
      const action = new DatasetsActions.GetSilent({ reducerMapKey });

      deepFreeze(state);
      deepFreeze(action);

      expect(datasetsReducer(state, action)).toEqual(initialDatasetsState);
    });
  });

  describe("GET_SUCCESS action", () => {
    it("should set the loading state to false and update the collection", () => {
      const state = {
        collection: [],
        sourceFiles: [],
        get: { loading: true, error: false }
      };
      const action = new DatasetsActions.GetSuccess({
        reducerMapKey,
        datasets
      });

      deepFreeze(state);
      deepFreeze(action);

      expect(datasetsReducer(state, action)).toEqual({
        collection: datasets,
        sourceFiles: [],
        get: { loading: false, error: false }
      });
    });
  });

  describe("GET_FAIL action", () => {
    it("should set the loading state to false and error state to true", () => {
      const state = {
        collection: [],
        sourceFiles: [],
        get: { loading: true, error: false }
      };
      const action = new DatasetsActions.GetFail({ reducerMapKey });

      deepFreeze(state);
      deepFreeze(action);

      expect(datasetsReducer(state, action)).toEqual({
        collection: [],
        sourceFiles: [],
        get: { loading: false, error: true }
      });
    });
  });

  describe("GET_SOURCE_FILES action", () => {
    it("should set the loading state to true", () => {
      const state = initialDatasetsState;
      const action = new DatasetsActions.GetSourceFiles({ reducerMapKey });

      deepFreeze(state);
      deepFreeze(action);

      expect(datasetsReducer(state, action)).toEqual({
        collection: [],
        sourceFiles: [],
        get: { loading: true, error: false }
      });
    });
  });

  describe("GET_SOURCE_FILES_SUCCESS action", () => {
    it("should set the loading state to false and update the collection", () => {
      const state = {
        collection: [],
        sourceFiles: [],
        get: { loading: true, error: false }
      };
      const action = new DatasetsActions.GetSourceFilesSuccess({
        reducerMapKey,
        sourceFiles
      });

      deepFreeze(state);
      deepFreeze(action);

      expect(datasetsReducer(state, action)).toEqual({
        collection: [],
        sourceFiles: sourceFiles,
        get: { loading: false, error: false }
      });
    });
  });

  describe("GET_SOURCE_FILES_FAIL action", () => {
    it("should set the loading state to false and error state to true", () => {
      const state = {
        collection: [],
        sourceFiles: [],
        get: { loading: true, error: false }
      };
      const action = new DatasetsActions.GetSourceFilesFail({ reducerMapKey });

      deepFreeze(state);
      deepFreeze(action);

      expect(datasetsReducer(state, action)).toEqual({
        collection: [],
        sourceFiles: [],
        get: { loading: false, error: true }
      });
    });
  });

  describe("EMPTY action", () => {
    it("should return the initial state", () => {
      const state = {
        collection: [],
        sourceFiles: [],
        get: { loading: false, error: false }
      };
      const action = new DatasetsActions.Empty({ reducerMapKey });

      deepFreeze(state);
      deepFreeze(action);

      expect(datasetsReducer(state, action)).toEqual(initialDatasetsState);
    });
  });
});

describe("datasets map reducer", () => {
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
      const action = new DatasetsActions.Get({ reducerMapKey });

      deepFreeze(action);

      expect(Object.keys(reducer(initialState, action))).toContain(
        reducerMapKey
      );
    });
  });

  describe("GET_SUCCESS action", () => {
    it("should set the map key", () => {
      const action = new DatasetsActions.GetSuccess({
        reducerMapKey,
        datasets
      });

      deepFreeze(action);

      expect(Object.keys(reducer(initialState, action))).toContain(
        reducerMapKey
      );
    });
  });

  describe("GET_FAIL action", () => {
    it("should set the map key", () => {
      const action = new DatasetsActions.GetFail({ reducerMapKey });

      deepFreeze(action);

      expect(Object.keys(reducer(initialState, action))).toContain(
        reducerMapKey
      );
    });
  });

  describe("GET_SOURCE_FILES action", () => {
    it("should set the map key", () => {
      const action = new DatasetsActions.GetSourceFiles({ reducerMapKey });

      deepFreeze(action);

      expect(Object.keys(reducer(initialState, action))).toContain(
        reducerMapKey
      );
    });
  });

  describe("GET_SOURCE_FILES_SUCCESS action", () => {
    it("should set the map key", () => {
      const action = new DatasetsActions.GetSourceFilesSuccess({
        reducerMapKey,
        sourceFiles
      });

      deepFreeze(action);

      expect(Object.keys(reducer(initialState, action))).toContain(
        reducerMapKey
      );
    });
  });

  describe("GET_SOURCE_FILES_FAIL action", () => {
    it("should set the map key", () => {
      const action = new DatasetsActions.GetSourceFilesFail({ reducerMapKey });

      deepFreeze(action);

      expect(Object.keys(reducer(initialState, action))).toContain(
        reducerMapKey
      );
    });
  });

  describe("EMPTY action", () => {
    it("should set the map key", () => {
      const action = new DatasetsActions.Empty({ reducerMapKey });

      deepFreeze(action);

      expect(Object.keys(reducer(initialState, action))).toContain(
        reducerMapKey
      );
    });
  });
});
