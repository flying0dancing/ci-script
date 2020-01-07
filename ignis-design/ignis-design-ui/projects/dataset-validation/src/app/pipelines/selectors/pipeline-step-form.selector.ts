import { createFeatureSelector, createSelector } from '@ngrx/store';
import { ResponseState } from '../../core/reducers/reducers-utility.service';
import { PipelineFormState } from '../reducers/pipeline-step-form.reducer';

export const PIPELINE_STEP_FORM_STATE_NAME = 'pipeline-step-form';
export const PIPELINE_STEP_FORM_STATE = createFeatureSelector<PipelineFormState>(
  PIPELINE_STEP_FORM_STATE_NAME
);

export const ERRORS = createSelector(
  PIPELINE_STEP_FORM_STATE,
  (state: PipelineFormState) => state.updateError
);

export const LOADING = createSelector(
  PIPELINE_STEP_FORM_STATE,
  (state: ResponseState) => state.loading
);
export const LOADED = createSelector(
  PIPELINE_STEP_FORM_STATE,
  (state: ResponseState) => state.loaded
);
