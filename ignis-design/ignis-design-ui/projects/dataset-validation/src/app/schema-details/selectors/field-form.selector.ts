import { createFeatureSelector, createSelector } from '@ngrx/store';
import { ResponseState } from '../../core/reducers/reducers-utility.service';

export const FIELD_FORM_STATE_NAME = 'field-form';
export const FIELD_FORM_STATE = createFeatureSelector<ResponseState>(
  FIELD_FORM_STATE_NAME
);

export const ERRORS = createSelector(
  FIELD_FORM_STATE,
  (state: ResponseState) => state.errorResponse.errors
);

export const LOADING = createSelector(
  FIELD_FORM_STATE,
  (state: ResponseState) => state.loading
);
export const LOADED = createSelector(
  FIELD_FORM_STATE,
  (state: ResponseState) => state.loaded
);
