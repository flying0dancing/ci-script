import { Action } from "@ngrx/store";

import * as Interfaces from "./products.interfaces";

export const GET = "products/get";
export const GET_SUCCESS = "products/get-success";
export const GET_FAIL = "products/get-fail";
export const IMPORT_STARTED = "products/import-started";
export const NO_IMPORT_IN_PROGRESS = "products/no-import-in-progress";
export const DELETE = "products/delete";
export const DELETE_SUCCESS = "products/delete-success";
export const DELETE_FAIL = "products/delete-fail";
export const EMPTY = "products/empty";

export class Get implements Action {
  readonly type = GET;

  constructor(public payload: { reducerMapKey: string }) {}
}

export class GetSuccess implements Action {
  readonly type = GET_SUCCESS;

  constructor(
    public payload: { reducerMapKey: string; products: Interfaces.Product[] }
  ) {}
}

export class GetFail implements Action {
  readonly type = GET_FAIL;

  constructor(public payload: { reducerMapKey: string }) {}
}

export class ImportStarted implements Action {
  readonly type = IMPORT_STARTED;

  constructor(public payload: { reducerMapKey: string }) {}
}

export class NoImportInProgress implements Action {
  readonly type = NO_IMPORT_IN_PROGRESS;

  constructor(public payload: { reducerMapKey: string }) {}
}

export class Delete implements Action {
  readonly type = DELETE;

  constructor(public payload: { reducerMapKey: string; id: number }) {}
}

export class DeleteSuccess implements Action {
  readonly type = DELETE_SUCCESS;

  constructor(public payload: { reducerMapKey: string; id: number }) {}
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
  | ImportStarted
  | NoImportInProgress
  | Delete
  | DeleteSuccess
  | DeleteFail
  | Empty;
