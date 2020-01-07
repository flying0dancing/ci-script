import {
  DayOfWeek,
  ProductWorkingDay
} from "@/core/api/working-days/working-days.interface";
import { Action } from "@ngrx/store";

export const NAMESPACE = "workingDays";
export const GET = "workingDays/get";
export const GET_SUCCESS = "workingDays/get-success";
export const GET_FAIL = "workingDays/get-fail";
export const UPDATE = "workingDays/update";
export const UPDATE_SUCCESS = "workingDays/update-success";
export const UPDATE_FAIL = "workingDays/update-fail";
export const EMPTY = "workingDays/empty";

export class Get implements Action {
  readonly type = GET;

  constructor(public payload: { reducerMapKey: string }) {}
}

export class GetSuccess implements Action {
  readonly type = GET_SUCCESS;

  constructor(
    public payload: { reducerMapKey: string; workingDays: ProductWorkingDay[] }
  ) {}
}

export class GetFail implements Action {
  readonly type = GET_FAIL;

  constructor(public payload: { reducerMapKey: string }) {}
}

export class Update implements Action {
  readonly type = UPDATE;

  constructor(
    public payload: {
      reducerMapKey: string;
      productId: number;
      productName: string;
      workingDays: DayOfWeek[];
    }
  ) {}
}

export class UpdateSuccess implements Action {
  readonly type = UPDATE_SUCCESS;

  constructor(
    public payload: {
      reducerMapKey: string;
      productName: string;
      newWorkingDays: ProductWorkingDay[];
    }
  ) {}
}

export class UpdateFail implements Action {
  readonly type = UPDATE_FAIL;

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
  | Update
  | UpdateSuccess
  | UpdateFail
  | Empty;
