import { createFeatureSelector, createSelector } from "@ngrx/store";
import { NAMESPACE } from "./users.constants";
import { initialUsersState, State, UsersState } from "./users.reducer";

export const getUsersState = createFeatureSelector<State>(NAMESPACE);

export const getUsersFactory = (key: string) =>
  createSelector(
    getUsersState,
    (state: State) => state[key]
  );

export const getUsersCollectionFactory = (key: string) =>
  createSelector(
    getUsersFactory(key),
    (state: UsersState) =>
      (state && state.collection) || initialUsersState.collection
  );

export const getUsersGetStateFactory = (key: string) =>
  createSelector(
    getUsersFactory(key),
    (state: UsersState) => (state && state.get) || initialUsersState.get
  );

export const getUsersPostStateFactory = (key: string) =>
  createSelector(
    getUsersFactory(key),
    (state: UsersState) => (state && state.post) || initialUsersState.post
  );

export const getUsersGetLoadingStateFactory = (key: string) =>
  createSelector(
    getUsersFactory(key),
    (state: UsersState) =>
      (state && state.get.loading) || initialUsersState.get.loading
  );

export const getUsersPostLoadingStateFactory = (key: string) =>
  createSelector(
    getUsersFactory(key),
    (state: UsersState) =>
      (state && state.post.loading) || initialUsersState.post.loading
  );

export const getUsersPostErrorStateFactory = (key: string) =>
  createSelector(
    getUsersFactory(key),
    (state: UsersState) =>
      (state && state.post.error) || initialUsersState.post.error
  );

export const getCurrentUserFactory = (key: string) =>
  createSelector(
    getUsersFactory(key),
    (state: UsersState) =>
      (state && state.currentUser) || initialUsersState.currentUser
  );

export const getCurrentUserGetLoadingStateFactory = (key: string) =>
  createSelector(
    getUsersFactory(key),
    (state: UsersState) =>
      (state && state.getCurrentUser.loading) ||
      initialUsersState.getCurrentUser.loading
  );

export const getUsersChangePasswordStateFactory = (key: string) =>
  createSelector(
    getUsersFactory(key),
    (state: UsersState) =>
      (state && state.changePassword) || initialUsersState.changePassword
  );

export const getUsersChangePasswordLoadingStateFactory = (key: string) =>
  createSelector(
    getUsersFactory(key),
    (state: UsersState) =>
      (state && state.changePassword.loading) ||
      initialUsersState.changePassword.loading
  );

export const getUsersChangePasswordErrorStateFactory = (key: string) =>
  createSelector(
    getUsersFactory(key),
    (state: UsersState) =>
      (state && state.changePassword.error) ||
      initialUsersState.changePassword.error
  );

export const getCurrentUser = getCurrentUserFactory(NAMESPACE);
export const getCurrentUserLoading = getCurrentUserGetLoadingStateFactory(
  NAMESPACE
);
export const getUsersPostState = getUsersPostStateFactory(NAMESPACE);
export const getUsersPostErrorState = getUsersPostErrorStateFactory(NAMESPACE);
export const getUsersPostLoadingState = getUsersPostLoadingStateFactory(
  NAMESPACE
);
export const getUsersChangePasswordState = getUsersChangePasswordStateFactory(
  NAMESPACE
);
export const getUsersChangePasswordErrorState = getUsersChangePasswordErrorStateFactory(
  NAMESPACE
);
export const getUsersChangePasswordLoadingState = getUsersChangePasswordLoadingStateFactory(
  NAMESPACE
);
