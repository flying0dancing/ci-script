import { Action } from "@ngrx/store";

import * as Interfaces from "./features.interfaces";

export const GET = "features/get";
export const GET_SUCCESS = "features/get-success";
export const GET_FAIL = "features/get-fail";
export const EMPTY = "features/empty";

export class Get implements Action {
  readonly type = GET;

  constructor(public payload: { reducerMapKey: string }) {}
}

export class GetSuccess implements Action {
  readonly type = GET_SUCCESS;

  constructor(
    public payload: { reducerMapKey: string; features: Interfaces.Feature[] }
  ) {}
}

export class GetFail implements Action {
  readonly type = GET_FAIL;

  constructor(public payload: { reducerMapKey: string }) {}
}

export class Empty implements Action {
  readonly type = EMPTY;

  constructor(public payload: { reducerMapKey: string }) {}
}

export type Types = Get | GetSuccess | GetFail | Empty;
