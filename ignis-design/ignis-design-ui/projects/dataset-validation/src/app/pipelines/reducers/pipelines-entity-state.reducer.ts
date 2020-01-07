import { createEntityAdapter, EntityAdapter, EntityState } from '@ngrx/entity';
import { PipelineActions, PipelineActionTypes } from '../actions/pipelines.actions';
import { Pipeline } from '../interfaces/pipeline.interface';

export const ADAPTER: EntityAdapter<Pipeline> = createEntityAdapter<Pipeline>();

export const INITIAL_STATE: EntityState<Pipeline> = ADAPTER.getInitialState();

export function pipelinesEntityReducer(
  state = INITIAL_STATE,
  action: PipelineActions
): EntityState<Pipeline> {
  switch (action.type) {
    case PipelineActionTypes.GetAllSuccessful:
      return ADAPTER.addAll(action.payload, state);

    case PipelineActionTypes.GetOneSuccess:
      return ADAPTER.upsertOne(action.payload, state);

    default:
      return state;
  }
}
