import { NAMESPACE } from "../tables.constants";
import { initialTablesState } from "../tables.reducer";
import * as TablesSelectors from "../tables.selectors";

describe("Tables Selectors", () => {
  const key = "testing";

  describe("getState", () => {
    it(`should select the ${NAMESPACE} state`, () => {
      const state = { [NAMESPACE]: {} };

      expect(TablesSelectors.getTablesState(state)).toEqual({});
    });

    it(`should return undefined if the ${NAMESPACE} state does not exist`, () => {
      expect(TablesSelectors.getTablesState({})).toBeUndefined();
    });
  });

  describe("getFactory", () => {
    it(`should select the ${NAMESPACE} state using the reducer key`, () => {
      const state = {
        [NAMESPACE]: {
          [key]: initialTablesState
        }
      };

      expect(TablesSelectors.getTablesFactory(key)(state)).toEqual(
        initialTablesState
      );
    });

    it(`should select the ${NAMESPACE} state using the reducer key`, () => {
      const state = { [NAMESPACE]: {} };

      expect(TablesSelectors.getTablesFactory(key)(state)).toBeUndefined();
    });
  });

  describe("getCollectionFactory ", () => {
    it(`should select the collection of the ${NAMESPACE} state using the reducer key`, () => {
      const state = {
        [NAMESPACE]: {
          [key]: initialTablesState
        }
      };

      expect(TablesSelectors.getTablesCollectionFactory(key)(state)).toEqual(
        initialTablesState.collection
      );
    });

    it(`should return the collection initial state if there is no collection in the ${NAMESPACE} state using the reducer key`, () => {
      const state = { [NAMESPACE]: {} };

      expect(TablesSelectors.getTablesCollectionFactory(key)(state)).toEqual(
        initialTablesState.collection
      );
    });
  });

  describe("getGetStateFactory ", () => {
    it(`should select the get property of the ${NAMESPACE} state using the reducer key`, () => {
      const state = {
        [NAMESPACE]: {
          [key]: initialTablesState
        }
      };

      expect(TablesSelectors.getTablesGetStateFactory(key)(state)).toEqual(
        initialTablesState.get
      );
    });

    it(`should return the initial get state if there is no get property in the ${NAMESPACE} state using the reducer key`, () => {
      const state = { [NAMESPACE]: {} };

      expect(TablesSelectors.getTablesGetStateFactory(key)(state)).toEqual(
        initialTablesState.get
      );
    });
  });

  describe("getTablesGetLoadingStateFactory", () => {
    it(`should select the "get.loading" property of the ${NAMESPACE} state using the reducer key`, () => {
      const state = {
        [NAMESPACE]: {
          [key]: initialTablesState
        }
      };

      expect(
        TablesSelectors.getTablesGetLoadingStateFactory(key)(state)
      ).toEqual(initialTablesState.get.loading);
    });

    it(`should return the "get.loading" initial state if there is no get property
      in the ${NAMESPACE} state using the reducer key`, () => {
      const state = { [NAMESPACE]: {} };

      expect(
        TablesSelectors.getTablesGetLoadingStateFactory(key)(state)
      ).toEqual(initialTablesState.get.loading);
    });
  });
});
