import { createSelector } from '@ngrx/store';
import {
  GET_PRODUCT_CONFIG_ENTITIES,
  GET_PRODUCT_CONFIG_EVENTS_ENTITIES,
  ProductResponseState,
  PRODUCTS_FEATURE_STATE
} from '.';
import {
  selectCreated,
  selectCreating,
  selectErrorResponse,
  selectIds,
  selectLoaded,
  selectLoading
} from '../../core/reducers/reducers-utility.service';

export const PRODUCTS_STATE = createSelector(
  PRODUCTS_FEATURE_STATE,
  productConfigsState => productConfigsState.state
);

export const PRODUCTS_LOADING_STATE = createSelector(
  PRODUCTS_STATE,
  selectLoading
);

export const PRODUCTS_VALIDATING_STATE = createSelector(
  PRODUCTS_STATE,
  (state: ProductResponseState) => state.validating
);

export const PRODUCTS_VALIDATED_STATE = createSelector(
  PRODUCTS_STATE,
  (state: ProductResponseState) => state.validated
);

export const PRODUCTS_LOADED_STATE = createSelector(
  PRODUCTS_STATE,
  selectLoaded
);

export const PRODUCTS_CREATING_STATE = createSelector(
  PRODUCTS_STATE,
  selectCreating
);

export const PRODUCTS_CREATED_STATE = createSelector(
  PRODUCTS_STATE,
  selectCreated
);

export const PRODUCT_IDS_STATE = createSelector(
  PRODUCTS_STATE,
  selectIds
);

export const PRODUCT_ENTITIES = createSelector(
  PRODUCT_IDS_STATE,
  GET_PRODUCT_CONFIG_ENTITIES,
  (ids, productConfig) => ids && ids.map(id => productConfig[id])
);

export const PRODUCTS_ERROR_STATE = createSelector(
  PRODUCTS_STATE,
  selectErrorResponse
);

export const getProduct = (id: number) =>
  createSelector(
    GET_PRODUCT_CONFIG_ENTITIES,
    products => products[id]
  );

export const getProductValidationStatus = (id: number) =>
  createSelector(
    GET_PRODUCT_CONFIG_EVENTS_ENTITIES,
    productsEvents => productsEvents[id]
  );
