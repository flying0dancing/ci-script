import { IdStateUtility, LoadingState, LoadingUtility } from '../../core/reducers/reducers-utility.service';
import { Actions, ActionTypes } from '../actions/pipeline-step-test.actions';

export interface PipelineStepTestState {
  getState: LoadingState;
  updateState: LoadingState;
  deleteState: LoadingState;
  runState: LoadingState;
}

export interface State {
  [key: number]: PipelineStepTestState;
}

export const INITIAL_PIPELINE_STEP_TEST_STATE = {
  getState: LoadingUtility.getInitialState(),
  updateState: LoadingUtility.getInitialState(),
  deleteState: LoadingUtility.getInitialState(),
  runState: LoadingUtility.getInitialState()
};

const idStateUtility = new IdStateUtility(INITIAL_PIPELINE_STEP_TEST_STATE);

function pipelineStepTestReducer(
  state: PipelineStepTestState = INITIAL_PIPELINE_STEP_TEST_STATE,
  action: Actions
): PipelineStepTestState {
  switch (action.type) {
    case ActionTypes.GetOne:
      return {
        ...state,
        getState: LoadingUtility.toLoadingState()
      };

    case ActionTypes.GetOneSuccess:
      return {
        ...state,
        getState: LoadingUtility.toLoadedState()
      };

    case ActionTypes.GetOneFail:
      return {
        ...state,
        getState: LoadingUtility.toErrorState(action.errorResponse)
      };

    case ActionTypes.Update:
      return {
        ...state,
        updateState: LoadingUtility.toLoadingState()
      };

    case ActionTypes.UpdateSuccess:
      return {
        ...state,
        updateState: LoadingUtility.toLoadedState()
      };

    case ActionTypes.UpdateFail:
      return {
        ...state,
        updateState: LoadingUtility.toErrorState(action.errorResponse)
      };

    case ActionTypes.Delete:
      return {
        ...state,
        deleteState: LoadingUtility.toLoadingState()
      };

    case ActionTypes.DeleteSuccess:
      return {
        ...state,
        deleteState: LoadingUtility.toLoadedState()
      };

    case ActionTypes.DeleteFail:
      return {
        ...state,
        deleteState: LoadingUtility.toErrorState(action.errorResponse)
      };

    case ActionTypes.Run:
      return {
        ...state,
        runState: LoadingUtility.toLoadingState()
      };

    case ActionTypes.RunSuccess:
      return {
        ...state,
        runState: LoadingUtility.toLoadedState()
      };

    case ActionTypes.RunFail:
      return {
        ...state,
        runState: LoadingUtility.toErrorState(action.errorResponse)
      };

    default:
      return state;
  }
}

export function reducer(state: State, action: Actions) {
  const createdReducer = idStateUtility.createReducer(pipelineStepTestReducer, [
    ActionTypes.GetOne,
    ActionTypes.GetOneSuccess,
    ActionTypes.GetOneFail,
    ActionTypes.Update,
    ActionTypes.UpdateSuccess,
    ActionTypes.UpdateFail,
    ActionTypes.Delete,
    ActionTypes.DeleteSuccess,
    ActionTypes.DeleteFail,
    ActionTypes.Run,
    ActionTypes.RunSuccess,
    ActionTypes.RunFail
  ]);

  return createdReducer(state, action);
}

export const selectState = (state: State, id: number) =>
  idStateUtility.selectState(state, id);

export const selectGetState = (state: PipelineStepTestState) => state.getState;
export const selectGetStateLoading = (state: PipelineStepTestState) =>
  selectGetState(state).loading;
export const selectGetStateLoaded = (state: PipelineStepTestState) =>
  selectGetState(state).loaded;
export const selectGetStateErrors = (state: PipelineStepTestState) =>
  selectGetState(state).errors;

export const selectUpdateState = (state: PipelineStepTestState) =>
  state.updateState;
export const selectUpdateStateLoading = (state: PipelineStepTestState) =>
  selectUpdateState(state).loading;
export const selectUpdateStateLoaded = (state: PipelineStepTestState) =>
  selectUpdateState(state).loaded;
export const selectUpdateStateErrors = (state: PipelineStepTestState) =>
  selectUpdateState(state).errors;

export const selectDeleteState = (state: PipelineStepTestState) =>
  state.deleteState;
export const selectDeleteStateLoading = (state: PipelineStepTestState) =>
  selectDeleteState(state).loading;
export const selectDeleteStateLoaded = (state: PipelineStepTestState) =>
  selectDeleteState(state).loaded;
export const selectDeleteStateErrors = (state: PipelineStepTestState) =>
  selectDeleteState(state).errors;

export const selectRunState = (state: PipelineStepTestState) => state.runState;
export const selectRunStateLoading = (state: PipelineStepTestState) =>
  selectRunState(state).loading;
export const selectRunStateLoaded = (state: PipelineStepTestState) =>
  selectRunState(state).loaded;
export const selectRunStateErrors = (state: PipelineStepTestState) =>
  selectRunState(state).errors;
