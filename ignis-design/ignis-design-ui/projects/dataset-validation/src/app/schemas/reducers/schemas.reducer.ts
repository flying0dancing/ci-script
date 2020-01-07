import { createEntityAdapter, EntityAdapter, EntityState } from '@ngrx/entity';
import { Schema } from '..';

import * as actions from '../actions/schemas.actions';

export const ADAPTER: EntityAdapter<Schema> = createEntityAdapter<Schema>();

export const INITIAL_STATE: EntityState<Schema> = ADAPTER.getInitialState();

export function reducer(
  state = INITIAL_STATE,
  action: actions.Types
): EntityState<Schema> {
  switch (action.type) {
    case actions.GET_SUCCESS:
      return ADAPTER.addAll(action.payload, state);
    case actions.GET_ONE_SUCCESS:
      return ADAPTER.addAll([action.payload], state);
    case actions.UPDATE_ONE_SUCCESS:
      return ADAPTER.updateOne(
        { id: action.payload.id, changes: action.payload },
        state
      );
    default:
      return state;
  }
}
