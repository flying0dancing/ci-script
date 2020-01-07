import { createFeatureSelector, createSelector } from '@ngrx/store';
import { ResponseState } from '../../core/reducers/reducers-utility.service';

export const RULE_FORM_STATE_NAME = 'rule-form';
export const RULE_FORM_STATE = createFeatureSelector<ResponseState>(
  RULE_FORM_STATE_NAME
);

export const ERRORS = createSelector(
  RULE_FORM_STATE,
  (state: ResponseState) => state.errorResponse.errors
);

export const LOADING = createSelector(
  RULE_FORM_STATE,
  (state: ResponseState) => state.loading
);
export const LOADED = createSelector(
  RULE_FORM_STATE,
  (state: ResponseState) => state.loaded
);
