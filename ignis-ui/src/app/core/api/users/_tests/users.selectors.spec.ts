import { NAMESPACE } from "../users.constants";
import { initialUsersState } from "../users.reducer";
import * as UsersSelectors from "../users.selectors";

describe("Users Selectors", () => {
  const key = "testing";

  describe("getUsersState", () => {
    it(`should select the ${NAMESPACE} state`, () => {
      const state = { [NAMESPACE]: {} };

      expect(UsersSelectors.getUsersState(state)).toEqual({});
    });

    it(`should return undefined if the ${NAMESPACE} state does not exist`, () => {
      expect(UsersSelectors.getUsersState({})).toBeUndefined();
    });
  });

  describe("getUsersFactory", () => {
    it(`should select the ${NAMESPACE} state using the reducer key`, () => {
      const state = {
        [NAMESPACE]: {
          [key]: initialUsersState
        }
      };

      expect(UsersSelectors.getUsersFactory(key)(state)).toEqual(
        initialUsersState
      );
    });

    it(`should select the ${NAMESPACE} state using the reducer key`, () => {
      const state = { [NAMESPACE]: {} };

      expect(UsersSelectors.getUsersFactory(key)(state)).toBeUndefined();
    });
  });

  describe("getUsersGetStateFactory ", () => {
    it(`should select the get property of the ${NAMESPACE} state using the reducer key`, () => {
      const state = {
        [NAMESPACE]: {
          [key]: initialUsersState
        }
      };

      expect(UsersSelectors.getUsersGetStateFactory(key)(state)).toEqual(
        initialUsersState.get
      );
    });

    it(`should return the initial get state if there is no get property in the ${NAMESPACE} state using the reducer key`, () => {
      const state = { [NAMESPACE]: {} };

      expect(UsersSelectors.getUsersGetStateFactory(key)(state)).toEqual(
        initialUsersState.get
      );
    });
  });

  describe("getUsersCollectionFactory", () => {
    it(`should select the collection property of the ${NAMESPACE} state using the reducer key`, () => {
      const state = {
        [NAMESPACE]: {
          [key]: initialUsersState
        }
      };

      expect(UsersSelectors.getUsersCollectionFactory(key)(state)).toEqual(
        initialUsersState.collection
      );
    });

    it(`should return the initial collection state if there is no collection property
      in the ${NAMESPACE} state using the reducer key`, () => {
      const state = { [NAMESPACE]: {} };

      expect(UsersSelectors.getUsersCollectionFactory(key)(state)).toEqual(
        initialUsersState.collection
      );
    });
  });

  describe("getUsersPostStateFactory", () => {
    it(`should select the post property of the ${NAMESPACE} state using the reducer key`, () => {
      const state = {
        [NAMESPACE]: {
          [key]: initialUsersState
        }
      };

      expect(UsersSelectors.getUsersPostStateFactory(key)(state)).toEqual(
        initialUsersState.post
      );
    });

    it(`should return the initial post state if there is no post property
      in the ${NAMESPACE} state using the reducer key`, () => {
      const state = { [NAMESPACE]: {} };

      expect(UsersSelectors.getUsersPostStateFactory(key)(state)).toEqual(
        initialUsersState.post
      );
    });
  });

  describe("getUsersGetLoadingStateFactory", () => {
    it(`should return the "get.loading" of the ${NAMESPACE} state using the reducer key`, () => {
      const state = {
        [NAMESPACE]: {
          [key]: initialUsersState
        }
      };

      expect(UsersSelectors.getUsersGetLoadingStateFactory(key)(state)).toEqual(
        initialUsersState.get.loading
      );
    });

    it(`shouldreturn the "get.loading" initial state if there is no get property in the ${NAMESPACE} state using the reducer key`, () => {
      const state = { [NAMESPACE]: {} };

      expect(UsersSelectors.getUsersGetLoadingStateFactory(key)(state)).toEqual(
        initialUsersState.get.loading
      );
    });
  });

  describe("getUsersPostLoadingStateFactory  ", () => {
    it(`should return the "post.loading" of the ${NAMESPACE} state using the reducer key`, () => {
      const state = {
        [NAMESPACE]: {
          [key]: initialUsersState
        }
      };

      expect(
        UsersSelectors.getUsersPostLoadingStateFactory(key)(state)
      ).toEqual(initialUsersState.post.loading);
    });

    it(`should return the initial "post.loading" state if there is no post property in the
    ${NAMESPACE} state using the reducer key`, () => {
      const state = { [NAMESPACE]: {} };

      expect(
        UsersSelectors.getUsersPostLoadingStateFactory(key)(state)
      ).toEqual(initialUsersState.post.loading);
    });
  });

  describe("getUsersPostErrorStateFactory", () => {
    it(`should select the "post.error" property of the ${NAMESPACE} state using the reducer key`, () => {
      const state = {
        [NAMESPACE]: {
          [key]: initialUsersState
        }
      };

      expect(UsersSelectors.getUsersPostErrorStateFactory(key)(state)).toEqual(
        initialUsersState.post.error
      );
    });

    it(`should return the "post.error" initial state if there is no post property
      in the ${NAMESPACE} state using the reducer key`, () => {
      const state = { [NAMESPACE]: {} };

      expect(UsersSelectors.getUsersPostErrorStateFactory(key)(state)).toEqual(
        initialUsersState.post.error
      );
    });
  });

  describe("getCurrentUserFactory", () => {
    it(`should select the currentUser property of the ${NAMESPACE} state using the reducer key`, () => {
      const state = {
        [NAMESPACE]: {
          [key]: initialUsersState
        }
      };

      expect(UsersSelectors.getCurrentUserFactory(key)(state)).toEqual(
        initialUsersState.currentUser
      );
    });

    it(`should return the initial currentUser state if there is no currentUser property
      in the ${NAMESPACE} state using the reducer key`, () => {
      const state = { [NAMESPACE]: {} };

      expect(UsersSelectors.getCurrentUserFactory(key)(state)).toEqual(
        initialUsersState.currentUser
      );
    });
  });

  describe("getCurrentUserGetLoadingStateFactory  ", () => {
    it(`should return the "getCurrentUser.loading" of the ${NAMESPACE} state using the reducer key`, () => {
      const state = {
        [NAMESPACE]: {
          [key]: initialUsersState
        }
      };

      expect(
        UsersSelectors.getCurrentUserGetLoadingStateFactory(key)(state)
      ).toEqual(initialUsersState.getCurrentUser.loading);
    });

    it(`should return the initial "getCurrentUser.loading" state if there is no getCurrentUser property in the
    ${NAMESPACE} state using the reducer key`, () => {
      const state = { [NAMESPACE]: {} };

      expect(
        UsersSelectors.getCurrentUserGetLoadingStateFactory(key)(state)
      ).toEqual(initialUsersState.getCurrentUser.loading);
    });
  });

  describe("getUsersChangePasswordStateFactory", () => {
    it(`should select the changePassword property of the ${NAMESPACE} state using the reducer key`, () => {
      const state = {
        [NAMESPACE]: {
          [key]: initialUsersState
        }
      };

      expect(
        UsersSelectors.getUsersChangePasswordStateFactory(key)(state)
      ).toEqual(initialUsersState.changePassword);
    });

    it(`should return the initial changePassword state if there is no post property
      in the ${NAMESPACE} state using the reducer key`, () => {
      const state = { [NAMESPACE]: {} };

      expect(
        UsersSelectors.getUsersChangePasswordStateFactory(key)(state)
      ).toEqual(initialUsersState.changePassword);
    });
  });

  describe("getUsersChangePasswordLoadingStateFactory  ", () => {
    it(`should return the "changePassword.loading" of the ${NAMESPACE} state using the reducer key`, () => {
      const state = {
        [NAMESPACE]: {
          [key]: initialUsersState
        }
      };

      expect(
        UsersSelectors.getUsersChangePasswordLoadingStateFactory(key)(state)
      ).toEqual(initialUsersState.changePassword.loading);
    });

    it(`should return the initial "changePassword.loading" state if there is no post property in the
    ${NAMESPACE} state using the reducer key`, () => {
      const state = { [NAMESPACE]: {} };

      expect(
        UsersSelectors.getUsersChangePasswordLoadingStateFactory(key)(state)
      ).toEqual(initialUsersState.changePassword.loading);
    });
  });

  describe("getUsersChangePasswordErrorStateFactory", () => {
    it(`should return the "changePassword.error" of the ${NAMESPACE} state using the reducer key`, () => {
      const state = {
        [NAMESPACE]: {
          [key]: initialUsersState
        }
      };

      expect(
        UsersSelectors.getUsersChangePasswordErrorStateFactory(key)(state)
      ).toEqual(initialUsersState.changePassword.error);
    });

    it(`should return the initial "changePassword.error" state if there is no post property in the
    ${NAMESPACE} state using the reducer key`, () => {
      const state = { [NAMESPACE]: {} };

      expect(
        UsersSelectors.getUsersChangePasswordErrorStateFactory(key)(state)
      ).toEqual(initialUsersState.changePassword.error);
    });
  });
});
