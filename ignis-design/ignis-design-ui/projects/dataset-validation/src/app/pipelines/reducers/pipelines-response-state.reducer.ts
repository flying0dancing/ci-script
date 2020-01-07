import {
  INITIAL_RESPONSE_STATE,
  ResponseState,
  toErrorState,
  toLoadingState,
  toSuccessfulGetAllState,
  toSuccessState
} from '../../core/reducers/reducers-utility.service';
import { PipelineActions, PipelineActionTypes } from '../actions/pipelines.actions';

export function pipelinesResponseReducer(
  state: ResponseState = INITIAL_RESPONSE_STATE,
  action: PipelineActions
): ResponseState {
  switch (action.type) {
    case PipelineActionTypes.GetOne:
    case PipelineActionTypes.GetAll:
    case PipelineActionTypes.Create:
    case PipelineActionTypes.Update:
    case PipelineActionTypes.Delete:
      return toLoadingState(state);

    case PipelineActionTypes.GetAllSuccessful:
      return toSuccessfulGetAllState(state, action);

    case PipelineActionTypes.GetOneSuccess:
    case PipelineActionTypes.CreateSuccessful:
    case PipelineActionTypes.UpdateSuccessful:
    case PipelineActionTypes.DeleteSuccessful:
      return toSuccessState(state);

    case PipelineActionTypes.GetOneFail:
    case PipelineActionTypes.GetAllFailed:
    case PipelineActionTypes.CreateFailed:
    case PipelineActionTypes.UpdateFailed:
    case PipelineActionTypes.DeleteFailed:
      return toErrorState(state, action.errorResponse);

    default:
      return state;
  }
}
