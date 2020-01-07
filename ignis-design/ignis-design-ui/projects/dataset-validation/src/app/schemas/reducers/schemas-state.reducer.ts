import {
  errorOccurred,
  INITIAL_STATE,
  loadingTrue,
  ResourceState,
  successfulGetAllResponse,
  successfulGetOneResponse
} from '../../core/reducers/reducers-utility.service';
import * as schemas from '../../schemas/actions/schemas.actions';

export function schemaReducer(
  state = INITIAL_STATE,
  action: schemas.Types
): ResourceState {
  switch (action.type) {
    case schemas.GET:
      return loadingTrue(state);
    case schemas.GET_FAIL:
      return errorOccurred(state);
    case schemas.GET_SUCCESS:
      return successfulGetAllResponse(state, action);
    case schemas.GET_ONE:
      return loadingTrue(state);
    case schemas.GET_ONE_FAIL:
      return errorOccurred(state);
    case schemas.GET_ONE_SUCCESS:
      return successfulGetOneResponse(state, action);
    case schemas.UPDATE_ONE:
      return loadingTrue(state);
    case schemas.UPDATE_ONE_FAIL:
      return errorOccurred(state);
    case schemas.UPDATE_ONE_SUCCESS:
      return successfulGetOneResponse(state, action);
    case schemas.UPDATE_ONE:
      return loadingTrue(state);
    case schemas.UPDATE_ONE_FAIL:
      return errorOccurred(state);
    case schemas.UPDATE_ONE_SUCCESS:
      return successfulGetOneResponse(state, action);
    default:
      return state;
  }
}
