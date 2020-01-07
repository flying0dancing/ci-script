import { NAMESPACE } from "../auth.constants";
import { initialAuthState } from "../auth.reducer";
import * as AuthSelectors from "../auth.selectors";

describe("Auth Selectors", () => {
  const key = "testing";

  describe("getAuthState", () => {
    it(`should select the ${NAMESPACE} state`, () => {
      const state = { [NAMESPACE]: {} };

      expect(AuthSelectors.getAuthState(state)).toEqual({});
    });

    it(`should return undefined if the ${NAMESPACE} state does not exist`, () => {
      expect(AuthSelectors.getAuthState({})).toBeUndefined();
    });
  });

  describe("getAuthFactory", () => {
    it(`should select the ${NAMESPACE} state using the reducer key`, () => {
      const state = {
        [NAMESPACE]: {
          [key]: initialAuthState
        }
      };

      expect(AuthSelectors.getAuthFactory(key)(state)).toEqual(
        initialAuthState
      );
    });

    it(`should select the ${NAMESPACE} state using the reducer key`, () => {
      const state = { [NAMESPACE]: {} };

      expect(AuthSelectors.getAuthFactory(key)(state)).toBeUndefined();
    });
  });

  describe("getAuthLoginStateFactory ", () => {
    it(`should select the login property of the ${NAMESPACE} state using the reducer key`, () => {
      const state = {
        [NAMESPACE]: {
          [key]: initialAuthState
        }
      };

      expect(AuthSelectors.getAuthLoginStateFactory(key)(state)).toEqual(
        initialAuthState.login
      );
    });

    it(`should return the initial login state if there is no get property in the ${NAMESPACE} state using the reducer key`, () => {
      const state = { [NAMESPACE]: {} };

      expect(AuthSelectors.getAuthLoginStateFactory(key)(state)).toEqual(
        initialAuthState.login
      );
    });
  });

  describe("getAllocationsLoadingFactory", () => {
    it(`should select the "login.loading" property of the ${NAMESPACE} state using the reducer key`, () => {
      const state = {
        [NAMESPACE]: {
          [key]: initialAuthState
        }
      };

      expect(AuthSelectors.getAuthLoginLoadingStateFactory(key)(state)).toEqual(
        initialAuthState.login.loading
      );
    });

    it(`should return the "login.loading" initial state if there is no get property
      in the ${NAMESPACE} state using the reducer key`, () => {
      const state = { [NAMESPACE]: {} };

      expect(AuthSelectors.getAuthLoginLoadingStateFactory(key)(state)).toEqual(
        initialAuthState.login.loading
      );
    });
  });

  describe("getAuthLoginErrorStateFactory  ", () => {
    it(`should return the "login.error" of the ${NAMESPACE} state using the reducer key`, () => {
      const state = {
        [NAMESPACE]: {
          [key]: initialAuthState
        }
      };

      expect(AuthSelectors.getAuthLoginErrorStateFactory(key)(state)).toEqual(
        initialAuthState.login.error
      );
    });

    it(`shouldreturn the "login.error" initial state if there is no login property in the ${NAMESPACE} state using the reducer key`, () => {
      const state = { [NAMESPACE]: {} };

      expect(AuthSelectors.getAuthLoginErrorStateFactory(key)(state)).toEqual(
        initialAuthState.login.error
      );
    });
  });

  describe("getAuthLogoutStateFactory  ", () => {
    it(`should select the logout property of the ${NAMESPACE} state using the reducer key`, () => {
      const state = {
        [NAMESPACE]: {
          [key]: initialAuthState
        }
      };

      expect(AuthSelectors.getAuthLogoutStateFactory(key)(state)).toEqual(
        initialAuthState.logout
      );
    });

    it(`should return the initial logout  state if there is no get property in the ${NAMESPACE} state using the reducer key`, () => {
      const state = { [NAMESPACE]: {} };

      expect(AuthSelectors.getAuthLogoutStateFactory(key)(state)).toEqual(
        initialAuthState.logout
      );
    });
  });

  describe("getAuthLogoutLoadingStateFactory", () => {
    it(`should select the "logout.loading" property of the ${NAMESPACE} state using the reducer key`, () => {
      const state = {
        [NAMESPACE]: {
          [key]: initialAuthState
        }
      };

      expect(
        AuthSelectors.getAuthLogoutLoadingStateFactory(key)(state)
      ).toEqual(initialAuthState.logout.loading);
    });

    it(`should return the "login.loading" initial state if there is no get property
      in the ${NAMESPACE} state using the reducer key`, () => {
      const state = { [NAMESPACE]: {} };

      expect(
        AuthSelectors.getAuthLogoutLoadingStateFactory(key)(state)
      ).toEqual(initialAuthState.logout.loading);
    });
  });
});
