import {
  INITIAL_RESPONSE_STATE,
  ResponseState,
  toErrorState,
  toLoadingState,
  toSuccessState
} from '../../core/reducers/reducers-utility.service';
import {
  FieldFormActions,
  FieldFormActionTypes
} from '../actions/field-form.actions';

export function fieldFormReducer(
  state: ResponseState = INITIAL_RESPONSE_STATE,
  action: FieldFormActions
): ResponseState {
  switch (action.type) {
    case FieldFormActionTypes.Post:
    case FieldFormActionTypes.Delete:
      return toLoadingState(state);

    case FieldFormActionTypes.PostSuccessful:
    case FieldFormActionTypes.DeleteSuccessful:
    case FieldFormActionTypes.Reset:
      return toSuccessState(state);

    case FieldFormActionTypes.PostFailed:
    case FieldFormActionTypes.DeleteFail:
      return toErrorState(state, action.errorResponse);

    default:
      return state;
  }
}
