import { Action } from '@ngrx/store';

export interface IdentifiableAction extends Action {
  id: number;
}
