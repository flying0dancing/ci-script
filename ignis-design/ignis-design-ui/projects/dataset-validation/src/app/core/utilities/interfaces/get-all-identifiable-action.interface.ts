import { Action } from '@ngrx/store';

export interface GetAllIdentifiableSuccessAction extends Action {
  ids: number[];
}
