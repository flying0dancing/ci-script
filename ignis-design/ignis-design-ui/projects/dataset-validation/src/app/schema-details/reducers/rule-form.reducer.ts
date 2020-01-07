import {
  INITIAL_RESPONSE_STATE,
  ResponseState,
  toErrorState,
  toLoadingState,
  toSuccessState
} from '../../core/reducers/reducers-utility.service';
import {
  RuleFormActions,
  RuleFormActionTypes
} from '../actions/rule-form.actions';

export function ruleFormReducer(
  state: ResponseState = INITIAL_RESPONSE_STATE,
  action: RuleFormActions
): ResponseState {
  switch (action.type) {
    case RuleFormActionTypes.Post:
    case RuleFormActionTypes.Delete:
      return toLoadingState(state);

    case RuleFormActionTypes.PostSuccessful:
    case RuleFormActionTypes.DeleteSuccessful:
    case RuleFormActionTypes.Reset:
      return toSuccessState(state);

    case RuleFormActionTypes.PostFailed:
    case RuleFormActionTypes.DeleteFail:
      return toErrorState(state, action.errorResponse);

    default:
      return state;
  }
}
