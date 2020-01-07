import { EntityState } from '@ngrx/entity';

import { createFeatureSelector, createSelector } from '@ngrx/store';
import { ResponseState } from '../../core/reducers/reducers-utility.service';
import { ProductConfig } from '../interfaces/product-config.interface';
import { ProductConfigTaskList } from '../interfaces/product-validation.interface';
import * as fromProductConfigEvents from './product-configs-events.reducer';
import * as fromProductConfigsState from './product-configs-state.reducer';

import * as fromProductConfigs from './product-configs.reducer';

export interface ProductResponseState extends ResponseState {
  validating: boolean;
  validated: boolean;
}

export interface ProductConfigsState {
  dictionary: EntityState<ProductConfig>;
  state: ProductResponseState;
  events: EntityState<ProductConfigTaskList>;
}

export const REDUCERS = {
  dictionary: fromProductConfigs.reducer,
  state: fromProductConfigsState.productConfigReducer,
  events: fromProductConfigEvents.reducer
};

export const PRODUCTS_FEATURE_STATE = createFeatureSelector<
  ProductConfigsState
>('product-configs');

export const DICTIONARY_STATE = createSelector(
  PRODUCTS_FEATURE_STATE,
  state => state.dictionary
);

export const EVENT_STATE = createSelector(
  PRODUCTS_FEATURE_STATE,
  state => state.events
);

export const {
  selectIds: GET_PRODUCT_CONFIG_IDS,
  selectEntities: GET_PRODUCT_CONFIG_ENTITIES,
  selectAll: GET_ALL_PRODUCT_CONFIGS,
  selectTotal: GET_TOTAL_PRODUCT_CONFIGS
} = fromProductConfigs.ADAPTER.getSelectors(DICTIONARY_STATE);

export const {
  selectEntities: GET_PRODUCT_CONFIG_EVENTS_ENTITIES,
  selectAll: GET_ALL_PRODUCT_CONFIGS_EVENTS,
  selectTotal: GET_TOTAL_PRODUCT_CONFIGS_EVENTS
} = fromProductConfigEvents.ADAPTER.getSelectors(EVENT_STATE);
