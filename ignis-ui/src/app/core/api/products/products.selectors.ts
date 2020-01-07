import { createFeatureSelector, createSelector } from "@ngrx/store";
import { initialProductsState, ProductsState, State } from "./products.reducer";

export const NAMESPACE = "products";

export const getProductsState = createFeatureSelector<State>(NAMESPACE);

export const getProductsFactory = (key: string) =>
  createSelector(
    getProductsState,
    (state: State) => state[key]
  );

export const getProductsCollectionFactory = (key: string) =>
  createSelector(
    getProductsFactory(key),
    (state: ProductsState) =>
      (state && state.collection) || initialProductsState.collection
  );

export const getProductsGetStateFactory = (key: string) =>
  createSelector(
    getProductsFactory(key),
    (state: ProductsState) => (state && state.get) || initialProductsState.get
  );

export const getProductsGetLoadingStateFactory = (key: string) =>
  createSelector(
    getProductsFactory(key),
    (state: ProductsState) =>
      (state && state.get.loading) || initialProductsState.get.loading
  );

export const importJobStateSelector = (key: string) =>
  createSelector(
    getProductsFactory(key),
    (state: ProductsState) =>
      (state && state.import) || initialProductsState.import
  );
