import { createFeatureSelector, createSelector } from '@ngrx/store';
import {
  getError,
  getIds,
  getLoading
} from '../../core/reducers/reducers-utility.service';
import {
  GET_SCHEMA_ENTITIES,
  SCHEMAS_STATE_NAME,
  SchemasState
} from './schemas-state.selector';

export const GET_STATE = createFeatureSelector<SchemasState>(
  SCHEMAS_STATE_NAME
);

export const GET_SCHEMAS_STATE = createSelector(
  GET_STATE,
  schemaState => schemaState.state
);

export const GET_SCHEMAS_IDS = createSelector(
  GET_SCHEMAS_STATE,
  getIds
);

export const GET_SCHEMAS_LOADING = createSelector(
  GET_SCHEMAS_STATE,
  getLoading
);

export const GET_SCHEMAS_ERROR = createSelector(
  GET_SCHEMAS_STATE,
  getError
);

export const GET_SCHEMAS = createSelector(
  GET_SCHEMAS_IDS,
  GET_SCHEMA_ENTITIES,
  (ids, schemas) => ids && ids.map(id => schemas[id])
);

export const getSchema = (id: number) =>
  createSelector(
    GET_SCHEMA_ENTITIES,
    schemas => schemas[id]
  );
