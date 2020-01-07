import { Action } from "@ngrx/store";

import * as Interfaces from "./datasets.interfaces";
import * as Types from "./datasets.types";

export const GET = "datasets/get";
export const GET_SUCCESS = "datasets/get-success";
export const GET_FAIL = "datasets/get-fail";
export const GET_SILENT = "datasets/get-silent";
export const GET_SILENT_SUCCESS = "datasets/get-silent-success";
export const GET_SOURCE_FILES = "datasets/get-source-files";
export const GET_SOURCE_FILES_SUCCESS = "datasets/get-source-files-success";
export const GET_SOURCE_FILES_FAIL = "datasets/get-source-files-fail";
export const EMPTY = "datasets/empty";

export class Get implements Action {
  readonly type = GET;

  constructor(public payload: { reducerMapKey: string }) {}
}

export class GetSilent implements Action {
  readonly type = GET_SILENT;

  constructor(public payload: { reducerMapKey: string }) {}
}

export class GetSuccess implements Action {
  readonly type = GET_SUCCESS;

  constructor(
    public payload: { reducerMapKey: string; datasets: Interfaces.Dataset[] }
  ) {}
}

export class GetSilentSuccess implements Action {
  readonly type = GET_SILENT_SUCCESS;

  constructor(
    public payload: { reducerMapKey: string; datasets: Interfaces.Dataset[] }
  ) {}
}

export class GetFail implements Action {
  readonly type = GET_FAIL;

  constructor(public payload: { reducerMapKey: string }) {}
}

export class GetSourceFiles implements Action {
  readonly type = GET_SOURCE_FILES;

  constructor(public payload: { reducerMapKey: string }) {}
}

export class GetSourceFilesSuccess implements Action {
  readonly type = GET_SOURCE_FILES_SUCCESS;

  constructor(
    public payload: { reducerMapKey: string; sourceFiles: Types.SourceFiles }
  ) {}
}

export class GetSourceFilesFail implements Action {
  readonly type = GET_SOURCE_FILES_FAIL;

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
  | GetSilent
  | GetSilentSuccess
  | GetSourceFiles
  | GetSourceFilesSuccess
  | GetSourceFilesFail
  | Empty;
