import { createEntityAdapter, EntityAdapter, EntityState } from '@ngrx/entity';
import * as pipelineStepTest from '../actions/pipeline-step-test.actions';
import * as pipelineStepTests from '../actions/pipeline-step-tests.actions';
import { PipelineStepTest } from '../interfaces/pipeline-step-test.interface';

type Actions = pipelineStepTest.Actions | pipelineStepTests.Actions;

export const ADAPTER: EntityAdapter<PipelineStepTest> = createEntityAdapter<
  PipelineStepTest
>();

export type State = EntityState<PipelineStepTest>;

export const INITIAL_STATE: State = ADAPTER.getInitialState();

export function reducer(state: State = INITIAL_STATE, action: Actions): State {
  switch (action.type) {
    case pipelineStepTests.ActionTypes.GetAllSuccess:
      return ADAPTER.upsertMany(action.payload, state);

    case pipelineStepTest.ActionTypes.GetOneSuccess:
      return ADAPTER.upsertOne(action.payload, state);

    case pipelineStepTest.ActionTypes.UpdateSuccess:
      return ADAPTER.updateOne(
        {
          id: action.payload.id,
          changes: action.payload
        },
        state
      );

    default:
      return state;
  }
}
