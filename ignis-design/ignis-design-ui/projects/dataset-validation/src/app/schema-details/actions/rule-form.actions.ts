import { Action } from '@ngrx/store';
import { GridDeleteAction } from '../../core/grid/interfaces/delete-params.interface';
import { FailedAction } from '../../core/utilities/interfaces/errors.interface';
import { RuleRequest } from '../../schemas';

export enum RuleFormActionTypes {
  Reset = '[Rule Form] Init',
  Post = '[Rule Form] Post',
  PostSuccessful = '[Rule Form] Post Success',
  PostFailed = '[Rule Form] Post Fail',
  Delete = '[Rule Form] Delete',
  DeleteSuccessful = '[Rule Form] Delete Success',
  DeleteFail = '[Rule Form] Delete Fail'
}

export class Reset implements Action {
  readonly type = RuleFormActionTypes.Reset;
}

export class Post implements Action {
  readonly type = RuleFormActionTypes.Post;

  constructor(
    public productId: number,
    public schemaId: number,
    public rule: RuleRequest
  ) {}
}

export class PostSuccessful implements Action {
  readonly type = RuleFormActionTypes.PostSuccessful;

  constructor(public productId: number, public schemaId: number) {}
}

export class PostFailed extends FailedAction {
  readonly type = RuleFormActionTypes.PostFailed;
}

export class Delete implements GridDeleteAction {
  readonly type = RuleFormActionTypes.Delete;

  constructor(
    public id: number,
    public schemaId: number,
    public productId: number
  ) {}
}

export class DeleteSuccess implements Action {
  readonly type = RuleFormActionTypes.DeleteSuccessful;
}

export class DeleteFail extends FailedAction {
  readonly type = RuleFormActionTypes.DeleteFail;
}

export type RuleFormActions =
  | Reset
  | Post
  | PostSuccessful
  | PostFailed
  | Delete
  | DeleteSuccess
  | DeleteFail;
