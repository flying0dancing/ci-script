import { Action } from "@ngrx/store";
import * as Types from "./tables.types";

export const GET = "tables/get";
export const GET_SUCCESS = "tables/get-success";
export const GET_FAIL = "tables/get-fail";
export const UPLOAD = "tables/upload";
export const UPLOAD_SUCCESS = "tables/upload-success";
export const UPLOAD_FAIL = "tables/upload-fail";
export const DELETE = "tables/delete";
export const DELETE_SUCCESS = "tables/delete-success";
export const DELETE_FAIL = "tables/delete-fail";
export const EMPTY = "tables/empty";

export class Get implements Action {
  readonly type = GET;

  constructor(public payload: { reducerMapKey: string }) {}
}

export class GetSuccess implements Action {
  readonly type = GET_SUCCESS;

  constructor(public payload: { reducerMapKey: string; tables: any }) {}
}

export class GetFail implements Action {
  readonly type = GET_FAIL;

  constructor(public payload: { reducerMapKey: string }) {}
}

export class Delete implements Action {
  readonly type = DELETE;

  constructor(public payload: { reducerMapKey: string; id: Types.Id }) {}
}

export class DeleteSuccess implements Action {
  readonly type = DELETE_SUCCESS;

  constructor(public payload: { reducerMapKey: string }) {}
}

export class DeleteFail implements Action {
  readonly type = DELETE_FAIL;

  constructor(public payload: { reducerMapKey: string }) {}
}

export class Empty implements Action {
  readonly type = EMPTY;

  constructor(public payload: { reducerMapKey: string }) {}
}

export type Types =
  | Get
  | GetSuccess
  | GetFail
  | Delete
  | DeleteSuccess
  | DeleteFail
  | Empty;
