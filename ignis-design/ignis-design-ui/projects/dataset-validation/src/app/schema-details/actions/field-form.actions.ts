import { Action } from '@ngrx/store';
import { GridDeleteAction } from '../../core/grid/interfaces/delete-params.interface';
import { FailedAction } from '../../core/utilities/interfaces/errors.interface';
import { Field } from '../../schemas';

export enum FieldFormActionTypes {
  Reset = '[Field Form] Init',
  Post = '[Field Form] Post',
  PostSuccessful = '[Field Form] Post Success',
  PostFailed = '[Field Form] Post Fail',
  Edit = '[Field Form] Edit',
  EditSuccessful = '[Field Form] Edit Success',
  EditFailed = '[Field Form] Edit Fail',
  Delete = '[Field Form] Delete',
  DeleteSuccessful = '[Field Form] Delete Success',
  DeleteFail = '[Field Form] Delete Fail'
}

export class Reset implements Action {
  readonly type = FieldFormActionTypes.Reset;
}

export class Post implements Action {
  readonly type = FieldFormActionTypes.Post;

  constructor(
    public productId: number,
    public schemaId: number,
    public field: Field
  ) {}
}

export class PostSuccessful implements Action {
  readonly type = FieldFormActionTypes.PostSuccessful;

  constructor(public productId: number, public schemaId: number) {}
}

export class PostFailed extends FailedAction {
  readonly type = FieldFormActionTypes.PostFailed;
}

export class Edit implements Action {
  readonly type = FieldFormActionTypes.Edit;

  constructor(
    public productId: number,
    public schemaId: number,
    public field: Field
  ) {}
}

export class EditSuccessful implements Action {
  readonly type = FieldFormActionTypes.EditSuccessful;

  constructor(public productId: number, public schemaId: number) {}
}

export class EditFailed extends FailedAction {
  readonly type = FieldFormActionTypes.EditFailed;
}
export class Delete implements GridDeleteAction {
  readonly type = FieldFormActionTypes.Delete;

  constructor(
    public id: number,
    public schemaId: number,
    public productId: number
  ) {}
}

export class DeleteSuccess implements Action {
  readonly type = FieldFormActionTypes.DeleteSuccessful;
}

export class DeleteFail extends FailedAction {
  readonly type = FieldFormActionTypes.DeleteFail;
}

export type FieldFormActions =
  | Reset
  | Post
  | PostSuccessful
  | PostFailed
  | Edit
  | EditSuccessful
  | EditFailed
  | Delete
  | DeleteSuccess
  | DeleteFail;
