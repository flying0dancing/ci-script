import { EntityState } from '@ngrx/entity';

import { createFeatureSelector, createSelector } from '@ngrx/store';
import { Schema } from '..';
import { ResourceState } from '../../core/reducers/reducers-utility.service';
import * as fromSchemasState from './schemas-state.reducer';

import * as fromSchemas from './schemas.reducer';

export interface SchemasState {
  dictionary: EntityState<Schema>;
  state: ResourceState;
}

export const REDUCERS = {
  dictionary: fromSchemas.reducer,
  state: fromSchemasState.schemaReducer
};

export const SCHEMAS_STATE_NAME = 'schemas';

export const GET_STATE = createFeatureSelector<SchemasState>(
  SCHEMAS_STATE_NAME
);

export const GET_DICTIONARY_STATE = createSelector(
  GET_STATE,
  state => state.dictionary
);

export const {
  selectIds: GET_SCHEMA_IDS,
  selectEntities: GET_SCHEMA_ENTITIES,
  selectAll: GET_ALL_SCHEMAS,
  selectTotal: GET_TOTAL_SCHEMAS
} = fromSchemas.ADAPTER.getSelectors(GET_DICTIONARY_STATE);
