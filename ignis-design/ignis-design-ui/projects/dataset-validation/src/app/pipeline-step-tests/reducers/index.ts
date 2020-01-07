import { createFeatureSelector, createSelector } from '@ngrx/store';
import * as pipelineStepTestRowsReducer from './pipeline-step-test-rows.reducer';
import * as pipelineStepTestReducer from './pipeline-step-test.reducer';
import * as pipelineStepTestsEntityReducer from './pipeline-step-tests-entity.reducer';
import * as pipelineStepTestsReducer from './pipeline-step-tests.reducer';

export const STATE_KEY = 'pipeline-step-tests';

export interface State {
  dictionary: pipelineStepTestsEntityReducer.State;
  pipelineStepTests: pipelineStepTestsReducer.State;
  pipelineStepTest: pipelineStepTestReducer.State;
  pipelineStepTestRows: pipelineStepTestRowsReducer.State;
}

export const REDUCERS = {
  dictionary: pipelineStepTestsEntityReducer.reducer,
  pipelineStepTests: pipelineStepTestsReducer.reducer,
  pipelineStepTest: pipelineStepTestReducer.reducer,
  pipelineStepTestRows: pipelineStepTestRowsReducer.reducer
};

export const FEATURE_STATE = createFeatureSelector<State>(STATE_KEY);

export const DICTIONARY_STATE = createSelector(
  FEATURE_STATE,
  state => state.dictionary
);

const PIPELINE_STEP_TESTS_STATE = createSelector(
  FEATURE_STATE,
  state => state.pipelineStepTests
);

const PIPELINE_STEP_TEST_STATE = createSelector(
  FEATURE_STATE,
  state => state.pipelineStepTest
);

const PIPELINE_STEP_TEST_ROWS_STATE = createSelector(
  FEATURE_STATE,
  state => state.pipelineStepTestRows
);

const {
  selectEntities: PIPELINE_STEP_TEST_ENTITIES
} = pipelineStepTestsEntityReducer.ADAPTER.getSelectors(DICTIONARY_STATE);

export const IDS = createSelector(
  PIPELINE_STEP_TESTS_STATE,
  pipelineStepTestsReducer.selectIds
);

export const GET_PIPELINE_STEP_TESTS_LOADING_STATE = createSelector(
  PIPELINE_STEP_TESTS_STATE,
  pipelineStepTestsReducer.selectGetState
);

export const GET_PIPELINE_STEP_TESTS_LOADING = createSelector(
  PIPELINE_STEP_TESTS_STATE,
  pipelineStepTestsReducer.selectGetStateLoading
);

export const GET_PIPELINE_STEP_TESTS_LOADED = createSelector(
  PIPELINE_STEP_TESTS_STATE,
  pipelineStepTestsReducer.selectGetStateLoaded
);

export const GET_PIPELINE_STEP_TESTS_ERRORS = createSelector(
  PIPELINE_STEP_TESTS_STATE,
  pipelineStepTestsReducer.selectGetStateErrors
);

export const GET_PIPELINE_STEP_TESTS_CREATE_STATE = createSelector(
  PIPELINE_STEP_TESTS_STATE,
  pipelineStepTestsReducer.selectCreateState
);

export const GET_PIPELINE_STEP_TESTS_CREATING = createSelector(
  PIPELINE_STEP_TESTS_STATE,
  pipelineStepTestsReducer.selectCreateStateLoading
);

export const GET_PIPELINE_STEP_TESTS_CREATED = createSelector(
  PIPELINE_STEP_TESTS_STATE,
  pipelineStepTestsReducer.selectCreateStateLoaded
);

export const GET_PIPELINE_STEP_TESTS_CREATE_ERRORS = createSelector(
  PIPELINE_STEP_TESTS_STATE,
  pipelineStepTestsReducer.selectCreateStateErrors
);

export const GET_PIPELINE_STEP_TESTS = createSelector(
  IDS,
  PIPELINE_STEP_TEST_ENTITIES,
  (ids, entities) => ids.map(id => entities[id])
);

const getPipelineStepTestState = (id: number) =>
  createSelector(
    PIPELINE_STEP_TEST_STATE,
    state => pipelineStepTestReducer.selectState(state, id)
  );

export const getPipelineStepTestLoadingState = (id: number) =>
  createSelector(
    getPipelineStepTestState(id),
    pipelineStepTestReducer.selectGetState
  );

export const getPipelineStepTestLoading = (id: number) =>
  createSelector(
    getPipelineStepTestState(id),
    pipelineStepTestReducer.selectGetStateLoading
  );

export const getPipelineStepTestLoaded = (id: number) =>
  createSelector(
    getPipelineStepTestState(id),
    pipelineStepTestReducer.selectGetStateLoaded
  );

export const getPipelineStepTestLoadingErrors = (id: number) =>
  createSelector(
    getPipelineStepTestState(id),
    pipelineStepTestReducer.selectGetStateErrors
  );

export const getPipelineStepTestUpdateState = (id: number) =>
  createSelector(
    getPipelineStepTestState(id),
    pipelineStepTestReducer.selectUpdateState
  );

export const getPipelineStepTestUpdating = (id: number) =>
  createSelector(
    getPipelineStepTestState(id),
    pipelineStepTestReducer.selectUpdateStateLoading
  );

export const getPipelineStepTestUpdated = (id: number) =>
  createSelector(
    getPipelineStepTestState(id),
    pipelineStepTestReducer.selectUpdateStateLoaded
  );

export const getPipelineStepTestUpdateErrors = (id: number) =>
  createSelector(
    getPipelineStepTestState(id),
    pipelineStepTestReducer.selectUpdateStateErrors
  );

export const getPipelineStepTestDeleteState = (id: number) =>
  createSelector(
    getPipelineStepTestState(id),
    pipelineStepTestReducer.selectDeleteState
  );

export const getPipelineStepTestDeleting = (id: number) =>
  createSelector(
    getPipelineStepTestState(id),
    pipelineStepTestReducer.selectDeleteStateLoading
  );

export const getPipelineStepTestDeleted = (id: number) =>
  createSelector(
    getPipelineStepTestState(id),
    pipelineStepTestReducer.selectDeleteStateLoaded
  );

export const getPipelineStepTestDeleteErrors = (id: number) =>
  createSelector(
    getPipelineStepTestState(id),
    pipelineStepTestReducer.selectDeleteStateErrors
  );

export const getInputDataRowCreateState = (id: number) =>
  createSelector(
    PIPELINE_STEP_TEST_ROWS_STATE,
    pipelineStepTestRowsReducer.selectCreateInputDataRowState
  );

export const getInputDataRowCreating = (id: number) =>
  createSelector(
    PIPELINE_STEP_TEST_ROWS_STATE,
    pipelineStepTestRowsReducer.selectCreateInputDataRowStateLoading
  );

export const getInputDataRowCreated = (id: number) =>
  createSelector(
    PIPELINE_STEP_TEST_ROWS_STATE,
    pipelineStepTestRowsReducer.selectCreateInputDataRowStateLoaded
  );

export const getInputDataRowCreateErrors = (id: number) =>
  createSelector(
    PIPELINE_STEP_TEST_ROWS_STATE,
    pipelineStepTestRowsReducer.selectCreateInputDataRowStateErrors
  );

export const getInputDataRowDeleteState = (id: number) =>
  createSelector(
    PIPELINE_STEP_TEST_ROWS_STATE,
    pipelineStepTestRowsReducer.selectDeleteInputDataRowState
  );

export const getInputDataRowDeleting = (id: number) =>
  createSelector(
    PIPELINE_STEP_TEST_ROWS_STATE,
    pipelineStepTestRowsReducer.selectDeleteInputDataRowStateLoading
  );

export const getInputDataRowDeleted = (id: number) =>
  createSelector(
    PIPELINE_STEP_TEST_ROWS_STATE,
    pipelineStepTestRowsReducer.selectDeleteInputDataRowStateLoaded
  );

export const getInputDataRowDeleteErrors = (id: number) =>
  createSelector(
    PIPELINE_STEP_TEST_ROWS_STATE,
    pipelineStepTestRowsReducer.selectDeleteInputDataRowStateErrors
  );

export const getExpectedDataRowCreateState = (id: number) =>
  createSelector(
    PIPELINE_STEP_TEST_ROWS_STATE,
    pipelineStepTestRowsReducer.selectCreateExpectedDataRowState
  );

export const getExpectedDataRowCreating = (id: number) =>
  createSelector(
    PIPELINE_STEP_TEST_ROWS_STATE,
    pipelineStepTestRowsReducer.selectCreateExpectedDataRowStateLoading
  );

export const getExpectedDataRowCreated = (id: number) =>
  createSelector(
    PIPELINE_STEP_TEST_ROWS_STATE,
    pipelineStepTestRowsReducer.selectCreateExpectedDataRowStateLoaded
  );

export const getExpectedDataRowCreateErrors = (id: number) =>
  createSelector(
    PIPELINE_STEP_TEST_ROWS_STATE,
    pipelineStepTestRowsReducer.selectCreateExpectedDataRowStateErrors
  );

export const getExpectedDataRowDeleteState = (id: number) =>
  createSelector(
    PIPELINE_STEP_TEST_ROWS_STATE,
    pipelineStepTestRowsReducer.selectDeleteExpectedDataRowState
  );

export const getExpectedDataRowDeleting = (id: number) =>
  createSelector(
    PIPELINE_STEP_TEST_ROWS_STATE,
    pipelineStepTestRowsReducer.selectDeleteExpectedDataRowStateLoading
  );

export const getExpectedDataRowDeleted = (id: number) =>
  createSelector(
    PIPELINE_STEP_TEST_ROWS_STATE,
    pipelineStepTestRowsReducer.selectDeleteExpectedDataRowStateLoaded
  );

export const getExpectedDataRowDeleteErrors = (id: number) =>
  createSelector(
    PIPELINE_STEP_TEST_ROWS_STATE,
    pipelineStepTestRowsReducer.selectDeleteExpectedDataRowStateErrors
  );

export const getRunState = (id: number) =>
  createSelector(
    getPipelineStepTestState(id),
    pipelineStepTestReducer.selectRunState
  );

export const getRunning = (id: number) =>
  createSelector(
    getPipelineStepTestState(id),
    pipelineStepTestReducer.selectRunStateLoading
  );

export const getRan = (id: number) =>
  createSelector(
    getPipelineStepTestState(id),
    pipelineStepTestReducer.selectRunStateLoaded
  );

export const getRunErrors = (id: number) =>
  createSelector(
    getPipelineStepTestState(id),
    pipelineStepTestReducer.selectRunStateErrors
  );

export const getPipelineStepTest = (id: number) =>
  createSelector(
    PIPELINE_STEP_TEST_ENTITIES,
    entities => entities[id]
  );

export const getTestInputRowsLoading =
  createSelector(
    PIPELINE_STEP_TEST_ROWS_STATE,
    pipelineStepTestRowsReducer.selectGetTestInputRowsStateLoading
  );

export const getTestInputRowsLoaded =
  createSelector(
    PIPELINE_STEP_TEST_ROWS_STATE,
    pipelineStepTestRowsReducer.selectGetTestInputRowsStateLoaded
  );

export const getTestInputRowsError =
  createSelector(
    PIPELINE_STEP_TEST_ROWS_STATE,
    pipelineStepTestRowsReducer.selectGetTestInputRowsStateErrors
  );

export const getTestInputRows =
  createSelector(
    PIPELINE_STEP_TEST_ROWS_STATE,
    pipelineStepTestRowsReducer.selectGetTestInputRows
  );

export const getUpdateRowCellLoading =
  createSelector(
    PIPELINE_STEP_TEST_ROWS_STATE,
    pipelineStepTestRowsReducer.selectUpdateRowCellDataStateLoading
  );

export const getTestExpectedRowsLoading =
  createSelector(
    PIPELINE_STEP_TEST_ROWS_STATE,
    pipelineStepTestRowsReducer.selectGetTestExpectedRowsStateLoading
  );

export const getTestExpectedRowsLoaded =
  createSelector(
    PIPELINE_STEP_TEST_ROWS_STATE,
    pipelineStepTestRowsReducer.selectGetTestExpectedRowsStateLoaded
  );

export const getTestExpectedRowsError =
  createSelector(
    PIPELINE_STEP_TEST_ROWS_STATE,
    pipelineStepTestRowsReducer.selectGetTestExpectedRowsStateErrors
  );

export const getOutputData =
  createSelector(
    PIPELINE_STEP_TEST_ROWS_STATE,
    pipelineStepTestRowsReducer.selectGetOutputData
  );

export const getUpdateExpectedRowCellLoading =
  createSelector(
    PIPELINE_STEP_TEST_ROWS_STATE,
    pipelineStepTestRowsReducer.selectUpdateExpectedRowCellDataStateLoading
  );
