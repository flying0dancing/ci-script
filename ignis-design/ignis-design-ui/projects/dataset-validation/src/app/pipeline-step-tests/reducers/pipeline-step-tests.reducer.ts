import * as pipelineStepTestActions from '../actions/pipeline-step-test.actions';
import { Actions, ActionTypes } from '../actions/pipeline-step-tests.actions';
import {
  LoadingUtility,
  LoadingState
} from '../../core/reducers/reducers-utility.service';

export interface State {
  ids: number[];
  getState: LoadingState;
  createState: LoadingState;
}

export const INITIAL_STATE = {
  ids: [],
  getState: LoadingUtility.getInitialState(),
  createState: LoadingUtility.getInitialState()
};

export function reducer(
  state: State = INITIAL_STATE,
  action: Actions | pipelineStepTestActions.Actions
): State {
  switch (action.type) {
    case ActionTypes.GetAll:
      return {
        ...state,
        getState: LoadingUtility.toLoadingState()
      };

    case ActionTypes.GetAllSuccess:
      return {
        ...state,
        ids: action.ids,
        getState: LoadingUtility.toLoadedState()
      };

    case ActionTypes.GetAllFail:
      return {
        ...state,
        getState: LoadingUtility.toErrorState(action.errorResponse)
      };

    case ActionTypes.Create:
      return {
        ...state,
        createState: LoadingUtility.toLoadingState()
      };

    case ActionTypes.CreateSuccess:
      return {
        ...state,
        createState: LoadingUtility.toLoadedState()
      };

    case ActionTypes.CreateFail:
      return {
        ...state,
        createState: LoadingUtility.toErrorState(action.errorResponse)
      };

    case pipelineStepTestActions.ActionTypes.DeleteSuccess:
      return {
        ...state,
        ids: state.ids.filter(id => id !== action.id)
      };

    default:
      return state;
  }
}

export const selectIds = (state: State) => state.ids;

export const selectGetState = (state: State) => state.getState;
export const selectGetStateLoading = (state: State) =>
  LoadingUtility.selectLoading(state.getState);
export const selectGetStateLoaded = (state: State) =>
  LoadingUtility.selectLoaded(state.getState);
export const selectGetStateErrors = (state: State) =>
  LoadingUtility.selectErrors(state.getState);

export const selectCreateState = (state: State) => state.createState;
export const selectCreateStateLoading = (state: State) =>
  LoadingUtility.selectLoading(state.createState);
export const selectCreateStateLoaded = (state: State) =>
  LoadingUtility.selectLoaded(state.createState);
export const selectCreateStateErrors = (state: State) =>
  LoadingUtility.selectErrors(state.createState);
