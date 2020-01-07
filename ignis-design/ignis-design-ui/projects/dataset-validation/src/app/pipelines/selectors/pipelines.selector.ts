import { createSelector } from '@ngrx/store';
import {
  selectErrorResponse,
  selectIds,
  selectLoaded,
  selectLoading
} from '../../core/reducers/reducers-utility.service';
import {
  GET_PIPELINE_ENTITIES,
  PIPELINES_FEATURE_STATE
} from '../../pipelines/reducers';

export const PIPELINES_RESPONSE_STATE = createSelector(
  PIPELINES_FEATURE_STATE,
  pipelinesState => pipelinesState.state
);

export const PIPELINES_LOADING_STATE = createSelector(
  PIPELINES_RESPONSE_STATE,
  selectLoading
);

export const PIPELINES_LOADED_STATE = createSelector(
  PIPELINES_RESPONSE_STATE,
  selectLoaded
);

export const PIPELINE_IDS_STATE = createSelector(
  PIPELINES_RESPONSE_STATE,
  selectIds
);

export const PIPELINE_ENTITIES = createSelector(
  PIPELINE_IDS_STATE,
  GET_PIPELINE_ENTITIES,
  (ids, pipeline) => ids && ids.map(id => pipeline[id])
);

export const PIPELINES_ERROR_STATE = createSelector(
  PIPELINES_RESPONSE_STATE,
  selectErrorResponse
);

export const getPipeline = (id: number) =>
  createSelector(
    GET_PIPELINE_ENTITIES,
    pipelines => pipelines[id]
  );
