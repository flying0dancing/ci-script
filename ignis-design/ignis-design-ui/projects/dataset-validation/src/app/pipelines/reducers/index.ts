import { EntityState } from '@ngrx/entity';

import { createFeatureSelector, createSelector } from '@ngrx/store';
import { ResponseState } from '../../core/reducers/reducers-utility.service';
import { Pipeline } from '../interfaces/pipeline.interface';
import {
  ADAPTER,
  pipelinesEntityReducer
} from './pipelines-entity-state.reducer';
import { pipelinesResponseReducer } from './pipelines-response-state.reducer';

export interface PipelinesState {
  dictionary: EntityState<Pipeline>;
  state: ResponseState;
}

export const REDUCERS = {
  dictionary: pipelinesEntityReducer,
  state: pipelinesResponseReducer
};

export const PIPELINES_FEATURE_STATE = createFeatureSelector<PipelinesState>(
  'pipelines'
);

export const DICTIONARY_STATE = createSelector(
  PIPELINES_FEATURE_STATE,
  state => state.dictionary
);

export const {
  selectIds: GET_PIPELINE_IDS,
  selectEntities: GET_PIPELINE_ENTITIES,
  selectAll: GET_ALL_PIPELINES,
  selectTotal: GET_TOTAL_PIPELINES
} = ADAPTER.getSelectors(DICTIONARY_STATE);
