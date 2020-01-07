import { Action } from '@ngrx/store';

export interface GetOneIdentifiableSuccessAction extends Action {
  id: number;
}
