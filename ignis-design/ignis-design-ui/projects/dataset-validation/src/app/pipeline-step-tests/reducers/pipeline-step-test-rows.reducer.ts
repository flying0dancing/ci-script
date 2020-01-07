import { LoadingState, LoadingUtility, } from '../../core/reducers/reducers-utility.service';
import { Actions, ActionTypes } from '../actions/pipeline-step-test-rows.actions';
import { OutputData, PipelineStepInputRows } from '../interfaces/pipeline-step-test.interface';

export interface State {
  testInputRows: PipelineStepInputRows;
  outputData: OutputData[];
  getTestInputRowsState: LoadingState;
  createInputDataRowState: LoadingState;
  deleteInputDataRowState: LoadingState;
  updateRowCellDataState: LoadingState;
  getTestExpectedRowsState: LoadingState;
  createExpectedDataRowState: LoadingState;
  deleteExpectedDataRowState: LoadingState;
  updateExpectedRowCellDataState: LoadingState;
}

export const INITIAL_PIPELINE_STEP_TEST_ROWS_STATE = {
  testInputRows: null,
  outputData: [],
  getTestInputRowsState: LoadingUtility.getInitialState(),
  createInputDataRowState: LoadingUtility.getInitialState(),
  updateRowCellDataState: LoadingUtility.getInitialState(),
  deleteInputDataRowState: LoadingUtility.getInitialState(),
  getTestExpectedRowsState: LoadingUtility.getInitialState(),
  createExpectedDataRowState: LoadingUtility.getInitialState(),
  deleteExpectedDataRowState: LoadingUtility.getInitialState(),
  updateExpectedRowCellDataState: LoadingUtility.getInitialState()
};

export function reducer(
  state: State = INITIAL_PIPELINE_STEP_TEST_ROWS_STATE,
  action: Actions
): State {
  switch (action.type) {
    case ActionTypes.GetTestInputRows:
      return {
        ...state,
        getTestInputRowsState: LoadingUtility.toLoadingState()
      };

    case ActionTypes.GetTestInputRowsSuccess:
      return {
        ...state,
        getTestInputRowsState: LoadingUtility.toLoadedState(),
        testInputRows: action.inputRows
      };

    case ActionTypes.GetTestInputRowsFail:
      return {
        ...state,
        getTestInputRowsState: LoadingUtility.toErrorState(action.errorResponse)
      };

    case ActionTypes.CreateInputDataRow:
      return {
        ...state,
        createInputDataRowState: LoadingUtility.toLoadingState()
      };

    case ActionTypes.CreateInputDataRowSuccess:
      return {
        ...state,
        createInputDataRowState: LoadingUtility.toLoadedState()
      };

    case ActionTypes.CreateInputDataRowFail:
      return {
        ...state,
        createInputDataRowState: LoadingUtility.toErrorState(
          action.errorResponse
        )
      };

    case ActionTypes.DeleteInputDataRow:
      return {
        ...state,
        deleteInputDataRowState: LoadingUtility.toLoadingState()
      };

    case ActionTypes.DeleteInputDataRowSuccess:
      return {
        ...state,
        deleteInputDataRowState: LoadingUtility.toLoadedState()
      };

    case ActionTypes.DeleteInputDataRowFail:
      return {
        ...state,
        deleteInputDataRowState: LoadingUtility.toErrorState(
          action.errorResponse
        )
      };

    case ActionTypes.UpdateInputRowCellDataSuccess:
      return {
        ...state,
        updateRowCellDataState: LoadingUtility.toLoadedState()
      };

    case ActionTypes.UpdateInputRowCellDataFail:
      return {
        ...state,
        updateRowCellDataState: LoadingUtility.toErrorState(
          action.errorResponse
        )
      };

    case ActionTypes.UpdateExpectedRowCellData:
      return {
        ...state,
        updateExpectedRowCellDataState: LoadingUtility.toLoadingState()
      };

    case ActionTypes.GetTestExpectedRows:
      return {
        ...state,
        getTestExpectedRowsState: LoadingUtility.toLoadingState()
      };

    case ActionTypes.GetTestExpectedRowsSuccess:
      return {
        ...state,
        getTestExpectedRowsState: LoadingUtility.toLoadedState(),
        outputData: action.outputRows
      };

    case ActionTypes.GetTestExpectedRowsFail:
      return {
        ...state,
        getTestExpectedRowsState: LoadingUtility.toErrorState(action.errorResponse)
      };

    case ActionTypes.CreateExpectedDataRow:
      return {
        ...state,
        createExpectedDataRowState: LoadingUtility.toLoadingState()
      };

    case ActionTypes.CreateExpectedDataRowSuccess:
      return {
        ...state,
        createExpectedDataRowState: LoadingUtility.toLoadedState()
      };

    case ActionTypes.CreateExpectedDataRowFail:
      return {
        ...state,
        createExpectedDataRowState: LoadingUtility.toErrorState(
          action.errorResponse
        )
      };

    case ActionTypes.DeleteExpectedDataRow:
      return {
        ...state,
        deleteExpectedDataRowState: LoadingUtility.toLoadingState()
      };

    case ActionTypes.DeleteExpectedDataRowSuccess:
      return {
        ...state,
        deleteExpectedDataRowState: LoadingUtility.toLoadedState()
      };

    case ActionTypes.DeleteExpectedDataRowFail:
      return {
        ...state,
        deleteExpectedDataRowState: LoadingUtility.toErrorState(
          action.errorResponse
        )
      };

    case ActionTypes.UpdateInputRowCellData:
      return {
        ...state,
        updateRowCellDataState: LoadingUtility.toLoadingState()
      };

    case ActionTypes.UpdateExpectedRowCellDataSuccess:
      return {
        ...state,
        updateExpectedRowCellDataState: LoadingUtility.toLoadedState()
      };

    case ActionTypes.UpdateExpectedRowCellDataFail:
      return {
        ...state,
        updateExpectedRowCellDataState: LoadingUtility.toErrorState(
          action.errorResponse
        )
      };

    default:
      return state;
  }
}

export const selectGetTestInputRowsState = (state: State) =>
  state.getTestInputRowsState;
export const selectGetTestInputRowsStateLoading = (state: State) =>
  selectGetTestInputRowsState(state).loading;
export const selectGetTestInputRowsStateLoaded = (state: State) =>
  selectGetTestInputRowsState(state).loaded;
export const selectGetTestInputRowsStateErrors = (state: State) =>
  selectGetTestInputRowsState(state).errors;
export const selectGetTestInputRows = (state: State) =>
  state.testInputRows;

export const selectCreateInputDataRowState = (state: State) =>
  state.createInputDataRowState;
export const selectCreateInputDataRowStateLoading = (
  state: State
) => selectCreateInputDataRowState(state).loading;
export const selectCreateInputDataRowStateLoaded = (
  state: State
) => selectCreateInputDataRowState(state).loaded;
export const selectCreateInputDataRowStateErrors = (
  state: State
) => selectCreateInputDataRowState(state).errors;

export const selectDeleteInputDataRowState = (state: State) =>
  state.deleteInputDataRowState;
export const selectDeleteInputDataRowStateLoading = (
  state: State
) => selectDeleteInputDataRowState(state).loading;
export const selectDeleteInputDataRowStateLoaded = (
  state: State
) => selectDeleteInputDataRowState(state).loaded;
export const selectDeleteInputDataRowStateErrors = (
  state: State
) => selectDeleteInputDataRowState(state).errors;

export const selectUpdateRowCellDataState = (state: State) =>
  state.updateRowCellDataState;
export const selectUpdateRowCellDataStateLoading = (state: State) =>
  selectUpdateRowCellDataState(state).loading;
export const selectUpdateRowCellDataStateLoaded = (state: State) =>
  selectUpdateRowCellDataState(state).loaded;
export const selectUpdateRowCellDataStateErrors = (state: State) =>
  selectUpdateRowCellDataState(state).errors;

export const selectGetTestExpectedRowsState = (state: State) =>
  state.getTestExpectedRowsState;
export const selectGetTestExpectedRowsStateLoading = (state: State) =>
  selectGetTestExpectedRowsState(state).loading;
export const selectGetTestExpectedRowsStateLoaded = (state: State) =>
  selectGetTestExpectedRowsState(state).loaded;
export const selectGetTestExpectedRowsStateErrors = (state: State) =>
  selectGetTestExpectedRowsState(state).errors;
export const selectGetOutputData = (state: State) => state.outputData;

export const selectCreateExpectedDataRowState = (state: State) =>
  state.createExpectedDataRowState;
export const selectCreateExpectedDataRowStateLoading = (state: State) =>
  selectCreateExpectedDataRowState(state).loading;
export const selectCreateExpectedDataRowStateLoaded = (state: State) =>
  selectCreateExpectedDataRowState(state).loaded;
export const selectCreateExpectedDataRowStateErrors = (state: State) =>
  selectCreateExpectedDataRowState(state).errors;

export const selectDeleteExpectedDataRowState = (state: State) =>
  state.deleteExpectedDataRowState;
export const selectDeleteExpectedDataRowStateLoading = (state: State) =>
  selectDeleteExpectedDataRowState(state).loading;
export const selectDeleteExpectedDataRowStateLoaded = (state: State) =>
  selectDeleteExpectedDataRowState(state).loaded;
export const selectDeleteExpectedDataRowStateErrors = (state: State) =>
  selectDeleteExpectedDataRowState(state).errors;

export const selectUpdateExpectedRowCellDataState = (state: State) =>
  state.updateExpectedRowCellDataState;
export const selectUpdateExpectedRowCellDataStateLoading = (state: State) =>
  selectUpdateExpectedRowCellDataState(state).loading;
export const selectUpdateExpectedRowCellDataStateLoaded = (state: State) =>
  selectUpdateExpectedRowCellDataState(state).loaded;
export const selectUpdateExpectedRowCellDataStateErrors = (state: State) =>
  selectUpdateExpectedRowCellDataState(state).errors;
