import { Action } from '@ngrx/store';

import { Schema } from '..';
import { GridDeleteAction } from '../../core/grid/interfaces/delete-params.interface';
import { GetAllIdentifiableSuccessAction } from '../../core/utilities/interfaces/get-all-identifiable-action.interface';
import { GetOneIdentifiableSuccessAction } from '../../core/utilities/interfaces/get-one-identifiable-action.interface';
import { UpdateSchemaRequest } from '../../schema-details/interfaces/update-schema-request.interface';
import { CopySchemaRequest } from '../interfaces/copy-schema-request.interface';
import { CreateSchemaRequest } from '../interfaces/create-schema-request.interface';

export const GET = '[Schemas] Get';
export const GET_FAIL = '[Schemas] Get Fail';
export const GET_SUCCESS = '[Schemas] Get Success';

export const CREATE = '[Schemas] Create';
export const CREATE_FAIL = '[Schemas] Create Fail';
export const CREATE_SUCCESS = '[Schemas] Create Success';

export const COPY = '[Schemas] Copy';
export const COPY_FAIL = '[Schemas] Copy Fail';
export const COPY_SUCCESS = '[Schemas] Copy Success';

export const CREATE_NEW_VERSION = '[Schemas] Create New Version';
export const CREATE_NEW_VERSION_FAIL = '[Schemas] Create New Version Fail';
export const CREATE_NEW_VERSION_SUCCESS =
  '[Schemas] Create New Version Success';

export const DELETE = '[Schemas] Delete';
export const DELETE_FAIL = '[Schemas] Delete Fail';
export const DELETE_SUCCESS = '[Schemas] Delete Success';

export const GET_ONE = '[Schemas] Get One';
export const GET_ONE_FAIL = '[Schemas] Get One Fail';
export const GET_ONE_SUCCESS = '[Schemas] Get One Success';

export const UPDATE_ONE = '[Schemas] Update One';
export const UPDATE_ONE_FAIL = '[Schemas] Update One Fail';
export const UPDATE_ONE_SUCCESS = '[Schemas] Update One Success';

export const UPDATE_REQUEST = '[Schemas] Update Request';
export const UPDATE_REQUEST_FAIL = '[Schemas] Update Request Fail';
export const UPDATE_REQUEST_SUCCESS = '[Schemas] Update Request Success';

export class Get implements Action {
  readonly type = GET;
}

export class GetFail implements Action {
  readonly type = GET_FAIL;
}

export class GetSuccess implements GetAllIdentifiableSuccessAction {
  readonly type = GET_SUCCESS;

  constructor(public payload: Schema[]) {}

  get ids(): number[] {
    return this.payload.map(schema => schema.id);
  }
}

export class Create implements Action {
  readonly type = CREATE;

  constructor(public productId: number, public request: CreateSchemaRequest) {}
}

export class CreateFail implements Action {
  readonly type = CREATE_FAIL;
}

export class CreateSuccess implements Action {
  readonly type = CREATE_SUCCESS;

  constructor(public payload: Schema) {}
}

export class Copy implements Action {
  readonly type = COPY;

  constructor(public productId: number, public schemaId: number, public request: CopySchemaRequest) {}
}

export class CopyFail implements Action {
  readonly type = COPY_FAIL;
}

export class CopySuccess implements Action {
  readonly type = COPY_SUCCESS;

  constructor(public payload: Schema) {}
}

export class CreateNewVersion implements Action {
  readonly type = CREATE_NEW_VERSION;

  constructor(
    public productId: number,
    public schemaId: number,
    public startDate: string
  ) {}
}

export class CreateNewVersionFail implements Action {
  readonly type = CREATE_NEW_VERSION_FAIL;
}

export class CreateNewVersionSuccess implements Action {
  readonly type = CREATE_NEW_VERSION_SUCCESS;

  constructor(public payload: Schema) {}
}

export class Delete implements GridDeleteAction {
  readonly type = DELETE;

  constructor(public productId: number, public id: number) {}
}

export class DeleteFail implements Action {
  readonly type = DELETE_FAIL;
}

export class DeleteSuccess implements Action {
  readonly type = DELETE_SUCCESS;

  constructor(public id: number) {}
}

export class GetOne implements Action {
  readonly type = GET_ONE;

  constructor(public productId: number, public id: number) {}
}

export class GetOneFail implements Action {
  readonly type = GET_ONE_FAIL;
}

export class GetOneSuccess implements GetOneIdentifiableSuccessAction {
  readonly type = GET_ONE_SUCCESS;

  constructor(public payload: Schema) {}

  get id(): number {
    return this.payload.id;
  }
}

export class UpdateOne implements Action {
  readonly type = UPDATE_ONE;

  constructor(public productId: number, public id: number) {}
}

export class UpdateOneFail implements Action {
  readonly type = UPDATE_ONE_FAIL;
}

export class UpdateOneSuccess implements GetOneIdentifiableSuccessAction {
  readonly type = UPDATE_ONE_SUCCESS;

  constructor(public payload: Schema) {}

  get id(): number {
    return this.payload.id;
  }
}

export class UpdateRequest implements Action {
  readonly type = UPDATE_REQUEST;

  constructor(
    public productId: number,
    public schemaId: number,
    public request: UpdateSchemaRequest
  ) {}
}

export class UpdateRequestFail implements Action {
  readonly type = UPDATE_REQUEST_FAIL;
}

export class UpdateRequestSuccess implements Action {
  readonly type = UPDATE_REQUEST_SUCCESS;
}

export type Types =
  | Get
  | GetFail
  | GetSuccess
  | Create
  | CreateSuccess
  | CreateFail
  | Copy
  | CopySuccess
  | CopyFail
  | CreateNewVersion
  | CreateNewVersionSuccess
  | CreateNewVersionFail
  | Delete
  | DeleteSuccess
  | DeleteFail
  | GetOne
  | GetOneFail
  | GetOneSuccess
  | UpdateOne
  | UpdateOneFail
  | UpdateOneSuccess
  | UpdateRequest
  | UpdateRequestFail
  | UpdateRequestSuccess;
