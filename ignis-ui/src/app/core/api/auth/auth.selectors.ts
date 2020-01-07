import { createFeatureSelector, createSelector } from "@ngrx/store";
import { NAMESPACE } from "./auth.constants";
import { AuthState, initialAuthState, State } from "./auth.reducer";

export const getAuthState = createFeatureSelector<State>(NAMESPACE);

export const getAuthFactory = (key: string) =>
  createSelector(
    getAuthState,
    (state: State) => state[key]
  );

export const getAuthLoginStateFactory = (key: string) =>
  createSelector(
    getAuthFactory(key),
    (state: AuthState) => (state && state.login) || initialAuthState.login
  );

export const getAuthLoginLoadingStateFactory = (key: string) =>
  createSelector(
    getAuthFactory(key),
    (state: AuthState) =>
      (state && state.login.loading) || initialAuthState.login.loading
  );

export const getAuthLoginErrorStateFactory = (key: string) =>
  createSelector(
    getAuthFactory(key),
    (state: AuthState) =>
      (state && state.login.error) || initialAuthState.login.error
  );

export const getAuthLogoutStateFactory = (key: string) =>
  createSelector(
    getAuthFactory(key),
    (state: AuthState) => (state && state.logout) || initialAuthState.logout
  );

export const getAuthLogoutLoadingStateFactory = (key: string) =>
  createSelector(
    getAuthFactory(key),
    (state: AuthState) =>
      (state && state.logout.loading) || initialAuthState.logout.loading
  );

export const getAuthLoginState = getAuthLoginStateFactory(NAMESPACE);
export const getAuthLoginErrorState = getAuthLoginErrorStateFactory(NAMESPACE);
export const getAuthLoginLoadingState = getAuthLoginLoadingStateFactory(
  NAMESPACE
);
